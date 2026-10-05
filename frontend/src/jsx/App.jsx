import { lazy, Suspense } from 'react';
import { Outlet, Route, Routes } from 'react-router-dom';
import RequireRole from './auth/RequireRole.jsx';
import Footer from './components/Footer.jsx';
import Header from './components/Header.jsx';
import MobileTabBar from './components/MobileTabBar.jsx';
import RouteScrollManager from './components/RouteScrollManager.jsx';
import HomePage from './pages/HomePage.jsx';

const AboutPage = lazy(() => import('./pages/AboutPage.jsx'));
const AccountSettingsPage = lazy(() => import('./pages/AccountSettingsPage.jsx'));
const AdminAccountGovernancePage = lazy(() => import('./pages/AdminAccountGovernancePage.jsx'));
const AdminDashboardPage = lazy(() => import('./pages/AdminDashboardPage.jsx'));
const BuyerDashboardPage = lazy(() => import('./pages/BuyerDashboardPage.jsx'));
const CartPage = lazy(() => import('./pages/CartPage.jsx'));
const CommunityPage = lazy(() => import('./pages/CommunityPage.jsx'));
const MarketplacePage = lazy(() => import('./pages/MarketplacePage.jsx'));
const ModeratorDashboardPage = lazy(() => import('./pages/ModeratorDashboardPage.jsx'));
const NotificationsPage = lazy(() => import('./pages/NotificationsPage.jsx'));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage.jsx'));
const OrderDetailPage = lazy(() => import('./pages/OrderDetailPage.jsx'));
const OrderPaymentPage = lazy(() => import('./pages/OrderPaymentPage.jsx'));
const ProductDetailPage = lazy(() => import('./pages/ProductDetailPage.jsx'));
const RegisterPage = lazy(() => import('./pages/RegisterPage.jsx'));
const SellerDashboardPage = lazy(() => import('./pages/SellerDashboardPage.jsx'));
const SignInPage = lazy(() => import('./pages/SignInPage.jsx'));
const VendorStatusPage = lazy(() => import('./pages/VendorStatusPage.jsx'));

function RouteFallback() {
  return <div className="route-loading" role="status" aria-live="polite"><span className="route-loading__mark" /><p>Loading this UniMarket page…</p></div>;
}

function SiteLayout() {
  return <>
    <RouteScrollManager />
    <a className="skip-link" href="#route-content">Skip to main content</a>
    <Header />
    <div id="route-content" tabIndex="-1">
      <Suspense fallback={<RouteFallback />}><Outlet /></Suspense>
    </div>
    <Footer />
    <MobileTabBar />
  </>;
}

export default function App() {
  return <Routes>
    <Route element={<SiteLayout />}>
      <Route index element={<HomePage />} />
      <Route path="about" element={<AboutPage />} />
      <Route path="sign-in" element={<SignInPage />} />
      <Route path="register" element={<RegisterPage />} />
      <Route path="marketplace" element={<RequireRole allowGuests excludedRoles={['SELLER', 'ADMIN']}><MarketplacePage /></RequireRole>} />
      <Route path="community" element={<RequireRole roles={['BUYER']} excludedRoles={['SELLER', 'ADMIN']}><CommunityPage /></RequireRole>} />
      <Route path="products/:productId" element={<RequireRole allowGuests excludedRoles={['SELLER', 'ADMIN']}><ProductDetailPage /></RequireRole>} />
      <Route path="notifications" element={<RequireRole><NotificationsPage /></RequireRole>} />
      <Route path="settings" element={<RequireRole><AccountSettingsPage /></RequireRole>} />
      <Route path="buyer" element={<RequireRole roles={['BUYER']} excludedRoles={['SELLER', 'ADMIN']}><BuyerDashboardPage /></RequireRole>} />
      <Route path="cart" element={<RequireRole roles={['BUYER']} excludedRoles={['SELLER', 'ADMIN']}><CartPage /></RequireRole>} />
      <Route path="orders/:orderId" element={<RequireRole roles={['BUYER']} excludedRoles={['SELLER', 'ADMIN']}><OrderDetailPage /></RequireRole>} />
      <Route path="orders/:orderId/payment" element={<RequireRole roles={['BUYER']} excludedRoles={['SELLER', 'ADMIN']}><OrderPaymentPage /></RequireRole>} />
      <Route path="vendor-status" element={<RequireRole roles={['BUYER', 'SELLER']} excludedRoles={['ADMIN']}><VendorStatusPage /></RequireRole>} />
      <Route path="seller" element={<RequireRole roles={['SELLER']} excludedRoles={['MODERATOR', 'ADMIN']}><SellerDashboardPage /></RequireRole>} />
      <Route path="moderation" element={<RequireRole roles={['MODERATOR', 'ADMIN']}><ModeratorDashboardPage /></RequireRole>} />
      <Route path="admin" element={<RequireRole roles={['ADMIN']}><AdminDashboardPage /></RequireRole>} />
      <Route path="admin/accounts" element={<RequireRole roles={['ADMIN']}><AdminAccountGovernancePage /></RequireRole>} />
      <Route path="*" element={<NotFoundPage />} />
    </Route>
  </Routes>;
}
