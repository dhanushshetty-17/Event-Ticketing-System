import apiClient from './axios'
import type { Booking, Ticket } from '../types/booking'

export const getBookings = async (): Promise<Booking[]> => {
  const response = await apiClient.get<{ content: Booking[] }>('/bookings')
  return response.data.content
}

export const getBooking = async (bookingId: string): Promise<Booking> => {
  const response = await apiClient.get<Booking>(`/bookings/${bookingId}`)
  return response.data
}

export const confirmBooking = async (
  bookingId: string,
): Promise<Booking> => {
  const response = await apiClient.post<Booking>(
    `/bookings/${bookingId}/confirm`,
  )
  return response.data
}

export const cancelBooking = async (
  bookingId: string,
): Promise<Booking> => {
  const response = await apiClient.post<Booking>(
    `/bookings/${bookingId}/cancel`,
  )
  return response.data
}

export const getTicket = async (ticketId: string): Promise<Ticket> => {
  const response = await apiClient.get<Ticket>(`/tickets/${ticketId}`)
  return response.data
}

export const getTicketByBooking = async (
  bookingId: string,
): Promise<Ticket> => {
  const response = await apiClient.get<Ticket>(
    `/tickets/booking/${bookingId}`,
  )
  return response.data
}