export type BookingStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'EXPIRED'

export interface Booking {
  bookingId: string
  eventId: string
  seatId: string
  status: BookingStatus
  createdAt: string
  expiresAt: string | null
}

export interface Ticket {
  ticketId: string
  bookingId: string
  eventName: string
  venueName: string
  venueAddress: string | null
  venueCity: string | null
  gateDetails: string | null
  startsAt: string
  seatIdentifier: string
  ticketCode: string
  qrPayload: string
  status: 'CONFIRMED' | 'ISSUED' | 'CANCELLED' | 'USED'
  scannedAt: string | null
  scannedBy: string | null
}