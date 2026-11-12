
import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, CalendarDays, Check, MapPin, RefreshCw, ShieldCheck } from 'lucide-react'

import { getEvent } from '../api/eventApi'
import { getEventSeats, type EventSeat } from '../api/seatApi'
import { reserveSeat } from '../api/reservationApi'
import SeatMap from '../components/SeatMap'

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
        `Seat reserved. Booking ID: ${booking.bookingId}. Reservation expires at ${new Date(booking.expiresAt ?? '').toLocaleString()}.`,
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
      <main className="flex min-h-screen items-center justify-center bg-[#f7f4ee]">
        <p className="text-slate-600">Loading event and seats...</p>
      </main>
    )
  }

  if (eventQuery.isError || seatsQuery.isError || !eventQuery.data) {
    return (
      <main className="min-h-screen bg-[#f7f4ee] p-8">
        <p className="text-red-700">
          Unable to load event details or seat availability. Please refresh and try again.
        </p>
        <button
          type="button"
          onClick={() => navigate('/events')}
          className="mt-4 rounded-lg bg-[#18251f] px-4 py-2 text-white"
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
    <main className="min-h-screen bg-[#f7f4ee] px-4 py-8 text-[#18251f] sm:px-6">
      <div className="mx-auto max-w-5xl">
        <button
          type="button"
          onClick={() => navigate('/events')}
          className="mb-6 flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-[#c2523b]"
        >
          <ArrowLeft size={17} />
          Back to events
        </button>

        <section className="relative overflow-hidden rounded-[2rem] bg-[#18251f] p-7 text-[#f7f4ee] shadow-xl md:p-10">
          <div className="absolute -right-10 -top-20 h-60 w-60 rounded-full border-[34px] border-[#e7b85b]/20" />
          <p className="relative text-sm font-bold uppercase tracking-[0.25em] text-[#e7b85b]">
            You found a good one
          </p>
          <h1 className="relative mt-3 max-w-3xl font-serif text-4xl sm:text-5xl">
            {event.name}
          </h1>
          <p className="relative mt-4 max-w-2xl leading-7 text-[#d3ddd6]">{event.description}</p>

          <div className="relative mt-7 flex flex-wrap items-center gap-5 text-sm text-[#d3ddd6]">
            <span className="flex items-center gap-2">
              <MapPin size={17} />
              {event.venueName}
            </span>
            <span className="flex items-center gap-2"><CalendarDays size={17} /> {new Date(event.startsAt).toLocaleString()}</span>
          </div>
        </section>

        <section className="mt-6 rounded-[2rem] border border-[#ded8cd] bg-white p-6 shadow-sm md:p-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-sm font-bold uppercase tracking-[0.2em] text-[#c2523b]">Your view of the night</p>
              <h2 className="mt-2 font-serif text-3xl text-[#18251f]">
                Pick your seat
              </h2>
              <p className="mt-2 text-sm text-slate-500">
                {availableCount} of {seats.length} seats are available. Blue means your current pick.
              </p>
            </div>

            <button
              type="button"
              onClick={() =>
                queryClient.invalidateQueries({
                  queryKey: ['event-seats', eventId],
                })
              }
              className="flex items-center gap-2 rounded-lg border border-[#cfc7b8] px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-[#f7f4ee]"
            >
              <RefreshCw size={15} /> Refresh
            </button>
          </div>

          <SeatMap
            seats={seats}
            selectedSeatId={selectedSeatId}
            disabled={reservationMutation.isPending}
            onSelect={(seat) => {
              setSelectedSeatId(seat.seatId)
              setSuccessMessage('')
            }}
          />

          {selectedSeat && (
            <div className="mt-6 rounded-2xl bg-[#e8f0e8] p-5">
              <p className="flex items-center gap-2 text-sm font-semibold text-[#18251f]">
                <Check size={17} className="text-[#2d8a61]" />
                Selected seat: <strong>{selectedSeat.seatLabel}</strong>
              </p>
              <button
                type="button"
                disabled={reservationMutation.isPending}
                onClick={() => reservationMutation.mutate(selectedSeat)}
                className="mt-4 w-full rounded-xl bg-[#c2523b] px-4 py-3 text-sm font-bold text-white hover:bg-[#a54230] disabled:cursor-not-allowed disabled:opacity-60"
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
              className="mt-4 flex items-start gap-2 break-words rounded-lg bg-[#f7f4ee] p-3 text-sm text-slate-700"
            >
              <ShieldCheck size={16} className="mt-0.5 shrink-0 text-[#2d8a61]" />
              {successMessage}
            </p>
          )}
        </section>
      </div>
    </main>
  )
}

export default EventDetailsPage
