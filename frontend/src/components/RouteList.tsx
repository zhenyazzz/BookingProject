import { useState, useEffect } from 'react';
import { Card, CardContent } from './ui/card';
import { Button } from './ui/button';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
  PaginationEllipsis,
} from './ui/pagination';
import { routesApi, RouteResponse, CityResponse } from '../api/routes';
import { ArrowRight } from 'lucide-react';
import { City } from '../types';

interface RouteListProps {
  onRouteClick: (from: City, to: City) => void;
}

export function RouteList({ onRouteClick }: RouteListProps) {
  const [routes, setRoutes] = useState<RouteResponse[]>([]);
  const [cities, setCities] = useState<CityResponse[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const pageSize = 12;

  useEffect(() => {
    const fetchCities = async () => {
      try {
        const citiesData = await routesApi.getCities();
        setCities(citiesData);
      } catch (error) {
        console.error('Failed to fetch cities', error);
      }
    };
    fetchCities();
  }, []);

  useEffect(() => {
    const fetchRoutes = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await routesApi.getRoutes({
          page: currentPage,
          size: pageSize,
        });
        setRoutes(response.content);
        setTotalPages(response.totalPages);
        setTotalElements(response.totalElements);
      } catch (err) {
        console.error('Failed to fetch routes', err);
        setError('Не удалось загрузить маршруты');
      } finally {
        setLoading(false);
      }
    };

    fetchRoutes();
  }, [currentPage]);

  const handleRouteClick = (route: RouteResponse) => {
    const fromCityData = cities.find(c => c.nameRu === route.fromCity || c.name === route.fromCity);
    const toCityData = cities.find(c => c.nameRu === route.toCity || c.name === route.toCity);
    
    if (fromCityData && toCityData) {
      const fromCity: City = {
        id: fromCityData.name,
        name: fromCityData.name,
        nameRu: fromCityData.nameRu,
      };
      const toCity: City = {
        id: toCityData.name,
        name: toCityData.name,
        nameRu: toCityData.nameRu,
      };
      onRouteClick(fromCity, toCity);
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
      <Pagination>
        <PaginationContent>
          <PaginationItem>
            <PaginationPrevious
              onClick={(e) => {
                e.preventDefault();
                if (currentPage > 0) setCurrentPage(currentPage - 1);
              }}
              className={currentPage === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
              href={currentPage > 0 ? '#' : undefined}
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
              href={currentPage < totalPages - 1 ? '#' : undefined}
            />
          </PaginationItem>
        </PaginationContent>
      </Pagination>
    );
  };

  if (loading) {
    return (
      <section>
        <h2 className="mb-4">Доступные маршруты</h2>
        <div className="text-center py-8">
          <p className="text-muted-foreground">Загрузка маршрутов...</p>
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section>
        <h2 className="mb-4">Доступные маршруты</h2>
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-muted-foreground">{error}</p>
          </CardContent>
        </Card>
      </section>
    );
  }

  return (
    <section>
      <div className="flex items-center justify-between mb-4">
        <h2>Доступные маршруты</h2>
        {totalElements > 0 && (
          <p className="text-sm text-muted-foreground">
            Всего: {totalElements}
          </p>
        )}
      </div>
      
      {routes.length === 0 ? (
        <Card>
          <CardContent className="p-8 text-center">
            <p className="text-muted-foreground">
              Маршруты не найдены
            </p>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
            {routes.map((route) => (
              <Card
                key={route.id}
                className="hover:shadow-md transition-shadow cursor-pointer"
                onClick={() => handleRouteClick(route)}
              >
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div className="flex-1">
                      <div className="text-sm text-muted-foreground mb-1">
                        {route.fromCity}
                      </div>
                      <div className="flex items-center gap-2 mb-1">
                        <div className="h-px bg-border flex-1" />
                        <ArrowRight className="w-4 h-4 text-muted-foreground" />
                        <div className="h-px bg-border flex-1" />
                      </div>
                      <div className="text-sm text-muted-foreground">
                        {route.toCity}
                      </div>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="ml-4"
                      style={{ color: '#2563EB' }}
                    >
                      Выбрать
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
          
          {renderPagination()}
        </>
      )}
    </section>
  );
}
