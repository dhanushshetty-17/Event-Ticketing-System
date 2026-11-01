import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CalendarDays, CheckCircle2, CircleDollarSign, Plus, Users, X } from 'lucide-react'

import { cancelEvent, createEvent, getOrganizerEventSummaries, getOrganizerEvents, publishEvent } from '../api/eventApi'
import { getVenues } from '../api/venueApi'
import LoadingSpinner from '../components/LoadingSpinner'

interface EventForm {
  name: string
  description: string
  venueId: string
  startsAt: string
  endsAt: string
}

const initialForm: EventForm = { name: '', description: '', venueId: '', startsAt: '', endsAt: '' }

function OrganizerDashboardPage() {
  const queryClient = useQueryClient()
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [form, setForm] = useState<EventForm>(initialForm)
  const [formError, setFormError] = useState('')
  const [feedback, setFeedback] = useState('')
  const eventsQuery = useQuery({ queryKey: ['organizer-events'], queryFn: getOrganizerEvents })
  const summariesQuery = useQuery({ queryKey: ['organizer-event-summaries'], queryFn: getOrganizerEventSummaries })
  const venuesQuery = useQuery({ queryKey: ['venues'], queryFn: getVenues })
  const events = eventsQuery.data ?? []
  const summaries = summariesQuery.data ?? []
  const totalTicketsSold = summaries.reduce((sum, summary) => sum + summary.soldCount, 0)
  const totalSeats = summaries.reduce((sum, summary) => sum + summary.totalSeats, 0)
  const averageOccupancy = totalSeats ? Math.round(totalTicketsSold / totalSeats * 100) : 0
  const grossRevenue = summaries.some((summary) => summary.grossRevenue !== null)
    ? summaries.reduce((sum, summary) => sum + (summary.grossRevenue ?? 0), 0).toFixed(2)
    : 'Not provided'

  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['organizer-events'] })
    await queryClient.invalidateQueries({ queryKey: ['organizer-event-summaries'] })
  }
  const createMutation = useMutation({
    mutationFn: createEvent,
    onSuccess: async () => { await refresh(); setForm(initialForm); setIsCreateOpen(false); setFeedback('Event created as a draft.') },
  })
  const statusMutation = useMutation({
    mutationFn: ({ eventId, status }: { eventId: string; status: 'publish' | 'cancel' }) => status === 'publish' ? publishEvent(eventId) : cancelEvent(eventId),
    onSuccess: async (_, variables) => { await refresh(); setFeedback(`Event ${variables.status === 'publish' ? 'published' : 'cancelled'} successfully.`) },
  })
  const submitEvent = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const startsAt = new Date(form.startsAt)
    const endsAt = new Date(form.endsAt)
    if (!form.name.trim() || !form.venueId || !form.startsAt || !form.endsAt) return setFormError('Complete all required event fields.')
    if (startsAt <= new Date()) return setFormError('The start date must be in the future.')
    if (endsAt <= startsAt) return setFormError('The end date must be after the start date.')
    createMutation.mutate({ name: form.name.trim(), description: form.description.trim(), venueId: form.venueId, startsAt: startsAt.toISOString(), endsAt: endsAt.toISOString() })
  }

  if (eventsQuery.isLoading || summariesQuery.isLoading || venuesQuery.isLoading) return <main className="flex min-h-screen items-center justify-center bg-slate-950"><LoadingSpinner label="Loading organizer dashboard" /></main>
  if (eventsQuery.isError || summariesQuery.isError || venuesQuery.isError) return <main className="min-h-screen bg-slate-950 px-4 py-16 text-center text-red-200">Unable to load organizer data.</main>

  return (
    <main className="min-h-screen bg-slate-950 px-4 py-10 text-white sm:px-6">
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex flex-wrap items-end justify-between gap-4"><div><p className="text-sm font-semibold uppercase tracking-[0.25em] text-indigo-300">Organizer workspace</p><h1 className="mt-2 text-4xl font-bold">Dashboard</h1><p className="mt-3 text-slate-400">Manage events and monitor verified ticket inventory metrics.</p></div><button type="button" onClick={() => { setFormError(''); setIsCreateOpen(true) }} className="flex items-center gap-2 rounded-lg bg-indigo-500 px-4 py-3 text-sm font-semibold hover:bg-indigo-400"><Plus size={18} /> Create event</button></div>
        {feedback && <div role="status" className="mb-6 rounded-xl border border-emerald-400/30 bg-emerald-400/10 px-4 py-3 text-sm text-emerald-200">{feedback}</div>}
        <section className="mb-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">{[
          { label: 'Total Events', value: events.length.toString(), Icon: CalendarDays },
          { label: 'Total Tickets Sold', value: totalTicketsSold.toString(), Icon: Users },
          { label: 'Gross Revenue', value: grossRevenue === 'Not provided' ? grossRevenue : `$${grossRevenue}`, Icon: CircleDollarSign },
          { label: 'Average Occupancy Rate', value: `${averageOccupancy}%`, Icon: CheckCircle2 },
        ].map(({ label, value, Icon }) => <div key={label} className="rounded-2xl border border-slate-800 bg-slate-900 p-5"><Icon size={20} className="text-indigo-300" /><p className="mt-4 text-sm text-slate-400">{label}</p><p className="mt-1 text-2xl font-bold">{value}</p></div>)}</section>
        <p className="mb-6 rounded-xl border border-slate-800 bg-slate-900/60 p-4 text-sm text-slate-400">Pricing and attendee-roster endpoints are not present in the backend contract, so revenue and attendee details are not displayed as invented values.</p>
        <section><h2 className="mb-4 text-xl font-semibold">Event management</h2>{!events.length ? <div className="rounded-2xl border border-dashed border-slate-700 p-10 text-center text-slate-400">No events yet.</div> : <div className="overflow-x-auto rounded-2xl border border-slate-800"><table className="w-full min-w-[900px] text-left text-sm"><thead className="bg-slate-900 text-slate-400"><tr>{['Event', 'Status', 'Schedule', 'Capacity', 'Sold', 'Occupancy', 'Sections', 'Actions'].map((heading) => <th key={heading} className="px-4 py-4 font-medium">{heading}</th>)}</tr></thead><tbody className="divide-y divide-slate-800">{events.map((event) => { const summary = summaries.find((item) => item.eventId === event.id); const occupancy = summary?.totalSeats ? Math.round(summary.soldCount / summary.totalSeats * 100) : 0; const sections = summary?.sections.map((section) => `${section.section}: ${section.soldCount + section.reservedCount}/${section.totalSeats}`).join(' · ') ?? 'Loading…'; return <tr key={event.id} className="bg-slate-950/60"><td className="px-4 py-4"><p className="font-semibold">{event.name}</p><p className="text-xs text-slate-500">{event.venueName}</p></td><td className="px-4 py-4"><span className={`rounded-full px-2 py-1 text-xs font-semibold ${event.status === 'PUBLISHED' ? 'bg-emerald-400/15 text-emerald-300' : event.status === 'CANCELLED' ? 'bg-red-400/15 text-red-300' : 'bg-amber-400/15 text-amber-300'}`}>{event.status}</span></td><td className="px-4 py-4 text-slate-400">{new Date(event.startsAt).toLocaleString()}</td><td className="px-4 py-4">{summary?.totalSeats ?? 'Loading…'}</td><td className="px-4 py-4 text-indigo-300">{summary?.soldCount ?? 'Loading…'}</td><td className="px-4 py-4">{occupancy}%</td><td className="px-4 py-4 text-xs text-slate-400">{sections}</td><td className="px-4 py-4">{event.status === 'DRAFT' && <button type="button" disabled={statusMutation.isPending} onClick={() => statusMutation.mutate({ eventId: event.id, status: 'publish' })} className="mr-2 rounded-lg bg-emerald-500 px-3 py-2 text-xs font-semibold text-slate-950 disabled:opacity-50">Publish</button>}{event.status !== 'CANCELLED' && <button type="button" disabled={statusMutation.isPending} onClick={() => statusMutation.mutate({ eventId: event.id, status: 'cancel' })} className="rounded-lg border border-red-400/40 px-3 py-2 text-xs font-semibold text-red-300 disabled:opacity-50">Cancel</button>}</td></tr> })}</tbody></table></div>}</section>
      </div>
      {isCreateOpen && <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 px-4 py-8"><section role="dialog" aria-modal="true" aria-labelledby="create-event-title" className="max-h-full w-full max-w-2xl overflow-y-auto rounded-2xl border border-slate-700 bg-slate-900 p-6 shadow-2xl"><div className="flex items-center justify-between"><h2 id="create-event-title" className="text-2xl font-bold">Create event</h2><button type="button" aria-label="Close dialog" onClick={() => setIsCreateOpen(false)}><X size={20} /></button></div><p className="mt-2 text-sm text-slate-400">Venue seats determine capacity. Ticket pricing is not supported by the current backend.</p>{formError && <p role="alert" className="mt-4 rounded-lg bg-red-400/10 p-3 text-sm text-red-200">{formError}</p>}<form onSubmit={submitEvent} className="mt-6 space-y-4"><label className="block text-sm text-slate-300">Event name<input value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} required maxLength={200} className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-white" /></label><label className="block text-sm text-slate-300">Description<textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} maxLength={2000} rows={3} className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-white" /></label><label className="block text-sm text-slate-300">Venue<select value={form.venueId} onChange={(event) => setForm({ ...form, venueId: event.target.value })} required className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-white"><option value="">Select a venue</option>{venuesQuery.data?.map((venue) => <option key={venue.id} value={venue.id}>{venue.name} — {venue.address}</option>)}</select></label><div className="grid gap-4 sm:grid-cols-2"><label className="block text-sm text-slate-300">Starts at<input type="datetime-local" value={form.startsAt} onChange={(event) => setForm({ ...form, startsAt: event.target.value })} required className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-white" /></label><label className="block text-sm text-slate-300">Ends at<input type="datetime-local" value={form.endsAt} onChange={(event) => setForm({ ...form, endsAt: event.target.value })} required className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-3 text-white" /></label></div><div className="flex justify-end gap-3"><button type="button" onClick={() => setIsCreateOpen(false)} className="rounded-lg border border-slate-700 px-4 py-2">Cancel</button><button type="submit" disabled={createMutation.isPending} className="rounded-lg bg-indigo-500 px-4 py-2 font-semibold disabled:opacity-60">{createMutation.isPending ? 'Creating…' : 'Create draft'}</button></div></form></section></div>}
    </main>
  )
}

export default OrganizerDashboardPage
