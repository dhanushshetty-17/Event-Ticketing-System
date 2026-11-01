import { useEffect, useState } from 'react'
import axios from 'axios'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { AlertTriangle, ArrowLeft, CalendarDays, CheckCircle2, MapPin } from 'lucide-react'

import { confirmBooking, getBooking } from '../api/bookingApi'
import { getEvent } from '../api/eventApi'
import { getEventSeats } from '../api/seatApi'
import LoadingSpinner from '../components/LoadingSpinner'

const formatRemaining = (milliseconds: number) => {
  const seconds = Math.max(0, Math.floor(milliseconds / 1000))
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, '0')}`
}

function BookingPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [now, setNow] = useState(() => Date.now())
  const bookingQuery = useQuery({
    queryKey: ['booking', bookingId],
    queryFn: () => getBooking(bookingId!),
    enabled: Boolean(bookingId),
  })
  const booking = bookingQuery.data
  const eventQuery = useQuery({
    queryKey: ['booking-event', booking?.eventId],
    queryFn: () => getEvent(booking!.eventId),
    enabled: Boolean(booking?.eventId),
  })
  const seatsQuery = useQuery({
    queryKey: ['booking-seats', booking?.eventId],
    queryFn: () => getEventSeats(booking!.eventId),
    enabled: Boolean(booking?.eventId),
  })
  const remaining = booking?.expiresAt
    ? new Date(booking.expiresAt).getTime() - now
    : 0
  const isPending = booking?.status === 'PENDING'
  const isExpired = isPending && Boolean(booking?.expiresAt) && remaining <= 0

  useEffect(() => {
    if (!isPending || !booking?.expiresAt) return
    const timer = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(timer)
  }, [booking?.expiresAt, isPending])

  const confirmMutation = useMutation({
    mutationFn: () => confirmBooking(bookingId!),
    onSuccess: async (confirmed) => {
      await queryClient.setQueryData(['booking', bookingId], confirmed)
      await queryClient.invalidateQueries({ queryKey: ['bookings'] })
      navigate(`/tickets/${bookingId}`, { replace: true })
    },
  })
  const backendExpired = axios.isAxiosError(confirmMutation.error)
    && [409, 410].includes(confirmMutation.error.response?.status ?? 0)
  const showExpired = isExpired || backendExpired

  if (bookingQuery.isLoading) return <main className="flex min-h-screen items-center justify-center bg-slate-950"><LoadingSpinner label="Loading reservation" /></main>
  if (bookingQuery.isError || !booking) return <main className="min-h-screen bg-slate-950 px-4 py-16 text-center text-red-200"><p>Unable to load this reservation.</p><Link className="mt-4 inline-block text-indigo-300" to="/my-bookings">Back to my bookings</Link></main>
  if (eventQuery.isLoading || seatsQuery.isLoading) return <main className="flex min-h-screen items-center justify-center bg-slate-950"><LoadingSpinner label="Loading reservation details" /></main>
  if (eventQuery.isError || !eventQuery.data) return <main className="min-h-screen bg-slate-950 px-4 py-16 text-center text-red-200"><p>Unable to load event details.</p><Link className="mt-4 inline-block text-indigo-300" to="/my-bookings">Back to my bookings</Link></main>

  const event = eventQuery.data
  const seat = seatsQuery.data?.find((item) => item.seatId === booking.seatId)
  return (
    <main className="min-h-screen bg-slate-950 px-4 py-10 text-white sm:px-6">
      <div className="mx-auto max-w-3xl">
        <Link to="/my-bookings" className="mb-6 inline-flex items-center gap-2 text-sm text-slate-400 hover:text-white"><ArrowLeft size={16} /> My bookings</Link>
        <section className="overflow-hidden rounded-3xl border border-slate-800 bg-slate-900 shadow-2xl">
          <div className="border-b border-slate-800 bg-gradient-to-r from-indigo-500/20 to-violet-500/10 p-6 sm:p-8">
            <p className="text-xs font-semibold uppercase tracking-[0.25em] text-indigo-300">Reservation summary</p>
            <h1 className="mt-3 text-3xl font-bold">{event.name}</h1>
            <div className="mt-4 flex flex-wrap gap-4 text-sm text-slate-300"><span className="flex items-center gap-2"><MapPin size={16} /> {event.venueName}</span><span className="flex items-center gap-2"><CalendarDays size={16} /> {new Date(event.startsAt).toLocaleString()}</span></div>
          </div>
          <div className="grid gap-6 p-6 sm:grid-cols-3 sm:p-8">
            <div><p className="text-sm text-slate-400">Seat</p><p className="mt-1 text-2xl font-bold">{seat?.seatLabel ?? booking.seatId}</p><p className="mt-1 text-xs text-slate-500">Seat ID: {booking.seatId}</p></div>
            <div><p className="text-sm text-slate-400">Booking status</p><p className="mt-1 flex items-center gap-2 text-xl font-bold">{booking.status === 'CONFIRMED' && <CheckCircle2 className="text-emerald-400" size={22} />}{booking.status}</p></div>
            <div><p className="text-sm text-slate-400">Price</p><p className="mt-1 text-xl font-bold">Not provided</p><p className="text-xs text-slate-500">Pricing is not part of the backend DTO.</p></div>
          </div>
          {isPending && !showExpired && <div className={`mx-6 mb-6 rounded-2xl border p-5 sm:mx-8 ${remaining < 120000 ? 'border-amber-400/40 bg-amber-400/10' : 'border-indigo-400/30 bg-indigo-400/10'}`}><p className="text-sm text-slate-300">Reservation hold expires in</p><p className="mt-1 text-4xl font-bold tabular-nums">{formatRemaining(remaining)}</p><p className="mt-2 text-sm text-slate-400">Confirm before the timer reaches zero.</p></div>}
          {showExpired && <div role="alert" className="mx-6 mb-6 rounded-2xl border border-red-400/30 bg-red-400/10 p-5 text-red-200 sm:mx-8"><div className="flex items-start gap-3"><AlertTriangle className="shrink-0" size={20} /><div><p className="font-semibold">Expired Reservation</p><p className="mt-1 text-sm">This seat hold is no longer available.</p></div></div><Link to={`/events/${booking.eventId}`} className="mt-4 inline-flex rounded-lg bg-red-400/20 px-4 py-2 text-sm font-semibold hover:bg-red-400/30">Return to seat map</Link></div>}
          {confirmMutation.error && !backendExpired && <p role="alert" className="mx-6 mb-5 rounded-lg bg-red-400/10 p-4 text-sm text-red-200 sm:mx-8">We could not confirm this reservation. Please try again.</p>}
          {isPending && !showExpired && <button type="button" disabled={confirmMutation.isPending} onClick={() => confirmMutation.mutate()} className="m-6 flex w-[calc(100%-3rem)] items-center justify-center rounded-xl bg-indigo-500 px-5 py-3 font-semibold hover:bg-indigo-400 disabled:opacity-60 sm:m-8 sm:mt-0 sm:w-[calc(100%-4rem)]">{confirmMutation.isPending ? <LoadingSpinner label="Confirming booking" size="sm" /> : 'Confirm Booking'}</button>}
          {booking.status === 'CONFIRMED' && <Link to={`/tickets/${booking.bookingId}`} className="m-6 block rounded-xl bg-emerald-500 px-5 py-3 text-center font-semibold text-slate-950 hover:bg-emerald-400 sm:m-8 sm:mt-0">View ticket</Link>}
        </section>
      </div>
    </main>
  )
}

export default BookingPage
