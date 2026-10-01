import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  apiRequest,
  loginRequest,
  logoutRequest,
  refreshSession,
  registerRequest,
  setAccessToken,
} from '../../js/api/client.js';
import { getDefaultRoute, hasAnyRole } from '../../js/auth/roles.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [account, setAccount] = useState(null);
  const [status, setStatus] = useState('restoring');

  const clearSession = useCallback(() => {
    setAccessToken(null);
    setAccount(null);
    setStatus('anonymous');
  }, []);

  useEffect(() => {
    let active = true;
    refreshSession()
      .then((payload) => {
        if (!active) return;
        setAccount(payload.account);
        setStatus('authenticated');
      })
      .catch(() => {
        if (!active) return;
        clearSession();
      });

    const expireSession = () => clearSession();
    window.addEventListener('unimarket:auth-expired', expireSession);
    return () => {
      active = false;
      window.removeEventListener('unimarket:auth-expired', expireSession);
    };
  }, [clearSession]);

  const login = useCallback(async (credentials) => {
    const payload = await loginRequest(credentials);
    setAccount(payload.account);
    setStatus('authenticated');
    return payload.account;
  }, []);

  const register = useCallback(async ({
    accountDetails,
    password,
    accountType,
    organizationName,
    qualification,
    yearOfStudy,
    vendorDetails,
  }) => {
    const academicPersona = ['STUDENT', 'FACULTY'].includes(accountType);
    if (academicPersona) {
      await apiRequest('/api/v1/institutions/validate-email', {
        method: 'POST',
        auth: false,
        body: {
          organizationName,
          persona: accountType,
          email: accountDetails.email,
        },
      });
    }

    await registerRequest(accountDetails);
    const loginPayload = await loginRequest({ email: accountDetails.email, password });
    const authenticatedAccount = loginPayload.account;

    if (['STUDENT', 'FACULTY', 'RESIDENT'].includes(accountType)) {
      await apiRequest('/api/v1/account/personas', {
        method: 'POST',
        body: {
          persona: accountType,
          organizationName: accountType === 'RESIDENT' ? null : organizationName,
          qualification: academicPersona ? qualification || null : null,
          yearOfStudy: accountType === 'STUDENT' && yearOfStudy ? Number(yearOfStudy) : null,
        },
      });
    }

    if (accountType === 'VENDOR') {
      await apiRequest('/api/v1/account/vendor-profile', {
        method: 'POST',
        body: {
          vendorType: 'INDIVIDUAL',
          businessName: vendorDetails.businessName,
          description: vendorDetails.description,
          registrationNumber: null,
          websiteUrl: vendorDetails.websiteUrl || null,
        },
      });
    }

    const refreshedAccount = await apiRequest('/api/v1/auth/me');
    setAccount(refreshedAccount);
    setStatus('authenticated');
    return {
      account: refreshedAccount || authenticatedAccount,
      destination: accountType === 'VENDOR' ? '/vendor-status' : getDefaultRoute(refreshedAccount),
    };
  }, []);

  const logout = useCallback(async () => {
    try {
      await logoutRequest();
    } finally {
      clearSession();
    }
  }, [clearSession]);

  const reconcileAccount = useCallback(async () => {
    const latest = await apiRequest('/api/v1/auth/me');
    setAccount(latest);
    return latest;
  }, []);

  const value = useMemo(() => ({
    account,
    status,
    isAuthenticated: status === 'authenticated' && Boolean(account),
    login,
    register,
    logout,
    reconcileAccount,
    hasRole: (...roles) => hasAnyRole(account, roles),
    defaultRoute: getDefaultRoute(account),
  }), [account, status, login, register, logout, reconcileAccount]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider.');
  return context;
}
