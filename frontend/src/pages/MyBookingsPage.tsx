import { useEffect, useState } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { CalendarDays, Clock3, Ticket } from 'lucide-react'

import { getBookings } from '../api/bookingApi'
import { getEvent } from '../api/eventApi'
import LoadingSpinner from '../components/LoadingSpinner'
import type { Booking } from '../types/booking'

function BookingRow({ booking, eventName, now }: { booking: Booking; eventName: string; now: number }) {
  const remaining = booking.expiresAt ? new Date(booking.expiresAt).getTime() - now : 0
  const pending = booking.status === 'PENDING' && remaining > 0
  return (
    <article className="flex flex-col gap-4 rounded-2xl border border-slate-800 bg-slate-900 p-5 sm:flex-row sm:items-center sm:justify-between">
      <div><p className="font-semibold text-white">{eventName}</p><p className="mt-1 text-sm text-slate-400">Seat {booking.seatId}</p><p className="mt-2 flex items-center gap-2 text-xs text-slate-500"><CalendarDays size={14} /> {new Date(booking.createdAt).toLocaleString()}</p>{pending && <p className="mt-2 flex items-center gap-2 text-sm font-semibold text-amber-300"><Clock3 size={14} /> Hold remaining: {Math.floor(remaining / 60000)}:{String(Math.floor(remaining / 1000) % 60).padStart(2, '0')}</p>}</div>
      {pending && <Link to={`/bookings/${booking.bookingId}`} className="rounded-lg bg-indigo-500 px-3 py-2 text-center text-sm font-semibold hover:bg-indigo-400">Complete Checkout</Link>}
      {booking.status === 'CONFIRMED' && <Link to={`/tickets/${booking.bookingId}`} className="flex items-center justify-center gap-2 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-semibold text-slate-950 hover:bg-emerald-400"><Ticket size={15} /> View Ticket</Link>}
    </article>
  )
}

function BookingGroup({ title, bookings, eventNames, now }: { title: string; bookings: Booking[]; eventNames: Map<string, string>; now: number }) {
  if (!bookings.length) return null
  return <section><h2 className="mb-4 text-xl font-semibold text-white">{title}</h2><div className="space-y-3">{bookings.map((booking) => <BookingRow key={booking.bookingId} booking={booking} eventName={eventNames.get(booking.eventId) ?? 'Event booking'} now={now} />)}</div></section>
}

function MyBookingsPage() {
  const bookingsQuery = useQuery({ queryKey: ['bookings'], queryFn: getBookings })
  const [now, setNow] = useState(() => Date.now())
  const bookings = bookingsQuery.data ?? []
  const eventQueries = useQueries({ queries: [...new Set(bookings.map((booking) => booking.eventId))].map((eventId) => ({ queryKey: ['booking-event', eventId], queryFn: () => getEvent(eventId), enabled: Boolean(eventId) })) })
  useEffect(() => { const timer = window.setInterval(() => setNow(Date.now()), 1000); return () => window.clearInterval(timer) }, [])
  if (bookingsQuery.isLoading) return <main className="flex min-h-screen items-center justify-center bg-slate-950"><LoadingSpinner label="Loading bookings" /></main>
  if (bookingsQuery.isError) return <main className="min-h-screen bg-slate-950 px-4 py-16 text-center text-red-200"><p>Unable to load your bookings.</p></main>
  const eventNames = new Map(eventQueries.map((query) => [query.data?.id ?? '', query.data?.name ?? 'Event booking']))
  const active = bookings.filter((booking) => booking.status === 'PENDING' && booking.expiresAt !== null && new Date(booking.expiresAt).getTime() > now)
  const confirmed = bookings.filter((booking) => booking.status === 'CONFIRMED')
  const past = bookings.filter((booking) => !active.includes(booking) && !confirmed.includes(booking))
  return <main className="min-h-screen bg-slate-950 px-4 py-10 text-white sm:px-6"><div className="mx-auto max-w-5xl"><div className="mb-10"><p className="text-sm font-semibold uppercase tracking-[0.25em] text-indigo-300">Your account</p><h1 className="mt-2 text-4xl font-bold">My bookings</h1><p className="mt-3 text-slate-400">Manage reservations and access your confirmed tickets.</p></div><div className="space-y-10"><BookingGroup title="Active / Pending Reservations" bookings={active} eventNames={eventNames} now={now} /><BookingGroup title="Confirmed Tickets" bookings={confirmed} eventNames={eventNames} now={now} /><BookingGroup title="Past / Cancelled Events" bookings={past} eventNames={eventNames} now={now} />{!bookings.length && <div className="rounded-2xl border border-dashed border-slate-700 p-10 text-center text-slate-400">You have no bookings yet. <Link className="text-indigo-300 hover:text-indigo-200" to="/events">Browse events</Link></div>}</div></div></main>
}

export default MyBookingsPage
