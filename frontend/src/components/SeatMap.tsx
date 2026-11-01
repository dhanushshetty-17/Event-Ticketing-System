import { Armchair } from 'lucide-react'

import type { EventSeat } from '../api/seatApi'

interface SeatMapProps {
  seats: EventSeat[]
  selectedSeatId: string | null
  onSelect: (seat: EventSeat) => void
  disabled?: boolean
}

type SeatVisualState = 'AVAILABLE' | 'RESERVED' | 'SOLD' | 'SELECTED'

function getTier(seatLabel: string): 'VIP' | 'PREMIUM' | 'STANDARD' {
  const row = seatLabel.trim().charAt(0).toUpperCase()
  if (row === 'A' || row === 'B') return 'VIP'
  if (row === 'C' || row === 'D') return 'PREMIUM'
  return 'STANDARD'
}

function getState(seat: EventSeat, selectedSeatId: string | null): SeatVisualState {
  if (seat.seatId === selectedSeatId) return 'SELECTED'
  if (seat.status === 'AVAILABLE') return 'AVAILABLE'
  if (seat.status === 'RESERVED') return 'RESERVED'
  return 'SOLD'
}

function SeatMap({
  seats,
  selectedSeatId,
  onSelect,
  disabled = false,
}: SeatMapProps) {
  const tiers = ['VIP', 'PREMIUM', 'STANDARD'] as const
  return (
    <div>
      <div className="mb-6 flex flex-wrap gap-3 text-xs text-slate-400">
        {[
          ['bg-indigo-500', 'VIP'],
          ['bg-violet-500', 'Premium'],
          ['bg-emerald-500', 'Standard'],
        ].map(([color, label]) => (
          <span key={label} className="flex items-center gap-2">
            <span className={`h-3 w-3 rounded ${color}`} />
            {label}
          </span>
        ))}
        {[
          ['bg-slate-700', 'Reserved'],
          ['bg-slate-500', 'Sold'],
          ['bg-sky-500', 'Selected'],
        ].map(([color, label]) => (
          <span key={label} className="flex items-center gap-2">
            <span className={`h-3 w-3 rounded ${color}`} />
            {label}
          </span>
        ))}
      </div>

      <div className="mb-8 rounded-t-[50%] border-t-4 border-slate-700 py-3 text-center text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">
        Stage / Front
      </div>

      {seats.length === 0 ? (
        <p className="py-8 text-center text-slate-500">
          No seats are configured for this event.
        </p>
      ) : (
        <div className="space-y-6">
          {tiers.map((tier) => {
            const tierSeats = seats.filter((seat) => getTier(seat.seatLabel) === tier)
            if (!tierSeats.length) return null
            return <section key={tier} aria-labelledby={`seat-tier-${tier}`}><div className="mb-3 flex items-center justify-between"><h3 id={`seat-tier-${tier}`} className="text-sm font-semibold uppercase tracking-wider text-slate-300">{tier}</h3><span className="text-xs text-slate-500">{tierSeats.filter((seat) => seat.status === 'AVAILABLE').length} available</span></div><div className="grid grid-cols-2 gap-3 sm:grid-cols-4 md:grid-cols-6">
          {tierSeats.map((seat) => {
            const tier = getTier(seat.seatLabel)
            const state = getState(seat, selectedSeatId)
            const available = state === 'AVAILABLE'
            const color =
              state === 'SELECTED'
                ? 'bg-sky-500 text-white ring-2 ring-sky-300'
                : state === 'AVAILABLE' && tier === 'VIP'
                  ? 'bg-indigo-500/80 text-white hover:bg-indigo-400'
                  : state === 'AVAILABLE' && tier === 'PREMIUM'
                    ? 'bg-violet-500/80 text-white hover:bg-violet-400'
                    : state === 'AVAILABLE'
                      ? 'bg-emerald-500/80 text-white hover:bg-emerald-400'
                      : state === 'RESERVED'
                        ? 'bg-slate-700 text-slate-400'
                        : 'bg-slate-500/50 text-slate-500'

            return (
              <button
                key={seat.seatId}
                type="button"
                disabled={!available || disabled}
                onClick={() => onSelect(seat)}
                aria-label={`${seat.seatLabel}, ${tier}, ${state.toLowerCase()}`}
                aria-pressed={state === 'SELECTED'}
                title={`${seat.seatLabel} · ${tier} · ${state === 'AVAILABLE' ? 'Available' : state} · Price not provided`}
                className={`group relative flex min-h-20 flex-col items-center justify-center gap-1 rounded-xl border border-transparent p-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-80 ${color}`}
              >
                <Armchair size={21} aria-hidden="true" />
                <span>{seat.seatLabel}</span>
                <span className="text-[10px] font-medium">{tier}</span>
                <span className="pointer-events-none absolute -top-10 z-10 hidden whitespace-nowrap rounded-md bg-slate-950 px-2 py-1 text-[10px] text-white shadow-lg group-hover:block">
                  {seat.seatLabel} · {tier} · {available ? 'Available' : state} · Price not provided
                </span>
              </button>
            )
          })}</div></section>
          })}
        </div>
      )}
    </div>
  )
}

export default SeatMap