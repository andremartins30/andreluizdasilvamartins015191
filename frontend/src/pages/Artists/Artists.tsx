import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getArtists, type Artist } from '../../api/artistService';
import toast from 'react-hot-toast';

export default function ArtistList() {
    const navigate = useNavigate();
    const [artists, setArtists] = useState<Artist[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        loadArtists();
    }, [currentPage, sortDirection, search]);

    async function loadArtists() {
        setLoading(true);
        try {
            const response = await getArtists({
                page: currentPage,
                size: 10,
                name: search || undefined,
                sort: `name,${sortDirection}`
            });

            setArtists(response.data.content || []);
            setTotalPages(response.data.totalPages);
        } catch (err) {
            console.error('Erro ao carregar artistas:', err);
            toast.error('Erro ao carregar artistas');
            setArtists([]);
        } finally {
            setLoading(false);
        }
    }

    function handleSearch(e: React.FormEvent) {
        e.preventDefault();
        setCurrentPage(0);
        loadArtists();
    }

    function toggleSort() {
        setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
        setCurrentPage(0);
    }

    if (loading) {
        return (
            <div className="p-6">
                <p>Carregando...</p>
            </div>
        );
    }

    return (
        <div className="p-6">
            <div className="flex justify-between items-center mb-6">
                <h1 className="text-2xl font-bold">Artistas</h1>

                <button
                    onClick={() => navigate('/artists/new')}
                    className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                >
                    + Novo Artista
                </button>
            </div>

            <div className="flex gap-2 mb-6">
                <form onSubmit={handleSearch} className="flex gap-2 flex-1">
                    <input
                        type="text"
                        placeholder="Buscar por nome..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="flex-1 px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                        type="submit"
                        className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                    >
                        Buscar
                    </button>
                </form>

                <button
                    onClick={toggleSort}
                    className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
                >
                    {sortDirection === 'asc' ? '↑ A-Z' : '↓ Z-A'}
                </button>
            </div>

            {artists.length === 0 ? (
                <p className="text-gray-500">Nenhum artista encontrado</p>
            ) : (
                <>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {artists.map(artist => (
                            <div
                                key={artist.id}
                                onClick={() => navigate(`/artists/${artist.id}`)}
                                className="bg-white rounded shadow p-4 hover:shadow-lg transition-shadow cursor-pointer"
                            >
                                <h2 className="text-lg font-semibold">{artist.name}</h2>
                                <p className="text-sm text-gray-600">
                                    Álbuns: {artist.albumsCount}
                                </p>
                            </div>
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="flex justify-center gap-2 mt-6">
                            <button
                                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                                disabled={currentPage === 0}
                                className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
                            >
                                Anterior
                            </button>

                            <span className="px-4 py-2">
                                Página {currentPage + 1} de {totalPages}
                            </span>

                            <button
                                onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                                disabled={currentPage >= totalPages - 1}
                                className="px-4 py-2 bg-gray-200 rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
                            >
                                Próxima
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
}