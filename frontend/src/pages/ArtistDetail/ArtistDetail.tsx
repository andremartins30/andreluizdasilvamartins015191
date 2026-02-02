import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getArtistById, type Artist } from '../../api/artistService';
import { getAlbums, getAlbumCoverUrl, deleteAlbum, type Album } from '../../api/albumService';
import toast from 'react-hot-toast';
import { ChevronLeft, Plus, Pencil, Trash2, Music } from 'lucide-react';

export default function ArtistDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [artist, setArtist] = useState<Artist | null>(null);
    const [albums, setAlbums] = useState<Album[]>([]);
    const [loading, setLoading] = useState(true);
    const [coverUrls, setCoverUrls] = useState<Record<number, string>>({});

    useEffect(() => {
        if (!id) return;
        loadArtistData();
    }, [id]);

    async function loadArtistData() {
        if (!id) return;

        setLoading(true);
        try {
            const [artistRes, albumsRes] = await Promise.all([
                getArtistById(Number(id)),
                getAlbums({ artistName: '', page: 0, size: 100 })
            ]);

            setArtist(artistRes.data);

            // Filtrar álbuns do artista específico
            const artistAlbums = albumsRes.data.content.filter(
                album => album.artistId === Number(id)
            );
            setAlbums(artistAlbums);

            // Buscar URLs das capas
            loadCoverUrls(artistAlbums);

        } catch (err) {
            console.error('Erro ao carregar dados:', err);
            toast.error('Erro ao carregar informações do artista');
        } finally {
            setLoading(false);
        }
    }

    async function loadCoverUrls(albums: Album[]) {
        const urls: Record<number, string> = {};

        for (const album of albums) {
            try {
                const response = await getAlbumCoverUrl(album.id);
                urls[album.id] = response.data.url;
            } catch (err) {
                // Álbum sem capa, ignorar erro
                console.log(`Álbum ${album.id} sem capa`);
            }
        }

        setCoverUrls(urls);
    }

    async function handleDeleteAlbum(albumId: number, title: string, e: React.MouseEvent) {
        e.stopPropagation();

        if (!confirm(`Tem certeza que deseja excluir o álbum "${title}"?`)) {
            return;
        }

        try {
            await deleteAlbum(albumId);
            toast.success('Álbum excluído com sucesso!');
            loadArtistData();
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao excluir álbum';
            toast.error(errorMessage);
        }
    }

    function handleEditAlbum(albumId: number, e: React.MouseEvent) {
        e.stopPropagation();
        navigate(`/albums/${albumId}/edit`);
    }

    if (loading) {
        return (
            <div className="p-6">
                <p>Carregando...</p>
            </div>
        );
    }

    if (!artist) {
        return (
            <div className="p-6">
                <p className="text-red-600">Artista não encontrado</p>
                <button
                    onClick={() => navigate('/artists')}
                    className="mt-4 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                >
                    Voltar
                </button>
            </div>
        );
    }

    return (
        <div className="p-6">
            <button
                onClick={() => navigate('/artists')}
                className="mb-4 text-gray-600 hover:text-gray-800 flex items-center gap-2 transition-colors"
            >
                <ChevronLeft size={20} />
                Voltar para lista
            </button>

            <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold text-gray-800 mb-2">{artist.name}</h1>
                        <p className="text-gray-500">
                            {artist.albumsCount} {artist.albumsCount === 1 ? 'álbum' : 'álbuns'}
                        </p>
                    </div>
                    <button
                        onClick={() => navigate(`/albums/new?artistId=${artist.id}`)}
                        className="bg-gray-800 text-white px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors flex items-center gap-2"
                    >
                        <Plus size={20} />
                        Novo Álbum
                    </button>
                </div>
            </div>

            <h2 className="text-2xl font-bold text-gray-800 mb-4">Álbuns</h2>

            {albums.length === 0 ? (
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-8 text-center">
                    <Music size={48} className="mx-auto text-gray-300 mb-3" />
                    <p className="text-gray-500">
                        Este artista ainda não possui álbuns cadastrados.
                    </p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {albums.map(album => (
                        <div
                            key={album.id}
                            className="bg-white rounded-lg border border-gray-200 hover:shadow-lg hover:border-gray-300 transition-all p-4"
                        >
                            <div className="aspect-square bg-gray-100 rounded-lg mb-3 flex items-center justify-center overflow-hidden">
                                {coverUrls[album.id] ? (
                                    <img
                                        src={coverUrls[album.id]}
                                        alt={`Capa de ${album.title}`}
                                        className="w-full h-full object-cover"
                                    />
                                ) : (
                                    <Music size={48} className="text-gray-300" />
                                )}
                            </div>
                            <h3 className="font-semibold text-lg text-gray-800 mb-3">{album.title}</h3>

                            <div className="flex gap-2 justify-end">
                                <button
                                    onClick={(e) => handleEditAlbum(album.id, e)}
                                    className="p-2 text-gray-600 hover:text-gray-800 hover:bg-gray-100 rounded-lg transition-colors"
                                    title="Editar álbum"
                                >
                                    <Pencil size={18} />
                                </button>
                                <button
                                    onClick={(e) => handleDeleteAlbum(album.id, album.title, e)}
                                    className="p-2 text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                                    title="Excluir álbum"
                                >
                                    <Trash2 size={18} />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
