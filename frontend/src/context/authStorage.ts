import type { AuthUser } from '../types/auth'

const tokenKey = 'accessToken'
const userKey = 'user'

export function getStoredAuth(): AuthUser | null {
  const token = localStorage.getItem(tokenKey)
  const storedUser = localStorage.getItem(userKey)

  if (!token || !storedUser) {
    return null
  }

  try {
    const parsedUser = JSON.parse(storedUser) as AuthUser
    if (
      parsedUser.role !== 'ROLE_USER' &&
      parsedUser.role !== 'ROLE_ORGANIZER'
    ) {
      clearStoredAuth()
      return null
    }
    return parsedUser
  } catch {
    clearStoredAuth()
    return null
  }
}

export function storeAuth(token: string, user: AuthUser): void {
  localStorage.setItem(tokenKey, token)
  localStorage.setItem(userKey, JSON.stringify(user))
}

export function clearStoredAuth(): void {
  localStorage.removeItem(tokenKey)
  localStorage.removeItem(userKey)
}
