import { Navigate, useLocation } from 'react-router-dom'

import { useAuth } from '../context/useAuth'
import type { UserRole } from '../types/auth'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: UserRole
}

function ProtectedRoute({
  children,
  requiredRole,
}: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: location.pathname }}
      />
    )
  }

  if (
    requiredRole &&
    (!user || user.role !== requiredRole)
  ) {
    return <Navigate to="/events" replace />
  }

  return children
}

export default ProtectedRoute