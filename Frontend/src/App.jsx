import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './auth/Login';
import Register from './auth/Register';
import AdminDashboard from './dashboard/AdminDashboard';
import ShopDashboard from './dashboard/ShopDashboard';
import SupplierDashboard from './dashboard/SupplierDashboard';
import CustomerDashboard from './dashboard/CustomerDashboard';

const App = () => {
  const role = localStorage.getItem('role');
  const token = localStorage.getItem('token');

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* Dashboards */}
        <Route path="/dashboard/admin" element={<AdminDashboard />} />
        <Route path="/dashboard/shop" element={<ShopDashboard />} />
        <Route path="/dashboard/supplier" element={<SupplierDashboard />} />
        <Route path="/dashboard/customer" element={<CustomerDashboard />} />
        
        <Route path="/" element={token ? (
          role === 'admin' ? <Navigate to="/dashboard/admin" /> :
          role === 'shop_owner' || role === 'salesman' ? <Navigate to="/dashboard/shop" /> :
          role === 'supplier' ? <Navigate to="/dashboard/supplier" /> :
          role === 'customer' ? <Navigate to="/dashboard/customer" /> :
          <Login />
        ) : <Login />} />
      </Routes>
    </Router>
  );
};

export default App;
