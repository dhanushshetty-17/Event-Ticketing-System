import { useState, type FormEvent } from 'react'
import axios from 'axios'
import { ArrowRight, Check, Eye, EyeOff, Link as LinkIcon, Mail, Sparkles, Ticket } from 'lucide-react'
import { Link, Navigate, useNavigate } from 'react-router-dom'

import { register } from '../api/authApi'
import LoadingSpinner from '../components/LoadingSpinner'
import { useAuth } from '../context/useAuth'

function RegisterPage() {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  if (isAuthenticated) return <Navigate to="/events" replace />

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    const normalizedEmail = email.trim()
    if (!normalizedEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) {
      setError('Enter a valid email address.')
      return
    }
    if (password.length < 8 || password.length > 72) {
      setError('Password must be between 8 and 72 characters.')
      return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    setIsLoading(true)
    try {
      await register({ email: normalizedEmail, password })
      navigate('/login', { replace: true, state: { message: 'Account created. You can now sign in.' } })
    } catch (registrationError: unknown) {
      if (axios.isAxiosError(registrationError) && registrationError.response?.status === 409) {
        setError('That email is already registered. Try signing in instead.')
      } else {
        setError('We could not create your account. Please try again.')
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="min-h-screen bg-[#18251f] px-4 py-6 text-[#18251f] sm:px-8">
      <div className="mx-auto grid min-h-[calc(100vh-3rem)] max-w-6xl overflow-hidden rounded-[2rem] bg-[#f7f4ee] shadow-2xl lg:grid-cols-[0.95fr_1.05fr]">
        <section className="flex items-center justify-center p-6 sm:p-12">
          <div className="w-full max-w-md">
            <div className="mb-8 flex items-center gap-3 text-lg font-bold">
              <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#c2523b] text-white"><Ticket size={20} /></span>
              <span className="font-serif text-2xl">Evently</span>
            </div>
            <div className="mb-8">
              <p className="text-sm font-bold uppercase tracking-[0.2em] text-[#c2523b]">Make room for more</p>
              <h1 className="mt-3 font-serif text-4xl text-[#18251f]">Your next plan<br />starts here.</h1>
              <p className="mt-3 text-sm leading-6 text-slate-500">Create a free account and keep every event, seat hold and ticket close at hand.</p>
            </div>
            {error && <div role="alert" className="mb-5 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label htmlFor="register-email" className="mb-2 block text-sm font-medium text-slate-700">Email</label>
                <div className="relative">
                  <Mail size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" aria-hidden="true" />
                  <input id="register-email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required autoComplete="email" className="w-full rounded-xl border border-[#d8d1c5] bg-white py-3 pl-10 pr-4 text-sm outline-none transition placeholder:text-slate-400 focus:border-[#c2523b] focus:ring-2 focus:ring-[#c2523b]/15" placeholder="you@example.com" />
                </div>
              </div>
              <div>
                <label htmlFor="register-password" className="mb-2 block text-sm font-medium text-slate-700">Password</label>
                <div className="relative">
                  <input id="register-password" type={showPassword ? 'text' : 'password'} value={password} onChange={(event) => setPassword(event.target.value)} required minLength={8} maxLength={72} autoComplete="new-password" className="w-full rounded-xl border border-[#d8d1c5] bg-white px-4 py-3 pr-12 text-sm outline-none transition placeholder:text-slate-400 focus:border-[#c2523b] focus:ring-2 focus:ring-[#c2523b]/15" placeholder="At least 8 characters" />
                  <button type="button" onClick={() => setShowPassword((visible) => !visible)} className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-slate-400 hover:text-[#c2523b]" aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button>
                </div>
                <div className="mt-2 flex gap-3 text-xs text-slate-500"><span className={password.length >= 8 ? 'text-[#2d8a61]' : ''}><Check size={13} className="mr-1 inline" />8+ characters</span><span className={password.length > 0 && password.length <= 72 ? 'text-[#2d8a61]' : ''}><Check size={13} className="mr-1 inline" />Up to 72</span></div>
              </div>
              <div>
                <label htmlFor="confirm-password" className="mb-2 block text-sm font-medium text-slate-700">Confirm password</label>
                <div className="relative">
                  <input id="confirm-password" type={showConfirmPassword ? 'text' : 'password'} value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} required autoComplete="new-password" className="w-full rounded-xl border border-[#d8d1c5] bg-white px-4 py-3 pr-12 text-sm outline-none transition placeholder:text-slate-400 focus:border-[#c2523b] focus:ring-2 focus:ring-[#c2523b]/15" />
                  <button type="button" onClick={() => setShowConfirmPassword((visible) => !visible)} className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-slate-400 hover:text-[#c2523b]" aria-label={showConfirmPassword ? 'Hide password confirmation' : 'Show password confirmation'}>{showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button>
                </div>
              </div>
              <button type="submit" disabled={isLoading} className="flex min-h-11 w-full items-center justify-center gap-2 rounded-xl bg-[#18251f] px-4 py-3.5 text-sm font-bold text-white transition hover:bg-[#2b4035] disabled:cursor-not-allowed disabled:opacity-60">
                {isLoading ? <LoadingSpinner label="Creating account" size="sm" /> : <>Create my Evently account <ArrowRight size={17} /></>}
              </button>
            </form>
            <p className="mt-6 text-center text-sm text-slate-500">Already have an account? <Link to="/login" className="font-semibold text-[#c2523b] hover:text-[#a54230]">Sign in</Link></p>
            <p className="mt-6 flex items-center justify-center gap-1 text-xs text-slate-400"><LinkIcon size={12} /> Free to join. No booking commitment.</p>
          </div>
        </section>
        <aside className="relative hidden overflow-hidden bg-[#e7b85b] p-10 lg:flex lg:flex-col lg:justify-between">
          <div className="absolute -bottom-24 -right-20 h-80 w-80 rounded-full border-[42px] border-[#18251f]/10" />
          <div className="relative">
            <p className="flex items-center gap-2 text-xs font-bold uppercase tracking-[0.25em] text-[#18251f]/65"><Sparkles size={15} /> Built for spontaneous plans</p>
            <h2 className="mt-6 max-w-md font-serif text-6xl leading-[0.95] text-[#18251f]">Less scrolling.<br /><span className="text-[#c2523b]">More living.</span></h2>
            <p className="mt-6 max-w-sm leading-7 text-[#18251f]/70">A calmer way to discover what is happening around you, choose your view and show up ready.</p>
          </div>
          <div className="relative grid grid-cols-2 gap-3 text-sm"><div className="rounded-2xl bg-white/35 p-4"><p className="font-serif text-3xl">01</p><p className="mt-2 text-[#18251f]/70">Find your next thing</p></div><div className="rounded-2xl bg-white/35 p-4"><p className="font-serif text-3xl">02</p><p className="mt-2 text-[#18251f]/70">Keep the good seat</p></div></div>
        </aside>
      </div>
    </main>
  )
}

export default RegisterPage
