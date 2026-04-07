import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/ui/card';
import { Skeleton } from '../../../shared/ui/skeleton';
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip, Legend } from 'recharts';
import { PieChart as PieChartIcon, ArrowUpRight } from 'lucide-react';
import { Button } from '../../../shared/ui/button';

interface CategoryData {
  name: string;
  value: number;
  percentage: number;
  color: string;
}
// NOTE: If CategoryData.value represents amounts in pence (not counts),
// switch displays to formatCurrency(value) for GBP formatting.

interface DonationDistributionDonutProps {
  data?: CategoryData[];
  loading?: boolean;
  onViewDetails?: () => void;
  className?: string;
}

// Category-specific color palette for professional donation distribution
const MUTED_COLORS = [
  "#2F6B4F", // Forest Green - Environment
  "#5B7C99", // Muted Blue - Education
  "#3A7F7A", // Muted Teal - Health
  "#D9B36A", // Muted Amber - Crisis Relief
  "#8FCFB3", // Soft Mint - Welfare/Social
  "#B6B8BC", // Warm Gray - Uncategorized/Other
  "#2F6B4F", // Forest Green (repeat)
  "#5B7C99", // Muted Blue (repeat)
];

const CustomTooltip = ({ active, payload }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className="bg-white p-3 border border-gray-100 shadow-lg rounded-lg text-sm">
        <div className="flex items-center gap-2 mb-1">
          <div 
            className="w-3 h-3 rounded-full" 
            style={{ backgroundColor: data.color }}
          />
          <span className="font-semibold text-gray-900">{data.name}</span>
        </div>
        <p className="text-gray-600">
          {data.value} donations ({data.percentage}%)
        </p>
      </div>
    );
  }
  return null;
};

const CustomLegend = ({ payload }: any) => {
  return (
    <div className="flex flex-wrap justify-center gap-4 mt-4">
      {payload.map((entry: any, index: number) => (
        <div key={index} className="flex items-center gap-2">
          <div 
            className="w-3 h-3 rounded-full" 
            style={{ backgroundColor: entry.color }}
          />
          <span className="text-sm text-gray-600">{entry.value}</span>
        </div>
      ))}
    </div>
  );
};

export const DonationDistributionDonut: React.FC<DonationDistributionDonutProps> = ({
  data = [],
  loading = false,
  onViewDetails,
  className = '',
}) => {
  const [activeIndex, setActiveIndex] = useState<number | null>(null);

  // Ensure data uses muted colors
  const mutedData = data.map((item, index) => ({
    ...item,
    color: item.color ?? MUTED_COLORS[index % MUTED_COLORS.length]
  }));

  // Calculate most popular category by highest value
  const mostPopular = mutedData.length > 0 ? [...mutedData].sort((a, b) => b.value - a.value)[0] : null;

  if (loading) {
    return (
      <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm ${className}`}>
        <CardHeader className="p-6 border-b border-gray-50">
          <div className="flex items-center justify-between">
            <div>
              <Skeleton className="h-6 w-48 mb-2" />
              <Skeleton className="h-4 w-64" />
            </div>
            <Skeleton className="h-8 w-24" />
          </div>
        </CardHeader>
        <CardContent className="p-6 flex flex-col items-center transition-opacity duration-300">
          <Skeleton className="h-52 w-52 max-w-[min(100%,13rem)] rounded-full flex-shrink-0" />
          <div className="mt-6 flex flex-wrap justify-center gap-x-4 gap-y-2 w-full">
            {[0, 1, 2].map((i) => (
              <div key={i} className="flex items-center gap-2">
                <Skeleton className="h-3 w-3 rounded-full flex-shrink-0" />
                <Skeleton className={`h-3 rounded ${i === 0 ? "w-24" : i === 1 ? "w-20" : "w-16"}`} />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm flex flex-col ${className}`}>
      <CardHeader className="p-6 border-b border-gray-50 flex-shrink-0">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg font-semibold text-gray-900 flex items-center">
              <div className="w-8 h-8 rounded-lg bg-slate-100 text-slate-600 flex items-center justify-center mr-3">
                <PieChartIcon className="w-4 h-4" />
              </div>
              Donation Distribution
            </CardTitle>
            <CardDescription className="text-sm text-gray-500 mt-1 ml-11">
              Distribution by campaign category
            </CardDescription>
          </div>
          {onViewDetails && (
            <Button
              variant="ghost"
              size="sm"
              onClick={onViewDetails}
              className="text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium"
            >
              <ArrowUpRight className="w-4 h-4 mr-1" />
              Details
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="p-6 flex-1 flex flex-col">{mutedData.length > 0 ? (
          <div className="space-y-6 flex-1 flex flex-col">
            {/* Metrics Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 flex-shrink-0">
              {/* Total Donations */}
              <div className="bg-slate-50 rounded-lg p-3 border border-slate-200">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-6 h-6 rounded-lg bg-slate-500 flex items-center justify-center">
                    <span className="text-white font-bold text-xs">£</span>
                  </div>
                  <span className="text-xs font-medium text-slate-700">Total<br/>Donations</span>
                </div>
                <div className="text-xl font-bold text-slate-900 mb-1">
                  {mutedData.reduce((sum, item) => sum + item.value, 0)}
                </div>
                <div className="text-xs text-slate-600">
                  donations received
                </div>
              </div>

              {/* Most Popular */}
              <div className="bg-stone-50 rounded-lg p-3 border border-stone-200">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-6 h-6 rounded-lg bg-stone-500 flex items-center justify-center">
                    <span className="text-white font-bold text-xs">👤</span>
                  </div>
                  <span className="text-xs font-medium text-stone-700">Most<br/>Popular</span>
                </div>
                <div className="text-base font-bold text-stone-900 mb-1 truncate">
                  {mostPopular ? mostPopular.name.toLowerCase() : 'N/A'}
                </div>
                <div className="text-xs text-stone-600">
                  {mostPopular ? `${mostPopular.value} donations (${mostPopular.percentage}%)` : '0 donations'}
                </div>
              </div>

              {/* Category Count */}
              <div className="bg-gray-50 rounded-lg p-3 border border-gray-200">
                <div className="flex items-center gap-2 mb-2">
                  <div className="w-6 h-6 rounded-lg bg-gray-500 flex items-center justify-center">
                    <span className="text-white font-bold text-xs">📈</span>
                  </div>
                  <span className="text-xs font-medium text-gray-700">Active<br/>Categories</span>
                </div>
                <div className="text-base font-bold text-gray-900 mb-1">
                  {mutedData.length}
                </div>
                <div className="text-xs text-gray-600">
                  categories
                </div>
              </div>
            </div>

            {/* Chart */}
            <div className="relative flex-1 min-h-[280px] flex items-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={mutedData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={2}
                    dataKey="value"
                    onMouseEnter={(_, index) => setActiveIndex(index)}
                    onMouseLeave={() => setActiveIndex(null)}
                  >
                    {mutedData.map((entry, index) => (
                      <Cell 
                        key={`cell-${index}`} 
                        fill={entry.color}
                        stroke={activeIndex === index ? '#fff' : 'none'}
                        strokeWidth={activeIndex === index ? 2 : 0}
                        style={{
                          filter: activeIndex === index ? 'brightness(1.1)' : 'none',
                          cursor: 'pointer'
                        }}
                      />
                    ))}
                  </Pie>
                  <Tooltip content={<CustomTooltip />} />
                  <Legend content={<CustomLegend />} />
                </PieChart>
              </ResponsiveContainer>
              
              {/* Center text */}
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="text-center">
                  <p className="text-2xl font-bold text-gray-900">
                    {mutedData.reduce((sum, item) => sum + item.value, 0)}
                  </p>
                  <p className="text-sm text-gray-600">Total</p>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500 flex-1 flex flex-col justify-center">
            <PieChartIcon className="mx-auto h-12 w-12 text-gray-400 mb-3" />
            <p className="text-lg font-medium mb-2">No Distribution Data</p>
            <p className="text-sm">Category distribution will appear here once donations are received.</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
