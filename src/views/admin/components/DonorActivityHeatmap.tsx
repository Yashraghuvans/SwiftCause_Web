import React from 'react';
import { Card, CardContent } from '../../../shared/ui/card';
import { Skeleton } from '../../../shared/ui/skeleton';

interface HeatmapData {
  day: string;
  hour: number;
  value: number;
  donations: number;
}

interface DonorActivityHeatmapProps {
  data?: HeatmapData[];
  loading?: boolean;
  className?: string;
}

// Enterprise-grade legend colors - softer, more muted
const LEGEND_COLORS = [
  'bg-gray-50 border-gray-100',
  'bg-emerald-50 border-emerald-100',
  'bg-emerald-100 border-emerald-200',
  'bg-emerald-300 border-emerald-400',
  'bg-emerald-500 border-emerald-600',
];

const getCellClass = (intensity: number): string => {
  if (intensity === 0)
    return 'bg-gray-50 border border-gray-100 hover:bg-gray-100 transition-colors';
  if (intensity <= 0.25)
    return 'bg-emerald-50 border border-emerald-100 hover:bg-emerald-100 transition-colors';
  if (intensity <= 0.5)
    return 'bg-emerald-100 border border-emerald-200 hover:bg-emerald-200 transition-colors';
  if (intensity <= 0.75)
    return 'bg-emerald-300 border border-emerald-400 hover:bg-emerald-400 transition-colors';
  return 'bg-emerald-500 border border-emerald-600 hover:bg-emerald-600 transition-colors';
};

export const DonorActivityHeatmap: React.FC<DonorActivityHeatmapProps> = ({
  data = [],
  loading = false,
  className = '',
}) => {
  // Fixed grid dimensions - 7 days × 24 hours
  const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const hours = Array.from({ length: 24 }, (_, i) => i);

  if (loading) {
    return (
      <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm ${className}`}>
        <CardContent className="p-6">
          <div className="space-y-4">
            <div>
              <Skeleton className="h-6 w-48 mb-2" />
              <Skeleton className="h-4 w-64" />
            </div>
            <div className="h-40 w-full rounded-lg overflow-hidden border border-slate-100 bg-slate-50/80 p-1">
              <div className="grid h-full grid-rows-7 gap-0.5">
                {Array.from({ length: 7 }).map((_, row) => (
                  <div key={row} className="grid grid-cols-8 gap-0.5 min-h-0">
                    {Array.from({ length: 8 }).map((_, col) => (
                      <Skeleton
                        key={`${row}-${col}`}
                        className="h-full min-h-[6px] w-full rounded-[1px]"
                      />
                    ))}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    );
  }

  // Pre-initialize the complete 7×24 grid with zeros
  const initializeGrid = (): Map<string, { count: number; intensity: number }> => {
    const grid = new Map<string, { count: number; intensity: number }>();

    days.forEach((day) => {
      hours.forEach((hour) => {
        const key = `${day}-${hour}`;
        grid.set(key, { count: 0, intensity: 0 });
      });
    });

    return grid;
  };

  // Process incoming data and populate grid
  const processGridData = (): Map<string, { count: number; intensity: number }> => {
    const grid = initializeGrid();

    // Find max count for normalization
    let maxCount = 0;
    data.forEach((item) => {
      if (item.donations > maxCount) {
        maxCount = item.donations;
      }
    });

    // Populate grid with actual data
    data.forEach((item) => {
      const key = `${item.day}-${item.hour}`;
      if (grid.has(key)) {
        const count = item.donations || 0;
        const intensity = maxCount > 0 ? count / maxCount : 0;
        grid.set(key, { count, intensity });
      }
    });

    return grid;
  };

  const gridData = processGridData();

  const getCellData = (day: string, hour: number): { count: number; intensity: number } => {
    const key = `${day}-${hour}`;
    return gridData.get(key) || { count: 0, intensity: 0 };
  };

  return (
    <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm ${className}`}>
      <CardContent className="p-3 sm:p-6">
        <div className="space-y-3 sm:space-y-5">
          {/* Header */}
          <div>
            <h3 className="text-base sm:text-lg font-semibold text-gray-900 mb-1">
              Donor Activity Heatmap
            </h3>
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2">
              <p className="text-xs sm:text-sm text-gray-600 leading-relaxed">
                Weekly engagement patterns by hour and day
              </p>

              {/* Enterprise Legend - Aligned right on desktop, below on mobile */}
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <span className="text-xs font-medium text-gray-400">LOW</span>
                <div className="flex gap-1">
                  {LEGEND_COLORS.map((color, index) => (
                    <div key={index} className={`w-3 h-3 rounded-[2px] ${color}`} />
                  ))}
                </div>
                <span className="text-xs font-medium text-gray-400">HIGH</span>
              </div>
            </div>
          </div>

          {/* Heatmap Container - Responsive with horizontal scroll on mobile */}
          <div className="relative overflow-x-auto -mx-3 px-3 sm:mx-0 sm:px-0">
            <div className="inline-block min-w-full">
              {/* Hour labels - Responsive sizing */}
              <div className="flex mb-2 sm:mb-3">
                <div className="w-8 sm:w-12 flex-shrink-0"></div>
                <div className="flex-1">
                  <div className="grid grid-cols-24 gap-[3px] sm:gap-[6px]">
                    {hours.map((hour) => (
                      <div
                        key={hour}
                        className="text-[9px] sm:text-[10px] text-gray-400 text-center font-medium"
                      >
                        {hour % 4 === 0 ? `${hour.toString().padStart(2, '0')}` : ''}
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Responsive Grid: 7 rows × 24 columns */}
              {days.map((day) => (
                <div key={day} className="flex items-center mb-1">
                  {/* Day label - Responsive width */}
                  <div className="w-8 sm:w-12 flex-shrink-0 text-[10px] sm:text-xs font-medium text-gray-500">
                    {day.toUpperCase()}
                  </div>

                  {/* Hour cells - Responsive grid */}
                  <div className="flex-1">
                    <div className="grid grid-cols-24 gap-[3px] sm:gap-[6px]">
                      {hours.map((hour) => {
                        const cellData = getCellData(day, hour);
                        const { count, intensity } = cellData;

                        return (
                          <div
                            key={`${day}-${hour}`}
                            className={`rounded-[2px] cursor-pointer aspect-square ${getCellClass(intensity)}`}
                            title={
                              count > 0
                                ? `${day} ${hour}:00–${hour + 1}:00 — ${count} donations`
                                : `${day} ${hour}:00–${hour + 1}:00 — No activity`
                            }
                          />
                        );
                      })}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
