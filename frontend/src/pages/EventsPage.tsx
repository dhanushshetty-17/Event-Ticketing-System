import { useMemo, useState } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { Search, SlidersHorizontal, X } from 'lucide-react'

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
    <main className="min-h-screen bg-slate-950 px-4 py-10 text-white sm:px-6">
      <div className="mx-auto max-w-7xl">
        <div className="mb-8">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-indigo-300">Events</p>
          <h1 className="mt-2 text-4xl font-bold">Discover upcoming events</h1>
          <p className="mt-3 max-w-2xl text-slate-400">Browse published events and choose your seats.</p>
        </div>
        <section className="mb-8 rounded-2xl border border-slate-800 bg-slate-900 p-4">
          <div className="flex flex-col gap-3 lg:flex-row">
            <label className="relative flex-1">
              <span className="sr-only">Search events</span>
              <Search size={18} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by event title or keyword" className="w-full rounded-lg border border-slate-700 bg-slate-950 py-3 pl-10 pr-4 text-sm text-white outline-none focus:border-indigo-400" />
            </label>
            <select value={venue} onChange={(event) => setVenue(event.target.value)} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-sm text-slate-200 outline-none focus:border-indigo-400"><option>All venues</option>{venues.slice(1).map((item) => <option key={item}>{item}</option>)}</select>
            <select value={sort} onChange={(event) => setSort(event.target.value as SortOption)} className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-sm text-slate-200 outline-none focus:border-indigo-400"><option value="date">Sort by date</option><option value="availability">Sort by availability</option></select>
          </div>
          <div className="mt-4 flex flex-wrap items-center gap-2">
            <span className="mr-1 flex items-center gap-2 text-xs text-slate-500"><SlidersHorizontal size={14} /> Filters</span>
            <button type="button" onClick={() => setCategory('All categories')} className={`rounded-full px-3 py-1.5 text-xs font-medium ${category === 'All categories' ? 'bg-indigo-500 text-white' : 'bg-slate-800 text-slate-300'}`}>All categories</button>
            <span className="rounded-full border border-slate-800 px-3 py-1.5 text-xs text-slate-500">Categories unavailable from API</span>
            {hasFilters && <button type="button" onClick={resetFilters} className="ml-auto flex items-center gap-1 text-xs text-indigo-300 hover:text-indigo-200"><X size={14} /> Reset filters</button>}
          </div>
        </section>
        {filteredEvents.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-700 bg-slate-900/60 p-12 text-center"><h2 className="text-xl font-semibold">No events match your filters</h2><p className="mt-2 text-sm text-slate-400">Try a different search or clear the active filters.</p><button type="button" onClick={resetFilters} className="mt-5 rounded-lg bg-indigo-500 px-4 py-2 text-sm font-semibold hover:bg-indigo-400">Clear filters</button></div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">{filteredEvents.map((event) => { const counts = availability.get(event.id); return <EventCard key={event.id} event={event} availableSeats={counts?.available} totalSeats={counts?.total} /> })}</div>
        )}
      </div>
    </main>
  )
}

export default EventsPage
