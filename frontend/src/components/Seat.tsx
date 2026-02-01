import { Seat } from '../types';

interface SeatProps {
  seat: Seat;
  onToggle: (seatId: number) => void;
  isSelected: boolean;
}

export function SeatComponent({ seat, onToggle, isSelected }: SeatProps) {
  const getColor = () => {
    if (isSelected) return '#2563EB'; // blue
    switch (seat.status) {
      case 'available':
        return '#10B981'; // green
      case 'occupied':
        return '#9CA3AF'; // gray
      case 'blocked':
        return '#EF4444'; // red
      default:
        return '#9CA3AF';
    }
  };

  const isClickable = seat.status === 'available' || isSelected;

  return (
    <button
      onClick={() => isClickable && onToggle(seat.id)}
      disabled={!isClickable}
      className="relative w-full aspect-square rounded transition-all"
      style={{
        backgroundColor: getColor(),
        cursor: isClickable ? 'pointer' : 'not-allowed',
        opacity: seat.status === 'available' || isSelected ? 1 : 0.5,
      }}
    >
      <span className="text-xs text-white absolute inset-0 flex items-center justify-center">
        {seat.id}
      </span>
    </button>
  );
}
