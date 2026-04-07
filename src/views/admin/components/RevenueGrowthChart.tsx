import React from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../../../shared/ui/card';
import { Skeleton } from '../../../shared/ui/skeleton';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip } from 'recharts';
import { TrendingUp } from 'lucide-react';
import { formatCurrency as formatGbp } from '../../../shared/lib/currencyFormatter';

interface RevenueDataPoint {
  month: string;
  donationRevenue: number;
  totalRevenue: number;
}

interface RevenueGrowthChartProps {
  data?: RevenueDataPoint[];
  loading?: boolean;
  formatCurrency?: (amount: number) => string;
  className?: string;
}

const CustomTooltip = ({ active, payload, label, formatCurrency }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-white p-3 border border-gray-200 shadow-lg rounded-lg text-sm">
        <p className="font-semibold text-gray-900 mb-2">{label}</p>
        {payload.map((entry: any, index: number) => (
          <div key={index} className="flex items-center justify-between gap-4 mb-1 last:mb-0">
            <div className="flex items-center gap-2">
              <div 
                className="w-3 h-3 rounded-full" 
                style={{ backgroundColor: entry.color }}
              />
              <span className="text-gray-600">{entry.name}:</span>
            </div>
            <span className="font-semibold text-gray-900">
              {formatCurrency(entry.value)}
            </span>
          </div>
        ))}
      </div>
    );
  }
  return null;
};

export const RevenueGrowthChart: React.FC<RevenueGrowthChartProps> = ({
  data = [],
  loading = false,
  formatCurrency = formatGbp,
  className = '',
}) => {
  if (loading) {
    return (
      <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm ${className}`}>
        <CardHeader className="p-6 pb-4">
          <div className="flex items-center justify-between">
            <div>
              <Skeleton className="h-6 w-40 mb-2" />
              <Skeleton className="h-4 w-60" />
            </div>
          </div>
        </CardHeader>
        <CardContent className="p-6 pt-2">
          <div
            className="h-64 flex items-end justify-center gap-1.5 px-2 transition-opacity duration-300"
            aria-hidden
          >
            {[72, 110, 88, 140, 96, 160, 78, 130, 102, 148, 85, 124].map((h, i) => (
              <Skeleton
                key={i}
                className="min-w-0 flex-1 max-w-[10%] rounded-t-sm"
                style={{ height: h }}
              />
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm ${className}`}>
      <CardHeader className="p-6 pb-4 border-b border-gray-50">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg font-semibold text-gray-900 flex items-center">
              <div className="w-8 h-8 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center mr-3">
                <TrendingUp className="w-4 h-4" />
              </div>
              Revenue Growth
            </CardTitle>
            <CardDescription className="text-sm text-gray-500 mt-1 ml-11">
              Monthly revenue trends including Gift Aid uplift
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="p-3 sm:p-6 pt-3 sm:pt-5">
        {data.length > 0 ? (
          <>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={data} margin={{ top: 10, right: 5, left: 10, bottom: 0 }}>
                <defs>
                  <linearGradient id="donationRevenueGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#1E3A8A" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#1E3A8A" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="totalRevenueGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#166534" stopOpacity={0.15}/>
                    <stop offset="95%" stopColor="#166534" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#F9FAFB" />
                <XAxis 
                  dataKey="month" 
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: '#9CA3AF', fontSize: 11 }}
                  interval="preserveStartEnd"
                />
                <YAxis 
                  tickFormatter={(value) => {
                    const valueInGbp = value / 100;
                    // Compact format for mobile
                    if (typeof window !== 'undefined' && window.innerWidth < 640) {
                      if (valueInGbp >= 1000000) return `£${(valueInGbp / 1000000).toFixed(1)}M`;
                      if (valueInGbp >= 1000) return `£${(valueInGbp / 1000).toFixed(0)}K`;
                      return formatCurrency(value);
                    }
                    return formatCurrency(value);
                  }}
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: '#9CA3AF', fontSize: 10 }}
                  width={55}
                />
                <Tooltip content={<CustomTooltip formatCurrency={formatCurrency} />} />
                <Area
                  type="monotone"
                  dataKey="donationRevenue"
                  stroke="#1E3A8A"
                  strokeWidth={2}
                  strokeDasharray="5 5"
                  fill="url(#donationRevenueGradient)"
                  name="Donations"
                />
                <Area
                  type="monotone"
                  dataKey="totalRevenue"
                  stroke="#166534"
                  strokeWidth={3}
                  fill="url(#totalRevenueGradient)"
                  name="Total Revenue"
                />
              </AreaChart>
            </ResponsiveContainer>
            
            {/* Refined Legend */}
            <div className="flex items-center justify-center gap-4 sm:gap-8 mt-4 sm:mt-6 pt-3 sm:pt-4 border-t border-gray-50">
              <div className="flex items-center gap-2">
                <div className="w-3 h-0.5 bg-blue-800" style={{ borderStyle: 'dashed', borderWidth: '1px 0' }}></div>
                <span className="text-xs sm:text-sm text-gray-600 font-medium">Donations</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-0.5 bg-green-800"></div>
                <span className="text-xs sm:text-sm text-gray-900 font-medium">Total Revenue</span>
              </div>
            </div>
          </>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <TrendingUp className="mx-auto h-12 w-12 text-gray-300 mb-4" />
            <p className="text-lg font-medium mb-2 text-gray-700">No Revenue Data</p>
            <p className="text-sm text-gray-500">Revenue data will appear here once donations are received.</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
