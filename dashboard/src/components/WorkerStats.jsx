import React from 'react';
import { CheckCircle, AlertCircle, Zap } from 'lucide-react';

export default function WorkerStats({ stats, jobs }) {
  const workers = [
    { id: 1, status: 'ACTIVE', processed: 312, currentJob: 1247, cpuUsage: 45, memoryUsage: 512 },
    { id: 2, status: 'ACTIVE', processed: 298, currentJob: 1246, cpuUsage: 38, memoryUsage: 480 },
    { id: 3, status: 'ACTIVE', processed: 287, currentJob: 1245, cpuUsage: 52, memoryUsage: 520 },
    { id: 4, status: 'IDLE', processed: 264, currentJob: null, cpuUsage: 2, memoryUsage: 256 },
    { id: 5, status: 'ACTIVE', processed: 326, currentJob: 1244, cpuUsage: 48, memoryUsage: 535 },
  ];

  const processingJobs = jobs.filter(job => job.status === 'PROCESSING');

  return (
    <div className="space-y-6">
      {/* Worker Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <p className="text-green-700 text-sm mb-2 font-medium">Active Workers</p>
          <p className="text-3xl font-bold text-green-600">{stats.workersActive}/5</p>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p className="text-blue-700 text-sm mb-2 font-medium">Total Processed</p>
          <p className="text-3xl font-bold text-blue-600">1847</p>
          <p className="text-blue-700 text-xs mt-1">avg per worker: 369</p>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
          <p className="text-purple-700 text-sm mb-2 font-medium">Avg Processing Time</p>
          <p className="text-3xl font-bold text-purple-600">2.5s</p>
          <p className="text-purple-700 text-xs mt-1">↓ 0.3s from avg</p>
        </div>
        <div className="bg-indigo-50 border border-indigo-200 rounded-lg p-4">
          <p className="text-indigo-700 text-sm mb-2 font-medium">Queue Depth</p>
          <p className="text-3xl font-bold text-indigo-600">{stats.queueSize}</p>
          <p className="text-indigo-700 text-xs mt-1">jobs waiting</p>
        </div>
      </div>

      {/* Individual Worker Status */}
      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden shadow-sm">
        <div className="bg-gray-50 border-b border-gray-200 px-6 py-4">
          <h3 className="font-semibold text-black">Worker Pool Details</h3>
        </div>
        <div className="divide-y divide-gray-200">
          {workers.map((worker) => (
            <div key={worker.id} className="p-6 hover:bg-gray-50 transition-colors">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className={`w-3 h-3 rounded-full ${worker.status === 'ACTIVE' ? 'bg-green-500' : 'bg-gray-400'}`}></div>
                  <h4 className="text-black font-semibold">Worker {worker.id}</h4>
                  <span className={`text-xs px-2 py-1 rounded border ${
                    worker.status === 'ACTIVE' 
                      ? 'bg-green-50 text-green-700 border-green-200'
                      : 'bg-gray-50 text-gray-700 border-gray-200'
                  }`}>
                    {worker.status}
                  </span>
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <div>
                  <p className="text-gray-600 text-xs mb-1">Total Processed</p>
                  <p className="text-black font-semibold">{worker.processed}</p>
                </div>
                <div>
                  <p className="text-gray-600 text-xs mb-1">Current Job</p>
                  <p className="text-blue-700 font-mono text-sm">{worker.currentJob ? `#${worker.currentJob}` : 'None'}</p>
                </div>
                <div>
                  <p className="text-gray-600 text-xs mb-1">CPU Usage</p>
                  <p className="text-black font-semibold">{worker.cpuUsage}%</p>
                </div>
                <div>
                  <p className="text-gray-600 text-xs mb-1">Memory</p>
                  <p className="text-black font-semibold">{worker.memoryUsage} MB</p>
                </div>
              </div>

              {/* Progress Bars */}
              <div className="space-y-3">
                <div>
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-xs text-gray-600">CPU</span>
                    <span className="text-xs text-gray-600">{worker.cpuUsage}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                    <div
                      className={`h-full transition-all ${
                        worker.cpuUsage > 70 ? 'bg-red-500' : worker.cpuUsage > 50 ? 'bg-yellow-500' : 'bg-green-500'
                      }`}
                      style={{ width: `${worker.cpuUsage}%` }}
                    ></div>
                  </div>
                </div>

                <div>
                  <div className="flex justify-between items-center mb-1">
                    <span className="text-xs text-gray-600">Memory</span>
                    <span className="text-xs text-gray-600">{worker.memoryUsage} MB / 1024 MB</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                    <div
                      className={`h-full transition-all ${
                        worker.memoryUsage > 800 ? 'bg-red-500' : worker.memoryUsage > 600 ? 'bg-yellow-500' : 'bg-green-500'
                      }`}
                      style={{ width: `${(worker.memoryUsage / 1024) * 100}%` }}
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Currently Processing Jobs */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <h3 className="font-semibold text-black mb-4 flex items-center gap-2">
          <Zap className="text-yellow-600" size={20} />
          Currently Processing ({processingJobs.length})
        </h3>
        <div className="space-y-3">
          {processingJobs.length === 0 ? (
            <p className="text-gray-600 text-sm">No jobs currently processing</p>
          ) : (
            processingJobs.map((job) => (
              <div key={job.jobId} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
                <div className="flex-1">
                  <p className="text-black font-semibold">Job #{job.jobId}</p>
                  <p className="text-gray-600 text-sm">Started: {job.createdAt}</p>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-yellow-500 animate-pulse"></div>
                  <span className="text-yellow-700 text-sm font-medium">In Progress</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Health Status */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-center gap-2 mb-3">
            <CheckCircle className="text-green-600" size={20} />
            <h4 className="font-semibold text-green-700">All Workers Healthy</h4>
          </div>
          <p className="text-green-800 text-sm">
            All 5 workers are running smoothly with no errors. Average utilization is 45%.
          </p>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center gap-2 mb-3">
            <Zap className="text-blue-600" size={20} />
            <h4 className="font-semibold text-blue-700">Performance Tip</h4>
          </div>
          <p className="text-blue-800 text-sm">
            Consider scaling up workers if queue depth exceeds 100 jobs for more than 5 minutes.
          </p>
        </div>
      </div>
    </div>
  );
}
