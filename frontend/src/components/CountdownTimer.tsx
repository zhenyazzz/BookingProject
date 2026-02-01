import { useEffect, useState } from 'react';
import { Clock } from 'lucide-react';

interface CountdownTimerProps {
  minutes: number;
  onExpire: () => void;
}

export function CountdownTimer({ minutes, onExpire }: CountdownTimerProps) {
  const [timeLeft, setTimeLeft] = useState(minutes * 60); // in seconds

  useEffect(() => {
    if (timeLeft <= 0) {
      onExpire();
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          onExpire();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [timeLeft, onExpire]);

  const mins = Math.floor(timeLeft / 60);
  const secs = timeLeft % 60;
  const percentage = (timeLeft / (minutes * 60)) * 100;

  const getColor = () => {
    if (percentage > 50) return '#10B981'; // green
    if (percentage > 25) return '#F59E0B'; // orange
    return '#EF4444'; // red
  };

  return (
    <div 
      className="p-4 rounded-lg"
      style={{ backgroundColor: `${getColor()}20` }}
    >
      <div className="flex items-center gap-3">
        <Clock className="w-6 h-6" style={{ color: getColor() }} />
        <div className="flex-1">
          <div className="text-sm text-muted-foreground mb-1">
            Времени на бронирование
          </div>
          <div className="text-2xl" style={{ color: getColor() }}>
            {String(mins).padStart(2, '0')}:{String(secs).padStart(2, '0')}
          </div>
        </div>
      </div>
      <div className="mt-3 h-2 bg-white/50 rounded-full overflow-hidden">
        <div 
          className="h-full transition-all duration-1000"
          style={{ 
            width: `${percentage}%`,
            backgroundColor: getColor(),
          }}
        />
      </div>
    </div>
  );
}
