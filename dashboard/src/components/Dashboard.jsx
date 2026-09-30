import React from 'react';
import { CheckCircle, Clock, AlertCircle, TrendingUp, Users, Activity } from 'lucide-react';
import StatCard from './StatCard';

export default function Dashboard({ stats }) {
  const cards = [
    {
      title: 'Total Jobs',
      value: stats.total.toLocaleString(),
      icon: Activity,
      color: 'bg-blue-50 border-blue-200',
      textColor: 'text-blue-700',
      iconColor: 'text-blue-500',
      change: '+145 today'
    },
    {
      title: 'Success Rate',
      value: `${stats.successRate.toFixed(1)}%`,
      icon: CheckCircle,
      color: 'bg-green-50 border-green-200',
      textColor: 'text-green-700',
      iconColor: 'text-green-500',
      change: '↑ 2.3% vs yesterday'
    },
    {
      title: 'Processing',
      value: stats.processing,
      icon: Clock,
      color: 'bg-yellow-50 border-yellow-200',
      textColor: 'text-yellow-700',
      iconColor: 'text-yellow-500',
      change: `${stats.queueSize} in queue`
    },
    {
      title: 'Failed Jobs',
      value: stats.failed,
      icon: AlertCircle,
      color: 'bg-red-50 border-red-200',
      textColor: 'text-red-700',
      iconColor: 'text-red-500',
      change: stats.deadLetterCount > 0 ? `${stats.deadLetterCount} moved to DLQ` : 'None in DLQ'
    },
    {
      title: 'Avg Processing Time',
      value: `${stats.avgProcessingTime}s`,
      icon: TrendingUp,
      color: 'bg-purple-50 border-purple-200',
      textColor: 'text-purple-700',
      iconColor: 'text-purple-500',
      change: '↓ 0.3s vs average'
    },
    {
      title: 'Active Workers',
      value: stats.workersActive,
      icon: Users,
      color: 'bg-indigo-50 border-indigo-200',
      textColor: 'text-indigo-700',
      iconColor: 'text-indigo-500',
      change: 'All healthy'
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
                <span className="text-black font-semibold">{stats.completed}</span>
                <span className="text-gray-500 text-sm">({((stats.completed/stats.total)*100).toFixed(1)}%)</span>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
                <span className="text-gray-700">Processing</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{stats.processing}</span>
                <span className="text-gray-500 text-sm">({((stats.processing/stats.total)*100).toFixed(1)}%)</span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-blue-500"></div>
                <span className="text-gray-700">Pending</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{stats.pending}</span>
                <span className="text-gray-500 text-sm">({((stats.pending/stats.total)*100).toFixed(1)}%)</span>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-red-500"></div>
                <span className="text-gray-700">Failed</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-black font-semibold">{stats.failed}</span>
                <span className="text-gray-500 text-sm">({((stats.failed/stats.total)*100).toFixed(1)}%)</span>
              </div>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mt-6 space-y-2">
            <div className="text-xs text-gray-600">Overall Progress</div>
            <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
              <div 
                className="bg-gradient-to-r from-green-500 to-green-400 h-full"
                style={{ width: `${stats.successRate}%` }}
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
                <div className="w-2 h-2 rounded-full bg-green-500"></div>
                <span className="text-green-600 text-sm font-semibold">Healthy</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Redis Connection</span>
              <span className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-green-500"></div>
                <span className="text-green-600 text-sm font-semibold">Healthy</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Email Service</span>
              <span className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-green-500"></div>
                <span className="text-green-600 text-sm font-semibold">Operational</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Worker Pool</span>
              <span className="flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-green-500"></div>
                <span className="text-green-600 text-sm font-semibold">5/5 Active</span>
              </span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">CPU Usage</span>
              <span className="text-blue-600 text-sm font-semibold">23%</span>
            </div>

            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
              <span className="text-gray-700">Memory Usage</span>
              <span className="text-blue-600 text-sm font-semibold">1.2 GB / 2 GB</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Activity Alert */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div className="flex gap-4">
          <div className="flex-shrink-0">
            <Activity className="text-blue-600" size={24} />
          </div>
          <div>
            <h3 className="font-semibold text-black mb-2">System Status</h3>
            <p className="text-gray-700 text-sm">
              ✓ All systems operational. Job processing running smoothly with a {stats.successRate.toFixed(1)}% success rate. 
              Last maintenance: 2 hours ago.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
