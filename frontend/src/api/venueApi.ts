import apiClient from './axios'

export interface Venue {
  id: string
  name: string
  address: string
}

export const getVenues = async (): Promise<Venue[]> => {
  const response = await apiClient.get<Venue[]>('/venues')
  return response.data
}
