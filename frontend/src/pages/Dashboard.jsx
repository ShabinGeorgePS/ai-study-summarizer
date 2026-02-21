import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { summaryService } from '../services/summaryService';
import api from '../services/api';
import Button from '../components/common/Button';
import Card from '../components/common/Card';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const [summaries, setSummaries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [deleteConfirm, setDeleteConfirm] = useState(null); // Track which summary to delete
    const [deleting, setDeleting] = useState(false);

    useEffect(() => {
        fetchSummaries();
    }, []);

    const fetchSummaries = async () => {
        try {
            setLoading(true);
            const data = await summaryService.getAllSummaries();
            setSummaries(data);
        } catch (err) {
            setError('Failed to load summaries');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteSummary = async (summaryId) => {
        try {
            setDeleting(true);
            await api.delete(`/v1/summaries/${summaryId}`);
            setSummaries(prev => prev.filter(s => s.id !== summaryId));
            setDeleteConfirm(null);
        } catch (err) {
            setError('Failed to delete summary');
            console.error(err);
        } finally {
            setDeleting(false);
        }
    };

    return (
        <div className="min-h-screen">
            {/* Header */}
            <header className="fixed top-0 w-full z-50 glass-panel border-b border-white/5 backdrop-blur-xl">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 group cursor-pointer">
                            <div className="w-10 h-10 bg-gradient-to-br from-neon-purple to-neon-pink rounded-xl flex items-center justify-center transform group-hover:rotate-12 transition-transform duration-300 shadow-lg shadow-purple-900/50">
                                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                                </svg>
                            </div>
                            <h1 className="text-xl font-bold bg-gradient-to-r from-white to-gray-400 bg-clip-text text-transparent">AI Study Summarizer</h1>
                        </div>
                        <div className="flex items-center space-x-6">
                            <span className="text-gray-400 hidden sm:block">Welcome, <span className="text-white font-medium">{user?.email?.split('@')[0]}</span></span>
                            <Button variant="secondary" onClick={logout} className="!px-4 !py-2 text-sm">
                                Logout
                            </Button>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 sm:py-32">
                <div className="mb-12 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
                    <div>
                        <h2 className="text-4xl font-bold text-white mb-2">Your Library</h2>
                        <p className="text-gray-400 text-lg">Manage and review your AI-generated summaries</p>
                    </div>
                    <Link to="/upload">
                        <Button className="group">
                            <span className="flex items-center">
                                <svg className="w-5 h-5 mr-2 transform group-hover:rotate-90 transition-transform duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                </svg>
                                New Summary
                            </span>
                        </Button>
                    </Link>
                </div>

                {loading ? (
                    <div className="text-center py-20">
                        <div className="relative inline-block">
                            <div className="absolute inset-0 bg-neon-purple/50 rounded-full blur-xl animate-pulse"></div>
                            <div className="relative inline-block animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-neon-purple"></div>
                        </div>
                        <p className="mt-6 text-gray-400 animate-pulse">Loading intelligence...</p>
                    </div>
                ) : error ? (
                    <Card className="p-8 text-center border-red-500/30 bg-red-500/10">
                        <p className="text-red-400 text-lg">{error}</p>
                    </Card>
                ) : summaries.length === 0 ? (
                    <Card className="p-16 text-center border-dashed border-white/10 hover:border-white/20 transition-colors group">
                        <div className="w-24 h-24 mx-auto mb-6 bg-white/5 rounded-full flex items-center justify-center group-hover:scale-110 transition-transform duration-500">
                            <svg className="w-10 h-10 text-gray-500 group-hover:text-neon-purple transition-colors duration-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                        </div>
                        <h3 className="text-2xl font-bold text-white mb-2">No summaries yet</h3>
                        <p className="text-gray-400 mb-8 max-w-md mx-auto">Upload your first document to unleash the power of AI-driven study summaries.</p>
                        <Link to="/upload">
                            <Button variant="primary" className="!px-8 !py-4 shadow-neon-purple/50">Create Your First Summary</Button>
                        </Link>
                    </Card>
                ) : (
                    <>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {summaries.map((summary) => (
                                <div key={summary.id} className="group relative">
                                    <Link to={`/results/${summary.id}`}>
                                        <Card className="h-full p-6 transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl hover:shadow-purple-900/20 hover:border-neon-purple/50 bg-white/5 hover:bg-white/10 relative overflow-hidden">
                                            {/* Hover glow effect */}
                                            <div className="absolute inset-0 bg-gradient-to-br from-neon-purple/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none"></div>

                                            <div className="relative z-10 flex flex-col h-full">
                                                <div className="flex justify-between items-start mb-4">
                                                    <div className="p-3 bg-indigo-500/20 rounded-lg group-hover:bg-indigo-500/30 transition-colors">
                                                        <svg className="w-6 h-6 text-indigo-400 group-hover:text-indigo-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                                        </svg>
                                                    </div>
                                                    <span className="text-xs font-mono text-gray-500 bg-black/20 px-2 py-1 rounded border border-white/5">
                                                        {new Date(summary.createdAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                                                    </span>
                                                </div>

                                                <h3 className="font-bold text-xl text-white mb-3 line-clamp-1 group-hover:text-neon-purple transition-colors">
                                                    {summary.documentTitle || 'Untitled Document'}
                                                </h3>

                                                <p className="text-gray-400 text-sm mb-6 line-clamp-3 leading-relaxed flex-grow">
                                                    {summary.content?.executiveSummary || 'No summary preview available.'}
                                                </p>

                                                <div className="flex items-center text-sm font-medium text-neon-purple group-hover:text-neon-pink transition-colors mt-auto">
                                                    View Analysis
                                                    <svg className="w-4 h-4 ml-2 transform group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 8l4 4m0 0l-4 4m4-4H3" />
                                                    </svg>
                                                </div>
                                            </div>
                                        </Card>
                                    </Link>
                                    {/* Delete button - positioned absolutely */}
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            setDeleteConfirm(summary.id);
                                        }}
                                        className="absolute top-4 right-4 z-20 p-2 rounded-lg bg-red-500/20 text-red-400 hover:bg-red-500/40 transition-colors sm:opacity-0 sm:group-hover:opacity-100"
                                    >
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                            ))}
                        </div>

                        {/* Delete Confirmation Modal */}
                        {deleteConfirm && (
                            <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
                                <Card className="p-8 max-w-sm w-full border-red-500/30 bg-red-500/10">
                                    <div className="flex items-center justify-center w-12 h-12 mx-auto mb-4 rounded-full bg-red-500/20">
                                        <svg className="w-6 h-6 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </div>
                                    <h3 className="text-xl font-bold text-white text-center mb-2">Delete Summary?</h3>
                                    <p className="text-gray-400 text-center mb-6">This action cannot be undone. This will permanently delete the summary and its data.</p>
                                    <div className="flex gap-3">
                                        <Button
                                            onClick={() => setDeleteConfirm(null)}
                                            variant="secondary"
                                            className="flex-1"
                                            disabled={deleting}
                                        >
                                            Cancel
                                        </Button>
                                        <Button
                                            onClick={() => handleDeleteSummary(deleteConfirm)}
                                            className="flex-1 !bg-red-600 hover:!bg-red-700"
                                            disabled={deleting}
                                        >
                                            {deleting ? 'Deleting...' : 'Delete'}
                                        </Button>
                                    </div>
                                </Card>
                            </div>
                        )}
                    </>
                )}
            </main>
        </div>
    );
};

export default Dashboard;
