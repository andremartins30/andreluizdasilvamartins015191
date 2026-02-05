import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getArtists, deleteArtist, type Artist } from '../../api/artistService';
import toast from 'react-hot-toast';
import { Pencil, Trash2, Plus, Search, ArrowUpDown, LayoutGrid, List } from 'lucide-react';

type ViewMode = 'cards' | 'list';

export default function ArtistList() {
    const navigate = useNavigate();
    const [artists, setArtists] = useState<Artist[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');
    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [viewMode, setViewMode] = useState<ViewMode>('cards');

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

        const artist = artists.find(a => a.id === id);
        const albumCount = artist?.albumsCount || 0;

        let confirmMessage = `Tem certeza que deseja excluir o artista "${name}"?`;

        if (albumCount > 0) {
            confirmMessage = `ATENÇÃO: O artista "${name}" possui ${albumCount} ${albumCount === 1 ? 'álbum' : 'álbuns'}.\n\nAo excluir este artista, TODOS os álbuns também serão excluídos permanentemente.\n\nDeseja continuar?`;
        }

        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            await deleteArtist(id);
            if (albumCount > 0) {
                toast.success(`Artista e ${albumCount} ${albumCount === 1 ? 'álbum excluídos' : 'álbuns excluídos'} com sucesso!`);
            } else {
                toast.success('Artista excluído com sucesso!');
            }
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
            <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-6">
                <p className="text-gray-600">Carregando...</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-6">
            <div className="max-w-7xl mx-auto">
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-3xl font-bold text-gray-900">Artistas</h1>

                    <button
                        onClick={() => navigate('/artists/new')}
                        className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-500 transition-all shadow-lg hover:shadow-xl flex items-center gap-2 font-medium"
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
                            className="flex-1 px-4 py-2 border border-gray-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
                        />
                        <button
                            type="submit"
                            className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-500 transition-all shadow-md flex items-center gap-2 font-medium"
                        >
                            <Search size={18} />
                            Buscar
                        </button>
                    </form>

                    <button
                        onClick={toggleSort}
                        className="bg-white text-gray-700 px-4 py-2 rounded-lg border border-gray-200 hover:bg-gray-50 transition-all flex items-center gap-2"
                    >
                        <ArrowUpDown size={18} />
                        {sortDirection === 'asc' ? 'A-Z' : 'Z-A'}
                    </button>

                    <div className="flex bg-white border border-gray-200 rounded-lg overflow-hidden">
                        <button
                            onClick={() => setViewMode('cards')}
                            className={`px-4 py-2 transition-all flex items-center gap-2 ${viewMode === 'cards'
                                ? 'bg-red-600 text-white'
                                : 'text-gray-700 hover:bg-gray-50'
                                }`}
                            title="Visualização em Cards"
                        >
                            <LayoutGrid size={18} />
                        </button>
                        <button
                            onClick={() => setViewMode('list')}
                            className={`px-4 py-2 transition-all flex items-center gap-2 border-l border-gray-200 ${viewMode === 'list'
                                ? 'bg-red-600 text-white'
                                : 'text-gray-700 hover:bg-gray-50'
                                }`}
                            title="Visualização em Lista"
                        >
                            <List size={18} />
                        </button>
                    </div>
                </div>

                {artists.length === 0 ? (
                    <p className="text-gray-500">Nenhum artista encontrado</p>
                ) : (
                    <>
                        {viewMode === 'cards' ? (
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                                {artists.map(artist => (
                                    <div
                                        key={artist.id}
                                        onClick={() => navigate(`/artists/${artist.id}`)}
                                        className="bg-white rounded-lg border border-gray-200 p-4 hover:shadow-lg hover:border-gray-300 transition-all cursor-pointer"
                                    >
                                        <h2 className="text-lg font-semibold text-gray-900 mb-1">{artist.name}</h2>
                                        <p className="text-sm text-gray-500 mb-4">
                                            {artist.albumsCount} {artist.albumsCount === 1 ? 'álbum' : 'álbuns'}
                                        </p>

                                        <div className="flex gap-2 justify-end">
                                            <button
                                                onClick={(e) => handleEdit(artist.id, e)}
                                                className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
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
                        ) : (
                            <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
                                <table className="w-full">
                                    <thead className="bg-gray-50 border-b border-gray-200">
                                        <tr>
                                            <th className="text-left px-6 py-3 text-sm font-semibold text-gray-900">Nome</th>
                                            <th className="text-left px-6 py-3 text-sm font-semibold text-gray-900">Álbuns</th>
                                            <th className="text-right px-6 py-3 text-sm font-semibold text-gray-900">Ações</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                        {artists.map(artist => (
                                            <tr
                                                key={artist.id}
                                                onClick={() => navigate(`/artists/${artist.id}`)}
                                                className="hover:bg-gray-50 cursor-pointer transition-colors"
                                            >
                                                <td className="px-6 py-4 text-gray-900 font-medium">{artist.name}</td>
                                                <td className="px-6 py-4 text-gray-600">
                                                    {artist.albumsCount} {artist.albumsCount === 1 ? 'álbum' : 'álbuns'}
                                                </td>
                                                <td className="px-6 py-4">
                                                    <div className="flex gap-2 justify-end">
                                                        <button
                                                            onClick={(e) => handleEdit(artist.id, e)}
                                                            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
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
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}

                        {totalPages > 1 && (
                            <div className="flex justify-center gap-2 mt-6">
                                <button
                                    onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
                                    disabled={currentPage === 0}
                                    className="px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 transition-all font-medium"
                                >
                                    Anterior
                                </button>

                                <span className="px-4 py-2 text-gray-700 font-medium">
                                    Página {currentPage + 1} de {totalPages}
                                </span>

                                <button
                                    onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
                                    disabled={currentPage >= totalPages - 1}
                                    className="px-4 py-2 bg-white border border-gray-200 text-gray-700 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 transition-all font-medium"
                                >
                                    Próxima
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}