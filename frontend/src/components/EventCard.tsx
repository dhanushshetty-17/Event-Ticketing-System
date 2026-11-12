import { CalendarDays, ArrowUpRight, MapPin, Ticket } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

import type { Event } from '../types/event'

interface EventCardProps {
  event: Event
  availableSeats?: number
  totalSeats?: number
  featured?: boolean
}

function EventCard({
  event,
  availableSeats,
  totalSeats,
  featured = false,
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
    <article className={`group overflow-hidden rounded-[1.5rem] border border-[#ded8cd] bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-xl ${featured ? 'md:col-span-2 lg:col-span-1' : ''}`}>
      <div className={`relative flex h-44 items-end overflow-hidden p-5 ${featured ? 'bg-[#c2523b]' : 'bg-[#d6a84f]'}`}>
        <div className="absolute -right-7 -top-10 h-40 w-40 rounded-full border-[24px] border-white/15" />
        <Ticket size={62} className="absolute right-7 top-8 rotate-12 text-white/20 transition group-hover:rotate-0 group-hover:scale-110" aria-hidden="true" />
        <div className="relative"><p className="text-xs font-bold uppercase tracking-[0.2em] text-white/70">{featured ? 'Editor pick' : 'On the calendar'}</p><p className="mt-1 font-serif text-2xl text-white">{new Date(event.startsAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}</p></div>
        <span className={`absolute right-4 top-4 rounded-full px-3 py-1 text-xs font-semibold ${badge.style}`}>
          {badge.label}
        </span>
      </div>
      <div className="p-6">
        <h2 className="line-clamp-1 font-serif text-2xl text-[#18251f]">{event.name}</h2>
        <p className="mt-2 line-clamp-2 min-h-12 text-sm leading-6 text-slate-500">{event.description}</p>
        <div className="mt-5 space-y-3 text-sm text-slate-500">
          <div className="flex items-center gap-2"><MapPin size={16} /> <span>{event.venueName}</span></div>
          <div className="flex items-center gap-2"><CalendarDays size={16} /> <span>{new Date(event.startsAt).toLocaleString()}</span></div>
        </div>
        <div className="mt-5 flex items-center justify-between border-t border-[#eee9e0] pt-4 text-xs text-slate-500">
          <span className="font-semibold text-[#c2523b]">{availableSeats !== undefined ? `${availableSeats} seats left` : 'Check availability'}</span>
          <span>Choose your view</span>
        </div>
        <button
          type="button"
          disabled={soldOut}
          onClick={() => navigate(`/events/${event.id}`)}
          className="mt-5 flex w-full items-center justify-between rounded-xl bg-[#18251f] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#2b4035] disabled:cursor-not-allowed disabled:bg-slate-300 disabled:text-slate-500"
        >
          {soldOut ? 'Sold out' : 'See the experience'} {!soldOut && <ArrowUpRight size={17} />}
        </button>
      </div>
    </article>
  )
}

export default EventCard