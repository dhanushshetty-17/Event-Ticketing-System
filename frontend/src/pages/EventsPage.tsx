import { useQuery } from '@tanstack/react-query'
import { CalendarDays, MapPin } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { getEvents } from '../api/eventApi'

function EventsPage() {
  const navigate = useNavigate()
  const {
    data: events,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ['events'],
    queryFn: getEvents,
  })


  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50">
        <p className="text-sm text-slate-500">
          Loading events...
        </p>
      </main>
    )
  }

  if (isError) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
        <div className="rounded-xl border border-red-200 bg-red-50 p-6 text-center">
          <h1 className="font-semibold text-red-800">
            Unable to load events
          </h1>

          <p className="mt-2 text-sm text-red-600">
            {error instanceof Error
              ? error.message
              : 'Something went wrong.'}
          </p>
        </div>
      </main>
    )
  }

  const publishedEvents =
    events?.filter((event) => event.status === 'PUBLISHED') ?? []

  return (
    <main className="min-h-screen bg-slate-50 px-6 py-10">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8">
          <p className="text-sm font-semibold uppercase tracking-wider text-slate-500">
            Events
          </p>

          <h1 className="mt-2 text-4xl font-bold text-slate-900">
            Discover upcoming events
          </h1>

          <p className="mt-3 max-w-2xl text-slate-600">
            Browse published events and choose your seats.
          </p>
        </div>

        {publishedEvents.length === 0 ? (
          <div className="rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center">
            <p className="text-slate-500">
              No published events are available.
            </p>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {publishedEvents.map((event) => (
              <article
                key={event.id}
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-lg"
              >
                <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-xl bg-slate-900 text-white">
                  <CalendarDays size={22} />
                </div>

                <h2 className="text-xl font-bold text-slate-900">
                  {event.name}
                </h2>

                <p className="mt-2 line-clamp-3 text-sm leading-6 text-slate-600">
                  {event.description}
                </p>

                <div className="mt-5 space-y-3 text-sm text-slate-500">
                  <div className="flex items-center gap-2">
                    <MapPin size={17} />
                    <span>{event.venueName}</span>
                  </div>

                  <div>
                    {new Date(event.startsAt).toLocaleString()}
                  </div>
                </div>

<button
  type="button"
onClick={() => navigate(`/events/${event.id}`)}
  className="mt-6 w-full rounded-lg bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
>
  View event
</button>

              </article>
            ))}
          </div>
        )}
      </div>
    </main>
  )
}

export default EventsPage
