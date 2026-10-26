import React, { useState, useEffect, useCallback } from 'react';
import {
  Activity, Zap, Clock, AlertCircle, TrendingUp, RefreshCw, Menu, X,
  LogIn, LogOut, User as UserIcon, ShieldAlert, CheckCircle2, Lock, Mail
} from 'lucide-react';
import Dashboard from './components/Dashboard';
import JobsTable from './components/JobsTable';
import DeadLetterQueue from './components/DeadLetterQueue';
import WorkerStats from './components/WorkerStats';
import Charts from './components/Charts';
import API_CONFIG, { apiGet, apiPost } from './config/api.js';

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
    workersActive: 0,
    queueSize: 0,
    avgRetryCount: 0,
    mostCommonError: null,
  });

  const [jobs, setJobs] = useState([]);
  const [deadLetterJobs, setDeadLetterJobs] = useState([]);
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [lastUpdated, setLastUpdated] = useState(new Date());
  const [error, setError] = useState(null);

  // Auth state
  const [currentUser, setCurrentUser] = useState(() => {
    try {
      const stored = localStorage.getItem(API_CONFIG.USER_KEY);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [authMode, setAuthMode] = useState('login'); // 'login' | 'signup'
  const [authEmail, setAuthEmail] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authUsername, setAuthUsername] = useState('');
  const [authRole, setAuthRole] = useState('USER');
  const [authLoading, setAuthLoading] = useState(false);
  const [authError, setAuthError] = useState(null);
  const [authSuccess, setAuthSuccess] = useState(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [statsRes, jobsRes, dlqRes, workersRes] = await Promise.allSettled([
        apiGet(API_CONFIG.ENDPOINTS.STATS),
        apiGet(API_CONFIG.ENDPOINTS.JOBS),
        apiGet(API_CONFIG.ENDPOINTS.DEAD_LETTER_QUEUE),
        apiGet(API_CONFIG.ENDPOINTS.WORKERS),
      ]);

      let hadSuccess = false;

      if (statsRes.status === 'fulfilled' && statsRes.value) {
        setStats(prev => ({
          ...prev,
          ...statsRes.value,
          successRate: statsRes.value.successRate != null ? Number(statsRes.value.successRate) : prev.successRate,
          avgProcessingTime: statsRes.value.avgProcessingTime != null ? Number(statsRes.value.avgProcessingTime) : prev.avgProcessingTime,
        }));
        hadSuccess = true;
      }

      if (jobsRes.status === 'fulfilled' && jobsRes.value) {
        const jobList = Array.isArray(jobsRes.value) ? jobsRes.value : (jobsRes.value.content || []);
        setJobs(jobList);
        hadSuccess = true;
      }

      if (dlqRes.status === 'fulfilled' && dlqRes.value) {
        const dlqList = Array.isArray(dlqRes.value) ? dlqRes.value : (dlqRes.value.content || []);
        setDeadLetterJobs(dlqList);
        hadSuccess = true;
      }

      if (workersRes.status === 'fulfilled' && workersRes.value) {
        if (Array.isArray(workersRes.value)) {
          setWorkers(workersRes.value);
        }
        hadSuccess = true;
      }

      if (hadSuccess) {
        setError(null);
        setLastUpdated(new Date());
      } else {
        const firstErr = statsRes.reason || jobsRes.reason || dlqRes.reason || workersRes.reason;
        const msg = firstErr?.message || 'Unable to fetch data from backend service.';
        setError(msg);
      }
    } catch (err) {
      console.error('Error fetching data:', err);
      setError(err?.message || 'Failed to connect to backend server.');
    } finally {
      setLoading(false);
    }
  }, []);

  // Listen for unauthorized 401 events from apiCall
  useEffect(() => {
    const handleUnauthorized = () => {
      setCurrentUser(null);
      setAuthError('Your session has expired or authentication is required. Please sign in.');
      setShowAuthModal(true);
    };

    window.addEventListener('auth:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  // Polling data
  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, API_CONFIG.REFRESH_INTERVAL || 5000);
    return () => clearInterval(interval);
  }, [fetchData]);

  const handleAuthSubmit = async (e) => {
    e.preventDefault();
    setAuthLoading(true);
    setAuthError(null);
    setAuthSuccess(null);

    try {
      if (authMode === 'login') {
        const res = await apiPost(API_CONFIG.ENDPOINTS.AUTH_LOGIN, {
          email: authEmail,
          password: authPassword,
        });

        if (res && res.token) {
          localStorage.setItem(API_CONFIG.TOKEN_KEY, res.token);
          const userObj = { email: res.email || authEmail };
          localStorage.setItem(API_CONFIG.USER_KEY, JSON.stringify(userObj));
          setCurrentUser(userObj);
          setShowAuthModal(false);
          setAuthPassword('');
          fetchData();
        } else {
          throw new Error('Authentication response did not contain a valid token.');
        }
      } else {
        // Signup
        await apiPost(API_CONFIG.ENDPOINTS.AUTH_SIGNUP, {
          username: authUsername || authEmail.split('@')[0],
          email: authEmail,
          password: authPassword,
          role: authRole || 'USER',
        });

        setAuthSuccess('Account registered successfully! Logging you in...');
        
        // Auto-login after signup
        const loginRes = await apiPost(API_CONFIG.ENDPOINTS.AUTH_LOGIN, {
          email: authEmail,
          password: authPassword,
        });

        if (loginRes && loginRes.token) {
          localStorage.setItem(API_CONFIG.TOKEN_KEY, loginRes.token);
          const userObj = { email: loginRes.email || authEmail };
          localStorage.setItem(API_CONFIG.USER_KEY, JSON.stringify(userObj));
          setCurrentUser(userObj);
          setTimeout(() => {
            setShowAuthModal(false);
            setAuthPassword('');
            setAuthSuccess(null);
            fetchData();
          }, 800);
        } else {
          setAuthMode('login');
        }
      }
    } catch (err) {
      setAuthError(err.message || 'Authentication failed. Please check your credentials.');
    } finally {
      setAuthLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem(API_CONFIG.TOKEN_KEY);
    localStorage.removeItem(API_CONFIG.USER_KEY);
    setCurrentUser(null);
    fetchData();
  };

  const tabs = [
    { id: 'overview', label: 'Overview', icon: Activity },
    { id: 'jobs', label: 'All Jobs', icon: TrendingUp },
    { id: 'charts', label: 'Analytics', icon: Clock },
    { id: 'workers', label: 'Workers', icon: Zap },
    { id: 'dead-letter', label: 'Dead Letter Queue', icon: AlertCircle },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-50 text-gray-900">
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

            <div className="flex items-center gap-3">
              {currentUser ? (
                <div className="flex items-center gap-2 px-3 py-1.5 bg-gray-100 rounded-lg text-sm text-gray-800">
                  <UserIcon size={16} className="text-gray-600" />
                  <span className="font-medium text-xs sm:text-sm">{currentUser.email}</span>
                  <button
                    onClick={handleLogout}
                    title="Sign Out"
                    className="ml-1 text-gray-400 hover:text-red-600 transition-colors p-0.5"
                  >
                    <LogOut size={16} />
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => {
                    setAuthError(null);
                    setShowAuthModal(true);
                  }}
                  className="flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium border border-gray-300 rounded-lg hover:bg-gray-100 text-gray-800 transition-all"
                >
                  <LogIn size={16} />
                  <span>Sign In</span>
                </button>
              )}

              <button
                onClick={fetchData}
                disabled={loading}
                className="hidden sm:flex items-center gap-2 px-4 py-2 bg-black hover:bg-gray-900 text-white rounded-lg transition-all disabled:opacity-50 font-medium text-sm"
              >
                <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
                Refresh
              </button>

              <button
                onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                className="sm:hidden p-2 hover:bg-gray-100 rounded-lg"
              >
                {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
              </button>
            </div>
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
                  {tab.id === 'dead-letter' && deadLetterJobs.length > 0 && (
                    <span className="ml-1.5 px-1.5 py-0.5 text-xs bg-red-100 text-red-700 rounded-full font-semibold">
                      {deadLetterJobs.length}
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Error notification banner if API connection or query fails */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-lg flex items-center justify-between gap-3 mb-6 shadow-sm">
            <div className="flex items-center gap-2">
              <AlertCircle className="text-red-600 flex-shrink-0" size={20} />
              <span className="text-sm font-medium">{error}</span>
            </div>
            <div className="flex items-center gap-2">
              {!currentUser && (
                <button
                  onClick={() => setShowAuthModal(true)}
                  className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white rounded font-medium text-xs transition-colors"
                >
                  Log In
                </button>
              )}
              <button
                onClick={fetchData}
                className="px-3 py-1 bg-red-100 hover:bg-red-200 text-red-900 rounded font-medium text-xs transition-colors"
              >
                Retry
              </button>
            </div>
          </div>
        )}

        {activeTab === 'overview' && <Dashboard stats={stats} />}
        {activeTab === 'jobs' && <JobsTable jobs={jobs} loading={loading} onRefresh={fetchData} />}
        {activeTab === 'charts' && <Charts stats={stats} jobs={jobs} />}
        {activeTab === 'workers' && <WorkerStats stats={stats} jobs={jobs} workers={workers} />}
        {activeTab === 'dead-letter' && <DeadLetterQueue jobs={deadLetterJobs} loading={loading} onRefresh={fetchData} />}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 py-4 mt-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row justify-between items-center gap-2">
          <p className="text-gray-600 text-sm">
            © 2026 Notification Processing System. Auto-refreshing every 5 seconds.
          </p>
          <div className="flex items-center gap-2 text-xs text-gray-500">
            <span>API: {API_CONFIG.BASE_URL}</span>
            <span>•</span>
            <span className={error ? 'text-red-500 font-medium' : 'text-green-600 font-medium'}>
              {error ? 'Disconnected' : 'Connected'}
            </span>
          </div>
        </div>
      </footer>

      {/* Authentication Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6 relative border border-gray-200">
            <button
              onClick={() => setShowAuthModal(false)}
              className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 p-1 rounded-lg"
            >
              <X size={20} />
            </button>

            <div className="flex items-center gap-2 mb-6">
              <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center text-white">
                <Lock size={16} />
              </div>
              <h3 className="text-xl font-bold text-black">
                {authMode === 'login' ? 'Sign In to Dashboard' : 'Create an Account'}
              </h3>
            </div>

            {/* Mode Switch Tabs */}
            <div className="flex border-b border-gray-200 mb-6">
              <button
                onClick={() => {
                  setAuthMode('login');
                  setAuthError(null);
                  setAuthSuccess(null);
                }}
                className={`flex-1 py-2 font-medium text-sm text-center border-b-2 transition-all ${
                  authMode === 'login'
                    ? 'border-black text-black'
                    : 'border-transparent text-gray-500 hover:text-black'
                }`}
              >
                Sign In
              </button>
              <button
                onClick={() => {
                  setAuthMode('signup');
                  setAuthError(null);
                  setAuthSuccess(null);
                }}
                className={`flex-1 py-2 font-medium text-sm text-center border-b-2 transition-all ${
                  authMode === 'signup'
                    ? 'border-black text-black'
                    : 'border-transparent text-gray-500 hover:text-black'
                }`}
              >
                Register
              </button>
            </div>

            {authError && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg flex items-start gap-2">
                <AlertCircle size={18} className="flex-shrink-0 mt-0.5" />
                <span>{authError}</span>
              </div>
            )}

            {authSuccess && (
              <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg flex items-start gap-2">
                <CheckCircle2 size={18} className="flex-shrink-0 mt-0.5" />
                <span>{authSuccess}</span>
              </div>
            )}

            <form onSubmit={handleAuthSubmit} className="space-y-4">
              {authMode === 'signup' && (
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Username
                  </label>
                  <input
                    type="text"
                    required
                    value={authUsername}
                    onChange={(e) => setAuthUsername(e.target.value)}
                    placeholder="john_doe"
                    className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm text-black focus:outline-none focus:border-black"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                  <input
                    type="email"
                    required
                    value={authEmail}
                    onChange={(e) => setAuthEmail(e.target.value)}
                    placeholder="admin@example.com"
                    className="w-full pl-9 pr-3 py-2 bg-white border border-gray-300 rounded-lg text-sm text-black focus:outline-none focus:border-black"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Password
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                  <input
                    type="password"
                    required
                    minLength={authMode === 'signup' ? 8 : 1}
                    value={authPassword}
                    onChange={(e) => setAuthPassword(e.target.value)}
                    placeholder={authMode === 'signup' ? 'Min. 8 characters' : 'Enter password'}
                    className="w-full pl-9 pr-3 py-2 bg-white border border-gray-300 rounded-lg text-sm text-black focus:outline-none focus:border-black"
                  />
                </div>
              </div>

              {authMode === 'signup' && (
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Role
                  </label>
                  <select
                    value={authRole}
                    onChange={(e) => setAuthRole(e.target.value)}
                    className="w-full px-3 py-2 bg-white border border-gray-300 rounded-lg text-sm text-black focus:outline-none focus:border-black"
                  >
                    <option value="USER">User (Standard)</option>
                    <option value="ADMIN">Admin (Full Control)</option>
                  </select>
                </div>
              )}

              <button
                type="submit"
                disabled={authLoading}
                className="w-full mt-2 py-2.5 px-4 bg-black hover:bg-gray-800 text-white font-medium rounded-lg text-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                {authLoading && <RefreshCw size={16} className="animate-spin" />}
                {authMode === 'login' ? 'Sign In' : 'Create Account'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
