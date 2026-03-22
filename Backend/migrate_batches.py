import sqlite3
import os
from dotenv import load_dotenv
from datetime import datetime

# Load environment variables
load_dotenv()

def migrate_batches():
    # 1. Get database path
    db_path = os.environ.get('DATABASE_URL')
    if db_path and db_path.startswith('postgres'):
        print("Detected PostgreSQL database. Attempting migration via psycopg2...")
        try:
            import psycopg2
            # Handle potential 'postgres://' vs 'postgresql://' issue
            if db_path.startswith('postgres://'):
                db_path = db_path.replace('postgres://', 'postgresql://', 1)
            
            conn = psycopg2.connect(db_path)
            cursor = conn.cursor()
            
            # 1. Create table
            try:
                cursor.execute("""
                CREATE TABLE IF NOT EXISTS product_batches (
                    id SERIAL PRIMARY KEY,
                    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                    unit_option_id INTEGER REFERENCES product_unit_options(id) ON DELETE CASCADE,
                    quantity INTEGER DEFAULT 0,
                    expiry_date DATE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """)
                print("Table 'product_batches' created (PostgreSQL).")
                
                # 2. Check if already migrated
                cursor.execute("SELECT COUNT(*) FROM product_batches")
                if cursor.fetchone()[0] == 0:
                    # 3. Migrate from product_unit_options
                    cursor.execute("""
                        INSERT INTO product_batches (product_id, unit_option_id, quantity)
                        SELECT product_id, id, stock_quantity FROM product_unit_options WHERE stock_quantity > 0
                    """)
                    print("Migrated stock from product_unit_options to product_batches.")
                    
                    # 4. Migrate from main products (only if no unit options)
                    cursor.execute("""
                        INSERT INTO product_batches (product_id, quantity, expiry_date)
                        SELECT id, stock_quantity, expiry_date FROM products 
                        WHERE stock_quantity > 0 AND id NOT IN (SELECT product_id FROM product_unit_options)
                    """)
                    print("Migrated remaining stock from products to product_batches.")
                else:
                    print("Batches already exist, skipping migration.")
                
                conn.commit()
            except Exception as e:
                conn.rollback()
                print(f"Error during migration: {e}")
            finally:
                conn.close()
        except ImportError:
            print("psycopg2 not installed. Cannot migrate PostgreSQL automatically.")
            print("Please run these SQL commands manually on your Neon console:")
            print("""
            CREATE TABLE product_batches (
                id SERIAL PRIMARY KEY,
                product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                unit_option_id INTEGER REFERENCES product_unit_options(id) ON DELETE CASCADE,
                quantity INTEGER DEFAULT 0,
                expiry_date DATE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            -- Migrate existing stock to first batch
            INSERT INTO product_batches (product_id, unit_option_id, quantity, expiry_date)
            SELECT product_id, id, stock_quantity, NULL FROM product_unit_options WHERE stock_quantity > 0;
            
            -- If no unit options, migrate from main product table
            INSERT INTO product_batches (product_id, quantity, expiry_date)
            SELECT id, stock_quantity, expiry_date FROM products WHERE stock_quantity > 0 AND id NOT IN (SELECT product_id FROM product_unit_options);
            """)
        except Exception as e:
            print(f"PostgreSQL connection error: {e}")
        return

    # 2. Local SQLite migration
    db_paths = [
        os.path.join('Backend', 'instance', 'inventory.db'),
        os.path.join('instance', 'inventory.db'),
        'inventory.db'
    ]
    
    for db_path in db_paths:
        if os.path.exists(db_path):
            print(f"\nMigrating local SQLite database at {db_path}...")
            conn = sqlite3.connect(db_path)
            cursor = conn.cursor()
            
            try:
                # 1. Create product_batches table
                cursor.execute("""
                    CREATE TABLE IF NOT EXISTS product_batches (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        product_id INTEGER NOT NULL,
                        unit_option_id INTEGER,
                        quantity INTEGER DEFAULT 0,
                        expiry_date DATE,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                        FOREIGN KEY (unit_option_id) REFERENCES product_unit_options(id) ON DELETE CASCADE
                    )
                """)
                print("Table 'product_batches' created.")
                
                # 2. Check if already migrated
                cursor.execute("SELECT COUNT(*) FROM product_batches")
                if cursor.fetchone()[0] == 0:
                    # 3. Migrate from product_unit_options
                    cursor.execute("""
                        INSERT INTO product_batches (product_id, unit_option_id, quantity)
                        SELECT product_id, id, stock_quantity FROM product_unit_options WHERE stock_quantity > 0
                    """)
                    print("Migrated stock from product_unit_options to product_batches.")
                    
                    # 4. Migrate from main products (only if no unit options)
                    cursor.execute("""
                        INSERT INTO product_batches (product_id, quantity, expiry_date)
                        SELECT id, stock_quantity, expiry_date FROM products 
                        WHERE stock_quantity > 0 AND id NOT IN (SELECT product_id FROM product_unit_options)
                    """)
                    print("Migrated remaining stock from products to product_batches.")
                else:
                    print("Batches already exist, skipping migration.")
                    
                conn.commit()
            except Exception as e:
                print(f"Error migrating {db_path}: {e}")
                conn.rollback()
            finally:
                conn.close()

if __name__ == "__main__":
    migrate_batches()
