import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getArtists, deleteArtist, type Artist } from '../../api/artistService';
import toast from 'react-hot-toast';
import { Pencil, Trash2, Plus, Search, ArrowUpDown } from 'lucide-react';

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

    async function handleDelete(id: number, name: string, e: React.MouseEvent) {
        e.stopPropagation();

        if (!confirm(`Tem certeza que deseja excluir o artista "${name}"?`)) {
            return;
        }

        try {
            await deleteArtist(id);
            toast.success('Artista excluído com sucesso!');
            loadArtists();
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao excluir artista';
            toast.error(errorMessage);
        }
    }

    function handleEdit(id: number, e: React.MouseEvent) {
        e.stopPropagation();
        navigate(`/artists/${id}/edit`);
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
                <h1 className="text-2xl font-bold text-gray-800">Artistas</h1>

                <button
                    onClick={() => navigate('/artists/new')}
                    className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors flex items-center gap-2"
                >
                    <Plus size={20} />
                    Novo Artista
                </button>
            </div>

            <div className="flex gap-2 mb-6">
                <form onSubmit={handleSearch} className="flex gap-2 flex-1">
                    <input
                        type="text"
                        placeholder="Buscar por nome..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                    />
                    <button
                        type="submit"
                        className="bg-gray-700 text-white px-4 py-2 rounded-lg hover:bg-gray-600 transition-colors flex items-center gap-2"
                    >
                        <Search size={18} />
                        Buscar
                    </button>
                </form>

                <button
                    onClick={toggleSort}
                    className="bg-gray-100 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2"
                >
                    <ArrowUpDown size={18} />
                    {sortDirection === 'asc' ? 'A-Z' : 'Z-A'}
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
                                className="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-lg hover:border-gray-300 transition-all cursor-pointer relative"
                            >
                                <h2 className="text-lg font-semibold text-gray-800 mb-1">{artist.name}</h2>
                                <p className="text-sm text-gray-500 mb-4">
                                    {artist.albumsCount} {artist.albumsCount === 1 ? 'álbum' : 'álbuns'}
                                </p>

                                <div className="flex gap-2 justify-end">
                                    <button
                                        onClick={(e) => handleEdit(artist.id, e)}
                                        className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
                                        title="Editar artista"
                                    >
                                        <Pencil size={18} />
                                    </button>
                                    <button
                                        onClick={(e) => handleDelete(artist.id, artist.name, e)}
                                        className="p-2 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                                        title="Excluir artista"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="flex justify-center gap-2 mt-6">
                            <button
                                onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                                disabled={currentPage === 0}
                                className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200 transition-colors"
                            >
                                Anterior
                            </button>

                            <span className="px-4 py-2 text-gray-600">
                                Página {currentPage + 1} de {totalPages}
                            </span>

                            <button
                                onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                                disabled={currentPage >= totalPages - 1}
                                className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-200 transition-colors"
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