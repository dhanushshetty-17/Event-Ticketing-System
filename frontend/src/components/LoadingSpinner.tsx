interface LoadingSpinnerProps {
  label?: string
  className?: string
  size?: 'sm' | 'md' | 'lg'
}

function LoadingSpinner({
  label = 'Loading',
  className = '',
  size = 'md',
}: LoadingSpinnerProps) {
  const sizeClass = {
    sm: 'h-4 w-4',
    md: 'h-5 w-5',
    lg: 'h-8 w-8',
  }[size]

  return (
    <div
      className={`inline-flex items-center gap-2 text-sm text-slate-400 ${className}`}
      role="status"
      aria-live="polite"
    >
      <svg
        className={`${sizeClass} animate-spin text-indigo-400`}
        viewBox="0 0 24 24"
        fill="none"
        aria-hidden="true"
      >
        <circle
          className="opacity-25"
          cx="12"
          cy="12"
          r="9"
          stroke="currentColor"
          strokeWidth="3"
        />
        <path
          className="opacity-90"
          d="M21 12a9 9 0 0 0-9-9"
          stroke="currentColor"
          strokeWidth="3"
          strokeLinecap="round"
        />
      </svg>
      {label && <span>{label}</span>}
    </div>
  )
}

export default LoadingSpinner