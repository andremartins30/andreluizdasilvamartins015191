import { createContext, useContext, useState } from 'react';

interface AuthContextData {
    token: string | null;
    signIn(token: string): void;
    signOut(): void;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export function AuthProvider({ children }: { children: React.ReactNode }) {
    const [token, setToken] = useState<string | null>(
        localStorage.getItem('token')
    );

    function signIn(token: string) {
        localStorage.setItem('token', token);
        setToken(token);
    }

    function signOut() {
        localStorage.removeItem('token');
        setToken(null);
    }

    return (
        <AuthContext.Provider value={{ token, signIn, signOut }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
