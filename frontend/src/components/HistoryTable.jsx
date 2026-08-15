import { useEffect, useState, useCallback } from 'react'
import { getHistory } from '../api/currencyApi'

export default function HistoryTable({ refreshKey }) {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async (p) => {
    setLoading(true)
    setError('')
    try {
      const result = await getHistory(p, 8)
      setData(result)
    } catch {
      setError('No se pudo cargar el historial.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load(page)
  }, [page, load, refreshKey])

  return (
    <div className="rounded-2xl border border-paper/10 bg-ink-light p-6 sm:p-8">
      <p className="font-mono text-xs uppercase tracking-[0.2em] text-gold mb-1">Historial</p>
      <h3 className="font-display text-xl font-semibold mb-5">Tus últimas conversiones</h3>

      {loading && <p className="text-sm text-slate">Cargando historial…</p>}
      {error && <p className="text-sm text-rust">{error}</p>}

      {!loading && !error && data && data.content.length === 0 && (
        <p className="text-sm text-slate">Aún no has hecho ninguna conversión. Cuando conviertas, aparecerá aquí.</p>
      )}

      {!loading && !error && data && data.content.length > 0 && (
        <>
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-paper/15 text-slate">
                  <th className="pb-2 pr-4 font-medium">Fecha</th>
                  <th className="pb-2 pr-4 font-medium">Par</th>
                  <th className="pb-2 pr-4 font-medium">Monto</th>
                  <th className="pb-2 pr-4 font-medium">Tasa</th>
                  <th className="pb-2 font-medium">Convertido</th>
                </tr>
              </thead>
              <tbody className="font-mono">
                {data.content.map((h) => (
                  <tr key={h.id} className="border-b border-paper/5">
                    <td className="py-2.5 pr-4 whitespace-nowrap text-paper/70">
                      {new Date(h.createdAt).toLocaleDateString('es-CO')}
                    </td>
                    <td className="py-2.5 pr-4">
                      <span className="text-gold">{h.fromCurrency}</span> → {h.toCurrency}
                    </td>
                    <td className="py-2.5 pr-4">{h.amount}</td>
                    <td className="py-2.5 pr-4">{h.rate}</td>
                    <td className="py-2.5 font-semibold">{h.convertedAmount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-5 flex items-center justify-between text-sm">
            <button
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={data.first}
              className="rounded-md border border-paper/20 px-3 py-1.5 disabled:opacity-40 hover:border-gold hover:text-gold transition-colors"
            >
              ← Anterior
            </button>
            <span className="text-slate">
              Página {data.number + 1} de {Math.max(data.totalPages, 1)}
            </span>
            <button
              onClick={() => setPage((p) => p + 1)}
              disabled={data.last}
              className="rounded-md border border-paper/20 px-3 py-1.5 disabled:opacity-40 hover:border-gold hover:text-gold transition-colors"
            >
              Siguiente →
            </button>
          </div>
        </>
      )}
    </div>
  )
}
