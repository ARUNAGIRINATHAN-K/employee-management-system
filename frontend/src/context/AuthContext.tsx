import { createContext, useContext, useState, useCallback, type ReactNode } from 'react';
import { authService } from '../services/authService';
import { tokenUtils } from '../utils/tokenUtils';
import type { AuthUser, LoginRequest } from '../types';

interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  logout: () => void;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  isAdmin: () => boolean;
  isHR: () => boolean;
  isManager: () => boolean;
  isEmployee: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<AuthUser | null>(
    () => tokenUtils.getUser<AuthUser>()
  );
  const [isLoading, setIsLoading] = useState(false);

  const login = useCallback(async (data: LoginRequest) => {
    setIsLoading(true);
    try {
      const response = await authService.login(data);
      tokenUtils.setToken(response.token);
      const authUser: AuthUser = {
        username: response.username,
        email: response.email,
        roles: response.roles,
        employeeId: response.employeeId,
      };
      tokenUtils.setUser(authUser);
      setUser(authUser);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    tokenUtils.clear();
    setUser(null);
  }, []);

  const hasRole = useCallback(
    (role: string) => user?.roles.includes(role) ?? false,
    [user]
  );

  const hasAnyRole = useCallback(
    (roles: string[]) => roles.some((r) => user?.roles.includes(r)) ?? false,
    [user]
  );

  const isAdmin    = useCallback(() => hasRole('ROLE_ADMIN'),    [hasRole]);
  const isHR       = useCallback(() => hasRole('ROLE_HR'),       [hasRole]);
  const isManager  = useCallback(() => hasRole('ROLE_MANAGER'),  [hasRole]);
  const isEmployee = useCallback(() => hasRole('ROLE_EMPLOYEE'), [hasRole]);

  return (
    <AuthContext.Provider
      value={{ user, isAuthenticated: !!user, isLoading, login, logout, hasRole, hasAnyRole, isAdmin, isHR, isManager, isEmployee }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
};
