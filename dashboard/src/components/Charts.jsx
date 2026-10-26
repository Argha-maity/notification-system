import React from 'react';
import { TrendingUp, BarChart2 } from 'lucide-react';

export default function Charts({ stats = {}, jobs = [] }) {
  const emailRate = typeof stats.emailSuccessRate === 'number' ? stats.emailSuccessRate.toFixed(1) : '100.0';
  const smsRate = typeof stats.smsSuccessRate === 'number' ? stats.smsSuccessRate.toFixed(1) : '100.0';
  const pushRate = typeof stats.pushSuccessRate === 'number' ? stats.pushSuccessRate.toFixed(1) : '100.0';
  const avgTime = typeof stats.avgProcessingTime === 'number' ? stats.avgProcessingTime.toFixed(2) : '0.00';
  const total = stats.total || jobs.length || 0;
  const completed = stats.completed || 0;
  const successRate = typeof stats.successRate === 'number' ? stats.successRate.toFixed(1) : '0.0';

  // Build hourly timeline from actual jobs if available
  const hourlyCounts = {};
  jobs.forEach(job => {
    if (job.createdAt) {
      const d = new Date(job.createdAt);
      const hourKey = `${String(d.getHours()).padStart(2, '0')}:00`;
      if (!hourlyCounts[hourKey]) {
        hourlyCounts[hourKey] = { completed: 0, failed: 0, processing: 0, pending: 0 };
      }
      const st = (job.status || '').toLowerCase();
      if (st === 'completed') hourlyCounts[hourKey].completed++;
      else if (st === 'failed' || st === 'dead_letter') hourlyCounts[hourKey].failed++;
      else if (st === 'processing') hourlyCounts[hourKey].processing++;
      else hourlyCounts[hourKey].pending++;
    }
  });

  const timelineEntries = Object.entries(hourlyCounts).slice(-6);
  const displayTimeline = timelineEntries.length > 0
    ? timelineEntries.map(([time, counts]) => ({
        time,
        completed: counts.completed,
        failed: counts.failed,
        processing: counts.processing,
        total: counts.completed + counts.failed + counts.processing + counts.pending
      }))
    : [
        { time: '10:00', completed: Math.round(completed * 0.2), failed: 0, processing: 0, total: Math.round(total * 0.2) },
        { time: '11:00', completed: Math.round(completed * 0.3), failed: 0, processing: 1, total: Math.round(total * 0.3) },
        { time: '12:00', completed: Math.round(completed * 0.5), failed: stats.failed || 0, processing: stats.processing || 0, total: total || 1 },
      ];

  const maxTimelineTotal = Math.max(1, ...displayTimeline.map(t => t.total || (t.completed + t.failed + t.processing)));

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-black mb-2">Analytics & Performance Metrics</h2>
        <p className="text-gray-600">Detailed insights into job processing performance</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-black mb-6">Job Timeline Activity</h3>
          <div className="space-y-4">
            {displayTimeline.map((item, idx) => {
              const itemTotal = item.total || (item.completed + item.failed + item.processing) || 1;
              const completedPercent = Math.min(100, (item.completed / itemTotal) * 100);
              const processingPercent = Math.min(100 - completedPercent, (item.processing / itemTotal) * 100);
              const widthRatio = Math.min(100, Math.max(15, (itemTotal / maxTimelineTotal) * 100));

              return (
                <div key={idx} className="flex justify-between items-center gap-4">
                  <span className="text-gray-700 text-sm font-mono w-14">{item.time}</span>
                  <div className="flex-1 bg-gray-100 h-5 rounded-full overflow-hidden flex" style={{ maxWidth: '300px' }}>
                    <div
                      className="bg-green-500 h-full"
                      style={{ width: `${(widthRatio * completedPercent) / 100}%` }}
                      title={`Completed: ${item.completed}`}
                    />
                    <div
                      className="bg-yellow-500 h-full"
                      style={{ width: `${(widthRatio * processingPercent) / 100}%` }}
                      title={`Processing: ${item.processing}`}
                    />
                    <div
                      className="bg-red-500 h-full"
                      style={{ width: `${item.failed > 0 ? 10 : 0}%` }}
                      title={`Failed: ${item.failed}`}
                    />
                  </div>
                  <span className="text-black font-semibold text-sm w-20 text-right">
                    {itemTotal} jobs
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-black mb-6">Channel Success Rates</h3>
          <div className="space-y-5">
            {[
              { channel: 'Email Channel', rate: emailRate },
              { channel: 'SMS Channel', rate: smsRate },
              { channel: 'Push Channel', rate: pushRate }
            ].map((item, idx) => (
              <div key={idx}>
                <div className="flex justify-between mb-2">
                  <span className="text-gray-700 font-medium text-sm">{item.channel}</span>
                  <span className="text-black font-semibold text-sm">{item.rate}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                  <div
                    className="bg-green-500 h-full rounded-full transition-all duration-500"
                    style={{ width: `${Math.min(100, Math.max(0, parseFloat(item.rate)))}%` }}
                  ></div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
        <div className="flex gap-3">
          <TrendingUp className="text-blue-600 flex-shrink-0" size={24} />
          <div>
            <h3 className="font-semibold text-black mb-2">Performance Insights</h3>
            <ul className="space-y-1.5 text-gray-700 text-sm">
              <li>• System-wide success rate is currently at <strong className="text-black">{successRate}%</strong> across {total.toLocaleString()} total jobs</li>
              <li>• Average job processing duration is <strong className="text-black">{avgTime}s</strong></li>
              <li>• Email channel success rate is <strong className="text-black">{emailRate}%</strong></li>
              <li>• Average retry count per job: <strong className="text-black">{stats.avgRetryCount != null ? Number(stats.avgRetryCount).toFixed(2) : '0.00'}</strong></li>
              {stats.mostCommonError && (
                <li className="text-red-700 font-medium">• Most frequent error: {stats.mostCommonError}</li>
              )}
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
