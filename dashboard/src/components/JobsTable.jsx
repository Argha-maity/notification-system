import React, { useState, useEffect } from 'react';
import { ChevronDown, Search, Filter, RotateCcw } from 'lucide-react';
import API_CONFIG, { apiPost } from '../config/api';

export default function JobsTable({ jobs = [], loading = false, onRefresh }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [expandedRows, setExpandedRows] = useState(new Set());
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [retryingJobId, setRetryingJobId] = useState(null);

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, filterStatus]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-50 text-green-700 border-green-200';
      case 'PROCESSING':
        return 'bg-yellow-50 text-yellow-700 border-yellow-200';
      case 'PENDING':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'FAILED':
      case 'DEAD_LETTER':
      case 'DEAD':
        return 'bg-red-50 text-red-700 border-red-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  };

  const getStatusDot = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-500';
      case 'PROCESSING':
        return 'bg-yellow-500 animate-pulse';
      case 'PENDING':
        return 'bg-blue-500';
      case 'FAILED':
      case 'DEAD_LETTER':
      case 'DEAD':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  const filteredJobs = jobs.filter(job => {
    const jobIdStr = (job.jobId || '').toString();
    const recipientStr = (job.recipient || '').toLowerCase();
    const typeStr = (job.jobType || job.type || '').toLowerCase();
    const term = searchTerm.toLowerCase();

    const matchesSearch = jobIdStr.includes(term) || recipientStr.includes(term) || typeStr.includes(term);
    const matchesFilter = filterStatus === 'ALL' || job.status === filterStatus;
    return matchesSearch && matchesFilter;
  });

  const totalPages = Math.max(1, Math.ceil(filteredJobs.length / pageSize));
  const startIndex = (currentPage - 1) * pageSize;
  const paginatedJobs = filteredJobs.slice(startIndex, startIndex + pageSize);

  const toggleRow = (jobId) => {
    const newSet = new Set(expandedRows);
    if (newSet.has(jobId)) {
      newSet.delete(jobId);
    } else {
      newSet.add(jobId);
    }
    setExpandedRows(newSet);
  };

  const handleRetryJob = async (jobId, e) => {
    if (e) e.stopPropagation();
    try {
      setRetryingJobId(jobId);
      await apiPost(API_CONFIG.ENDPOINTS.JOB_RETRY(jobId));
      if (onRefresh) onRefresh();
    } catch (err) {
      alert(`Retry failed: ${err.message}`);
    } finally {
      setRetryingJobId(null);
    }
  };

  return (
    <div className="space-y-6">
      {/* Search and Filter Bar */}
      <div className="flex gap-4 flex-col sm:flex-row">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Search by Job ID, email, or channel..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-lg text-black placeholder-gray-400 focus:outline-none focus:border-black focus:ring-1 focus:ring-black"
          />
        </div>
        <div className="flex gap-2">
          <Filter className="text-gray-400 mt-2" size={20} />
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="px-4 py-2 bg-white border border-gray-200 rounded-lg text-black focus:outline-none focus:border-black focus:ring-1 focus:ring-black"
          >
            <option value="ALL">All Status</option>
            <option value="PENDING">Pending</option>
            <option value="PROCESSING">Processing</option>
            <option value="COMPLETED">Completed</option>
            <option value="FAILED">Failed</option>
            <option value="DEAD_LETTER">Dead Letter</option>
          </select>
          <select
            value={pageSize}
            onChange={(e) => setPageSize(Number(e.target.value))}
            className="px-3 py-2 bg-white border border-gray-200 rounded-lg text-black text-sm focus:outline-none focus:border-black"
          >
            <option value={10}>10 / page</option>
            <option value={25}>25 / page</option>
            <option value={50}>50 / page</option>
          </select>
        </div>
      </div>

      {/* Jobs Table */}
      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 border-b border-gray-200">
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Job ID</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Status</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Type / Channel</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Attempts</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Created</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {loading ? (
                <tr>
                  <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                    Loading jobs from database...
                  </td>
                </tr>
              ) : filteredJobs.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                    No jobs found
                  </td>
                </tr>
              ) : (
                paginatedJobs.map((job) => {
                  const jobType = job.jobType || job.type || 'EMAIL';
                  const attempts = job.attemptCount ?? job.attempts ?? 0;
                  const maxAttempts = job.maxAttempts || 5;
                  const canRetry = job.status === 'FAILED' || job.status === 'DEAD_LETTER';

                  return (
                    <React.Fragment key={job.jobId}>
                      <tr className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 text-black font-semibold">#{job.jobId}</td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(job.status)}`}>
                            <span className={`w-2 h-2 rounded-full ${getStatusDot(job.status)}`}></span>
                            {job.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-gray-700 font-medium">{jobType}</td>
                        <td className="px-6 py-4 text-gray-700">{attempts}/{maxAttempts}</td>
                        <td className="px-6 py-4 text-gray-600 text-sm">
                          {job.createdAt ? new Date(job.createdAt).toLocaleString() : 'N/A'}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-3">
                            {canRetry && (
                              <button
                                onClick={(e) => handleRetryJob(job.jobId, e)}
                                disabled={retryingJobId === job.jobId}
                                className="flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-blue-700 bg-blue-50 border border-blue-200 rounded hover:bg-blue-100 transition-colors disabled:opacity-50"
                              >
                                <RotateCcw size={12} className={retryingJobId === job.jobId ? 'animate-spin' : ''} />
                                Retry
                              </button>
                            )}
                            <button
                              onClick={() => toggleRow(job.jobId)}
                              className="text-black hover:text-gray-600 transition-colors p-1"
                              aria-label="Toggle details"
                            >
                              <ChevronDown
                                size={18}
                                className={`transform transition-transform ${expandedRows.has(job.jobId) ? 'rotate-180' : ''}`}
                              />
                            </button>
                          </div>
                        </td>
                      </tr>

                      {/* Expanded Details */}
                      {expandedRows.has(job.jobId) && (
                        <tr className="bg-gray-50">
                          <td colSpan="6" className="px-6 py-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 text-sm">
                              <div>
                                <p className="text-gray-500 text-xs uppercase font-medium">Notification ID</p>
                                <p className="text-black font-mono mt-0.5">{job.notificationId || 'N/A'}</p>
                              </div>
                              <div>
                                <p className="text-gray-500 text-xs uppercase font-medium">Recipient</p>
                                <p className="text-black font-medium mt-0.5">{job.recipient || 'N/A'}</p>
                              </div>
                              <div>
                                <p className="text-gray-500 text-xs uppercase font-medium">Subject</p>
                                <p className="text-black mt-0.5">{job.subject || 'N/A'}</p>
                              </div>
                              <div>
                                <p className="text-gray-500 text-xs uppercase font-medium">Completed At</p>
                                <p className="text-black mt-0.5">
                                  {job.completedAt ? new Date(job.completedAt).toLocaleString() : 'In Progress / Pending'}
                                </p>
                              </div>
                              {job.processingTimeSeconds != null && (
                                <div>
                                  <p className="text-gray-500 text-xs uppercase font-medium">Processing Time</p>
                                  <p className="text-black font-mono mt-0.5">{job.processingTimeSeconds}s</p>
                                </div>
                              )}
                              {job.lastError && (
                                <div className="md:col-span-2 lg:col-span-4">
                                  <p className="text-red-700 text-xs uppercase font-semibold mb-1">Last Error Details</p>
                                  <p className="text-red-700 font-mono text-xs bg-red-50 p-2.5 rounded border border-red-200 break-words">
                                    {job.lastError}
                                  </p>
                                </div>
                              )}
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination Info & Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-center gap-3 text-sm text-gray-600">
        <p>
          Showing {filteredJobs.length === 0 ? 0 : startIndex + 1} to{' '}
          {Math.min(startIndex + pageSize, filteredJobs.length)} of {filteredJobs.length} jobs (Total: {jobs.length})
        </p>
        <div className="flex items-center gap-2">
          <span className="text-xs text-gray-500 mr-2">Page {currentPage} of {totalPages}</span>
          <button
            onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
            disabled={currentPage === 1}
            className="px-3 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 transition-colors disabled:opacity-40 disabled:cursor-not-allowed font-medium text-black"
          >
            Previous
          </button>
          <button
            onClick={() => setCurrentPage((p) => Math.min(p + 1, totalPages))}
            disabled={currentPage >= totalPages}
            className="px-3 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 transition-colors disabled:opacity-40 disabled:cursor-not-allowed font-medium text-black"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
