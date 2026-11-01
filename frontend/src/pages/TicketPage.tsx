import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import { CalendarDays, Download, MapPin, Printer, Ticket as TicketIcon } from 'lucide-react'
import { QRCodeSVG } from 'qrcode.react'

import { getTicketByBooking } from '../api/bookingApi'
import LoadingSpinner from '../components/LoadingSpinner'

function downloadCalendar(name: string, startsAt: string, venue: string) {
  const date = new Date(startsAt).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '')
  const content = `BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nDTSTART:${date}\r\nSUMMARY:${name}\r\nLOCATION:${venue}\r\nDESCRIPTION:Evently ticket\r\nEND:VEVENT\r\nEND:VCALENDAR`
  const url = URL.createObjectURL(new Blob([content], { type: 'text/calendar;charset=utf-8' }))
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `${name.replace(/[^a-z0-9]+/gi, '-').toLowerCase()}.ics`
  anchor.click()
  URL.revokeObjectURL(url)
}

function TicketPage() {
  const { bookingId } = useParams<{ bookingId: string }>()
  const [pollCount, setPollCount] = useState(0)
  const ticketQuery = useQuery({
    queryKey: ['ticket', bookingId],
    queryFn: () => getTicketByBooking(bookingId!),
    enabled: Boolean(bookingId),
    retry: false,
  })
  const ticket = ticketQuery.data
  const { isError, isLoading, refetch } = ticketQuery
  useEffect(() => {
    if (!isError || ticket || pollCount >= 10) return
    const timer = window.setTimeout(() => {
      setPollCount((count) => count + 1)
      void refetch()
    }, 2000)
    return () => window.clearTimeout(timer)
  }, [isError, pollCount, refetch, ticket])
  const stillIssuing = !ticket && (isLoading || pollCount < 10)

  if (ticketQuery.isLoading || stillIssuing) {
    return <main className="flex min-h-screen items-center justify-center bg-slate-950"><LoadingSpinner label="Issuing your digital ticket..." /></main>
  }
  if (ticketQuery.isError || !ticket) {
    return <main className="min-h-screen bg-slate-950 px-4 py-16 text-center text-red-200"><p>Ticket issuance is taking longer than expected.</p><button type="button" onClick={() => ticketQuery.refetch()} className="mt-4 rounded-lg bg-indigo-500 px-4 py-2 font-semibold text-white">Retry</button><Link className="ml-3 inline-block text-indigo-300" to="/my-bookings">Back to my bookings</Link></main>
  }

  return (
    <main className="min-h-screen bg-slate-950 px-4 py-10 text-white sm:px-6 print:bg-white print:p-0 print:text-black">
      <div className="mx-auto max-w-4xl">
        <div className="mb-6 flex flex-wrap items-center justify-between gap-3 print:hidden"><Link to="/my-bookings" className="text-sm text-slate-400 hover:text-white">← My bookings</Link><div className="flex gap-2"><button type="button" onClick={() => window.print()} className="flex items-center gap-2 rounded-lg border border-slate-700 px-3 py-2 text-sm hover:border-indigo-400"><Printer size={16} /> Print / Save PDF</button><button type="button" onClick={() => downloadCalendar(ticket.eventName, ticket.startsAt, ticket.venueName)} className="flex items-center gap-2 rounded-lg border border-slate-700 px-3 py-2 text-sm hover:border-indigo-400"><Download size={16} /> Download .ics Calendar File</button></div></div>
        <article className="overflow-hidden rounded-3xl border border-slate-800 bg-slate-900 shadow-2xl print:rounded-none print:border-slate-300 print:bg-white print:shadow-none">
          <div className="border-b border-dashed border-slate-700 bg-gradient-to-r from-indigo-500/30 to-violet-500/20 p-7 print:bg-white"><div className="flex items-center gap-2 text-indigo-300 print:text-indigo-700"><TicketIcon size={19} /> Evently digital ticket</div><h1 className="mt-5 text-4xl font-black">{ticket.eventName}</h1></div>
          <div className="grid gap-8 p-7 md:grid-cols-[1fr_auto]"><div className="grid gap-6 sm:grid-cols-2"><div><p className="text-xs uppercase tracking-widest text-slate-500">Event date</p><p className="mt-2 flex items-center gap-2 font-semibold"><CalendarDays size={17} /> {new Date(ticket.startsAt).toLocaleString()}</p></div><div><p className="text-xs uppercase tracking-widest text-slate-500">Venue</p><p className="mt-2 flex items-center gap-2 font-semibold"><MapPin size={17} /> {ticket.venueName}</p><p className="mt-1 text-sm text-slate-400">{ticket.venueAddress ?? 'Venue address not provided'}{ticket.venueCity ? `, ${ticket.venueCity}` : ''}</p></div><div><p className="text-xs uppercase tracking-widest text-slate-500">Gate details</p><p className="mt-2 font-semibold">{ticket.gateDetails ?? 'Not provided by event API'}</p></div><div><p className="text-xs uppercase tracking-widest text-slate-500">Seat</p><p className="mt-2 text-2xl font-bold">{ticket.seatIdentifier}</p></div><div><p className="text-xs uppercase tracking-widest text-slate-500">Ticket status</p><p className="mt-2 inline-flex rounded-full bg-emerald-400/15 px-3 py-1 font-bold text-emerald-300">{ticket.status}</p></div><div className="sm:col-span-2"><p className="text-xs uppercase tracking-widest text-slate-500">Ticket code</p><p className="mt-2 break-all font-mono text-sm text-slate-300">{ticket.ticketCode}</p></div></div><div className="flex flex-col items-center gap-3"><QRCodeSVG value={ticket.qrPayload} size={144} level="H" includeMargin bgColor="#ffffff" fgColor="#0b0f19" aria-label="Ticket QR code" /><p className="max-w-36 text-center text-[10px] text-slate-500">Scan to validate this ticket</p></div></div>
        </article>
      </div>
    </main>
  )
}

export default TicketPage
