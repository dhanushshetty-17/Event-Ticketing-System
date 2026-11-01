import apiClient from './axios'
import type { Event, OrganizerEvent, OrganizerEventSummary } from '../types/event'

export interface CreateEventRequest {
  name: string
  description: string
  venueId: string
  startsAt: string
  endsAt: string
}

export const getEvents = async (): Promise<Event[]> => {
  const response = await apiClient.get<{ content: Event[] }>('/events')

  return response.data.content
}

export const getEvent = async (eventId: string): Promise<Event> => {
  const response = await apiClient.get<Event>(`/events/${eventId}`)

  return response.data
}

export const createEvent = async (
  request: CreateEventRequest,
): Promise<Event> => {
  const response = await apiClient.post<Event>('/events', request)
  return response.data
}

export const publishEvent = async (eventId: string): Promise<Event> => {
  const response = await apiClient.patch<Event>(`/events/${eventId}/publish`)
  return response.data
}

export const cancelEvent = async (eventId: string): Promise<Event> => {
  const response = await apiClient.patch<Event>(`/events/${eventId}/cancel`)
  return response.data
}

export const getOrganizerEvents = async (): Promise<OrganizerEvent[]> => {
  const response = await apiClient.get<OrganizerEvent[]>(
    '/organizer/events',
  )
  return response.data
}

export const getOrganizerEventSummaries = async (): Promise<OrganizerEventSummary[]> => {
  const response = await apiClient.get<OrganizerEventSummary[]>('/organizer/events/summary')
  return response.data
}
