
import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Armchair, MapPin } from 'lucide-react'

import { getEvent } from '../api/eventApi'
import { getEventSeats, type EventSeat } from '../api/seatApi'
import { reserveSeat } from '../api/reservationApi'

function EventDetailsPage() {
  const { eventId } = useParams<{ eventId: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [selectedSeatId, setSelectedSeatId] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState('')

  const eventQuery = useQuery({
    queryKey: ['event', eventId],
    queryFn: () => getEvent(eventId!),
    enabled: Boolean(eventId),
  })

  const seatsQuery = useQuery({
    queryKey: ['event-seats', eventId],
    queryFn: () => getEventSeats(eventId!),
    enabled: Boolean(eventId),
    refetchInterval: 5000,
  })

  const reservationMutation = useMutation({
    mutationFn: (seat: EventSeat) =>
      reserveSeat({
        eventId: eventId!,
        seatId: seat.seatId,
      }),
    onSuccess: async (booking) => {
      setSelectedSeatId(null)
      setSuccessMessage(
        `Seat reserved. Booking ID: ${booking.id}. Reservation expires at ${new Date(booking.expiresAt).toLocaleString()}.`,
      )
      await queryClient.invalidateQueries({
        queryKey: ['event-seats', eventId],
      })
    },
    onError: (error: unknown) => {
      const message =
        error instanceof Error ? error.message : 'Please try again.'
      setSuccessMessage(`Reservation failed: ${message}`)
      queryClient.invalidateQueries({
        queryKey: ['event-seats', eventId],
      })
    },
  })

  if (eventQuery.isLoading || seatsQuery.isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50">
        <p className="text-slate-600">Loading event and seats...</p>
      </main>
    )
  }

  if (eventQuery.isError || seatsQuery.isError || !eventQuery.data) {
    return (
      <main className="min-h-screen bg-slate-50 p-8">
        <p className="text-red-700">
          Unable to load event details or seat availability. Please refresh and try again.
        </p>
        <button
          type="button"
          onClick={() => navigate('/events')}
          className="mt-4 rounded-lg bg-slate-900 px-4 py-2 text-white"
        >
          Back to events
        </button>
      </main>
    )
  }

  const event = eventQuery.data
  const seats = seatsQuery.data ?? []
  const selectedSeat = seats.find(
    (seat) => seat.seatId === selectedSeatId,
  )
  const availableCount = seats.filter(
    (seat) => seat.status === 'AVAILABLE',
  ).length

  return (
    <main className="min-h-screen bg-slate-50 px-6 py-10">
      <div className="mx-auto max-w-5xl">
        <button
          type="button"
          onClick={() => navigate('/events')}
          className="mb-6 flex items-center gap-2 text-sm font-medium text-slate-600 hover:text-slate-900"
        >
          <ArrowLeft size={17} />
          Back to events
        </button>

        <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:p-8">
          <p className="text-sm font-semibold uppercase tracking-wider text-slate-500">
            Event details
          </p>
          <h1 className="mt-2 text-3xl font-bold text-slate-900">
            {event.name}
          </h1>
          <p className="mt-3 text-slate-600">{event.description}</p>

          <div className="mt-5 flex flex-wrap items-center gap-4 text-sm text-slate-500">
            <span className="flex items-center gap-2">
              <MapPin size={17} />
              {event.venueName}
            </span>
            <span>{new Date(event.startsAt).toLocaleString()}</span>
          </div>
        </section>

        <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 md:p-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold text-slate-900">
                Choose your seat
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                {availableCount} available out of {seats.length} seats
              </p>
            </div>

            <button
              type="button"
              onClick={() =>
                queryClient.invalidateQueries({
                  queryKey: ['event-seats', eventId],
                })
              }
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            >
              Refresh seats
            </button>
          </div>

          <div className="mt-6 flex flex-wrap gap-4 text-xs text-slate-600">
            <span className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-emerald-500" />
              Available
            </span>
            <span className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-amber-400" />
              Reserved
            </span>
            <span className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-slate-400" />
              Booked
            </span>
            <span className="flex items-center gap-2">
              <span className="h-3 w-3 rounded bg-blue-600" />
              Selected
            </span>
          </div>

          <div className="my-8 rounded-t-2xl border-t-4 border-slate-300 py-3 text-center text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">
            Stage / Front
          </div>

          {seats.length === 0 ? (
            <p className="py-8 text-center text-slate-500">
              No seats are configured for this event.
            </p>
          ) : (
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4 md:grid-cols-6">
              {seats.map((seat) => {
                const available = seat.status === 'AVAILABLE'
                const selected = selectedSeatId === seat.seatId

                const seatStyle = selected
                  ? 'bg-blue-600 text-white ring-2 ring-blue-300'
                  : available
                    ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-200'
                    : seat.status === 'RESERVED'
                      ? 'bg-amber-100 text-amber-800'
                      : 'bg-slate-200 text-slate-500'

                return (
                  <button
                    key={seat.seatId}
                    type="button"
                    disabled={!available || reservationMutation.isPending}
                    onClick={() => {
                      setSelectedSeatId(seat.seatId)
                      setSuccessMessage('')
                    }}
                    aria-pressed={selected}
                    className={`flex min-h-20 flex-col items-center justify-center gap-1 rounded-xl border border-transparent p-3 text-sm font-semibold transition disabled:cursor-not-allowed ${seatStyle}`}
                    title={`${seat.seatLabel}: ${seat.status}`}
                  >
                    <Armchair size={21} />
                    <span>{seat.seatLabel}</span>
                    <span className="text-[10px] font-medium">
                      {selected ? 'SELECTED' : seat.status}
                    </span>
                  </button>
                )
              })}
            </div>
          )}

          {selectedSeat && (
            <div className="mt-6 rounded-xl bg-blue-50 p-4">
              <p className="text-sm text-blue-900">
                Selected seat: <strong>{selectedSeat.seatLabel}</strong>
              </p>
              <button
                type="button"
                disabled={reservationMutation.isPending}
                onClick={() => reservationMutation.mutate(selectedSeat)}
                className="mt-3 w-full rounded-lg bg-blue-600 px-4 py-3 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {reservationMutation.isPending
                  ? 'Reserving seat...'
                  : 'Reserve seat for 10 minutes'}
              </button>
            </div>
          )}

          {successMessage && (
            <p
              role="status"
              className="mt-4 break-words rounded-lg bg-slate-100 p-3 text-sm text-slate-700"
            >
              {successMessage}
            </p>
          )}
        </section>
      </div>
    </main>
  )
}

export default EventDetailsPage
