import { useState } from 'react'
import TickerTape from './components/TickerTape'
import ConverterCard from './components/ConverterCard'
import HistoryTable from './components/HistoryTable'
import RateChart from './components/RateChart'

export default function App() {
  const [lastConversion, setLastConversion] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  const handleConverted = (result) => {
    setLastConversion(result)
    setRefreshKey((k) => k + 1)
  }

  const chartPair = lastConversion
    ? { from: lastConversion.from, to: lastConversion.to }
    : { from: 'USD', to: 'EUR' }

  return (
    <div className="min-h-screen bg-ink bg-grain">
      <TickerTape />

      <header className="mx-auto max-w-6xl px-6 sm:px-8 pt-14 pb-10">
        <p className="font-mono text-xs uppercase tracking-[0.3em] text-gold mb-4">Casa de Cambio Digital</p>
        <h1 className="font-display text-4xl sm:text-6xl font-semibold leading-[1.05] max-w-3xl">
          Convierte tu dinero
          <br />
          <span className="italic font-normal text-paper/70">sin sorpresas en la tasa.</span>
        </h1>
        <p className="mt-5 max-w-xl text-paper/60">
          Tasas actualizadas por el Banco Central Europeo vía Frankfurter API, historial guardado
          en tu base de datos y la variación de los últimos días, todo en un solo lugar.
        </p>
      </header>

      <main className="mx-auto max-w-6xl px-6 sm:px-8 pb-20">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-6 items-start">
          <div className="lg:col-span-2">
            <ConverterCard onConverted={handleConverted} />
          </div>
          <div className="lg:col-span-3 flex flex-col gap-6">
            <RateChart from={chartPair.from} to={chartPair.to} />
            <HistoryTable refreshKey={refreshKey} />
          </div>
        </div>
      </main>

      <footer className="border-t border-paper/10 py-8">
        <p className="text-center font-mono text-xs text-slate">
          Datos de tasas de cambio proporcionados por Frankfurter API (Banco Central Europeo).
        </p>
      </footer>
    </div>
  )
}
