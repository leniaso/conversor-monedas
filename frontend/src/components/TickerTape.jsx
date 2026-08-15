import { useEffect, useState } from 'react'
import { getRates } from '../api/currencyApi'

const PAIRS = ['EUR', 'GBP', 'JPY', 'COP', 'MXN', 'BRL', 'CAD', 'CHF']

export default function TickerTape() {
  const [rates, setRates] = useState(null)
  const [error, setError] = useState(false)

  useEffect(() => {
    let mounted = true
    getRates('USD')
      .then((data) => mounted && setRates(data))
      .catch(() => mounted && setError(true))
    return () => {
      mounted = false
    }
  }, [])

  const items = rates
    ? PAIRS.filter((p) => rates[p] !== undefined).map((p) => ({ pair: `USD/${p}`, value: rates[p] }))
    : []

  const track = [...items, ...items] // duplicado para el loop continuo

  return (
    <div className="w-full overflow-hidden border-y border-gold/30 bg-ink-light">
      <div className="flex whitespace-nowrap py-2.5 animate-[ticker_28s_linear_infinite] motion-reduce:animate-none">
        {error && (
          <span className="px-6 font-mono text-xs text-slate">
            No se pudieron cargar las tasas en vivo — reintenta más tarde.
          </span>
        )}
        {!error && items.length === 0 &&
          Array.from({ length: 8 }).map((_, i) => (
            <span key={i} className="px-6 font-mono text-xs text-slate/60">cargando · · ·</span>
          ))}
        {track.map((item, i) => (
          <span key={i} className="flex items-center px-6 font-mono text-xs tracking-wide text-paper/90">
            <span className="text-gold mr-2">{item.pair}</span>
            <span>{item.value.toFixed(4)}</span>
            <span className="mx-6 text-slate">•</span>
          </span>
        ))}
      </div>
    </div>
  )
}
