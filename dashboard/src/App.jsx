import React, { useState, useEffect } from 'react';
import { Activity, Zap, Clock, AlertCircle, TrendingUp, RefreshCw, Menu, X } from 'lucide-react';
import Dashboard from './components/Dashboard';
import JobsTable from './components/JobsTable';
import DeadLetterQueue from './components/DeadLetterQueue';
import WorkerStats from './components/WorkerStats';
import Charts from './components/Charts';

export default function App() {
  const [activeTab, setActiveTab] = useState('overview');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [stats, setStats] = useState({
    total: 0,
    pending: 0,
    processing: 0,
    completed: 0,
    failed: 0,
    deadLetterCount: 0,
    successRate: 0,
    avgProcessingTime: 0,
    workersActive: 5,
    queueSize: 0,
  });
  
  const [jobs, setJobs] = useState([]);
  const [deadLetterJobs, setDeadLetterJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(new Date());

  const fetchData = async () => {
    setLoading(true);
    try {
      const mockStats = {
        total: 1247,
        pending: 45,
        processing: 12,
        completed: 1150,
        failed: 40,
        deadLetterCount: 8,
        successRate: 92.3,
        avgProcessingTime: 2.5,
        workersActive: 5,
        queueSize: 45,
      };

      const mockJobs = [
        { jobId: 1247, status: 'COMPLETED', type: 'EMAIL', attempts: 1, createdAt: '2026-01-15 10:30:45', completedAt: '2026-01-15 10:30:48', lastError: null },
        { jobId: 1246, status: 'PROCESSING', type: 'EMAIL', attempts: 1, createdAt: '2026-01-15 10:30:40', completedAt: null, lastError: null },
        { jobId: 1245, status: 'PENDING', type: 'EMAIL', attempts: 1, createdAt: '2026-01-15 10:30:35', completedAt: null, lastError: null },
        { jobId: 1244, status: 'COMPLETED', type: 'EMAIL', attempts: 2, createdAt: '2026-01-15 10:30:30', completedAt: '2026-01-15 10:30:55', lastError: null },
        { jobId: 1243, status: 'FAILED', type: 'EMAIL', attempts: 5, createdAt: '2026-01-15 10:30:25', completedAt: null, lastError: 'SMTP timeout' },
        { jobId: 1242, status: 'COMPLETED', type: 'EMAIL', attempts: 1, createdAt: '2026-01-15 10:30:20', completedAt: '2026-01-15 10:30:22', lastError: null },
        { jobId: 1241, status: 'PENDING', type: 'EMAIL', attempts: 1, createdAt: '2026-01-15 10:30:15', completedAt: null, lastError: null },
        { jobId: 1240, status: 'COMPLETED', type: 'EMAIL', attempts: 3, createdAt: '2026-01-15 10:30:10', completedAt: '2026-01-15 10:30:40', lastError: null },
      ];

      const mockDeadLetterJobs = [
        { jobId: 1243, status: 'FAILED', type: 'EMAIL', attempts: 5, lastError: 'SMTP timeout', movedAt: '2026-01-15 10:31:00' },
        { jobId: 1239, status: 'FAILED', type: 'EMAIL', attempts: 5, lastError: 'Invalid recipient email', movedAt: '2026-01-15 09:45:30' },
        { jobId: 1233, status: 'FAILED', type: 'EMAIL', attempts: 5, lastError: 'Mail server rejected', movedAt: '2026-01-15 08:20:15' },
      ];

      setStats(mockStats);
      setJobs(mockJobs);
      setDeadLetterJobs(mockDeadLetterJobs);
      setLastUpdated(new Date());
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const tabs = [
    { id: 'overview', label: 'Overview', icon: Activity },
    { id: 'jobs', label: 'All Jobs', icon: TrendingUp },
    { id: 'charts', label: 'Analytics', icon: Clock },
    { id: 'workers', label: 'Workers', icon: Zap },
    { id: 'dead-letter', label: 'Dead Letter Queue', icon: AlertCircle },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-50">
      {/* Header */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-black flex items-center gap-2">
                <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center">
                  <Zap className="text-white" size={20} />
                </div>
                JobFlow Dashboard
              </h1>
              <p className="text-gray-500 text-sm mt-1">Real-time notification job monitoring</p>
            </div>
            <button
              onClick={fetchData}
              disabled={loading}
              className="hidden sm:flex items-center gap-2 px-4 py-2 bg-black hover:bg-gray-900 text-white rounded-lg transition-all disabled:opacity-50 font-medium"
            >
              <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
              Refresh
            </button>
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="sm:hidden p-2 hover:bg-gray-100 rounded-lg"
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
          <div className="text-xs text-gray-400 mt-2">
            Last updated: {lastUpdated.toLocaleTimeString()}
          </div>
        </div>
      </header>

      {/* Navigation Tabs */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className={`${mobileMenuOpen ? 'flex' : 'hidden'} sm:flex flex-col sm:flex-row gap-4 sm:gap-8 py-4 sm:py-0`}>
            {tabs.map(tab => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => {
                    setActiveTab(tab.id);
                    setMobileMenuOpen(false);
                  }}
                  className={`py-4 px-1 border-b-2 font-medium flex items-center gap-2 transition-all whitespace-nowrap text-sm ${
                    activeTab === tab.id
                      ? 'border-black text-black'
                      : 'border-transparent text-gray-600 hover:text-black'
                  }`}
                >
                  <Icon size={18} />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'overview' && <Dashboard stats={stats} />}
        {activeTab === 'jobs' && <JobsTable jobs={jobs} loading={loading} />}
        {activeTab === 'charts' && <Charts stats={stats} jobs={jobs} />}
        {activeTab === 'workers' && <WorkerStats stats={stats} jobs={jobs} />}
        {activeTab === 'dead-letter' && <DeadLetterQueue jobs={deadLetterJobs} loading={loading} />}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 py-4 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <p className="text-gray-600 text-sm">
            © 2026 Notification Processing System. Auto-refreshing every 5 seconds.
          </p>
        </div>
      </footer>
    </div>
  );
}
