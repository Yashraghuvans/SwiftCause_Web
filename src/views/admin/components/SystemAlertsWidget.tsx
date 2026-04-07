import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../../../shared/ui/card';
import { AlertCircle, CheckCircle, Info, AlertTriangle, ArrowRight, Shield } from 'lucide-react';
import { SystemAlert, AlertSeverity } from '../../../shared/lib/hooks/useSystemAlerts';
import { Screen } from '../../../shared/types';
import { Skeleton } from '../../../shared/ui/skeleton';

interface SystemAlertsWidgetProps {
  alerts: SystemAlert[];
  loading: boolean;
  onNavigate: (screen: Screen) => void;
}

export function SystemAlertsWidget({ alerts, loading, onNavigate }: SystemAlertsWidgetProps) {
  const getSeverityConfig = (severity: AlertSeverity) => {
    switch (severity) {
      case 'critical':
        return {
          icon: AlertCircle,
          iconColor: 'text-red-600',
          bgColor: 'bg-red-50/50',
          borderColor: 'border-l-4 border-red-500',
        };
      case 'warning':
        return {
          icon: AlertTriangle,
          iconColor: 'text-yellow-600',
          bgColor: 'bg-yellow-50/50',
          borderColor: 'border-l-4 border-yellow-500',
        };
      case 'info':
        return {
          icon: Info,
          iconColor: 'text-blue-600',
          bgColor: 'bg-blue-50/50',
          borderColor: 'border-l-4 border-blue-500',
        };
      case 'success':
        return {
          icon: CheckCircle,
          iconColor: 'text-green-600',
          bgColor: 'bg-green-50/50',
          borderColor: 'border-l-4 border-green-500',
        };
      default:
        return {
          icon: Info,
          iconColor: 'text-gray-600',
          bgColor: 'bg-gray-50/50',
          borderColor: 'border-l-4 border-gray-500',
        };
    }
  };

  const handleAlertClick = (alert: SystemAlert) => {
    if (alert.actionScreen) {
      onNavigate(alert.actionScreen as Screen);
    }
  };

  if (loading) {
    return (
      <Card className="bg-white rounded-xl border border-gray-200 shadow-sm">
        <CardHeader className="p-6 border-b border-gray-100">
          <CardTitle className="text-lg font-bold text-gray-900 flex items-center">
            <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center mr-3">
              <Shield className="w-4 h-4" />
            </div>
            System Alerts
          </CardTitle>
        </CardHeader>
        <CardContent className="p-6">
          <div className="space-y-3">
            {[0, 1].map((i) => (
              <div
                key={i}
                className="flex items-start gap-3 rounded-lg border border-slate-100 bg-slate-50/60 p-4 transition-opacity duration-300"
              >
                <Skeleton className="h-9 w-9 rounded-lg flex-shrink-0" />
                <div className="min-w-0 flex-1 space-y-2">
                  <Skeleton className={`h-4 ${i === 0 ? "w-3/4" : "w-2/3"}`} />
                  <Skeleton className={`h-3 ${i === 0 ? "w-full" : "w-4/5"}`} />
                  <Skeleton className="h-2.5 w-1/4" />
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  // Group alerts by severity for better organization
  const criticalAlerts = alerts.filter((a) => a.severity === 'critical');
  const warningAlerts = alerts.filter((a) => a.severity === 'warning');
  const infoAlerts = alerts.filter((a) => a.severity === 'info');
  const successAlerts = alerts.filter((a) => a.severity === 'success');
  const sortedAlerts = [...criticalAlerts, ...warningAlerts, ...infoAlerts, ...successAlerts];

  return (
    <Card className="bg-white rounded-xl border border-gray-200 shadow-sm">
      <CardHeader className="p-6 border-b border-gray-100">
        <div className="flex items-center justify-between">
          <CardTitle className="text-lg font-bold text-gray-900 flex items-center">
            <div className="w-8 h-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center mr-3">
              <Shield className="w-4 h-4" />
            </div>
            System Alerts
          </CardTitle>
          {alerts.length > 0 && (
            <button className="text-sm text-blue-600 hover:text-blue-500 font-medium transition-colors">
              View All
            </button>
          )}
        </div>
      </CardHeader>
      <CardContent className="p-0">
        {alerts.length === 0 ? (
          <div
            className="flex flex-col items-center justify-center px-4 text-center"
            style={{ minHeight: '320px' }}
          >
            <CheckCircle className="h-12 w-12 text-green-500 mb-3" />
            <p className="text-sm sm:text-base font-medium text-gray-900 mb-1">
              All systems operational
            </p>
            <p className="text-xs sm:text-sm text-gray-500">
              No issues detected. Your platform is running smoothly.
            </p>
          </div>
        ) : (
          <div className="max-h-[320px] overflow-y-auto">
            {sortedAlerts.map((alert, index) => {
              const config = getSeverityConfig(alert.severity);
              const Icon = config.icon;

              return (
                <div
                  key={alert.id}
                  className={`
                    ${config.borderColor} ${config.bgColor}
                    p-4 cursor-pointer transition-all hover:bg-opacity-80
                    ${index !== 0 ? 'border-t border-gray-100' : ''}
                  `}
                  onClick={() => handleAlertClick(alert)}
                >
                  <div className="flex items-start gap-3">
                    <div className={`flex-shrink-0 ${config.iconColor}`}>
                      <Icon className="h-5 w-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h4 className="text-sm font-semibold text-gray-900 mb-1">{alert.title}</h4>
                      <p className="text-sm text-gray-800 mb-1">{alert.message}</p>
                      <p className="text-xs text-gray-400 mt-1">
                        {alert.timestamp ? new Date(alert.timestamp).toLocaleString() : 'Just now'}
                      </p>
                      {alert.actionScreen && (
                        <button
                          className="mt-2 text-xs text-blue-600 hover:text-blue-500 font-medium inline-flex items-center transition-colors"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleAlertClick(alert);
                          }}
                        >
                          View Details
                          <ArrowRight className="h-3 w-3 ml-1" />
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
