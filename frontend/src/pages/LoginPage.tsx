import { useState, type FormEvent } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { LockKeyhole, Mail } from 'lucide-react'

import { useAuth } from '../context/AuthContext'

function LoginPage() {
  const { isAuthenticated, loginUser } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('user@example.com')
  const [password, setPassword] = useState('password123')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to="/events" replace />
  }

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    setError('')
    setIsLoading(true)

    try {
      await loginUser(email, password)
      navigate('/events', { replace: true })
    } catch {
      setError('Invalid email or password.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-slate-950 px-4 py-8">
      <section className="w-full max-w-md rounded-2xl bg-white p-8 shadow-2xl">
        <div className="mb-8">
          <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 text-white">
            <LockKeyhole size={24} />
          </div>

          <h1 className="text-3xl font-bold text-slate-900">
            Welcome back
          </h1>

          <p className="mt-2 text-sm text-slate-500">
            Sign in to manage your event bookings.
          </p>
        </div>

        {error && (
          <div
            role="alert"
            className="mb-5 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
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
                className="w-full rounded-lg border border-slate-300 py-3 pl-10 pr-4 text-sm outline-none transition focus:border-slate-900 focus:ring-2 focus:ring-slate-200"
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

              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                required
                autoComplete="current-password"
                className="w-full rounded-lg border border-slate-300 py-3 pl-10 pr-4 text-sm outline-none transition focus:border-slate-900 focus:ring-2 focus:ring-slate-200"
                placeholder="Enter your password"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full rounded-lg bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isLoading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="mt-6 rounded-lg bg-slate-50 p-4 text-xs text-slate-500">
          <p className="font-medium text-slate-700">
            Development account
          </p>
          <p className="mt-1">user@example.com</p>
          <p>password123</p>
        </div>
      </section>
    </main>
  )
}

export default LoginPage