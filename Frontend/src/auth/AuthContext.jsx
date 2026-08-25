/* eslint-disable react-hooks/set-state-in-effect */
import { useCallback, useEffect, useMemo, useState } from 'react';
import { onAuthenticationExpired } from '../api/client';
import { authService } from '../services/auth';
import { AuthContext } from './authContext';

export function AuthProvider({ children }) {
  const [state, setState] = useState({ status: 'loading', user: null, error: null });

  const setAuthenticated = useCallback((user) => {
    setState({ status: 'authenticated', user, error: null });
  }, []);

  const refresh = useCallback(async () => {
    try {
      const user = await authService.me();
      setAuthenticated(user);
      return user;
    } catch (error) {
      setState({
        status: error.status === 401 ? 'anonymous' : 'error',
        user: null,
        error: error.status === 401 ? null : error,
      });
      return null;
    }
  }, [setAuthenticated]);

  useEffect(() => {
    const removeAuthListener = onAuthenticationExpired(() => {
      setState({ status: 'anonymous', user: null, error: null });
    });
    refresh();
    return removeAuthListener;
  }, [refresh]);

  const login = useCallback(async (payload) => {
    const response = await authService.login(payload);
    setAuthenticated(response.user);
    return response;
  }, [setAuthenticated]);

  const bootstrap = useCallback(async (payload) => {
    await authService.bootstrap(payload);
    return login({ email: payload.email, password: payload.password });
  }, [login]);

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // Expired sessions are cleared locally even when the logout request cannot finish.
    } finally {
      setState({ status: 'anonymous', user: null, error: null });
    }
  }, []);

  const value = useMemo(() => ({
    ...state,
    isAuthenticated: state.status === 'authenticated',
    login,
    bootstrap,
    logout,
    refresh,
  }), [bootstrap, login, logout, refresh, state]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
