import { useEffect, useState } from 'react'
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts'
import { getVariation } from '../api/currencyApi'

const RANGES = [
  { label: '7D', days: 7 },
  { label: '30D', days: 30 },
  { label: '90D', days: 90 },
]

export default function RateChart({ from, to }) {
  const [days, setDays] = useState(30)
  const [points, setPoints] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!from || !to) return
    setLoading(true)
    setError('')
    const end = new Date()
    const start = new Date()
    start.setDate(end.getDate() - days)
    const fmt = (d) => d.toISOString().slice(0, 10)

    getVariation({ from, to, start: fmt(start), end: fmt(end) })
      .then((data) =>
        setPoints(
          data.points.map((p) => ({
            date: p.date.slice(5), // MM-DD
            rate: p.rate,
          })),
        ),
      )
      .catch(() => setError('No se pudo cargar la variación histórica.'))
      .finally(() => setLoading(false))
  }, [from, to, days])

  const first = points[0]?.rate
  const last = points[points.length - 1]?.rate
  const change = first && last ? (((last - first) / first) * 100).toFixed(2) : null
  const isUp = change !== null && Number(change) >= 0

  return (
    <div className="rounded-2xl border border-paper/10 bg-ink-light p-6 sm:p-8">
      <div className="flex flex-wrap items-start justify-between gap-4 mb-5">
        <div>
          <p className="font-mono text-xs uppercase tracking-[0.2em] text-gold mb-1">Variación</p>
          <h3 className="font-display text-xl font-semibold">
            {from} / {to}
            {change !== null && (
              <span className={`ml-3 font-mono text-sm ${isUp ? 'text-emerald-light' : 'text-rust'}`}>
                {isUp ? '▲' : '▼'} {Math.abs(change)}%
              </span>
            )}
          </h3>
        </div>
        <div className="flex gap-1.5">
          {RANGES.map((r) => (
            <button
              key={r.days}
              onClick={() => setDays(r.days)}
              className={`rounded-md px-3 py-1.5 text-xs font-mono border transition-colors ${
                days === r.days
                  ? 'border-gold bg-gold/10 text-gold'
                  : 'border-paper/20 text-slate hover:text-paper'
              }`}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>

      {loading && <p className="text-sm text-slate">Cargando gráfica…</p>}
      {error && <p className="text-sm text-rust">{error}</p>}

      {!loading && !error && points.length > 0 && (
        <div className="h-64">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={points} margin={{ top: 5, right: 10, left: -10, bottom: 0 }}>
              <defs>
                <linearGradient id="rateFill" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#C89B3C" stopOpacity={0.35} />
                  <stop offset="100%" stopColor="#C89B3C" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#F6F1E41A" vertical={false} />
              <XAxis dataKey="date" stroke="#4A5A63" fontSize={11} tickLine={false} axisLine={false} />
              <YAxis
                domain={['auto', 'auto']}
                stroke="#4A5A63"
                fontSize={11}
                tickLine={false}
                axisLine={false}
                width={60}
              />
              <Tooltip
                contentStyle={{ background: '#0B1F2A', border: '1px solid #C89B3C55', borderRadius: 8 }}
                labelStyle={{ color: '#C89B3C' }}
                itemStyle={{ color: '#F6F1E4' }}
              />
              <Area type="monotone" dataKey="rate" stroke="#C89B3C" strokeWidth={2} fill="url(#rateFill)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  )
}
