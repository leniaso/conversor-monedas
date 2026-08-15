import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const client = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

export async function getCurrencies() {
  const { data } = await client.get('/currencies')
  return data // { USD: "US Dollar", EUR: "Euro", ... }
}

export async function getRates(base = 'USD') {
  const { data } = await client.get('/rates', { params: { base } })
  return data // { EUR: 0.92, GBP: 0.78, ... }
}

export async function convertCurrency({ from, to, amount }) {
  const { data } = await client.post('/convert', { from, to, amount })
  return data
}

export async function getHistory(page = 0, size = 10) {
  const { data } = await client.get('/history', { params: { page, size } })
  return data // Page<ConversionHistory>
}

export async function getVariation({ from, to, start, end }) {
  const { data } = await client.get('/variation', { params: { from, to, start, end } })
  return data // { from, to, points: [{date, rate}] }
}

export default client
