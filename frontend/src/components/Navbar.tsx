import { useAuth } from '../auth/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';

export function Navbar() {
    const { signOut } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    function handleLogout() {
        signOut();
        navigate('/login');
    }

    const isActive = (path: string) => location.pathname.startsWith(path);

    return (
        <nav className="bg-white shadow-md border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 py-3">
                <div className="flex justify-between items-center">
                    <div className="flex items-center gap-6">
                        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">
                            Vinyl Club
                        </h1>
                        <div className="flex gap-2">
                            <button
                                onClick={() => navigate('/artists')}
                                className={`px-4 py-2 rounded-lg transition-all font-medium ${isActive('/artists')
                                    ? 'bg-red-600 text-white shadow-md'
                                    : 'text-gray-700 hover:bg-gray-100'
                                    }`}
                            >
                                Artistas
                            </button>
                            <button
                                onClick={() => navigate('/regionais')}
                                className={`px-4 py-2 rounded-lg transition-all font-medium ${isActive('/regionais')
                                    ? 'bg-red-600 text-white shadow-md'
                                    : 'text-gray-700 hover:bg-gray-100'
                                    }`}
                            >
                                Regionais PJC
                            </button>
                        </div>
                    </div>

                    <button
                        onClick={handleLogout}
                        className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-500 transition-all shadow-lg hover:shadow-xl font-medium"
                    >
                        Sair
                    </button>
                </div>
            </div>
        </nav>
    );
}
