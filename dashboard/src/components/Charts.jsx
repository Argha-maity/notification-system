import React from 'react';
import { TrendingUp } from 'lucide-react';

export default function Charts({ stats, jobs }) {
  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-black mb-2">Analytics & Performance Metrics</h2>
        <p className="text-gray-600">Detailed insights into job processing performance</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-black mb-6">Job Timeline (Last 7 Hours)</h3>
          <div className="space-y-3">
            {[
              { time: '08:00', completed: 45, failed: 3, processing: 2 },
              { time: '09:00', completed: 52, failed: 2, processing: 4 },
              { time: '10:00', completed: 38, failed: 5, processing: 6 },
              { time: '11:00', completed: 61, failed: 1, processing: 3 },
            ].map((item, idx) => (
              <div key={idx} className="flex justify-between items-center">
                <span className="text-gray-700">{item.time}</span>
                <div className="w-48 bg-gray-200 h-6 rounded flex" style={{
                  background: `linear-gradient(to right, #10b981 ${(item.completed/70)*100}%, #3b82f6 ${((item.completed+item.processing)/70)*100}%, #ef4444 100%)`
                }}></div>
                <span className="text-black font-semibold">{item.completed + item.failed + item.processing} jobs</span>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h3 className="text-lg font-semibold text-black mb-6">Channel Success Rates</h3>
          <div className="space-y-4">
            {[
              { channel: 'Email', rate: 94.2 },
              { channel: 'SMS', rate: 88.5 },
              { channel: 'Push', rate: 91.3 }
            ].map((item, idx) => (
              <div key={idx}>
                <div className="flex justify-between mb-2">
                  <span className="text-gray-700">{item.channel}</span>
                  <span className="text-black font-semibold">{item.rate}%</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div className="bg-green-500 h-full rounded-full" style={{ width: `${item.rate}%` }}></div>
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
            <h3 className="font-semibold text-black mb-2">📊 Performance Insights</h3>
            <ul className="space-y-1 text-gray-700 text-sm">
              <li>✓ Peak processing time was at 12:00 PM with average 2.9 seconds per job</li>
              <li>✓ Email channel maintains highest success rate at 94.2%</li>
              <li>✓ 82.4% of jobs completed on first attempt (no retries)</li>
              <li>✓ System processing 456 jobs per hour on average</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
