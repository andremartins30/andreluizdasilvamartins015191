import { Routes, Route, Navigate } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { Navbar } from '../components/Navbar';
import { useAuth } from '../auth/AuthContext';

const Login = lazy(() => import('../pages/Login/Login'));
const Artists = lazy(() => import('../pages/Artists/Artists'));

export function AppRoutes() {
    const { token } = useAuth();

    return (
        <Suspense fallback={<div className="p-4">Carregando...</div>}>
            <Routes>
                <Route
                    path="/login"
                    element={token ? <Navigate to="/artists" replace /> : <Login />}
                />

                <Route
                    path="/artists"
                    element={
                        <ProtectedRoute>
                            <Navbar />
                            <Artists />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/"
                    element={<Navigate to={token ? "/artists" : "/login"} replace />}
                />
            </Routes>
        </Suspense>
    );
}