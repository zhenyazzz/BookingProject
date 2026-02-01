import { Card, CardContent } from './ui/card';
import { PopularRoute } from '../types';

interface PopularRouteCardProps {
  route: PopularRoute;
  onClick: () => void;
}

export function PopularRouteCard({ route, onClick }: PopularRouteCardProps) {
  return (
    <Card 
      className="overflow-hidden cursor-pointer hover:shadow-lg transition-shadow"
      onClick={onClick}
    >
      <div className="relative h-32 overflow-hidden">
        <img 
          src={route.image} 
          alt={`${route.from} - ${route.to}`}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
        <div className="absolute bottom-2 left-3 right-3 text-white">
          <p className="text-sm opacity-90">{route.from}</p>
          <p className="text-sm opacity-90">↓</p>
          <p className="text-sm opacity-90">{route.to}</p>
        </div>
      </div>
      <CardContent className="p-3">
        <div className="flex items-center justify-between">
          <span className="text-muted-foreground text-sm">от</span>
          <span className="text-xl" style={{ color: '#2563EB' }}>{route.price} BYN</span>
        </div>
      </CardContent>
    </Card>
  );
}
