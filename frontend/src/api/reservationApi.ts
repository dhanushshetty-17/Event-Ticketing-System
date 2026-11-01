
import apiClient from './axios'
import type { Booking } from '../types/booking'

export interface SeatReservationRequest {
  eventId: string
  seatId: string
}

export async function reserveSeat(
  request: SeatReservationRequest,
): Promise<Booking> {
  const response = await apiClient.post<Booking>(
    '/reservations',
    request,
  )

  return response.data
}
