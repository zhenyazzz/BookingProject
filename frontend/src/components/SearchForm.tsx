import { useState, useMemo, useEffect } from 'react';
import { Search, Calendar, MapPin, Check, ChevronsUpDown } from 'lucide-react';
import { Button } from './ui/button';
import { cn } from './ui/utils';
import { Label } from './ui/label';
import { Card, CardContent } from './ui/card';
import { routesApi } from '../api/routes';
import { City } from '../types';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "./ui/command"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "./ui/popover"
import { Input } from './ui/input'; // Keep Input for Date

interface SearchFormProps {
  onSearch: (from: City, to: City, date: string) => void;
}

export function SearchForm({ onSearch }: SearchFormProps) {
  const [cities, setCities] = useState<City[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Form state
  const [fromCity, setFromCity] = useState<City | null>(null);
  const [toCity, setToCity] = useState<City | null>(null);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  
  // Popover open state
  const [openFrom, setOpenFrom] = useState(false);
  const [openTo, setOpenTo] = useState(false);

  useEffect(() => {
    const fetchCities = async () => {
      try {
        setLoading(true);
        const citiesData = await routesApi.getCities();
        const citiesList: City[] = citiesData.map((city, index) => ({
          id: String(index + 1),
          name: city.name,
          nameRu: city.nameRu,
        }));
        setCities(citiesList);
      } catch (error) {
        console.error('Failed to fetch cities', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCities();
  }, []);

  const handleSearch = () => {
    if (fromCity && toCity && date) {
      onSearch(fromCity, toCity, date);
    }
  };

  return (
    <Card className="w-full">
      <CardContent className="p-4 md:p-6">
        <div className="space-y-4">
          {/* From City */}
          <div className="relative flex flex-col gap-2">
            <Label className="flex items-center gap-2">
              <MapPin className="w-4 h-4 text-blue-600" />
              Откуда
            </Label>
            <Popover open={openFrom} onOpenChange={setOpenFrom}>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  role="combobox"
                  aria-expanded={openFrom}
                  className="w-full justify-between"
                >
                  {fromCity ? fromCity.nameRu : "Выберите город отправления..."}
                  <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-[--radix-popover-trigger-width] p-0">
                <Command>
                  <CommandInput placeholder="Поиск города..." />
                  <CommandList>
                    <CommandEmpty>Город не найден.</CommandEmpty>
                    <CommandGroup>
                      {cities.map((city) => (
                        <CommandItem
                          key={city.id}
                          value={city.nameRu}
                          onSelect={() => {
                            setFromCity(city);
                            setOpenFrom(false);
                          }}
                        >
                          <Check
                            className={cn(
                              "mr-2 h-4 w-4",
                              fromCity?.id === city.id ? "opacity-100" : "opacity-0"
                            )}
                          />
                          {city.nameRu}
                        </CommandItem>
                      ))}
                    </CommandGroup>
                  </CommandList>
                </Command>
              </PopoverContent>
            </Popover>
          </div>

          {/* To City */}
          <div className="relative flex flex-col gap-2">
             <Label className="flex items-center gap-2">
              <MapPin className="w-4 h-4 text-emerald-500" />
              Куда
            </Label>
            <Popover open={openTo} onOpenChange={setOpenTo}>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  role="combobox"
                  aria-expanded={openTo}
                  className="w-full justify-between"
                >
                  {toCity ? toCity.nameRu : "Выберите город прибытия..."}
                  <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-[--radix-popover-trigger-width] p-0">
                <Command>
                  <CommandInput placeholder="Поиск города..." />
                  <CommandList>
                    <CommandEmpty>Город не найден.</CommandEmpty>
                    <CommandGroup>
                      {cities.map((city) => (
                        <CommandItem
                          key={city.id}
                          value={city.nameRu}
                          onSelect={() => {
                            setToCity(city);
                            setOpenTo(false);
                          }}
                        >
                          <Check
                            className={cn(
                              "mr-2 h-4 w-4",
                              toCity?.id === city.id ? "opacity-100" : "opacity-0"
                            )}
                          />
                          {city.nameRu}
                        </CommandItem>
                      ))}
                    </CommandGroup>
                  </CommandList>
                </Command>
              </PopoverContent>
            </Popover>
          </div>

          {/* Date */}
          <div>
            <Label htmlFor="date" className="flex items-center gap-2 mb-2">
              <Calendar className="w-4 h-4 text-blue-600" />
              Дата поездки
            </Label>
            <Input
              id="date"
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              className="w-full"
            />
          </div>

          {/* Search Button */}
            <Button 
            className="w-full h-12 bg-blue-600 hover:bg-blue-700 text-black"
            onClick={handleSearch}
            disabled={!fromCity || !toCity || !date}
          >
            <Search className="w-5 h-5 mr-2" />
            Найти рейсы
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
