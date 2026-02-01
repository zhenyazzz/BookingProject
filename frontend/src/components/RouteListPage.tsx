import { useState, useMemo, useEffect } from 'react';
import { ArrowLeft, SlidersHorizontal, Loader2 } from 'lucide-react';
import { Button } from './ui/button';
import { RouteCard } from './RouteCard';
import { Card, CardContent } from './ui/card';
import { Label } from './ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Slider } from './ui/slider';
import { Route } from '../types';
import { tripsApi } from '../api/trips';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
  PaginationEllipsis,
} from './ui/pagination';

interface RouteListPageProps {
  fromCity: string;
  toCity: string;
  date: string;
  onBack: () => void;
  onSelectSeats: (route: Route) => void;
}

type SortOption = 'price-asc' | 'price-desc' | 'time-asc' | 'time-desc' | 'duration';

export function RouteListPage({ fromCity, toCity, date, onBack, onSelectSeats }: RouteListPageProps) {
  const [showFilters, setShowFilters] = useState(false);
  const [sortBy, setSortBy] = useState<SortOption>('time-asc');
  const [priceRange, setPriceRange] = useState([0, 100]);
  const [selectedVehicleTypes, setSelectedVehicleTypes] = useState<('bus' | 'minibus')[]>(['bus', 'minibus']);
  const [timeFilter, setTimeFilter] = useState<'all' | 'morning' | 'afternoon' | 'evening' | 'night'>('all');
  
  const [routes, setRoutes] = useState<Route[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  useEffect(() => {
    const fetchTrips = async () => {
      try {
        setLoading(true);
        setError(null);
        // Format date to YYYY-MM-DD if needed, assuming date prop comes in correct format or DD.MM.YYYY
        // If date comes as DD.MM.YYYY (from UI display), we might need to convert it for API
        // But assuming SearchForm passes ISO string or YYYY-MM-DD
        
        // Let's assume input is YYYY-MM-DD for API
        const response = await tripsApi.getTrips({
          fromCity,
          toCity,
          date,
          page: currentPage,
          size: pageSize
        });
        
        setRoutes(response.content);
        setTotalPages(response.totalPages);
        setTotalElements(response.totalElements);
      } catch (err) {
        console.error('Failed to fetch trips', err);
        setError('Не удалось загрузить рейсы. Попробуйте позже.');
      } finally {
        setLoading(false);
      }
    };

    fetchTrips();
  }, [fromCity, toCity, date, currentPage]);

  const filteredAndSortedRoutes = useMemo(() => {
    let filtered = routes.filter(route => {
      // Price filter
      if (route.price < priceRange[0] || route.price > priceRange[1]) return false;
      
      // Vehicle type filter
      if (!selectedVehicleTypes.includes(route.vehicleType)) return false;
      
      // Time filter
      if (timeFilter !== 'all') {
        const hour = parseInt(route.departureTime.split(':')[0]);
        if (timeFilter === 'morning' && (hour < 6 || hour >= 12)) return false;
        if (timeFilter === 'afternoon' && (hour < 12 || hour >= 18)) return false;
        if (timeFilter === 'evening' && (hour < 18 || hour >= 22)) return false;
        if (timeFilter === 'night' && (hour >= 6 && hour < 22)) return false;
      }
      
      return true;
    });

    // Sort
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'price-asc':
          return a.price - b.price;
        case 'price-desc':
          return b.price - a.price;
        case 'time-asc':
          return a.departureTime.localeCompare(b.departureTime);
        case 'time-desc':
          return b.departureTime.localeCompare(a.departureTime);
        case 'duration':
          return a.duration.localeCompare(b.duration);
        default:
          return 0;
      }
    });

    return filtered;
  }, [routes, sortBy, priceRange, selectedVehicleTypes, timeFilter]);

  const toggleVehicleType = (type: 'bus' | 'minibus') => {
    if (selectedVehicleTypes.includes(type)) {
      setSelectedVehicleTypes(selectedVehicleTypes.filter(t => t !== type));
    } else {
      setSelectedVehicleTypes([...selectedVehicleTypes, type]);
    }
  };

  const renderPagination = () => {
    if (totalPages <= 1) return null;

    const pages: (number | 'ellipsis')[] = [];
    const maxVisiblePages = 5;

    if (totalPages <= maxVisiblePages) {
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage < 3) {
        for (let i = 0; i < 4; i++) {
          pages.push(i);
        }
        pages.push('ellipsis');
        pages.push(totalPages - 1);
      } else if (currentPage > totalPages - 4) {
        pages.push(0);
        pages.push('ellipsis');
        for (let i = totalPages - 4; i < totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(0);
        pages.push('ellipsis');
        for (let i = currentPage - 1; i <= currentPage + 1; i++) {
          pages.push(i);
        }
        pages.push('ellipsis');
        pages.push(totalPages - 1);
      }
    }

    return (
      <Pagination className="mt-8">
        <PaginationContent>
          <PaginationItem>
            <PaginationPrevious
              onClick={(e) => {
                e.preventDefault();
                if (currentPage > 0) setCurrentPage(currentPage - 1);
              }}
              className={currentPage === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
              href="#"
            />
          </PaginationItem>
          
          {pages.map((page, index) => (
            <PaginationItem key={index}>
              {page === 'ellipsis' ? (
                <PaginationEllipsis />
              ) : (
                <PaginationLink
                  onClick={(e) => {
                    e.preventDefault();
                    setCurrentPage(page);
                  }}
                  isActive={page === currentPage}
                  className="cursor-pointer"
                  href="#"
                >
                  {page + 1}
                </PaginationLink>
              )}
            </PaginationItem>
          ))}
          
          <PaginationItem>
            <PaginationNext
              onClick={(e) => {
                e.preventDefault();
                if (currentPage < totalPages - 1) setCurrentPage(currentPage + 1);
              }}
              className={currentPage >= totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
              href="#"
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header 
        className="text-white py-4 px-4 sticky top-0 z-10"
        style={{ backgroundColor: '#2563EB' }}
      >
        <div className="max-w-6xl mx-auto">
          <div className="flex items-center gap-3 mb-3">
            <Button
              variant="ghost"
              size="sm"
              onClick={onBack}
              className="text-white hover:bg-blue-600 -ml-2"
            >
              <ArrowLeft className="w-5 h-5" />
            </Button>
            <div className="flex-1">
              <h2 className="text-white">{fromCity} → {toCity}</h2>
              <p className="text-sm text-blue-100">{date}</p>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-4 space-y-4">
        {/* Controls */}
        <div className="flex gap-2">
          <Button
            variant={showFilters ? 'default' : 'outline'}
            onClick={() => setShowFilters(!showFilters)}
            className="flex-shrink-0"
            style={showFilters ? { backgroundColor: '#2563EB' } : {}}
          >
            <SlidersHorizontal className="w-4 h-4 mr-2" />
            Фильтры
          </Button>
          
          <Select value={sortBy} onValueChange={(value) => setSortBy(value as SortOption)}>
            <SelectTrigger className="w-full max-w-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="time-asc">По времени ↑</SelectItem>
              <SelectItem value="time-desc">По времени ↓</SelectItem>
              <SelectItem value="price-asc">По цене ↑</SelectItem>
              <SelectItem value="price-desc">По цене ↓</SelectItem>
              <SelectItem value="duration">По длительности</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <Card>
            <CardContent className="p-4 space-y-6">
              {/* Time Filter */}
              <div>
                <Label className="mb-3 block">Время отправления</Label>
                <div className="grid grid-cols-2 gap-2">
                  {[
                    { value: 'all', label: 'Все' },
                    { value: 'morning', label: 'Утро (6-12)' },
                    { value: 'afternoon', label: 'День (12-18)' },
                    { value: 'evening', label: 'Вечер (18-22)' },
                    { value: 'night', label: 'Ночь (22-6)' },
                  ].map(option => (
                    <Button
                      key={option.value}
                      variant={timeFilter === option.value ? 'default' : 'outline'}
                      size="sm"
                      onClick={() => setTimeFilter(option.value as any)}
                      style={timeFilter === option.value ? { backgroundColor: '#2563EB' } : {}}
                    >
                      {option.label}
                    </Button>
                  ))}
                </div>
              </div>

              {/* Price Range */}
              <div>
                <Label className="mb-3 block">
                  Цена: {priceRange[0]} - {priceRange[1]} BYN
                </Label>
                <Slider
                  min={0}
                  max={100}
                  step={5}
                  value={priceRange}
                  onValueChange={setPriceRange}
                />
              </div>

              {/* Vehicle Type */}
              <div>
                <Label className="mb-3 block">Тип транспорта</Label>
                <div className="flex gap-2">
                  <Button
                    variant={selectedVehicleTypes.includes('bus') ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => toggleVehicleType('bus')}
                    style={selectedVehicleTypes.includes('bus') ? { backgroundColor: '#2563EB' } : {}}
                  >
                    Автобус
                  </Button>
                  <Button
                    variant={selectedVehicleTypes.includes('minibus') ? 'default' : 'outline'}
                    size="sm"
                    onClick={() => toggleVehicleType('minibus')}
                    style={selectedVehicleTypes.includes('minibus') ? { backgroundColor: '#2563EB' } : {}}
                  >
                    Маршрутка
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Results */}
        <div className="space-y-3">
          {loading ? (
             <div className="flex justify-center py-12">
               <Loader2 className="h-8 w-8 animate-spin text-blue-600" />
             </div>
          ) : error ? (
            <Card>
              <CardContent className="p-8 text-center">
                <p className="text-red-500 mb-2">{error}</p>
                <Button variant="outline" onClick={() => window.location.reload()}>Повторить</Button>
              </CardContent>
            </Card>
          ) : (
            <>
              <p className="text-sm text-muted-foreground">
                Найдено рейсов: {totalElements}
              </p>
              {filteredAndSortedRoutes.map(route => (
                <RouteCard
                  key={route.id}
                  route={route}
                  onSelectSeats={onSelectSeats}
                />
              ))}
              {filteredAndSortedRoutes.length === 0 && (
                <Card>
                  <CardContent className="p-8 text-center">
                    <p className="text-muted-foreground">
                      По вашему запросу рейсы не найдены
                    </p>
                  </CardContent>
                </Card>
              )}
              {renderPagination()}
            </>
          )}
        </div>
      </main>
    </div>
  );
}
