from models.db import db
import re

# Synonym groups: Any word in a group matches any other word in the same group
SYNONYM_GROUPS = [
    {"fuel", "kerosene", "diesel", "petrol", "gasoline", "gas"},
    {"oil", "sunflower oil", "vegetable oil", "cooking oil", "refined oil", "oils", "fats"},
    {"milk", "dairy", "cream", "milks", "beverage"},
    {"bread", "toast", "bun", "loaf", "bakery"},
    {"biscuit", "biscuits", "cookies", "cookie", "snacks"},
    {"soda", "coke", "soft drink", "beverage", "pop", "cola"},
    {"water", "mineral water", "aqua", "drink", "beverage"},
    {"flour", "atta", "maida", "powder", "grain"},
    {"sugar", "sweetener", "cane sugar", "powder"}
]

def normalize_name(name):
    """Normalize names for better matching: lowercase, strip, remove plurals, and standardizing."""
    if not name:
        return ""
    # Lowercase and remove special characters except spaces
    name = name.lower().strip()
    # Remove common measurement units if they are part of the name (e.g., "Milk 1L" -> "Milk")
    name = re.sub(r'\d+\s*(l|ml|kg|g|units|packs|ltr|litres)\b', '', name)
    name = re.sub(r'[^a-z0-9\s]', '', name)
    name = name.strip()
    
    # Simple singularization (very basic but effective for most common items)
    if name.endswith('ies') and len(name) > 5:
        name = name[:-3] + 'y'
    elif name.endswith('es') and len(name) > 4:
        if name.endswith(('oes', 'xes', 'ches', 'shes')):
            name = name[:-2]
    elif name.endswith('s') and len(name) > 3 and not name.endswith('ss'):
        name = name[:-1]
            
    return name

def names_match(name1, name2):
    """Check if two product names match using flexible fuzzy/synonym/keyword logic."""
    if not name1 or not name2:
        return False
        
    n1 = normalize_name(name1)
    n2 = normalize_name(name2)
    
    # 1. Direct normalized match
    if n1 == n2 and n1 != "":
        return True
        
    # 2. Substring match (e.g., "oil" matches "sunflower oil")
    if n1 != "" and n2 != "" and (n1 in n2 or n2 in n1):
        return True
        
    # 3. Word-based matching with Synonyms
    words1 = set(n1.split())
    words2 = set(n2.split())
    
    # Filter out generic terms
    generic_terms = {'product', 'item', 'pack', 'bottle', 'box', 'quantity', 'brand', 'fresh', 'premium', 'best'}
    words1 = {w for w in words1 if len(w) > 2 and w not in generic_terms}
    words2 = {w for w in words2 if len(w) > 2 and w not in generic_terms}
    
    if not words1 or not words2:
        return False

    # Check for direct word overlap
    if words1.intersection(words2):
        return True
        
    # Check Synonym Groups
    for w1 in words1:
        for w2 in words2:
            for group in SYNONYM_GROUPS:
                # If both words are in the same synonym group, it's a match
                # We need to normalize the group members too for comparison
                group_norm = {normalize_name(m) for m in group}
                if w1 in group_norm and w2 in group_norm:
                    return True

    return False

def find_catalog_match(supplier_id, product_sku, product_name):
    """
    Find a matching item in a supplier's catalog focusing on product name similarity.
    Name matching is prioritized over SKU matching.
    """
    from models import SupplierCatalog
    
    # 1. Search by Name (Primary - as requested by user)
    all_catalog_items = SupplierCatalog.query.filter_by(supplier_id=supplier_id).all()
    for item in all_catalog_items:
        if names_match(item.name, product_name):
            return item
            
    # 2. SKU fallback (Only as a secondary option if names are similar enough)
    # The user said "dont compare the sku", but we keep it as a very weak fallback 
    # only if the SKU is an exact match and the names aren't wildly different.
    sku = product_sku.lower().strip() if product_sku else ""
    if sku:
        match = SupplierCatalog.query.filter(
            SupplierCatalog.supplier_id == supplier_id,
            db.func.lower(SupplierCatalog.sku) == sku
        ).first()
        if match:
            # Check if names have AT LEAST some similarity before accepting SKU match
            # This prevents matching "sku:01" (Milk) with "sku:01" (Fuel)
            n1 = normalize_name(match.name)
            n2 = normalize_name(product_name)
            if n1 == "" or n2 == "" or n1 in n2 or n2 in n1 or set(n1.split()).intersection(set(n2.split())):
                return match
            
    return None
