import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { summaryService } from '../services/summaryService';
import Button from '../components/common/Button';
import Card from '../components/common/Card';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const [summaries, setSummaries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

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

    return (
        <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50">
            {/* Header */}
            <header className="bg-white border-b border-gray-200 shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 bg-gradient-to-br from-indigo-600 to-purple-600 rounded-lg flex items-center justify-center">
                                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                                </svg>
                            </div>
                            <h1 className="text-xl font-bold text-gray-900">AI Study Summarizer</h1>
                        </div>
                        <div className="flex items-center space-x-4">
                            <span className="text-gray-600">Welcome, {user?.email || 'User'}</span>
                            <Button variant="secondary" onClick={logout}>
                                Logout
                            </Button>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8 flex items-center justify-between">
                    <div>
                        <h2 className="text-3xl font-bold text-gray-900">Your Summaries</h2>
                        <p className="text-gray-600 mt-1">Manage and review your study materials</p>
                    </div>
                    <Link to="/upload">
                        <Button>
                            <span className="flex items-center">
                                <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                                </svg>
                                New Summary
                            </span>
                        </Button>
                    </Link>
                </div>

                {loading ? (
                    <div className="text-center py-12">
                        <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
                        <p className="mt-4 text-gray-600">Loading summaries...</p>
                    </div>
                ) : error ? (
                    <Card className="p-8 text-center">
                        <p className="text-red-600">{error}</p>
                    </Card>
                ) : summaries.length === 0 ? (
                    <Card className="p-12 text-center">
                        <svg className="w-16 h-16 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <h3 className="text-xl font-semibold text-gray-900 mb-2">No summaries yet</h3>
                        <p className="text-gray-600 mb-6">Upload your first document to get started</p>
                        <Link to="/upload">
                            <Button>Create Your First Summary</Button>
                        </Link>
                    </Card>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {summaries.map((summary) => (
                            <Link key={summary.id} to={`/results/${summary.id}`}>
                                <Card className="p-6 hover:shadow-2xl transition-shadow cursor-pointer">
                                    <h3 className="font-semibold text-lg text-gray-900 mb-2">{summary.title || 'Untitled'}</h3>
                                    <p className="text-gray-600 text-sm mb-4 line-clamp-2">{summary.summary || 'No description'}</p>
                                    <div className="flex items-center justify-between text-sm text-gray-500">
                                        <span>{new Date(summary.createdAt).toLocaleDateString()}</span>
                                        <span className="text-indigo-600 font-medium">View →</span>
                                    </div>
                                </Card>
                            </Link>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

export default Dashboard;
