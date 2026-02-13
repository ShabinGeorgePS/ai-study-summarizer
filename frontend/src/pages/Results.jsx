import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { summaryService } from '../services/summaryService';
import Button from '../components/common/Button';
import Card from '../components/common/Card';

const Results = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { logout } = useAuth();

    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [activeTab, setActiveTab] = useState('summary'); // 'summary', 'mcqs', 'flashcards'

    useEffect(() => {
        fetchSummary();
    }, [id]);

    const fetchSummary = async () => {
        try {
            setLoading(true);
            const data = await summaryService.getSummaryById(id);
            setSummary(data);
        } catch (err) {
            setError('Failed to load summary');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50">
                <div className="text-center">
                    <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
                    <p className="mt-4 text-gray-600">Loading summary...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50">
                <Card className="p-8 text-center">
                    <p className="text-red-600 mb-4">{error}</p>
                    <Button onClick={() => navigate('/dashboard')}>Back to Dashboard</Button>
                </Card>
            </div>
        );
    }

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
                            <Button variant="secondary" onClick={() => navigate('/dashboard')}>
                                Back to Dashboard
                            </Button>
                            <Button variant="secondary" onClick={logout}>
                                Logout
                            </Button>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-6">
                    <h2 className="text-3xl font-bold text-gray-900 mb-2">{summary?.title || 'Study Summary'}</h2>
                    <p className="text-gray-600">Generated on {new Date(summary?.createdAt).toLocaleDateString()}</p>
                </div>

                {/* Tab Navigation */}
                <div className="flex space-x-2 mb-6 bg-white p-1 rounded-lg shadow-sm border border-gray-200">
                    <button
                        onClick={() => setActiveTab('summary')}
                        className={`flex-1 py-3 px-4 rounded-md font-medium transition-all ${activeTab === 'summary'
                                ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md'
                                : 'text-gray-600 hover:text-gray-900'
                            }`}
                    >
                        Executive Summary
                    </button>
                    <button
                        onClick={() => setActiveTab('mcqs')}
                        className={`flex-1 py-3 px-4 rounded-md font-medium transition-all ${activeTab === 'mcqs'
                                ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md'
                                : 'text-gray-600 hover:text-gray-900'
                            }`}
                    >
                        MCQs
                    </button>
                    <button
                        onClick={() => setActiveTab('flashcards')}
                        className={`flex-1 py-3 px-4 rounded-md font-medium transition-all ${activeTab === 'flashcards'
                                ? 'bg-gradient-to-r from-indigo-600 to-purple-600 text-white shadow-md'
                                : 'text-gray-600 hover:text-gray-900'
                            }`}
                    >
                        Flashcards
                    </button>
                </div>

                {/* Tab Content */}
                <Card className="p-8">
                    {activeTab === 'summary' && (
                        <div>
                            <h3 className="text-2xl font-bold text-gray-900 mb-4">Executive Summary</h3>
                            <div className="prose max-w-none">
                                <p className="text-gray-700 leading-relaxed">
                                    {summary?.executiveSummary || summary?.summary || 'No summary available'}
                                </p>
                            </div>
                        </div>
                    )}

                    {activeTab === 'mcqs' && (
                        <div>
                            <h3 className="text-2xl font-bold text-gray-900 mb-6">Multiple Choice Questions</h3>
                            {summary?.mcqs && summary.mcqs.length > 0 ? (
                                <div className="space-y-6">
                                    {summary.mcqs.map((mcq, index) => (
                                        <div key={index} className="border-l-4 border-indigo-600 pl-4">
                                            <p className="font-semibold text-gray-900 mb-3">
                                                {index + 1}. {mcq.question}
                                            </p>
                                            <div className="space-y-2">
                                                {mcq.options.map((option, optIndex) => (
                                                    <div key={optIndex} className="flex items-center">
                                                        <span className="text-gray-700">{option}</span>
                                                    </div>
                                                ))}
                                            </div>
                                            <p className="mt-2 text-sm text-green-600 font-medium">
                                                Answer: {mcq.correctAnswer}
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="text-gray-600">No MCQs available</p>
                            )}
                        </div>
                    )}

                    {activeTab === 'flashcards' && (
                        <div>
                            <h3 className="text-2xl font-bold text-gray-900 mb-6">Flashcards</h3>
                            {summary?.flashcards && summary.flashcards.length > 0 ? (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                    {summary.flashcards.map((card, index) => (
                                        <div key={index} className="bg-gradient-to-br from-indigo-50 to-purple-50 p-6 rounded-lg border-2 border-indigo-200">
                                            <p className="font-semibold text-gray-900 mb-2">Q: {card.question}</p>
                                            <p className="text-gray-700">A: {card.answer}</p>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="text-gray-600">No flashcards available</p>
                            )}
                        </div>
                    )}
                </Card>
            </main>
        </div>
    );
};

export default Results;
