import { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { createAlbum, updateAlbum, getAlbumById, uploadAlbumCovers, getAlbumCoverUrls } from '../../api/albumService';
import { getArtists, type Artist } from '../../api/artistService';
import toast from 'react-hot-toast';
import { ChevronLeft } from 'lucide-react';

export default function AlbumForm() {
    const { id } = useParams<{ id: string }>();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const isEditing = !!id;

    const [title, setTitle] = useState('');
    const [artistId, setArtistId] = useState<number | ''>('');
    const [artists, setArtists] = useState<Artist[]>([]);
    const [coverFiles, setCoverFiles] = useState<File[]>([]);
    const [coverPreviews, setCoverPreviews] = useState<string[]>([]);
    const [currentCoverUrls, setCurrentCoverUrls] = useState<string[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadArtists();

        // Pegar artistId da URL se fornecido
        const urlArtistId = searchParams.get('artistId');
        if (urlArtistId) {
            setArtistId(Number(urlArtistId));
        }

        if (isEditing && id) {
            loadAlbum();
        }
    }, [id]);

    async function loadArtists() {
        try {
            const response = await getArtists({ page: 0, size: 1000 });
            setArtists(response.data.content || []);
        } catch (err) {
            toast.error('Erro ao carregar artistas');
        }
    }

    async function loadAlbum() {
        if (!id) return;

        try {
            const response = await getAlbumById(Number(id));
            setTitle(response.data.title);
            setArtistId(response.data.artistId);

            // Carregar todas as capas existentes
            try {
                const coverResponse = await getAlbumCoverUrls(Number(id));
                if (coverResponse.data && coverResponse.data.length > 0) {
                    setCurrentCoverUrls(coverResponse.data);
                }
            } catch (err) {
                console.log('Álbum sem capas');
            }
        } catch (err) {
            toast.error('Erro ao carregar álbum');
            navigate('/artists');
        }
    }

    function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
        const files = Array.from(e.target.files || []);
        if (files.length === 0) return;

        // Validar arquivos
        const validFiles: File[] = [];
        const previews: string[] = [];

        for (const file of files) {
            // Validar tipo de arquivo
            if (!file.type.startsWith('image/')) {
                toast.error(`${file.name} não é uma imagem válida`);
                continue;
            }

            // Validar tamanho (max 5MB)
            if (file.size > 5 * 1024 * 1024) {
                toast.error(`${file.name} é muito grande. Tamanho máximo: 5MB`);
                continue;
            }

            validFiles.push(file);

            // Preview
            const reader = new FileReader();
            reader.onloadend = () => {
                previews.push(reader.result as string);
                if (previews.length === validFiles.length) {
                    setCoverPreviews(previews);
                }
            };
            reader.readAsDataURL(file);
        }

        setCoverFiles(validFiles);
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        if (!title.trim()) {
            toast.error('Título do álbum é obrigatório');
            return;
        }

        if (!artistId) {
            toast.error('Selecione um artista');
            return;
        }

        setLoading(true);
        try {
            let albumId: number;

            if (isEditing && id) {
                await updateAlbum(Number(id), { title, artistId: Number(artistId) });
                albumId = Number(id);
                toast.success('Álbum atualizado com sucesso!');
            } else {
                const response = await createAlbum({ title, artistId: Number(artistId) });
                albumId = response.data.id;
                toast.success('Álbum criado com sucesso!');
            }

            // Upload da capa se selecionada
            if (coverFiles.length > 0) {
                try {
                    await uploadAlbumCovers(albumId, coverFiles);
                    toast.success(`${coverFiles.length} capa(s) enviada(s) com sucesso!`);
                } catch (err) {
                    toast.error('Erro ao enviar capas');
                }
            }

            navigate('/artists');
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao salvar álbum';
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="p-6 max-w-2xl mx-auto">
            <button
                onClick={() => navigate('/artists')}
                className="mb-4 text-gray-600 hover:text-gray-800 flex items-center gap-2 transition-colors"
            >
                <ChevronLeft size={20} />
                Voltar
            </button>

            <div className="bg-white rounded-lg border border-gray-200 p-6">
                <h1 className="text-2xl font-bold text-gray-800 mb-6">
                    {isEditing ? 'Editar Álbum' : 'Novo Álbum'}
                </h1>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                            Título do Álbum *
                        </label>
                        <input
                            id="title"
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                            placeholder="Digite o título do álbum"
                            required
                        />
                    </div>

                    <div>
                        <label htmlFor="artist" className="block text-sm font-medium text-gray-700 mb-1">
                            Artista *
                        </label>
                        <select
                            id="artist"
                            value={artistId}
                            onChange={(e) => setArtistId(Number(e.target.value))}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                            required
                        >
                            <option value="">Selecione um artista</option>
                            {artists.map(artist => (
                                <option key={artist.id} value={artist.id}>
                                    {artist.name}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div>
                        <label htmlFor="cover" className="block text-sm font-medium text-gray-700 mb-1">
                            {isEditing && currentCoverUrls.length > 0 ? 'Adicionar/Alterar Capas do Álbum' : 'Capas do Álbum'}
                        </label>

                        {isEditing && currentCoverUrls.length > 0 && coverPreviews.length === 0 && (
                            <div className="mb-3">
                                <p className="text-sm text-gray-600 mb-2">
                                    {currentCoverUrls.length} capa(s) atual(is):
                                </p>
                                <div className="grid grid-cols-3 gap-4">
                                    {currentCoverUrls.map((url, index) => (
                                        <img
                                            key={index}
                                            src={`http://localhost:8080${url}`}
                                            alt={`Capa ${index + 1}`}
                                            className="w-full h-32 object-cover rounded-lg shadow"
                                        />
                                    ))}
                                </div>
                            </div>
                        )}

                        <input
                            id="cover"
                            type="file"
                            accept="image/*"
                            multiple
                            onChange={handleFileChange}
                            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-400"
                        />
                        <p className="text-xs text-gray-500 mt-1">
                            Formatos aceitos: JPG, PNG. Tamanho máximo por imagem: 5MB. Você pode selecionar múltiplas imagens.
                        </p>
                    </div>

                    {coverPreviews.length > 0 && (
                        <div className="mt-4">
                            <p className="text-sm font-medium text-gray-700 mb-2">
                                {coverPreviews.length} nova(s) capa(s):
                            </p>
                            <div className="grid grid-cols-3 gap-4">
                                {coverPreviews.map((preview, index) => (
                                    <img
                                        key={index}
                                        src={preview}
                                        alt={`Preview ${index + 1}`}
                                        className="w-full h-32 object-cover rounded-lg shadow"
                                    />
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="flex gap-2 pt-4">
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 bg-gray-800 text-white font-medium py-2 rounded-lg hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            {loading ? 'Salvando...' : 'Salvar'}
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate('/artists')}
                            className="flex-1 bg-gray-100 text-gray-700 font-medium py-2 rounded-lg hover:bg-gray-200 transition-colors"
                        >
                            Cancelar
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
