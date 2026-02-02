import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { createArtist, updateArtist, getArtistById } from '../../api/artistService';
import toast from 'react-hot-toast';

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
        <div className="p-6 max-w-2xl mx-auto">
            <button
                onClick={() => navigate('/artists')}
                className="mb-4 text-blue-600 hover:text-blue-700"
            >
                ← Voltar
            </button>

            <div className="bg-white rounded-lg shadow p-6">
                <h1 className="text-2xl font-bold mb-6">
                    {isEditing ? 'Editar Artista' : 'Novo Artista'}
                </h1>

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-1">
                            Nome do Artista *
                        </label>
                        <input
                            id="name"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                            placeholder="Digite o nome do artista"
                            required
                        />
                    </div>

                    <div className="flex gap-2 pt-4">
                        <button
                            type="submit"
                            disabled={loading}
                            className="flex-1 bg-blue-600 text-white font-medium py-2 rounded hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? 'Salvando...' : 'Salvar'}
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate('/artists')}
                            className="flex-1 bg-gray-300 text-gray-700 font-medium py-2 rounded hover:bg-gray-400"
                        >
                            Cancelar
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
