import { createContext, useContext, useState, useEffect } from 'react';
import { webSocketService } from '../services/webSocketService';

interface AuthContextData {
    token: string | null;
    refreshToken: string | null;
    signIn(token: string, refreshToken: string): void;
    signOut(): void;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [token, setToken] = useState<string | null>(
        localStorage.getItem('token')
    );
    const [refreshToken, setRefreshToken] = useState<string | null>(
        localStorage.getItem('refreshToken')
    );

    useEffect(() => {
        // Conectar WebSocket quando autenticado
        if (token) {
            webSocketService.connect();
        }

        return () => {
            webSocketService.disconnect();
        };
    }, [token]);

    function signIn(token: string, refreshToken: string) {
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
        setToken(token);
        setRefreshToken(refreshToken);
    }

    function signOut() {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        setToken(null);
        setRefreshToken(null);
        webSocketService.disconnect();
    }

    return (
        <AuthContext.Provider value={{ token, refreshToken, signIn, signOut }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
