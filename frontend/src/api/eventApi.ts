import apiClient from './axios'
import type { Event } from '../types/event'

export const getEvents = async (): Promise<Event[]> => {
  const response = await apiClient.get<Event[]>('/events')

  return response.data
}

export const getEvent = async (eventId: string): Promise<Event> => {
  const response = await apiClient.get<Event>(`/events/${eventId}`)

  return response.data
}