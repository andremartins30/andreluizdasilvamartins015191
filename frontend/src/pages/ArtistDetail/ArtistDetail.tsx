import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getArtistById, type Artist } from '../../api/artistService';
import { getAlbums, type Album } from '../../api/albumService';
import toast from 'react-hot-toast';

export default function ArtistDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [artist, setArtist] = useState<Artist | null>(null);
    const [albums, setAlbums] = useState<Album[]>([]);
    const [loading, setLoading] = useState(true);

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
                album => album.artist.id === Number(id)
            );
            setAlbums(artistAlbums);

        } catch (err) {
            console.error('Erro ao carregar dados:', err);
            toast.error('Erro ao carregar informações do artista');
        } finally {
            setLoading(false);
        }
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
                className="mb-4 text-blue-600 hover:text-blue-700"
            >
                ← Voltar para lista
            </button>

            <div className="bg-white rounded-lg shadow p-6 mb-6">
                <div className="flex justify-between items-center">
                    <div>
                        <h1 className="text-3xl font-bold mb-2">{artist.name}</h1>
                        <p className="text-gray-600">
                            {artist.albumsCount} {artist.albumsCount === 1 ? 'álbum' : 'álbuns'}
                        </p>
                    </div>
                    <button
                        onClick={() => navigate(`/albums/new?artistId=${artist.id}`)}
                        className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
                    >
                        + Novo Álbum
                    </button>
                </div>
            </div>

            <h2 className="text-2xl font-bold mb-4">Álbuns</h2>

            {albums.length === 0 ? (
                <div className="bg-gray-50 border border-gray-200 rounded-lg p-8 text-center">
                    <p className="text-gray-500">
                        Este artista ainda não possui álbuns cadastrados.
                    </p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {albums.map(album => (
                        <div
                            key={album.id}
                            className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-4"
                        >
                            <div className="aspect-square bg-gray-200 rounded mb-3 flex items-center justify-center">
                                <span className="text-gray-400 text-4xl">♪</span>
                            </div>
                            <h3 className="font-semibold text-lg">{album.title}</h3>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}
