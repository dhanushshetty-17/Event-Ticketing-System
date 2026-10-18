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