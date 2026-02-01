import { Bus, Clock, MapPin, Users } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Route } from '../types';

interface RouteCardProps {
  route: Route;
  onSelectSeats: (route: Route) => void;
}

export function RouteCard({ route, onSelectSeats }: RouteCardProps) {
  const availabilityPercentage = (route.availableSeats / route.totalSeats) * 100;
  
  let availabilityColor = '#10B981'; // green
  if (availabilityPercentage < 30) {
    availabilityColor = '#EF4444'; // red
  } else if (availabilityPercentage < 50) {
    availabilityColor = '#F59E0B'; // orange
  }

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-4">
        <div className="space-y-4">
          {/* Header */}
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2">
              <div 
                className="w-10 h-10 rounded-lg flex items-center justify-center"
                style={{ backgroundColor: '#2563EB20' }}
              >
                <Bus className="w-5 h-5" style={{ color: '#2563EB' }} />
              </div>
              <div>
                <Badge variant="secondary">
                  {route.vehicleType === 'bus' ? 'Автобус' : 'Маршрутка'}
                </Badge>
              </div>
            </div>
            <div className="text-right">
              <div className="text-2xl" style={{ color: '#2563EB' }}>
                {route.price} BYN
              </div>
              <div className="text-xs text-muted-foreground">за место</div>
            </div>
          </div>

          {/* Route Info */}
          <div className="grid grid-cols-[1fr,auto,1fr] items-center gap-3">
            <div>
              <div className="text-sm text-muted-foreground mb-1">
                <MapPin className="w-3 h-3 inline mr-1" />
                {route.from.nameRu}
              </div>
              {route.departureDate && (
                <div className="text-xs text-muted-foreground">{route.departureDate}</div>
              )}
              <div className="text-xl">{route.departureTime}</div>
            </div>
            
            <div className="text-center px-3">
              <div className="text-xs text-muted-foreground mb-1 flex items-center gap-1">
                <Clock className="w-3 h-3" />
                {route.duration}
              </div>
              <div className="h-px bg-border w-16" />
            </div>
            
            <div className="text-right">
              <div className="text-sm text-muted-foreground mb-1">
                <MapPin className="w-3 h-3 inline mr-1" />
                {route.to.nameRu}
              </div>
              {route.arrivalDate && (
                <div className="text-xs text-muted-foreground">{route.arrivalDate}</div>
              )}
              <div className="text-xl">{route.arrivalTime}</div>
            </div>
          </div>

          {/* Availability */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-sm">
              <Users className="w-4 h-4" style={{ color: availabilityColor }} />
              <span style={{ color: availabilityColor }}>
                Свободно: {route.availableSeats} из {route.totalSeats}
              </span>
            </div>
            <Button 
              style={{ backgroundColor: '#2563EB' }}
              onClick={() => onSelectSeats(route)}
            >
              Выбрать места
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
