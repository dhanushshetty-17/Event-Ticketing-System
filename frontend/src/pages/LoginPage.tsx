import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ArrowRight, Eye, EyeOff, LockKeyhole, Mail, Sparkles, Ticket } from 'lucide-react'

import { useAuth } from '../context/useAuth'

function LoginPage() {
  const { isAuthenticated, loginUser } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const locationMessage = (location.state as { message?: string } | null)?.message

  if (isAuthenticated) {
    return <Navigate to="/events" replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    setError('')
    setIsLoading(true)

    try {
      await loginUser(email, password)
      const returnTo = (location.state as { returnTo?: string } | null)?.returnTo
      navigate(returnTo || '/events', { replace: true })
    } catch {
      setError('Invalid email or password.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-[#18251f] px-4 py-6 text-[#18251f] sm:px-8">
      <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl overflow-hidden rounded-[2rem] bg-[#f7f4ee] shadow-2xl lg:grid-cols-[1.05fr_0.95fr]">
        <aside className="relative hidden overflow-hidden bg-[#c2523b] p-10 text-[#f7f4ee] lg:flex lg:flex-col lg:justify-between">
          <div className="absolute -right-24 -top-20 h-80 w-80 rounded-full border-[42px] border-white/10" />
          <div className="absolute -bottom-24 -left-20 h-72 w-72 rounded-full bg-[#e7b85b]/20 blur-2xl" />
          <div className="relative">
            <div className="flex items-center gap-3 text-lg font-bold"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#18251f]"><Ticket size={20} /></span><span className="font-serif text-2xl">Evently</span></div>
            <p className="mt-24 flex items-center gap-2 text-xs font-bold uppercase tracking-[0.25em] text-[#ffd990]"><Sparkles size={15} /> A better kind of night out</p>
            <h2 className="mt-5 max-w-md font-serif text-6xl leading-[0.95]">Your next<br />good story<br /><span className="text-[#ffd990]">starts here.</span></h2>
            <p className="mt-6 max-w-sm leading-7 text-white/80">Discover the rooms, stages and tables that make ordinary weeks feel a little less ordinary.</p>
          </div>
          <div className="relative flex gap-3 text-sm text-white/75"><span className="rounded-full border border-white/25 px-4 py-2">Curated events</span><span className="rounded-full border border-white/25 px-4 py-2">Seat-first booking</span></div>
        </aside>
        <section className="flex items-center justify-center p-6 sm:p-12">
          <div className="w-full max-w-md">
            <div className="mb-8 lg:hidden"><div className="flex items-center gap-3 text-lg font-bold"><span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#c2523b] text-white"><Ticket size={20} /></span><span className="font-serif text-2xl">Evently</span></div></div>
            <div className="mb-8">
              <p className="text-sm font-bold uppercase tracking-[0.2em] text-[#c2523b]">Welcome back</p>
              <h1 className="mt-3 font-serif text-4xl text-[#18251f]">Pick up where<br />you left off.</h1>
              <p className="mt-3 text-sm leading-6 text-slate-500">Sign in to keep your plans, seats and tickets in one place.</p>
            </div>

        {locationMessage && !error && <div role="status" className="mb-5 rounded-xl border border-[#b8d7c1] bg-[#e8f0e8] px-4 py-3 text-sm text-[#245c40]">{locationMessage}</div>}
        {error && (
          <div
            role="alert"
            className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label
              htmlFor="email"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Email
            </label>

            <div className="relative">
              <Mail
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
              />

              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                required
                autoComplete="email"
                className="w-full rounded-xl border border-[#d8d1c5] bg-white py-3 pl-10 pr-4 text-sm outline-none transition focus:border-[#c2523b] focus:ring-2 focus:ring-[#c2523b]/15"
                placeholder="you@example.com"
              />
            </div>
          </div>

          <div>
            <label
              htmlFor="password"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Password
            </label>

            <div className="relative">
              <LockKeyhole
                size={18}
                className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
              />

              <div className="relative">
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                autoComplete="current-password"
                className="w-full rounded-xl border border-[#d8d1c5] bg-white px-4 py-3 pr-12 text-sm outline-none transition focus:border-[#c2523b] focus:ring-2 focus:ring-[#c2523b]/15"
                placeholder="Enter your password"
              />
              <button type="button" onClick={() => setShowPassword((visible) => !visible)} className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-slate-400 hover:text-[#c2523b]" aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button>
              </div>
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-[#18251f] px-4 py-3.5 text-sm font-bold text-white transition hover:bg-[#2b4035] disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isLoading ? 'Signing in...' : <>Enter Evently <ArrowRight size={17} /></>}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-500">
          New to Evently?{' '}
          <Link
            to="/register"
            className="font-semibold text-[#c2523b] hover:text-[#a54230]"
          >
            Create an account
          </Link>
        </p>
            <p className="mt-8 text-center text-xs text-slate-400">Your account is protected with secure sign-in.</p>
          </div>
        </section>
      </div>
    </main>
  )
}

export default LoginPage