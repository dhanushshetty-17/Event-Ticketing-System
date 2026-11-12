import { Menu, Ticket, X } from 'lucide-react'
import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'

import { useAuth } from '../context/useAuth'

function Navbar() {
  const { user, isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  if (!isAuthenticated) {
    return null
  }

  const closeMenu = () => setIsMenuOpen(false)
  const linkClass = ({ isActive }: { isActive: boolean }) =>
    `rounded-lg px-3 py-2 text-sm font-medium transition ${
      isActive
        ? 'bg-[#e9e3d8] text-[#c2523b]'
        : 'text-slate-600 hover:bg-[#e9e3d8] hover:text-[#18251f]'
    }`

  const handleLogout = () => {
    logout()
    closeMenu()
    navigate('/login', { replace: true })
  }

  return (
    <header className="border-b border-[#ded8cd] bg-[#f7f4ee]/95 text-[#18251f] backdrop-blur">
      <nav className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6">
        <NavLink
          to="/events"
          className="flex items-center gap-2 text-lg font-bold tracking-tight"
          onClick={closeMenu}
        >
          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#c2523b] text-[#f7f4ee]">
            <Ticket size={19} aria-hidden="true" />
          </span>
          <span className="font-serif text-xl">Evently</span>
        </NavLink>

        <button
          type="button"
          className="rounded-lg p-2 text-slate-300 hover:bg-slate-800 md:hidden"
          aria-label={isMenuOpen ? 'Close navigation menu' : 'Open navigation menu'}
          aria-expanded={isMenuOpen}
          onClick={() => setIsMenuOpen((open) => !open)}
        >
          {isMenuOpen ? <X size={22} /> : <Menu size={22} />}
        </button>

        <div className="hidden items-center gap-2 md:flex">
          <NavLink to="/events" className={linkClass}>
            Events
          </NavLink>
          <NavLink to="/my-bookings" className={linkClass}>
            My Bookings
          </NavLink>
          {user?.role === 'ROLE_ORGANIZER' && (
            <NavLink to="/organizer-dashboard" className={linkClass}>
              Organizer Console
            </NavLink>
          )}
          <div className="ml-3 flex items-center gap-3 border-l border-slate-800 pl-4">
            <span className="max-w-44 truncate text-sm text-slate-400">
              {user?.email} · {user?.role.replace('ROLE_', '')}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-lg border border-[#cfc7b8] px-3 py-2 text-sm font-medium text-slate-700 transition hover:border-[#c2523b]/50 hover:text-[#c2523b]"
            >
              Log out
            </button>
          </div>
        </div>
      </nav>

      {isMenuOpen && (
        <div className="border-t border-slate-800 px-4 py-3 md:hidden">
          <div className="flex flex-col gap-1">
            <NavLink to="/events" className={linkClass} onClick={closeMenu}>
              Events
            </NavLink>
            <NavLink
              to="/my-bookings"
              className={linkClass}
              onClick={closeMenu}
            >
              My Bookings
            </NavLink>
            {user?.role === 'ROLE_ORGANIZER' && (
              <NavLink
                to="/organizer-dashboard"
                className={linkClass}
                onClick={closeMenu}
              >
                Organizer Console
              </NavLink>
            )}
            <button
              type="button"
              onClick={handleLogout}
              className="mt-2 rounded-lg border border-slate-700 px-3 py-2 text-left text-sm font-medium text-slate-200"
            >
              Log out
            </button>
          </div>
        </div>
      )}
    </header>
  )
}

export default Navbar