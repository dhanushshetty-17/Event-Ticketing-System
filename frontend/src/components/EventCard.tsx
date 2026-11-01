import { CalendarDays, MapPin, Ticket } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

import type { Event } from '../types/event'

interface EventCardProps {
  event: Event
  availableSeats?: number
  totalSeats?: number
}

function EventCard({
  event,
  availableSeats,
  totalSeats,
}: EventCardProps) {
  const navigate = useNavigate()
  const soldOut = totalSeats !== undefined && totalSeats > 0 && availableSeats === 0
  const sellingFast =
    !soldOut &&
    availableSeats !== undefined &&
    totalSeats !== undefined &&
    totalSeats > 0 &&
    availableSeats / totalSeats <= 0.25
  const badge = soldOut
    ? { label: 'Sold Out', style: 'bg-red-400/15 text-red-300' }
    : sellingFast
      ? { label: 'Selling Fast', style: 'bg-amber-400/15 text-amber-300' }
      : { label: 'Upcoming', style: 'bg-emerald-400/15 text-emerald-300' }

  return (
    <article className="group overflow-hidden rounded-2xl border border-slate-800 bg-slate-900 shadow-lg transition hover:-translate-y-1 hover:border-indigo-400/40">
      <div className="relative flex h-36 items-center justify-center overflow-hidden bg-gradient-to-br from-indigo-500/25 via-slate-900 to-violet-500/20">
        <Ticket size={48} className="text-indigo-300/70 transition group-hover:scale-110" aria-hidden="true" />
        <span className={`absolute right-4 top-4 rounded-full px-3 py-1 text-xs font-semibold ${badge.style}`}>
          {badge.label}
        </span>
      </div>
      <div className="p-6">
        <h2 className="line-clamp-1 text-xl font-bold text-white">{event.name}</h2>
        <p className="mt-2 line-clamp-2 min-h-12 text-sm leading-6 text-slate-400">{event.description}</p>
        <div className="mt-5 space-y-3 text-sm text-slate-400">
          <div className="flex items-center gap-2"><MapPin size={16} /> <span>{event.venueName}</span></div>
          <div className="flex items-center gap-2"><CalendarDays size={16} /> <span>{new Date(event.startsAt).toLocaleString()}</span></div>
        </div>
        <div className="mt-4 flex items-center justify-between text-xs text-slate-500">
          <span>{availableSeats !== undefined ? `${availableSeats} seats available` : 'Check availability'}</span>
          <span>Pricing at seat selection</span>
        </div>
        <button
          type="button"
          disabled={soldOut}
          onClick={() => navigate(`/events/${event.id}`)}
          className="mt-6 w-full rounded-lg bg-indigo-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-indigo-400 disabled:cursor-not-allowed disabled:bg-slate-700 disabled:text-slate-500"
        >
          {soldOut ? 'Sold out' : 'View event'}
        </button>
      </div>
    </article>
  )
}

export default EventCard