
import { Navigate, Route, Routes } from 'react-router-dom'

import { useAuth } from './context/AuthContext'
import LoginPage from './pages/LoginPage'
import EventsPage from './pages/EventsPage'
import EventDetailsPage from './pages/EventDetailsPage'

function ProtectedRoute({
  children,
}: {
  children: React.ReactNode
}) {
  const { isAuthenticated } = useAuth()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return children
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

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

      <Route
        path="/"
        element={<Navigate to="/events" replace />}
      />

      <Route
        path="*"
        element={<Navigate to="/events" replace />}
      />
    </Routes>
  )
}

export default App
