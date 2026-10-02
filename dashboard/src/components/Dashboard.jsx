import React from 'react';
import { CheckCircle, Clock, AlertCircle, TrendingUp, Users, Activity, Check } from 'lucide-react';
import StatCard from './StatCard';

export default function Dashboard({ stats = {} }) {
  const total = stats.total || 0;
  const completed = stats.completed || 0;
  const processing = stats.processing || 0;
  const pending = stats.pending || 0;
  const failed = stats.failed || 0;
  const deadLetterCount = stats.deadLetterCount || 0;
  const successRate = typeof stats.successRate === 'number' ? stats.successRate.toFixed(1) : '0.0';
  const avgProcessingTime = typeof stats.avgProcessingTime === 'number' ? stats.avgProcessingTime.toFixed(2) : '0.00';
  const workersActive = stats.workersActive ?? 0;
  const queueSize = stats.queueSize ?? (pending + processing);

  const getPercentage = (val) => {
    if (!total || total === 0) return '0.0';
    return ((val / total) * 100).toFixed(1);
  };

  const getStatusBadge = (statusStr) => {
    const s = (statusStr || '').toUpperCase();
    if (s === 'HEALTHY' || s === 'OPERATIONAL' || s === 'UP') {
      return {
        bg: 'bg-green-500',
        text: 'text-green-600',
        label: statusStr || 'Healthy'
      };
    }
    if (s === 'DEGRADED') {
      return {
        bg: 'bg-yellow-500',
        text: 'text-yellow-600',
        label: 'Degraded'
      };
    }
    return {
      bg: 'bg-red-500',
      text: 'text-red-600',
      label: statusStr || 'Down'
    };
  };

  const dbStatus = getStatusBadge(stats.databaseStatus || 'HEALTHY');
  const redisStatus = getStatusBadge(stats.redisStatus || 'HEALTHY');
  const emailStatus = getStatusBadge(stats.emailServiceStatus || 'OPERATIONAL');

  const cards = [
    {
      title: 'Total Jobs',
      value: total.toLocaleString(),
      icon: Activity,
      color: 'bg-blue-50 border-blue-200',
      textColor: 'text-blue-700',
      iconColor: 'text-blue-500',
      change: 'Real-time database sync'
    },
    {
      title: 'Success Rate',
      value: `${successRate}%`,
      icon: CheckCircle,
      color: 'bg-green-50 border-green-200',
      textColor: 'text-green-700',
      iconColor: 'text-green-500',
      change: `${completed} completed of ${total}`
    },
    {
      title: 'Processing',
      value: processing,
      icon: Clock,
      color: 'bg-yellow-50 border-yellow-200',
      textColor: 'text-yellow-700',
      iconColor: 'text-yellow-500',
      change: `${queueSize} in active queue`
    },
    {
      title: 'Failed Jobs',
      value: failed,
      icon: AlertCircle,
      color: 'bg-red-50 border-red-200',
      textColor: 'text-red-700',
      iconColor: 'text-red-500',
      change: deadLetterCount > 0 ? `${deadLetterCount} moved to DLQ` : 'None in DLQ'
    },
    {
      title: 'Avg Processing Time',
      value: `${avgProcessingTime}s`,
      icon: TrendingUp,
      color: 'bg-purple-50 border-purple-200',
      textColor: 'text-purple-700',
      iconColor: 'text-purple-500',
      change: 'Calculated from completed jobs'
    },
    {
      title: 'Active Workers',
      value: workersActive,
      icon: Users,
      color: 'bg-indigo-50 border-indigo-200',
      textColor: 'text-indigo-700',
      iconColor: 'text-indigo-500',
      change: 'Worker pool active'
    },
  ];

  return (
    <div className="space-y-8">
      {/* Key Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {cards.map((card, index) => (
          <StatCard key={index} {...card} />
        ))}
      </div>

      {/* Quick Stats Summary */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Job Status Breakdown */}
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-black mb-6">Job Status Breakdown</h3>
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-green-500"></div>
                <span className="text-gray-700">Completed</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{completed}</span>
                <span className="text-gray-500 text-sm">({getPercentage(completed)}%)</span>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                <span className="text-gray-700">Processing</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{processing}</span>
                <span className="text-gray-500 text-sm">({getPercentage(processing)}%)</span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-blue-500"></div>
                <span className="text-gray-700">Pending</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{pending}</span>
                <span className="text-gray-500 text-sm">({getPercentage(pending)}%)</span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-red-500"></div>
                <span className="text-gray-700">Failed</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{failed}</span>
                <span className="text-gray-500 text-sm">({getPercentage(failed)}%)</span>
              </div>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mt-6 space-y-2">
            <div className="text-xs text-gray-600">Overall Success Rate</div>
            <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
              <div 
                className="bg-gradient-to-r from-green-500 to-green-400 h-full transition-all duration-500"
                style={{ width: `${Math.min(100, Math.max(0, parseFloat(successRate)))}%` }}
              ></div>
            </div>
          </div>
        </div>

        {/* System Health */}
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-black mb-6">System Health</h3>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Database Connection</span>
              <span className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${dbStatus.bg}`}></div>
                <span className={`${dbStatus.text} text-sm font-semibold`}>{dbStatus.label}</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Redis Connection</span>
              <span className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${redisStatus.bg}`}></div>
                <span className={`${redisStatus.text} text-sm font-semibold`}>{redisStatus.label}</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Email Service</span>
              <span className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${emailStatus.bg}`}></div>
                <span className={`${emailStatus.text} text-sm font-semibold`}>{emailStatus.label}</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Worker Pool</span>
              <span className="flex items-center gap-2">
                <div className={`w-2 h-2 rounded-full ${workersActive > 0 ? 'bg-green-500' : 'bg-yellow-500'}`}></div>
                <span className={`${workersActive > 0 ? 'text-green-600' : 'text-yellow-600'} text-sm font-semibold`}>
                  {workersActive} Active
                </span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Queue Backlog</span>
              <span className="text-blue-600 text-sm font-semibold">{queueSize} jobs</span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Dead Letter Backlog</span>
              <span className={`${deadLetterCount > 0 ? 'text-red-600' : 'text-gray-600'} text-sm font-semibold`}>
                {deadLetterCount} jobs
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* System Status Alert */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div className="flex gap-4">
          <div className="flex-shrink-0">
            <Activity className="text-blue-600" size={24} />
          </div>
          <div>
            <h3 className="font-semibold text-black mb-2">System Status</h3>
            <p className="text-gray-700 text-sm">
              All notification processing services connected. Success rate is {successRate}% across {total.toLocaleString()} total jobs.
              {stats.mostCommonError && (
                <span className="block mt-1 text-red-600 text-xs">
                  Most frequent error: {stats.mostCommonError}
                </span>
              )}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
