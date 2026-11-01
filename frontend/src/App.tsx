
import { useEffect } from 'react'
import { Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'

import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import EventsPage from './pages/EventsPage'
import EventDetailsPage from './pages/EventDetailsPage'
import BookingPage from './pages/BookingPage'
import MyBookingsPage from './pages/MyBookingsPage'
import TicketPage from './pages/TicketPage'
import OrganizerDashboardPage from './pages/OrganizerDashboardPage'
import { AUTH_SESSION_EXPIRED_EVENT } from './api/axios'
import { useAuth } from './context/useAuth'

function AuthSessionHandler() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    const handleSessionExpired = (event: Event) => {
      const returnTo = (event as CustomEvent<{ returnTo?: string }>).detail?.returnTo
      logout()
      if (location.pathname !== '/login') {
        navigate('/login', {
          replace: true,
          state: { returnTo: returnTo || `${location.pathname}${location.search}` },
        })
      }
    }
    window.addEventListener(AUTH_SESSION_EXPIRED_EVENT, handleSessionExpired)
    return () => window.removeEventListener(AUTH_SESSION_EXPIRED_EVENT, handleSessionExpired)
  }, [location.pathname, location.search, logout, navigate])

  return null
}

function App() {
  return (
    <>
      <AuthSessionHandler />
      <Navbar />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route
          path="/events"
          element={
            <ProtectedRoute>
              <EventsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/events/:eventId"
          element={
            <ProtectedRoute>
              <EventDetailsPage />
            </ProtectedRoute>
          }
        />
        <Route path="/my-bookings" element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>} />
        <Route path="/bookings" element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>} />
        <Route path="/bookings/:bookingId" element={<ProtectedRoute><BookingPage /></ProtectedRoute>} />
        <Route path="/tickets/:bookingId" element={<ProtectedRoute><TicketPage /></ProtectedRoute>} />
        <Route
          path="/organizer-dashboard"
          element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><OrganizerDashboardPage /></ProtectedRoute>}
        />
        <Route
          path="/organizer/dashboard"
          element={<ProtectedRoute requiredRole="ROLE_ORGANIZER"><OrganizerDashboardPage /></ProtectedRoute>}
        />
        <Route path="/" element={<Navigate to="/events" replace />} />
        <Route path="*" element={<Navigate to="/events" replace />} />
      </Routes>
    </>
  )
}

export default App
