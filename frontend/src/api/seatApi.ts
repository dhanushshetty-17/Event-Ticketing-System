
import apiClient from './axios'

export type EventSeatStatus = 'AVAILABLE' | 'RESERVED' | 'BOOKED'

export interface EventSeat {
  seatId: string
  seatLabel: string
  status: EventSeatStatus
}

export async function getEventSeats(eventId: string): Promise<EventSeat[]> {
  const response = await apiClient.get<EventSeat[]>(
    `/events/${eventId}/seats`,
  )

  return response.data
}
