import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createArtist, updateArtist, getArtistById } from '../../api/artistService';
import toast from 'react-hot-toast';
import { ChevronLeft } from 'lucide-react';

export default function ArtistForm() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const isEditing = !!id;

    const [name, setName] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isEditing && id) {
            loadArtist();
        }
    }, [id]);

    async function loadArtist() {
        if (!id) return;

        try {
            const response = await getArtistById(Number(id));
            setName(response.data.name);
        } catch (err) {
            toast.error('Erro ao carregar artista');
            navigate('/artists');
        }
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        if (!name.trim()) {
            toast.error('Nome do artista é obrigatório');
            return;
        }

        setLoading(true);
        try {
            if (isEditing && id) {
                await updateArtist(Number(id), { name });
                toast.success('Artista atualizado com sucesso!');
            } else {
                await createArtist({ name });
                toast.success('Artista criado com sucesso!');
            }
            navigate('/artists');
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao salvar artista';
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 p-6">
            <div className="max-w-2xl mx-auto">
                <button
                    onClick={() => navigate('/artists')}
                    className="mb-4 text-gray-600 hover:text-gray-900 flex items-center gap-2 transition-colors font-medium"
                >
                    <ChevronLeft size={20} />
                    Voltar
                </button>

                <div className="bg-white rounded-lg border border-gray-200 p-6 shadow-sm">
                    <h1 className="text-3xl font-bold text-gray-900 mb-6">
                        {isEditing ? 'Editar Artista' : 'Novo Artista'}
                    </h1>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label htmlFor="name" className="block text-sm font-medium text-gray-900 mb-1">
                                Nome do Artista *
                            </label>
                            <input
                                id="name"
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                className="w-full px-4 py-3 border border-gray-200 rounded-lg bg-gray-50 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
                                placeholder="Digite o nome do artista"
                                required
                            />
                        </div>

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
                                onClick={() => navigate('/artists')}
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
