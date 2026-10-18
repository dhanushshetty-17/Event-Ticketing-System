import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

import { login, type LoginResponse } from '../api/authApi'

interface AuthContextValue {
  user: LoginResponse | null
  isAuthenticated: boolean
  loginUser: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<LoginResponse | null>(null)

  useEffect(() => {
    const storedToken = localStorage.getItem('accessToken')
    const storedUser = localStorage.getItem('user')

    if (!storedToken || !storedUser) {
      return
    }

    try {
      const parsedUser = JSON.parse(storedUser) as LoginResponse
      setUser(parsedUser)
    } catch {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
    }
  }, [])

  const loginUser = async (
    email: string,
    password: string,
  ): Promise<void> => {
    const response = await login({
      email,
      password,
    })

    localStorage.setItem('accessToken', response.token)
    localStorage.setItem('user', JSON.stringify(response))

    setUser(response)
  }

  const logout = (): void => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')

    setUser(null)
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      loginUser,
      logout,
    }),
    [user],
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error(
      'useAuth must be used inside an AuthProvider',
    )
  }

  return context
}