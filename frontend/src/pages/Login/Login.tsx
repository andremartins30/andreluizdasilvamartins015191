import { useState, useEffect } from 'react';
import { login, register } from '../../api/authService';
import { useAuth } from '../../auth/AuthContext';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Eye, EyeOff } from 'lucide-react';

type Mode = 'login' | 'register';

// IDs de álbuns icônicos da Deezer API
const albumIds = [
    96126,          // Thriller - Michael Jackson
    12114240,       // The Dark Side of the Moon - Pink Floydwift
    12047952,       // Abbey Road - The Beatles
    746059,         // 21 - Adele
    1262014,        // Nevermind - Nirvana
    94528272,       // Lemonade - Beyoncé
    9410100,        // Back in Black - AC/DC
    504180521,      // 1989 (Taylor's Version) - Taylor Swift
    68685711,       // Legend - Bob Marley
    11591214,       // Led Zeppelin IV - Led Zeppelin
    12047960,       // Sgt. Pepper's - The Beatles
    68682261,       // Marília Mendonça - Perfil (Ao Vivo)
    59853992,       // Dark Side (2011 Remaster) - Pink Floyd
    14880539,       // 25 - Adele
    52175422,       // Buteco do Gusttavo Lima
    112126942,      // Abbey Road (Super Deluxe) - The Beatles
    262562462,      // Festa das Patroas 35% - Marília Mendonça
    8887733,        // Led Zeppelin IV (Deluxe) - Led Zeppelin
    9007779,        // 1989 (original) - Taylor Swift
    129798982,
    82107,
    708674,
    45442891,
    266682372,
    87325,
    1121183,
    66644212,
    55532632,
    829384671,
    45922662,
    12400334,
    6157080,
    11898198,
];

export default function AuthForm() {
    const [mode, setMode] = useState<Mode>('login');

    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);

    const [loading, setLoading] = useState(false);
    const [currentCovers, setCurrentCovers] = useState<string[]>([]);
    const [coverKeys, setCoverKeys] = useState<number[]>(Array.from({ length: 12 }, (_, i) => i));
    const [allCovers, setAllCovers] = useState<string[]>([]);
    const [, setCurrentIndex] = useState(0);

    const { signIn } = useAuth();
    const navigate = useNavigate();

    // Função genérica para embaralhar array
    const shuffleArray = <T,>(array: T[]): T[] => {
        const shuffled = [...array];
        for (let i = shuffled.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
        }
        return shuffled;
    };

    // Buscar capas de álbuns da Deezer API usando proxy CORS
    useEffect(() => {
        const fetchCovers = async () => {
            const loadedCovers: string[] = [];

            // Fazer todas as requisições em PARALELO para carregar mais rápido
            const coverPromises = albumIds.map(async (id, index) => {
                try {
                    // Usar proxy CORS para evitar bloqueio
                    const response = await fetch(`https://corsproxy.io/?https://api.deezer.com/album/${id}`);
                    const data = await response.json();

                    if (data.cover_big) {
                        loadedCovers.push(data.cover_big);

                        // Atualizar as capas visíveis conforme vão carregando
                        // Exibir incrementalmente quando tivermos pelo menos 12 capas
                        if (loadedCovers.length >= 12 && loadedCovers.length % 4 === 0) {
                            const shuffled = shuffleArray([...loadedCovers]);
                            setCurrentCovers(shuffled.slice(0, 12));
                        }
                    }

                    return data.cover_big || null;
                } catch (error) {
                    console.error(`Erro ao buscar álbum ${id}:`, error);
                    return null;
                }
            });

            // Aguardar todas as requisições completarem
            const results = await Promise.all(coverPromises);
            const covers = results.filter(cover => cover !== null) as string[];

            setAllCovers(covers);
            const shuffled = shuffleArray(covers);
            setCurrentCovers(shuffled.slice(0, 12));
        };

        fetchCovers();
    }, []);

    // Trocar as capas a cada 10 segundos - SEGUINDO ORDEM DO ARRAY (sem repetições)
    useEffect(() => {
        if (allCovers.length === 0 || currentCovers.length === 0) return;

        const interval = setInterval(() => {
            // Criar array de índices [0,1,2,...,11] e embaralhar para ordem aleatória de EXIBIÇÃO
            const positions = Array.from({ length: 12 }, (_, i) => i);
            const shuffledPositions = shuffleArray<number>(positions);

            setCurrentIndex(prevIdx => {
                // Trocar cada capa em ordem ALEATÓRIA com delay escalonado
                shuffledPositions.forEach((position: number, delayIndex: number) => {
                    setTimeout(() => {
                        const coverIdx = (prevIdx + position) % allCovers.length;

                        setCurrentCovers(prev => {
                            const newCovers = [...prev];
                            newCovers[position] = allCovers[coverIdx];
                            return newCovers;
                        });

                        // Forçar re-render com key change
                        setCoverKeys(prev => {
                            const newKeys = [...prev];
                            newKeys[position] = prev[position] + 1000;
                            return newKeys;
                        });
                    }, delayIndex * 1000); // Delay baseado na ordem aleatória
                });

                // Avançar o índice base em 12 posições
                const nextIdx = (prevIdx + 12) % allCovers.length;

                // Se voltou ao início, reembaralhar o array
                if (nextIdx < prevIdx) {
                    setTimeout(() => {
                        setAllCovers(prev => shuffleArray<string>(prev));
                    }, 12000); // Após todas as trocas
                }

                return nextIdx;
            });
        }, 10000);

        return () => clearInterval(interval);
    }, [allCovers, currentCovers]);

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

    // Dividir capas em 3 colunas
    const column1 = currentCovers.slice(0, 4);
    const column2 = currentCovers.slice(4, 8);
    const column3 = currentCovers.slice(8, 12);

    return (
        <div className="flex h-screen overflow-hidden bg-gradient-to-br from-gray-50 to-gray-100">
            {/* Vinyl Covers Gallery Area */}
            <div className="relative hidden w-3/5 overflow-hidden lg:flex items-center justify-center gap-10 p-8">
                <div className="flex items-center justify-center gap-10">
                    {/* Coluna 1 */}
                    <div className="flex flex-col gap-10">
                        {column1.map((cover, index) => (
                            <div
                                key={index}
                                className="relative group"
                                style={{
                                    transform: 'perspective(1500px) rotateY(-15deg) rotateX(8deg)',
                                    transformStyle: 'preserve-3d',
                                    animation: `floatDiagonal ${8 + index * 0.5}s ease-in-out infinite`
                                }}
                            >
                                <img
                                    key={coverKeys[index]}
                                    src={cover}
                                    alt={`Album ${index + 1}`}
                                    className="w-64 h-64 rounded-sm shadow-2xl object-cover group-hover:scale-105 animate-fadeIn"
                                    style={{
                                        boxShadow: '25px 25px 70px rgba(0,0,0,0.35), -15px -15px 50px rgba(255,255,255,0.1), inset 0 0 0 1px rgba(255,255,255,0.1)'
                                    }}
                                />
                            </div>
                        ))}
                    </div>

                    {/* Coluna 2 */}
                    <div className="flex flex-col gap-10 -mt-20">
                        {column2.map((cover, index) => (
                            <div
                                key={index}
                                className="relative group"
                                style={{
                                    transform: 'perspective(1500px) rotateY(-15deg) rotateX(8deg)',
                                    transformStyle: 'preserve-3d',
                                    animation: `floatDiagonal ${8 + index * 0.5}s ease-in-out infinite ${index * 0.3}s`
                                }}
                            >
                                <img
                                    key={coverKeys[index + 4]}
                                    src={cover}
                                    alt={`Album ${index + 5}`}
                                    className="w-64 h-64 rounded-sm shadow-2xl object-cover group-hover:scale-105 animate-fadeIn"
                                    style={{
                                        boxShadow: '25px 25px 70px rgba(0,0,0,0.35), -15px -15px 50px rgba(255,255,255,0.1), inset 0 0 0 1px rgba(255,255,255,0.1)'
                                    }}
                                />
                            </div>
                        ))}
                    </div>

                    {/* Coluna 3 */}
                    <div className="flex flex-col gap-10">
                        {column3.map((cover, index) => (
                            <div
                                key={index}
                                className="relative group"
                                style={{
                                    transform: 'perspective(1500px) rotateY(-15deg) rotateX(8deg)',
                                    transformStyle: 'preserve-3d',
                                    animation: `floatDiagonal ${8 + index * 0.5}s ease-in-out infinite ${index * 0.2}s`
                                }}
                            >
                                <img
                                    key={coverKeys[index + 8]}
                                    src={cover}
                                    alt={`Album ${index + 9}`}
                                    className="w-64 h-64 rounded-sm shadow-2xl object-cover group-hover:scale-105 animate-fadeIn"
                                    style={{
                                        boxShadow: '25px 25px 70px rgba(0,0,0,0.35), -15px -15px 50px rgba(255,255,255,0.1), inset 0 0 0 1px rgba(255,255,255,0.1)'
                                    }}
                                />
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Login Area */}
            <div className="flex w-full flex-col justify-center bg-white px-8 md:px-16 lg:w-2/5 border-l border-gray-200">
                <h1 className="mb-2 text-5xl font-bold tracking-tight text-gray-900">
                    Vinyl Club
                </h1>
                <p className="mb-10 text-gray-600">
                    {mode === 'login' ? 'Acesse sua coleção de discos' : 'Crie sua conta e comece sua coleção'}
                </p>

                <form onSubmit={handleSubmit} className="flex max-w-md flex-col gap-4">
                    <input
                        type="text"
                        placeholder="Usuário"
                        className="rounded-lg bg-gray-50 px-4 py-3 text-sm text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />

                    <div className="relative">
                        <input
                            type={showPassword ? 'text' : 'password'}
                            placeholder="Senha"
                            className="w-full rounded-lg bg-gray-50 px-4 py-3 pr-10 text-sm text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                        >
                            {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                    </div>

                    {mode === 'register' && (
                        <div className="relative">
                            <input
                                type={showConfirmPassword ? 'text' : 'password'}
                                placeholder="Confirmar senha"
                                className="w-full rounded-lg bg-gray-50 px-4 py-3 pr-10 text-sm text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
                                value={confirmPassword}
                                onChange={(e) => setConfirmPassword(e.target.value)}
                                required
                            />
                            <button
                                type="button"
                                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                            >
                                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                            </button>
                        </div>
                    )}

                    <button
                        type="submit"
                        disabled={loading}
                        className="mt-4 rounded-lg bg-red-600 py-3 font-medium text-white transition hover:bg-red-500 disabled:opacity-50 disabled:cursor-not-allowed shadow-lg hover:shadow-xl"
                    >
                        {loading
                            ? 'Processando...'
                            : mode === 'login'
                                ? 'Entrar'
                                : 'Registrar'}
                    </button>
                </form>

                <div className="mt-6 text-center text-sm">
                    {mode === 'login' ? (
                        <p className="text-gray-600">
                            Não tem uma conta?{' '}
                            <button
                                type="button"
                                onClick={() => setMode('register')}
                                className="text-red-600 hover:text-red-500 font-medium transition-colors"
                            >
                                Registre-se
                            </button>
                        </p>
                    ) : (
                        <p className="text-gray-600">
                            Já tem uma conta?{' '}
                            <button
                                type="button"
                                onClick={() => setMode('login')}
                                className="text-red-600 hover:text-red-500 font-medium transition-colors"
                            >
                                Faça login
                            </button>
                        </p>
                    )}
                </div>
            </div>

            <style>{`
                @keyframes floatDiagonal {
                    0%, 100% {
                        transform: perspective(1500px) rotateY(-15deg) rotateX(8deg) translate(0, 0);
                    }
                    50% {
                        transform: perspective(1500px) rotateY(-15deg) rotateX(8deg) translate(15px, -15px);
                    }
                }

                @keyframes fadeIn {
                    from {
                        opacity: 0;
                    }
                    to {
                        opacity: 1;
                    }
                }

                .animate-fadeIn {
                    animation: fadeIn 4s ease-in-out;
                }
            `}</style>
        </div>
    );
}