import React from 'react';
import { Card, CardContent } from '../../../shared/ui/card';
import { Skeleton } from '../../../shared/ui/skeleton';
import { LucideIcon } from 'lucide-react';

interface KpiCardProps {
  title: string;
  value: string | number;
  icon: LucideIcon;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  loading?: boolean;
  className?: string;
  isPrimary?: boolean; // New prop to make Total Raised visually dominant
}

export const KpiCard: React.FC<KpiCardProps> = ({
  title,
  value,
  icon: Icon,
  trend,
  loading = false,
  className = '',
  isPrimary = false,
}) => {
  if (loading) {
    return (
      <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm ${className}`}>
        <CardContent className="p-6">
          <div className="flex items-center justify-between">
            <div className="flex-1">
              <Skeleton className="h-3 w-28 mb-3" />
              <Skeleton className="h-7 w-24 max-w-[90%]" />
            </div>
            <Skeleton className="w-10 h-10 rounded-lg" />
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={`bg-white rounded-xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow duration-200 ${className}`}>
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div className="flex-1">
            <p className="text-xs font-medium uppercase tracking-wider text-gray-500 mb-3">{title}</p>
            <div className="flex items-baseline gap-2">
              <p className={`font-semibold leading-tight ${
                isPrimary 
                  ? 'text-2xl text-gray-900' 
                  : 'text-xl text-gray-800'
              }`}>
                {value}
              </p>
              {trend && (
                <span className={`text-xs font-medium ${
                  trend.isPositive ? 'text-emerald-600' : 'text-red-600'
                }`}>
                  {trend.isPositive ? '+' : ''}{trend.value}%
                </span>
              )}
            </div>
          </div>
          <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${
            isPrimary 
              ? 'bg-emerald-50 text-emerald-600' 
              : 'bg-gray-50 text-gray-500'
          }`}>
            <Icon className="w-5 h-5" />
          </div>
        </div>
      </CardContent>
    </Card>
  );
};