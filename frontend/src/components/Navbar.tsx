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
        <nav className="bg-white shadow-md">
            <div className="max-w-7xl mx-auto px-4 py-3">
                <div className="flex justify-between items-center">
                    <div className="flex gap-4">
                        <button
                            onClick={() => navigate('/artists')}
                            className={`px-4 py-2 rounded transition-colors ${isActive('/artists')
                                    ? 'bg-gray-800 text-white'
                                    : 'text-gray-700 hover:bg-gray-100'
                                }`}
                        >
                            Artistas
                        </button>
                        <button
                            onClick={() => navigate('/regionais')}
                            className={`px-4 py-2 rounded transition-colors ${isActive('/regionais')
                                    ? 'bg-gray-800 text-white'
                                    : 'text-gray-700 hover:bg-gray-100'
                                }`}
                        >
                            Regionais
                        </button>
                    </div>

                    <button
                        onClick={handleLogout}
                        className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                    >
                        Sair
                    </button>
                </div>
            </div>
        </nav>
    );
}
