import { useMemo, useState } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { Search, Sparkles, SlidersHorizontal, X } from 'lucide-react'

import { getEvents } from '../api/eventApi'
import { getEventSeats } from '../api/seatApi'
import EventCard from '../components/EventCard'
import LoadingSpinner from '../components/LoadingSpinner'

type SortOption = 'date' | 'availability'

function EventsPage() {
  const [search, setSearch] = useState('')
  const [venue, setVenue] = useState('All venues')
  const [category, setCategory] = useState('All categories')
  const [sort, setSort] = useState<SortOption>('date')
  const eventsQuery = useQuery({ queryKey: ['events'], queryFn: getEvents })
  const publishedEvents = useMemo(
    () => eventsQuery.data?.filter((event) => event.status === 'PUBLISHED') ?? [],
    [eventsQuery.data],
  )
  const seatQueries = useQueries({
    queries: publishedEvents.map((event) => ({
      queryKey: ['event-seats', event.id],
      queryFn: () => getEventSeats(event.id),
      staleTime: 30_000,
    })),
  })
  const availability = useMemo(
    () => new Map(publishedEvents.map((event, index) => {
      const seats = seatQueries[index]?.data ?? []
      return [event.id, { available: seats.filter((seat) => seat.status === 'AVAILABLE').length, total: seats.length }]
    })),
    [publishedEvents, seatQueries],
  )
  const venues = ['All venues', ...new Set(publishedEvents.map((event) => event.venueName))]
  const filteredEvents = publishedEvents
    .filter((event) => {
      const query = search.trim().toLowerCase()
      return (
        (!query || `${event.name} ${event.description}`.toLowerCase().includes(query)) &&
        (venue === 'All venues' || event.venueName === venue) &&
        category === 'All categories'
      )
    })
    .sort((left, right) => {
      if (sort === 'availability') {
        return (availability.get(right.id)?.available ?? 0) - (availability.get(left.id)?.available ?? 0)
      }
      return new Date(left.startsAt).getTime() - new Date(right.startsAt).getTime()
    })
  const hasFilters = search !== '' || venue !== 'All venues' || category !== 'All categories' || sort !== 'date'
  const resetFilters = () => {
    setSearch('')
    setVenue('All venues')
    setCategory('All categories')
    setSort('date')
  }

  if (eventsQuery.isLoading) return <main className="flex min-h-screen items-center justify-center bg-slate-950"><LoadingSpinner label="Loading events" /></main>
  if (eventsQuery.isError) return <main className="flex min-h-screen items-center justify-center bg-slate-950 px-4 text-center"><div className="rounded-2xl border border-red-400/30 bg-red-400/10 p-6 text-red-200"><h1 className="font-semibold">Unable to load events</h1><p className="mt-2 text-sm">Please refresh and try again.</p></div></main>

  return (
    <main className="min-h-screen bg-[#f7f4ee] px-4 py-8 text-slate-950 sm:px-6">
      <div className="mx-auto max-w-7xl">
        <section className="relative mb-8 overflow-hidden rounded-[2rem] bg-[#18251f] px-6 py-10 text-[#f7f4ee] shadow-xl sm:px-10 lg:py-14">
          <div className="absolute -right-16 -top-20 h-64 w-64 rounded-full border-[34px] border-[#e7b85b]/20" />
          <div className="absolute bottom-[-100px] right-24 h-52 w-52 rounded-full bg-[#dd6b4d]/20 blur-2xl" />
          <div className="relative max-w-2xl">
            <p className="flex items-center gap-2 text-sm font-bold uppercase tracking-[0.25em] text-[#e7b85b]"><Sparkles size={16} /> Your city, your scene</p>
            <h1 className="mt-4 font-serif text-4xl leading-tight sm:text-6xl">Make a night<br /><span className="text-[#e7b85b]">of it.</span></h1>
            <p className="mt-5 max-w-xl text-base leading-7 text-[#d3ddd6]">A hand-picked guide to the rooms, stages and tables worth showing up for. Find your next memorable plan.</p>
            <div className="mt-8 flex flex-wrap gap-3 text-sm font-semibold">
              <span className="rounded-full bg-[#e7b85b] px-4 py-2 text-[#18251f]">Live this month</span>
              <span className="rounded-full border border-white/20 px-4 py-2 text-white/80">Curated locally</span>
            </div>
          </div>
        </section>
        <div className="mb-8 flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
          <div>
            <p className="text-sm font-bold uppercase tracking-[0.25em] text-[#c2523b]">The event edit</p>
            <h2 className="mt-2 font-serif text-4xl text-[#18251f]">What are you in the mood for?</h2>
          </div>
          <p className="max-w-xs text-sm leading-6 text-slate-500">Reserve a seat in seconds. Your hold stays safe for 10 minutes while you decide.</p>
        </div>
        <section className="mb-8 rounded-2xl border border-[#ded8cd] bg-white p-4 shadow-sm">
          <div className="flex flex-col gap-3 lg:flex-row">
            <label className="relative flex-1">
              <span className="sr-only">Search events</span>
              <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by event, venue or vibe" className="w-full rounded-lg border border-[#ded8cd] bg-[#fbfaf7] py-3 pl-10 pr-4 text-sm outline-none focus:border-[#c2523b]" />
            </label>
            <select value={venue} onChange={(event) => setVenue(event.target.value)} className="rounded-lg border border-[#ded8cd] bg-[#fbfaf7] px-3 py-3 text-sm outline-none focus:border-[#c2523b]"><option>All venues</option>{venues.slice(1).map((item) => <option key={item}>{item}</option>)}</select>
            <select value={sort} onChange={(event) => setSort(event.target.value as SortOption)} className="rounded-lg border border-[#ded8cd] bg-[#fbfaf7] px-3 py-3 text-sm outline-none focus:border-[#c2523b]"><option value="date">Soonest first</option><option value="availability">Most seats left</option></select>
          </div>
          <div className="mt-4 flex flex-wrap items-center gap-2">
            <span className="mr-1 flex items-center gap-2 text-xs text-slate-500"><SlidersHorizontal size={14} /> Explore</span>
            <button type="button" onClick={() => setCategory('All categories')} className={`rounded-full px-3 py-1.5 text-xs font-semibold ${category === 'All categories' ? 'bg-[#18251f] text-white' : 'bg-slate-100 text-slate-600'}`}>All experiences</button>
            <span className="rounded-full border border-[#ded8cd] px-3 py-1.5 text-xs text-slate-500">Fresh picks every week</span>
            {hasFilters && <button type="button" onClick={resetFilters} className="ml-auto flex items-center gap-1 text-xs font-semibold text-[#c2523b] hover:text-[#a54230]"><X size={14} /> Reset</button>}
          </div>
        </section>
        {filteredEvents.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-[#cfc7b8] bg-white p-12 text-center"><h2 className="text-xl font-semibold">Nothing on this wavelength</h2><p className="mt-2 text-sm text-slate-500">Try a different search or clear the active filters.</p><button type="button" onClick={resetFilters} className="mt-5 rounded-lg bg-[#c2523b] px-4 py-2 text-sm font-semibold text-white hover:bg-[#a54230]">Clear filters</button></div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">{filteredEvents.map((event, index) => { const counts = availability.get(event.id); return <EventCard key={event.id} event={event} availableSeats={counts?.available} totalSeats={counts?.total} featured={index === 0} /> })}</div>
        )}
        <div className="mt-10 grid gap-4 sm:grid-cols-3">
          {[['01', 'Browse', 'Find a room that feels like you'], ['02', 'Pick a seat', 'See the view before you commit'], ['03', 'Show up', 'Your ticket is waiting in your pocket']].map(([number, title, copy]) => <div key={number} className="rounded-2xl bg-[#e9e3d8] p-5"><span className="text-sm font-bold text-[#c2523b]">{number}</span><h3 className="mt-6 font-serif text-2xl text-[#18251f]">{title}</h3><p className="mt-2 text-sm leading-6 text-slate-600">{copy}</p></div>)}
        </div>
      </div>
    </main>
  )
}

export default EventsPage
