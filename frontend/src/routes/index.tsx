import { Routes, Route, Navigate } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { ProtectedRoute } from '../components/ProtectedRoute';
import { Navbar } from '../components/Navbar';
import { useAuth } from '../auth/AuthContext';

const Login = lazy(() => import('../pages/Login/Login'));
const Artists = lazy(() => import('../pages/Artists/Artists'));
const ArtistDetail = lazy(() => import('../pages/ArtistDetail/ArtistDetail'));
const ArtistForm = lazy(() => import('../pages/ArtistForm/ArtistForm'));
const AlbumForm = lazy(() => import('../pages/AlbumForm/AlbumForm'));

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
                    path="/artists/new"
                    element={
                        <ProtectedRoute>
                            <Navbar />
                            <ArtistForm />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/artists/:id/edit"
                    element={
                        <ProtectedRoute>
                            <Navbar />
                            <ArtistForm />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/artists/:id"
                    element={
                        <ProtectedRoute>
                            <Navbar />
                            <ArtistDetail />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/albums/new"
                    element={
                        <ProtectedRoute>
                            <Navbar />
                            <AlbumForm />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/albums/:id/edit"
                    element={
                        <ProtectedRoute>
                            <Navbar />
                            <AlbumForm />
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