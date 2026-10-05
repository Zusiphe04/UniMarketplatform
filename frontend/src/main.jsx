import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './jsx/App.jsx';
import { AuthProvider } from './jsx/auth/AuthContext.jsx';
import { CartProvider } from './jsx/cart/CartContext.jsx';
import ApiStartupGate from './jsx/components/ApiStartupGate.jsx';
import './css/index.css';
import './css/layout.css';
import './css/components.css';
import './css/pages.css';
import './css/responsive.css';
import './css/polish.css';
import './css/mobile.css';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ApiStartupGate>
      <BrowserRouter>
        <AuthProvider>
          <CartProvider>
            <App />
          </CartProvider>
        </AuthProvider>
      </BrowserRouter>
    </ApiStartupGate>
  </StrictMode>,
);
