export type EventStatus =
  | 'DRAFT'
  | 'PUBLISHED'
  | 'CANCELLED'
  | 'COMPLETED'

export interface Event {
  id: string
  name: string
  description: string
  venueId: string
  venueName: string
  organizerId: string
  startsAt: string
  endsAt: string
  status: EventStatus
}

export interface OrganizerEvent extends Event {
  totalSeats: number
  reservedSeats: number
  soldSeats: number
  revenue: number | null
}

export interface SectionOccupancy {
  section: string
  totalSeats: number
  reservedCount: number
  soldCount: number
}

export interface OrganizerEventSummary {
  eventId: string
  totalSeats: number
  reservedCount: number
  soldCount: number
  grossRevenue: number | null
  sections: SectionOccupancy[]
}