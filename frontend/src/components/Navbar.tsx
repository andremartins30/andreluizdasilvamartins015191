import { useAuth } from '../auth/AuthContext';
import { useNavigate } from 'react-router-dom';

export function Navbar() {
    const { signOut } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        signOut();
        navigate('/login');
    }

    return (
        <nav className="bg-white shadow-md">
            <div className="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center">
                <h1 className="text-xl font-bold text-gray-800">
                    
                </h1>

                <button
                    onClick={handleLogout}
                    className="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-700"
                >
                    Sair
                </button>
            </div>
        </nav>
    );
}
