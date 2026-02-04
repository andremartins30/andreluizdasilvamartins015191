import { useEffect, useState } from 'react';
import { getRegionais, sincronizarRegionais, type Regional, type SincronizacaoResult } from '../../api/regionalService';
import toast from 'react-hot-toast';
import { RefreshCw, CheckCircle, XCircle } from 'lucide-react';

export default function Regionais() {
    const [regionais, setRegionais] = useState<Regional[]>([]);
    const [loading, setLoading] = useState(true);
    const [sincronizando, setSincronizando] = useState(false);
    const [filtroAtivo, setFiltroAtivo] = useState<'todos' | 'ativos' | 'inativos'>('todos');

    useEffect(() => {
        loadRegionais();
    }, []);

    async function loadRegionais() {
        setLoading(true);
        try {
            const response = await getRegionais();
            setRegionais(response.data || []);
        } catch (err) {
            console.error('Erro ao carregar regionais:', err);
            toast.error('Erro ao carregar regionais');
            setRegionais([]);
        } finally {
            setLoading(false);
        }
    }

    async function handleSincronizar() {
        setSincronizando(true);
        try {
            const response = await sincronizarRegionais();
            const resultado: SincronizacaoResult = response.data;

            toast.success(
                `Sincronização concluída!\n${resultado.inseridos} inseridos, ${resultado.atualizados} atualizados, ${resultado.inativados} inativados`,
                { duration: 5000 }
            );

            loadRegionais();
        } catch (err: any) {
            const errorMessage = err?.response?.data?.message || 'Erro ao sincronizar regionais';
            toast.error(errorMessage);
        } finally {
            setSincronizando(false);
        }
    }

    const regionaisFiltradas = regionais.filter(regional => {
        if (filtroAtivo === 'ativos') return regional.ativo;
        if (filtroAtivo === 'inativos') return !regional.ativo;
        return true;
    });

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
                <h1 className="text-2xl font-bold text-gray-800">Regionais da Polícia Civil</h1>

                <button
                    onClick={handleSincronizar}
                    disabled={sincronizando}
                    className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    <RefreshCw size={20} className={sincronizando ? 'animate-spin' : ''} />
                    {sincronizando ? 'Sincronizando...' : 'Sincronizar'}
                </button>
            </div>

            <div className="mb-6 flex gap-2">
                <button
                    onClick={() => setFiltroAtivo('todos')}
                    className={`px-4 py-2 rounded-lg transition-colors ${filtroAtivo === 'todos'
                            ? 'bg-gray-800 text-white'
                            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                >
                    Todos ({regionais.length})
                </button>
                <button
                    onClick={() => setFiltroAtivo('ativos')}
                    className={`px-4 py-2 rounded-lg transition-colors ${filtroAtivo === 'ativos'
                            ? 'bg-green-600 text-white'
                            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                >
                    Ativos ({regionais.filter(r => r.ativo).length})
                </button>
                <button
                    onClick={() => setFiltroAtivo('inativos')}
                    className={`px-4 py-2 rounded-lg transition-colors ${filtroAtivo === 'inativos'
                            ? 'bg-red-600 text-white'
                            : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                        }`}
                >
                    Inativos ({regionais.filter(r => !r.ativo).length})
                </button>
            </div>

            <div className="bg-white rounded-lg shadow overflow-hidden">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                ID
                            </th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Nome
                            </th>
                            <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Status
                            </th>
                        </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                        {regionaisFiltradas.length === 0 ? (
                            <tr>
                                <td colSpan={3} className="px-6 py-4 text-center text-gray-500">
                                    Nenhuma regional encontrada
                                </td>
                            </tr>
                        ) : (
                            regionaisFiltradas.map((regional) => (
                                <tr
                                    key={regional.id}
                                    className={!regional.ativo ? 'bg-gray-50 opacity-60' : ''}
                                >
                                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                        {regional.id}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                                        {regional.nome}
                                    </td>
                                    <td className="px-6 py-4 whitespace-nowrap text-center">
                                        {regional.ativo ? (
                                            <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                                                <CheckCircle size={14} />
                                                Ativo
                                            </span>
                                        ) : (
                                            <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium bg-red-100 text-red-800">
                                                <XCircle size={14} />
                                                Inativo
                                            </span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {regionaisFiltradas.length > 0 && (
                <div className="mt-4 text-sm text-gray-600">
                    Exibindo {regionaisFiltradas.length} de {regionais.length} regionais
                </div>
            )}
        </div>
    );
}
