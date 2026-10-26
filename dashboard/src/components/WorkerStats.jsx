import React from 'react';
import { CheckCircle, AlertCircle, Zap, Cpu, HardDrive } from 'lucide-react';

export default function WorkerStats({ stats = {}, jobs = [], workers = [] }) {
  const displayWorkers = workers && workers.length > 0
    ? workers.map((w, idx) => ({
        id: w.workerId || idx + 1,
        status: (w.status || 'ACTIVE').toUpperCase(),
        processed: w.jobsProcessed ?? 0,
        currentJob: w.currentJobId ?? (w.currentlyProcessing > 0 ? 'Active' : null),
        cpuUsage: Math.round(w.cpuUsagePercent ?? 0),
        memoryUsage: Math.round(w.memoryUsageMb ?? 256),
        totalMemory: Math.round(w.totalMemoryMb ?? 1024),
        healthStatus: w.healthStatus || 'HEALTHY',
      }))
    : [
        { id: 1, status: 'ACTIVE', processed: 312, currentJob: 1247, cpuUsage: 45, memoryUsage: 512, totalMemory: 1024, healthStatus: 'HEALTHY' },
        { id: 2, status: 'ACTIVE', processed: 298, currentJob: 1246, cpuUsage: 38, memoryUsage: 480, totalMemory: 1024, healthStatus: 'HEALTHY' },
        { id: 3, status: 'ACTIVE', processed: 287, currentJob: 1245, cpuUsage: 52, memoryUsage: 520, totalMemory: 1024, healthStatus: 'HEALTHY' },
        { id: 4, status: 'IDLE', processed: 264, currentJob: null, cpuUsage: 2, memoryUsage: 256, totalMemory: 1024, healthStatus: 'HEALTHY' },
        { id: 5, status: 'ACTIVE', processed: 326, currentJob: 1244, cpuUsage: 48, memoryUsage: 535, totalMemory: 1024, healthStatus: 'HEALTHY' },
      ];

  const processingJobs = jobs.filter(job => (job.status || '').toUpperCase() === 'PROCESSING');
  const activeWorkerCount = stats.workersActive ?? displayWorkers.filter(w => w.status === 'ACTIVE').length;
  const totalProcessed = stats.total ?? displayWorkers.reduce((acc, w) => acc + (w.processed || 0), 0);
  const avgPerWorker = displayWorkers.length > 0 ? Math.round(totalProcessed / displayWorkers.length) : 0;
  const avgProcessingTime = stats.avgProcessingTime != null ? `${Number(stats.avgProcessingTime).toFixed(1)}s` : '0.0s';
  const queueDepth = stats.queueSize ?? 0;

  const anyUnhealthy = displayWorkers.some(w => w.healthStatus === 'DEGRADED' || w.healthStatus === 'UNHEALTHY' || w.status === 'ERROR');

  return (
    <div className="space-y-6">
      {/* Worker Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <p className="text-green-700 text-sm mb-2 font-medium">Active Workers</p>
          <p className="text-3xl font-bold text-green-600">{activeWorkerCount}/{displayWorkers.length}</p>
          <p className="text-green-700 text-xs mt-1">pool capacity online</p>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <p className="text-blue-700 text-sm mb-2 font-medium">Total Processed</p>
          <p className="text-3xl font-bold text-blue-600">{totalProcessed.toLocaleString()}</p>
          <p className="text-blue-700 text-xs mt-1">avg per worker: ~{avgPerWorker}</p>
        </div>
        <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
          <p className="text-purple-700 text-sm mb-2 font-medium">Avg Processing Time</p>
          <p className="text-3xl font-bold text-purple-600">{avgProcessingTime}</p>
          <p className="text-purple-700 text-xs mt-1">system execution speed</p>
        </div>
        <div className="bg-indigo-50 border border-indigo-200 rounded-lg p-4">
          <p className="text-indigo-700 text-sm mb-2 font-medium">Queue Depth</p>
          <p className="text-3xl font-bold text-indigo-600">{queueDepth}</p>
          <p className="text-indigo-700 text-xs mt-1">jobs waiting in queue</p>
        </div>
      </div>

      {/* Individual Worker Status */}
      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden shadow-sm">
        <div className="bg-gray-50 border-b border-gray-200 px-6 py-4">
          <h3 className="font-semibold text-black">Worker Pool Details</h3>
        </div>
        <div className="divide-y divide-gray-200">
          {displayWorkers.map((worker) => {
            const isWorkerActive = worker.status === 'ACTIVE' || worker.status === 'BUSY';
            const totalMem = worker.totalMemory || 1024;
            const memPercent = Math.min(100, Math.round((worker.memoryUsage / totalMem) * 100));

            return (
              <div key={worker.id} className="p-6 hover:bg-gray-50 transition-colors">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className={`w-3 h-3 rounded-full ${isWorkerActive ? 'bg-green-500 animate-pulse' : 'bg-gray-400'}`}></div>
                    <h4 className="text-black font-semibold">Worker {worker.id}</h4>
                    <span className={`text-xs px-2 py-1 rounded border font-medium ${
                      isWorkerActive 
                        ? 'bg-green-50 text-green-700 border-green-200'
                        : 'bg-gray-50 text-gray-700 border-gray-200'
                    }`}>
                      {worker.status}
                    </span>
                    {worker.healthStatus && worker.healthStatus !== 'HEALTHY' && (
                      <span className="text-xs px-2 py-0.5 rounded bg-yellow-100 text-yellow-800 border border-yellow-200 font-medium">
                        {worker.healthStatus}
                      </span>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                  <div>
                    <p className="text-gray-600 text-xs mb-1">Total Processed</p>
                    <p className="text-black font-semibold">{worker.processed.toLocaleString()}</p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-xs mb-1">Current Job</p>
                    <p className="text-blue-700 font-mono text-sm">
                      {worker.currentJob ? (typeof worker.currentJob === 'number' ? `#${worker.currentJob}` : worker.currentJob) : 'Idle / None'}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-xs mb-1 flex items-center gap-1">
                      <Cpu size={12} /> CPU Usage
                    </p>
                    <p className="text-black font-semibold">{worker.cpuUsage}%</p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-xs mb-1 flex items-center gap-1">
                      <HardDrive size={12} /> Memory
                    </p>
                    <p className="text-black font-semibold">{worker.memoryUsage} MB</p>
                  </div>
                </div>

                {/* Progress Bars */}
                <div className="space-y-3">
                  <div>
                    <div className="flex justify-between items-center mb-1">
                      <span className="text-xs text-gray-600">CPU Load</span>
                      <span className="text-xs text-gray-600 font-medium">{worker.cpuUsage}%</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                      <div
                        className={`h-full transition-all duration-500 ${
                          worker.cpuUsage > 75 ? 'bg-red-500' : worker.cpuUsage > 50 ? 'bg-yellow-500' : 'bg-green-500'
                        }`}
                        style={{ width: `${Math.min(100, Math.max(0, worker.cpuUsage))}%` }}
                      ></div>
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between items-center mb-1">
                      <span className="text-xs text-gray-600">Memory Allocation</span>
                      <span className="text-xs text-gray-600 font-medium">{worker.memoryUsage} MB / {totalMem} MB ({memPercent}%)</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                      <div
                        className={`h-full transition-all duration-500 ${
                          memPercent > 80 ? 'bg-red-500' : memPercent > 60 ? 'bg-yellow-500' : 'bg-green-500'
                        }`}
                        style={{ width: `${memPercent}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
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
            <p className="text-gray-500 text-sm italic">No jobs actively running in worker threads at this moment</p>
          ) : (
            processingJobs.map((job) => (
              <div key={job.jobId} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg border border-gray-200">
                <div className="flex-1">
                  <p className="text-black font-semibold">Job #{job.jobId}</p>
                  <p className="text-gray-600 text-sm">
                    {job.channel || job.jobType || 'NOTIFICATION'} • Recipient: {job.recipient || 'N/A'} • Started: {job.createdAt || 'Just now'}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-2.5 h-2.5 rounded-full bg-yellow-500 animate-ping"></div>
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
            <h4 className="font-semibold text-green-700">
              {anyUnhealthy ? 'Worker Pool Warning' : 'Worker Pool Healthy'}
            </h4>
          </div>
          <p className="text-green-800 text-sm">
            {activeWorkerCount} of {displayWorkers.length} workers are active. Queue depth is {queueDepth} items with average turnaround of {avgProcessingTime}.
          </p>
        </div>
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center gap-2 mb-3">
            <Zap className="text-blue-600" size={20} />
            <h4 className="font-semibold text-blue-700">Scaling & Performance</h4>
          </div>
          <p className="text-blue-800 text-sm">
            Automatic dead letter routing is active after retry backoff. Consider scaling worker threads if queue depth exceeds 100 jobs.
          </p>
        </div>
      </div>
    </div>
  );
}
