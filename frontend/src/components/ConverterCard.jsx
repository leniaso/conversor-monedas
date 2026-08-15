import { useEffect, useState } from 'react'
import { getCurrencies, convertCurrency } from '../api/currencyApi'

export default function ConverterCard({ onConverted }) {
  const [currencies, setCurrencies] = useState({})
  const [from, setFrom] = useState('USD')
  const [to, setTo] = useState('EUR')
  const [amount, setAmount] = useState('100')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    getCurrencies()
      .then(setCurrencies)
      .catch(() => setError('No se pudo cargar la lista de monedas.'))
  }, [])

  const swap = () => {
    setFrom(to)
    setTo(from)
    setResult(null)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const parsedAmount = parseFloat(amount)
    if (!parsedAmount || parsedAmount <= 0) {
      setError('Ingresa un monto válido, mayor a 0.')
      return
    }
    setLoading(true)
    try {
      const data = await convertCurrency({ from, to, amount: parsedAmount })
      setResult(data)
      onConverted?.(data)
    } catch (err) {
      setError(err?.response?.data?.error || 'No se pudo realizar la conversión.')
    } finally {
      setLoading(false)
    }
  }

  const currencyOptions = Object.entries(currencies)

  return (
    <div className="rounded-2xl bg-paper text-ink shadow-2xl shadow-black/30 p-6 sm:p-8">
      <p className="font-mono text-xs uppercase tracking-[0.2em] text-emerald mb-1">Convertir</p>
      <h2 className="font-display text-2xl sm:text-3xl font-semibold mb-6">Calcula tu tasa al instante</h2>

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label htmlFor="amount" className="block text-sm font-medium text-slate mb-1.5">
            Monto
          </label>
          <input
            id="amount"
            type="number"
            inputMode="decimal"
            min="0"
            step="0.01"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="w-full rounded-lg border border-ink/15 bg-white px-4 py-3 font-mono text-lg focus:border-emerald focus:ring-1 focus:ring-emerald outline-none"
            placeholder="0.00"
          />
        </div>

        <div className="grid grid-cols-[1fr_auto_1fr] items-end gap-3">
          <div>
            <label htmlFor="from" className="block text-sm font-medium text-slate mb-1.5">
              De
            </label>
            <select
              id="from"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              className="w-full rounded-lg border border-ink/15 bg-white px-3 py-3 font-mono text-sm focus:border-emerald focus:ring-1 focus:ring-emerald outline-none"
            >
              {currencyOptions.length === 0 && <option value={from}>{from}</option>}
              {currencyOptions.map(([code, name]) => (
                <option key={code} value={code}>
                  {code} — {name}
                </option>
              ))}
            </select>
          </div>

          <button
            type="button"
            onClick={swap}
            aria-label="Intercambiar monedas"
            className="mb-1 grid h-11 w-11 place-items-center rounded-full border border-ink/15 bg-white text-emerald hover:bg-emerald hover:text-white transition-colors"
          >
            ⇄
          </button>

          <div>
            <label htmlFor="to" className="block text-sm font-medium text-slate mb-1.5">
              A
            </label>
            <select
              id="to"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              className="w-full rounded-lg border border-ink/15 bg-white px-3 py-3 font-mono text-sm focus:border-emerald focus:ring-1 focus:ring-emerald outline-none"
            >
              {currencyOptions.length === 0 && <option value={to}>{to}</option>}
              {currencyOptions.map(([code, name]) => (
                <option key={code} value={code}>
                  {code} — {name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {error && <p className="text-sm text-rust">{error}</p>}

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-emerald py-3.5 font-semibold text-white hover:bg-emerald-light transition-colors disabled:opacity-60"
        >
          {loading ? 'Convirtiendo…' : 'Convertir'}
        </button>
      </form>

      {result && (
        <div className="mt-6 rounded-xl bg-ink text-paper p-5">
          <p className="font-mono text-xs uppercase tracking-[0.2em] text-gold mb-2">Resultado</p>
          <p className="font-display text-3xl font-semibold">
            {result.convertedAmount.toLocaleString('es-CO', { maximumFractionDigits: 4 })}{' '}
            <span className="text-gold text-xl">{result.to}</span>
          </p>
          <p className="mt-2 font-mono text-xs text-paper/70">
            1 {result.from} = {result.rate.toFixed(6)} {result.to} · {result.date}
          </p>
        </div>
      )}
    </div>
  )
}
