import { Bus } from 'lucide-react';
import { SearchForm } from './SearchForm';
import { RouteList } from './RouteList';
import { Button } from './ui/button';
import { City } from '../types';

interface HomePageProps {
  onSearch: (from: City, to: City, date: string) => void;
  onProfileClick: () => void;
}

export function HomePage({ onSearch, onProfileClick }: HomePageProps) {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header 
        className="text-white py-6 px-4"
        style={{ backgroundColor: '#2563EB' }}
      >
        <div className="max-w-6xl mx-auto">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-3">
              <Bus className="w-8 h-8" />
              <h1 className="text-white">БилетБел</h1>
            </div>
            <Button 
              variant="ghost"
              size="sm"
              onClick={onProfileClick}
              className="text-white hover:bg-blue-600"
            >
              👤 Кабинет
            </Button>
          </div>
          <p className="text-blue-100 text-sm">
            Бронирование билетов на автобусы по Беларуси
          </p>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-6xl mx-auto px-4 py-6 space-y-8">
        {/* Search Form */}
        <section>
          <h2 className="mb-4">Поиск рейсов</h2>
          <SearchForm onSearch={onSearch} />
        </section>

        {/* Routes List */}
        <RouteList onRouteClick={(from, to) => {
          const today = new Date().toISOString().split('T')[0];
          onSearch(from, to, today);
        }} />
      </main>

      {/* Footer */}
      <footer className="border-t mt-12 py-6 px-4">
        <div className="max-w-6xl mx-auto text-center text-sm text-muted-foreground">
          <p>© 2026 БилетБел. Все права защищены.</p>
          <p className="mt-2">Служба поддержки: +375 (29) 123-45-67</p>
        </div>
      </footer>
    </div>
  );
}