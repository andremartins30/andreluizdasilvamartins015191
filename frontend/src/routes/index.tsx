import { Routes, Route } from 'react-router-dom';
import { lazy, Suspense } from 'react';

const Login = lazy(() => import('../pages/Login/Login'));
const Artists = lazy(() => import('../pages/Artists/Artists'));

export function AppRoutes() {
    return (
        <Suspense fallback={<div className="p-4">Carregando...</div>}>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/" element={<Artists />} />
            </Routes>
        </Suspense>
    );
}