import { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { createAlbum, updateAlbum, getAlbumById, uploadAlbumCovers, getAlbumCoverUrls, deleteAlbumCover } from '../../api/albumService';
import { getArtists, type Artist } from '../../api/artistService';
import toast from 'react-hot-toast';
import { ChevronLeft, X } from 'lucide-react';

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

    function handleRemovePreview(index: number) {
        const newFiles = coverFiles.filter((_, i) => i !== index);
        const newPreviews = coverPreviews.filter((_, i) => i !== index);

        setCoverFiles(newFiles);
        setCoverPreviews(newPreviews);

        // Resetar o input file para permitir selecionar novamente
        const fileInput = document.getElementById('cover') as HTMLInputElement;
        if (fileInput) {
            fileInput.value = '';
        }
    }

    async function handleRemoveExistingCover(url: string, index: number) {
        if (!id) return;

        if (!confirm('Deseja realmente remover esta capa?')) {
            return;
        }

        try {
            // Extrair o objectName da URL
            const urlParams = new URLSearchParams(url.split('?')[1]);
            const objectName = urlParams.get('path');

            if (!objectName) {
                toast.error('Erro ao identificar a capa');
                return;
            }

            await deleteAlbumCover(Number(id), objectName);

            // Atualizar a lista de capas
            const newCoverUrls = currentCoverUrls.filter((_, i) => i !== index);
            setCurrentCoverUrls(newCoverUrls);

            toast.success('Capa removida com sucesso!');
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao remover capa';
            toast.error(errorMessage);
        }
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        try {
            let albumId: number;
            const finalArtistId = Number(artistId);

            if (isEditing && id) {
                await updateAlbum(Number(id), { title, artistId: finalArtistId });
                albumId = Number(id);
                toast.success('Álbum atualizado com sucesso!');
            } else {
                const response = await createAlbum({ title, artistId: finalArtistId });
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

            navigate(`/artists/${finalArtistId}`);
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao salvar álbum';
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-6">
            <div className="max-w-4xl mx-auto">
                <button
                    onClick={() => navigate(artistId ? `/artists/${Number(artistId)}` : '/artists')}
                    className="mb-4 text-gray-600 hover:text-gray-800 flex items-center gap-2 transition-colors"
                >
                    <ChevronLeft size={20} />
                    Voltar
                </button>

                <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                    <h1 className="text-3xl font-bold text-gray-900 mb-6">
                        {isEditing ? 'Editar Álbum' : 'Novo Álbum'}
                    </h1>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label htmlFor="title" className="block text-sm font-medium text-gray-900 mb-1">
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
                            <label htmlFor="artistId" className="block text-sm font-medium text-gray-900 mb-1">
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

                            {isEditing && currentCoverUrls.length > 0 && (
                                <div className="mb-3">
                                    <p className="text-sm text-gray-600 mb-2">
                                        {currentCoverUrls.length} capa(s) atual(is):
                                    </p>
                                    <div className="grid grid-cols-3 gap-4">
                                        {currentCoverUrls.map((url, index) => (
                                            <div key={index} className="relative group">
                                                <img
                                                    src={`http://localhost:8080${url}`}
                                                    alt={`Capa ${index + 1}`}
                                                    className="w-full aspect-square object-cover rounded-lg shadow"
                                                />
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveExistingCover(url, index)}
                                                    className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors shadow-lg opacity-0 group-hover:opacity-100"
                                                    title="Remover capa"
                                                >
                                                    <X size={16} />
                                                </button>
                                            </div>
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
                                        <div key={index} className="relative group">
                                            <img
                                                src={preview}
                                                alt={`Preview ${index + 1}`}
                                                className="w-full aspect-square object-cover rounded-lg shadow"
                                            />
                                            <button
                                                type="button"
                                                onClick={() => handleRemovePreview(index)}
                                                className="absolute top-2 right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors shadow-lg opacity-0 group-hover:opacity-100"
                                                title="Remover imagem"
                                            >
                                                <X size={16} />
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        <div className="flex gap-2 pt-4">
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 bg-red-600 text-white font-medium py-3 rounded-lg hover:bg-red-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg hover:shadow-xl"
                            >
                                {loading ? 'Salvando...' : 'Salvar'}
                            </button>

                            <button
                                type="button"
                                onClick={() => navigate(artistId ? `/artists/${Number(artistId)}` : '/artists')}
                                className="flex-1 bg-white border border-gray-200 text-gray-700 font-medium py-3 rounded-lg hover:bg-gray-50 transition-all"
                            >
                                Cancelar
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}
