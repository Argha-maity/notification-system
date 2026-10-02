import React, { useState } from 'react';
import { AlertTriangle, Trash2, RotateCcw, Eye } from 'lucide-react';
import API_CONFIG, { apiPost, apiDelete } from '../config/api';

export default function DeadLetterQueue({ jobs = [], loading = false, onRefresh }) {
  const [selectedJob, setSelectedJob] = useState(null);
  const [actionLoading, setActionLoading] = useState({});

  const handleRetry = async (jobId) => {
    try {
      setActionLoading((prev) => ({ ...prev, [jobId]: 'retrying' }));
      await apiPost(API_CONFIG.ENDPOINTS.JOB_RETRY(jobId));
      if (onRefresh) onRefresh();
    } catch (err) {
      alert(`Failed to retry job #${jobId}: ${err.message}`);
    } finally {
      setActionLoading((prev) => ({ ...prev, [jobId]: null }));
    }
  };

  const handleDelete = async (jobId) => {
    if (!window.confirm(`Are you sure you want to permanently delete job #${jobId}?`)) return;
    try {
      setActionLoading((prev) => ({ ...prev, [jobId]: 'deleting' }));
      await apiDelete(API_CONFIG.ENDPOINTS.JOB_DELETE(jobId));
      if (onRefresh) onRefresh();
    } catch (err) {
      alert(`Failed to delete job #${jobId}: ${err.message}`);
    } finally {
      setActionLoading((prev) => ({ ...prev, [jobId]: null }));
    }
  };

  // Find most common error dynamically
  const errorCounts = {};
  jobs.forEach((j) => {
    const err = j.lastError || j.dlqReason || 'Unknown failure';
    errorCounts[err] = (errorCounts[err] || 0) + 1;
  });
  let mostCommonError = 'None';
  let mostCommonCount = 0;
  Object.entries(errorCounts).forEach(([err, count]) => {
    if (count > mostCommonCount) {
      mostCommonCount = count;
      mostCommonError = err;
    }
  });

  // Oldest job date
  let oldestJobDate = 'None';
  if (jobs.length > 0) {
    const dates = jobs
      .map((j) => (j.movedToDlqAt || j.createdAt ? new Date(j.movedToDlqAt || j.createdAt) : null))
      .filter(Boolean);
    if (dates.length > 0) {
      const oldest = new Date(Math.min(...dates));
      oldestJobDate = oldest.toLocaleString();
    }
  }

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
            <div className="text-gray-600 font-medium mb-1">✓ Queue is empty</div>
            <p className="text-gray-500 text-sm">All jobs are being processed successfully</p>
          </div>
        ) : (
          <div className="divide-y divide-gray-200">
            {jobs.map((job) => {
              const attempts = job.attemptCount ?? job.attempts ?? 0;
              const maxAttempts = job.maxAttempts || 5;
              const jobType = job.type || job.jobType || 'EMAIL';
              const movedDate = job.movedToDlqAt
                ? new Date(job.movedToDlqAt).toLocaleString()
                : (job.createdAt ? new Date(job.createdAt).toLocaleString() : 'N/A');

              return (
                <div key={job.jobId} className="p-6 hover:bg-gray-50 transition-colors">
                  <div className="flex items-start justify-between gap-4 mb-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <AlertTriangle className="text-red-600" size={20} />
                        <h3 className="text-black font-semibold">Job #{job.jobId}</h3>
                        <span className="text-xs bg-red-50 text-red-700 px-2 py-1 rounded border border-red-200">
                          {attempts}/{maxAttempts} attempts
                        </span>
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <p className="text-gray-600 text-xs">Type</p>
                          <p className="text-black font-medium">{jobType}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 text-xs">Moved At</p>
                          <p className="text-black text-xs">{movedDate}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 text-xs">User / Recipient</p>
                          <p className="text-black font-medium text-xs truncate max-w-xs">{job.recipient || 'N/A'}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 text-xs">Notification ID</p>
                          <p className="text-black font-mono text-xs">{job.notificationId || 'N/A'}</p>
                        </div>
                      </div>
                    </div>
                    <button
                      onClick={() => setSelectedJob(selectedJob === job.jobId ? null : job.jobId)}
                      className="text-blue-600 hover:text-blue-700 transition-colors flex-shrink-0 p-1"
                      aria-label="View details"
                    >
                      <Eye size={20} />
                    </button>
                  </div>

                  {/* Error Message */}
                  <div className="mb-4 bg-red-50 border border-red-200 rounded p-3">
                    <p className="text-red-700 font-mono text-xs break-words">{job.lastError || job.dlqReason || 'Unknown error occurred'}</p>
                  </div>

                  {/* Action Buttons */}
                  <div className="flex gap-2 flex-wrap">
                    <button
                      onClick={() => handleRetry(job.jobId)}
                      disabled={actionLoading[job.jobId] === 'retrying'}
                      className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors text-sm font-medium disabled:opacity-50"
                    >
                      <RotateCcw size={16} className={actionLoading[job.jobId] === 'retrying' ? 'animate-spin' : ''} />
                      {actionLoading[job.jobId] === 'retrying' ? 'Retrying...' : 'Retry Job'}
                    </button>
                    <button
                      onClick={() => handleDelete(job.jobId)}
                      disabled={actionLoading[job.jobId] === 'deleting'}
                      className="flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors text-sm font-medium disabled:opacity-50"
                    >
                      <Trash2 size={16} />
                      {actionLoading[job.jobId] === 'deleting' ? 'Deleting...' : 'Delete'}
                    </button>
                  </div>

                  {/* Expanded Details */}
                  {selectedJob === job.jobId && (
                    <div className="mt-4 pt-4 border-t border-gray-200 space-y-3 text-sm">
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div>
                          <p className="text-gray-600 text-xs">Subject</p>
                          <p className="text-black font-medium">{job.subject || 'N/A'}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 text-xs">Priority</p>
                          <p className="text-black font-mono">{job.priority || 'Normal'}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 text-xs">Max Attempts</p>
                          <p className="text-black font-mono">{maxAttempts}</p>
                        </div>
                        <div>
                          <p className="text-gray-600 text-xs">Attempts Made</p>
                          <p className="text-black font-mono">{attempts}</p>
                        </div>
                      </div>
                      <div>
                        <p className="text-gray-600 text-xs font-semibold mb-1">Error Information</p>
                        <div className="bg-gray-50 p-3 rounded border border-gray-200 max-h-40 overflow-y-auto">
                          <p className="text-red-700 font-mono text-xs break-words whitespace-pre-wrap">
                            {job.lastError || job.dlqReason}
                          </p>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* DLQ Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
          <p className="text-gray-600 text-sm mb-2 font-medium">Total in Queue</p>
          <p className="text-3xl font-bold text-red-600">{jobs.length}</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
          <p className="text-gray-600 text-sm mb-2 font-medium">Oldest Failed Job</p>
          <p className="text-black text-sm font-semibold truncate">{oldestJobDate}</p>
          <p className="text-gray-500 text-xs mt-1">Requires manual inspection</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
          <p className="text-gray-600 text-sm mb-2 font-medium">Most Common Error</p>
          <p className="text-black text-sm font-semibold truncate" title={mostCommonError}>{mostCommonError}</p>
          <p className="text-gray-500 text-xs mt-1">{mostCommonCount > 0 ? `${mostCommonCount} jobs affected` : 'No errors'}</p>
        </div>
      </div>
    </div>
  );
}
