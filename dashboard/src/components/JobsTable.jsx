import React, { useState } from 'react';
import { ChevronDown, Search, Filter } from 'lucide-react';

export default function JobsTable({ jobs, loading }) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [expandedRows, setExpandedRows] = useState(new Set());

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-50 text-green-700 border-green-200';
      case 'PROCESSING':
        return 'bg-yellow-50 text-yellow-700 border-yellow-200';
      case 'PENDING':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'FAILED':
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
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  const filteredJobs = jobs.filter(job => {
    const matchesSearch = job.jobId.toString().includes(searchTerm);
    const matchesFilter = filterStatus === 'ALL' || job.status === filterStatus;
    return matchesSearch && matchesFilter;
  });

  const toggleRow = (jobId) => {
    const newSet = new Set(expandedRows);
    if (newSet.has(jobId)) {
      newSet.delete(jobId);
    } else {
      newSet.add(jobId);
    }
    setExpandedRows(newSet);
  };

  return (
    <div className="space-y-6">
      {/* Search and Filter Bar */}
      <div className="flex gap-4 flex-col sm:flex-row">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Search by Job ID..."
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
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Type</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Attempts</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Created</th>
                <th className="px-6 py-4 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {loading ? (
                <tr>
                  <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                    Loading jobs...
                  </td>
                </tr>
              ) : filteredJobs.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                    No jobs found
                  </td>
                </tr>
              ) : (
                filteredJobs.map((job) => (
                  <React.Fragment key={job.jobId}>
                    <tr className="hover:bg-gray-50 transition-colors">
                      <td className="px-6 py-4 text-black font-semibold">#{job.jobId}</td>
                      <td className="px-6 py-4">
                        <span className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-sm font-medium border ${getStatusColor(job.status)}`}>
                          <span className={`w-2 h-2 rounded-full ${getStatusDot(job.status)}`}></span>
                          {job.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-gray-700">{job.type}</td>
                      <td className="px-6 py-4 text-gray-700">{job.attempts}/5</td>
                      <td className="px-6 py-4 text-gray-600 text-sm">{job.createdAt}</td>
                      <td className="px-6 py-4">
                        <button
                          onClick={() => toggleRow(job.jobId)}
                          className="text-black hover:text-gray-600 transition-colors"
                        >
                          <ChevronDown
                            size={20}
                            className={`transform transition-transform ${expandedRows.has(job.jobId) ? 'rotate-180' : ''}`}
                          />
                        </button>
                      </td>
                    </tr>

                    {/* Expanded Details */}
                    {expandedRows.has(job.jobId) && (
                      <tr className="bg-gray-50">
                        <td colSpan="6" className="px-6 py-4">
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                            <div>
                              <p className="text-gray-600">Notification ID</p>
                              <p className="text-black font-mono">ntf_12345</p>
                            </div>
                            <div>
                              <p className="text-gray-600">User Email</p>
                              <p className="text-black">user@example.com</p>
                            </div>
                            <div>
                              <p className="text-gray-600">Subject</p>
                              <p className="text-black">Welcome to our service</p>
                            </div>
                            <div>
                              <p className="text-gray-600">Completed At</p>
                              <p className="text-black">{job.completedAt || 'N/A'}</p>
                            </div>
                            {job.lastError && (
                              <div className="md:col-span-2">
                                <p className="text-gray-600">Last Error</p>
                                <p className="text-red-700 font-mono bg-red-50 p-2 rounded border border-red-200">{job.lastError}</p>
                              </div>
                            )}
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination Info */}
      <div className="flex justify-between items-center text-sm text-gray-600">
        <p>Showing {filteredJobs.length} of {jobs.length} jobs</p>
        <div className="flex gap-2">
          <button className="px-3 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 transition-colors disabled:opacity-50">
            Previous
          </button>
          <button className="px-3 py-1 bg-white border border-gray-200 rounded hover:bg-gray-50 transition-colors">
            Next
          </button>
        </div>
      </div>
    </div>
  );
}
