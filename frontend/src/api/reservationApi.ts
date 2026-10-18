
import apiClient from './axios'

export interface SeatReservationRequest {
  eventId: string
  seatId: string
}

export interface BookingResponse {
  id: string
  eventId: string
  seatId: string
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED'
  createdAt: string
  expiresAt: string
}

export async function reserveSeat(
  request: SeatReservationRequest,
): Promise<BookingResponse> {
  const response = await apiClient.post<BookingResponse>(
    '/reservations',
    request,
  )

  return response.data
}
