import React, { useState } from 'react';
import { AlertTriangle, Trash2, RotateCcw, Eye } from 'lucide-react';

export default function DeadLetterQueue({ jobs, loading }) {
  const [selectedJob, setSelectedJob] = useState(null);

  const handleRetry = (jobId) => {
    console.log('Retrying job:', jobId);
  };

  const handleDelete = (jobId) => {
    console.log('Deleting job:', jobId);
  };

  return (
    <div className="space-y-6">
      {/* Warning Banner */}
      <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex gap-4">
        <AlertTriangle className="text-red-600 flex-shrink-0 mt-0.5" size={24} />
        <div>
          <h3 className="font-semibold text-red-700 mb-1">Dead Letter Queue</h3>
          <p className="text-red-800 text-sm">
            These jobs have exceeded the maximum retry limit and require manual intervention. 
            Review the error messages before attempting to retry.
          </p>
        </div>
      </div>

      {/* DLQ Jobs List */}
      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-8 text-center text-gray-600">
            Loading dead letter queue...
          </div>
        ) : jobs.length === 0 ? (
          <div className="p-8 text-center">
            <div className="text-gray-600 mb-2">✓ Queue is empty</div>
            <p className="text-gray-500 text-sm">All jobs are being processed successfully</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {jobs.map((job) => (
              <div key={job.jobId} className="p-6 hover:bg-gray-50 transition-colors">
                <div className="flex items-start justify-between gap-4 mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <AlertTriangle className="text-red-600" size={20} />
                      <h3 className="text-black font-semibold">Job #{job.jobId}</h3>
                      <span className="text-xs bg-red-50 text-red-700 px-2 py-1 rounded border border-red-200">
                        {job.attempts} attempts
                      </span>
                    </div>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                      <div>
                        <p className="text-gray-600">Type</p>
                        <p className="text-black">{job.type}</p>
                      </div>
                      <div>
                        <p className="text-gray-600">Moved At</p>
                        <p className="text-black text-xs">{job.movedAt}</p>
                      </div>
                      <div>
                        <p className="text-gray-600">User Email</p>
                        <p className="text-black">user@example.com</p>
                      </div>
                      <div>
                        <p className="text-gray-600">Notification</p>
                        <p className="text-black text-xs">ntf_12345</p>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedJob(selectedJob === job.jobId ? null : job.jobId)}
                    className="text-blue-600 hover:text-blue-700 transition-colors flex-shrink-0"
                  >
                    <Eye size={20} />
                  </button>
                </div>

                {/* Error Message */}
                <div className="mb-4 bg-red-50 border border-red-200 rounded p-3">
                  <p className="text-red-700 font-mono text-sm break-words">{job.lastError}</p>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-2 flex-wrap">
                  <button
                    onClick={() => handleRetry(job.jobId)}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors text-sm font-medium"
                  >
                    <RotateCcw size={16} />
                    Retry Job
                  </button>
                  <button
                    onClick={() => handleDelete(job.jobId)}
                    className="flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors text-sm font-medium"
                  >
                    <Trash2 size={16} />
                    Delete
                  </button>
                </div>

                {/* Expanded Details */}
                {selectedJob === job.jobId && (
                  <div className="mt-4 pt-4 border-t border-gray-200 space-y-3 text-sm">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <p className="text-gray-600">Job Type</p>
                        <p className="text-black font-mono">{job.type}</p>
                      </div>
                      <div>
                        <p className="text-gray-600">Status</p>
                        <p className="text-black font-mono">{job.status}</p>
                      </div>
                      <div>
                        <p className="text-gray-600">Max Attempts</p>
                        <p className="text-black font-mono">5</p>
                      </div>
                      <div>
                        <p className="text-gray-600">Current Attempts</p>
                        <p className="text-black font-mono">{job.attempts}</p>
                      </div>
                    </div>
                    <div>
                      <p className="text-gray-600">Full Error Trace</p>
                      <div className="bg-gray-50 p-3 rounded mt-1 border border-gray-200 max-h-40 overflow-y-auto">
                        <p className="text-red-700 font-mono text-xs break-words whitespace-pre-wrap">
                          {job.lastError}
                          {'\n\n'}
                          Stack trace would appear here for debugging
                        </p>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {/* DLQ Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
          <p className="text-gray-600 text-sm mb-2">Total in Queue</p>
          <p className="text-3xl font-bold text-red-600">{jobs.length}</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
          <p className="text-gray-600 text-sm mb-2">Oldest Job</p>
          <p className="text-black text-sm">2026-01-15 08:20:15</p>
          <p className="text-gray-600 text-xs mt-1">~2 hours ago</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
          <p className="text-gray-600 text-sm mb-2">Most Common Error</p>
          <p className="text-black text-sm">SMTP timeout</p>
          <p className="text-gray-600 text-xs mt-1">2 jobs affected</p>
        </div>
      </div>
    </div>
  );
}
