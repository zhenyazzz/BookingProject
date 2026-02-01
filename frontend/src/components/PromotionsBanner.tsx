import { Tag } from 'lucide-react';
import { Card, CardContent } from './ui/card';
import { Badge } from './ui/badge';

const promotions = [
  {
    id: '1',
    title: 'Скидка 20% на ночные рейсы',
    description: 'Бронируйте билеты на рейсы после 22:00 и получайте скидку',
    discount: '-20%',
    color: '#10B981',
  },
  {
    id: '2',
    title: 'Групповое бронирование',
    description: 'При покупке от 4 билетов - скидка 15%',
    discount: '-15%',
    color: '#2563EB',
  },
];

export function PromotionsBanner() {
  return (
    <div className="space-y-3">
      {promotions.map(promo => (
        <Card key={promo.id} className="overflow-hidden">
          <CardContent className="p-4">
            <div className="flex items-start gap-3">
              <div 
                className="flex-shrink-0 w-12 h-12 rounded-lg flex items-center justify-center"
                style={{ backgroundColor: `${promo.color}20` }}
              >
                <Tag className="w-6 h-6" style={{ color: promo.color }} />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2 mb-1">
                  <h3 className="text-sm">{promo.title}</h3>
                  <Badge 
                    className="flex-shrink-0"
                    style={{ backgroundColor: promo.color, color: 'white' }}
                  >
                    {promo.discount}
                  </Badge>
                </div>
                <p className="text-sm text-muted-foreground">{promo.description}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}
