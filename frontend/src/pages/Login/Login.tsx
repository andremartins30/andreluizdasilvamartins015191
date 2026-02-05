import { useState } from 'react';
import { login, register } from '../../api/authService';
import { useAuth } from '../../auth/AuthContext';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Eye, EyeOff } from 'lucide-react';

type Mode = 'login' | 'register';

export default function AuthForm() {
    const [mode, setMode] = useState<Mode>('login');

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [loading, setLoading] = useState(false);

    const { signIn } = useAuth();
    const navigate = useNavigate();

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        if (mode === 'register' && password !== confirmPassword) {
            toast.error('As senhas não conferem');
            return;
        }

        setLoading(true);

        try {
            if (mode === 'login') {
                const response = await login({ username, password });
                signIn(response.token, response.refreshToken);
                toast.success('Login realizado com sucesso!');
                navigate('/artists');
            } else {
                await register({ username, password });
                toast.success('Usuário registrado com sucesso! Faça login.');
                setMode('login');
                setUsername('');
                setPassword('');
                setConfirmPassword('');
            }
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message ||
                (mode === 'login'
                    ? 'Usuário ou senha inválidos'
                    : 'Erro ao registrar usuário');
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="w-full max-w-md p-8">
                <div className="bg-white rounded-lg border border-gray-200 p-8">
                    <h1 className="text-2xl font-bold text-center text-gray-800 mb-6">
                        {mode === 'login' ? 'Login' : 'Registrar-se'}
                    </h1>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Usuário
                            </label>
                            <input
                                type="text"
                                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                Senha
                            </label>
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    className="w-full px-3 py-2 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors"
                                >
                                    {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                </button>
                            </div>
                        </div>

                        {mode === 'register' && (
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-1">
                                    Confirmar senha
                                </label>
                                <div className="relative">
                                    <input
                                        type={showConfirmPassword ? 'text' : 'password'}
                                        className="w-full px-3 py-2 pr-10 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                                        value={confirmPassword}
                                        onChange={(e) => setConfirmPassword(e.target.value)}
                                        required
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                        className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors"
                                    >
                                        {showConfirmPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                                    </button>
                                </div>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-gray-800 text-white font-medium py-2 rounded-lg hover:bg-gray-700 disabled:opacity-50 transition-colors"
                        >
                            {loading
                                ? 'Processando...'
                                : mode === 'login'
                                    ? 'Entrar'
                                    : 'Registrar'}
                        </button>
                    </form>

                    <div className="mt-4 text-center text-sm">
                        {mode === 'login' ? (
                            <button
                                type="button"
                                onClick={() => setMode('register')}
                                className="text-gray-600 hover:text-gray-800 transition-colors"
                            >
                                Registre-se
                            </button>
                        ) : (
                            <button
                                type="button"
                                onClick={() => setMode('login')}
                                className="text-gray-600 hover:text-gray-800 transition-colors"
                            >
                                Voltar para login
                            </button>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}