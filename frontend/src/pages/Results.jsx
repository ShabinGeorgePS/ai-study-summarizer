import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { summaryService } from '../services/summaryService';
import api from '../services/api';
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
    const [generatingMore, setGeneratingMore] = useState(null); // Track which content type is being generated
    const [generateError, setGenerateError] = useState('');
    const [userAnswers, setUserAnswers] = useState({}); // Track user answers for MCQs

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

    const handleGenerateMore = async (type) => {
        try {
            setGeneratingMore(type);
            setGenerateError('');
            const response = await api.post(`/summaries/${id}/generate-more/${type}`);
            setSummary(response.data);
        } catch (err) {
            setGenerateError(`Error generating more ${type}: ${err.message}`);
            console.error(err);
        } finally {
            setGeneratingMore(null);
        }
    };

    const handleSelectAnswer = (mcqIndex, selectedOption) => {
        setUserAnswers({
            ...userAnswers,
            [mcqIndex]: selectedOption
        });
    };

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center relative">
                    <div className="absolute inset-0 bg-neon-purple/50 rounded-full blur-xl animate-pulse"></div>
                    <div className="relative inline-block animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-neon-purple"></div>
                    <p className="mt-6 text-gray-400 font-medium">Decoding knowledge...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4">
                <Card className="p-8 text-center border-red-500/30 bg-red-500/10 max-w-lg w-full">
                    <div className="w-16 h-16 mx-auto mb-4 bg-red-500/20 rounded-full flex items-center justify-center">
                        <svg className="w-8 h-8 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                        </svg>
                    </div>
                    <p className="text-red-200 mb-6 text-lg">{error}</p>
                    <Button onClick={() => navigate('/dashboard')} variant="secondary" className="w-full">
                        Return to Dashboard
                    </Button>
                </Card>
            </div>
        );
    }

    return (
        <div className="min-h-screen pb-20">
            {/* Header */}
            <header className="glass-panel border-b border-white/5 sticky top-0 z-50 backdrop-blur-xl">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
                    <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-3 cursor-pointer" onClick={() => navigate('/dashboard')}>
                            <div className="w-10 h-10 bg-gradient-to-br from-neon-purple to-neon-pink rounded-xl flex items-center justify-center shadow-lg shadow-purple-900/50 hover:shadow-purple-700/50 transition-shadow">
                                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                                </svg>
                            </div>
                            <h1 className="text-xl font-bold text-white hidden sm:block">AI Study Summarizer</h1>
                        </div>
                        <div className="flex items-center space-x-4">
                            <Button variant="secondary" onClick={() => navigate('/dashboard')} className="!px-4 !py-2 text-sm">
                                ← Dashboard
                            </Button>
                            <Button variant="secondary" onClick={logout} className="!px-4 !py-2 text-sm">
                                Logout
                            </Button>
                        </div>
                    </div>
                </div>
            </header>

            {/* Main Content */}
            <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8 animate-[float_6s_ease-in-out_infinite]">
                    <h2 className="text-2xl sm:text-3xl lg:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-white to-gray-400 mb-2">
                        {summary?.documentTitle || 'Study Summary'}
                    </h2>
                    <p className="text-xs sm:text-sm text-gray-400 flex items-center">
                        <svg className="w-4 h-4 mr-2 text-neon-purple" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                        Generated on {new Date(summary?.createdAt).toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                    </p>
                </div>

                {/* Tab Navigation */}
                <div className="flex space-x-2 mb-8 bg-white/5 p-1 rounded-xl backdrop-blur-md border border-white/10">
                    {['summary', 'mcqs', 'flashcards'].map((tab) => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`flex-1 py-3 px-4 rounded-lg font-medium transition-all duration-300 capitalize ${activeTab === tab
                                    ? 'bg-neon-purple text-white shadow-lg shadow-purple-900/50 scale-100'
                                    : 'text-gray-400 hover:text-white hover:bg-white/5'
                                }`}
                        >
                            {tab === 'mcqs' ? 'MCQs' : tab}
                        </button>
                    ))}
                </div>

                {/* Tab Content */}
                <Card className="min-h-[350px] sm:min-h-[400px] relative overflow-hidden backdrop-blur-xl bg-white/5 border-white/10 shadow-2xl p-4 sm:p-6">
                    {/* Ambient Glow */}
                    <div className="absolute top-0 right-0 w-96 h-96 bg-neon-purple/5 rounded-full blur-[100px] pointer-events-none"></div>

                    {activeTab === 'summary' && (
                        <div className="animate-[fadeIn_0.5s_ease-out]">
                            <h3 className="text-xl sm:text-2xl font-bold text-white mb-6 flex items-center">
                                <span className="w-6 sm:w-8 h-1 bg-neon-purple mr-4 rounded-full"></span>
                                Executive Summary
                            </h3>
                            <div className="prose prose-invert max-w-none mb-8">
                                <p className="text-sm sm:text-base lg:text-lg text-gray-300 leading-relaxed whitespace-pre-line">
                                    {summary?.content?.executiveSummary || 'No summary available'}
                                </p>
                            </div>
                            <div className="flex gap-3">
                                <Button
                                    onClick={() => handleGenerateMore('summary')}
                                    disabled={generatingMore === 'summary'}
                                    className="!px-6 !py-3"
                                >
                                    {generatingMore === 'summary' ? (
                                        <>
                                            <span className="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
                                            Generating...
                                        </>
                                    ) : (
                                        '+ Generate More Summaries'
                                    )}
                                </Button>
                            </div>
                            {generateError && activeTab === 'summary' && (
                                <div className="mt-4 p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-300 text-sm">
                                    {generateError}
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'mcqs' && (
                        <div className="animate-[fadeIn_0.5s_ease-out]">
                            <h3 className="text-2xl font-bold text-white mb-8 flex items-center">
                                <span className="w-8 h-1 bg-neon-pink mr-4 rounded-full"></span>
                                Knowledge Check - Answer the Questions
                            </h3>
                            {summary?.content?.mcqs && summary.content.mcqs.length > 0 ? (
                                <div className="space-y-6">
                                    {summary.content.mcqs.map((mcq, index) => {
                                        const userAnswer = userAnswers[index];
                                        const isAnswered = userAnswer !== undefined;
                                        const isCorrect = userAnswer === mcq.answer;

                                        return (
                                            <div key={index} className="p-6 rounded-xl bg-white/5 border border-white/5 hover:border-neon-purple/30 transition-colors">
                                                <div className="flex items-start mb-4">
                                                    <span className="flex-shrink-0 w-8 h-8 flex items-center justify-center rounded-full bg-neon-purple/20 text-neon-purple font-bold mr-4 text-sm">
                                                        {index + 1}
                                                    </span>
                                                    <p className="text-lg font-medium text-white">
                                                        {mcq.question}
                                                    </p>
                                                </div>
                                                <div className="pl-12 space-y-3">
                                                    {mcq.options.map((option, optIndex) => {
                                                        const isSelectedByUser = userAnswer === option;
                                                        const isCorrectOption = option === mcq.answer;

                                                        let bgColor = 'bg-black/20 border-white/5 hover:bg-white/5';
                                                        let cursorStyle = 'cursor-pointer';

                                                        if (isAnswered) {
                                                            if (isCorrectOption) {
                                                                bgColor = 'bg-green-500/20 border-green-500/50';
                                                                cursorStyle = '';
                                                            } else if (isSelectedByUser && !isCorrect) {
                                                                bgColor = 'bg-red-500/20 border-red-500/50';
                                                                cursorStyle = '';
                                                            } else {
                                                                bgColor = 'bg-black/20 border-white/5opacity-50';
                                                                cursorStyle = '';
                                                            }
                                                        }

                                                        return (
                                                            <div
                                                                key={optIndex}
                                                                onClick={() => !isAnswered && handleSelectAnswer(index, option)}
                                                                className={`flex items-center p-3 rounded-lg border ${bgColor} transition-colors ${cursorStyle}`}
                                                            >
                                                                <div className={`w-4 h-4 rounded-full border mr-3 flex-shrink-0 ${
                                                                    isAnswered && isCorrectOption
                                                                        ? 'bg-green-500 border-green-500'
                                                                        : isAnswered && isSelectedByUser && !isCorrect
                                                                        ? 'bg-red-500 border-red-500'
                                                                        : isSelectedByUser
                                                                        ? 'bg-white/30 border-white/50'
                                                                        : 'border-gray-500'
                                                                }`}></div>
                                                                <span className="text-gray-300">{option}</span>
                                                                {isAnswered && isCorrectOption && (
                                                                    <svg className="w-5 h-5 ml-auto text-green-400" fill="currentColor" viewBox="0 0 20 20">
                                                                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                                                    </svg>
                                                                )}
                                                                {isAnswered && isSelectedByUser && !isCorrect && (
                                                                    <svg className="w-5 h-5 ml-auto text-red-400" fill="currentColor" viewBox="0 0 20 20">
                                                                        <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                                                                    </svg>
                                                                )}
                                                            </div>
                                                        );
                                                    })}
                                                </div>

                                                {!isAnswered && (
                                                    <div className="mt-4 p-3 rounded-lg bg-blue-500/10 border border-blue-500/20 text-blue-300 text-sm text-center font-medium">
                                                        Click an option to answer
                                                    </div>
                                                )}

                                                {isAnswered && (
                                                    <>
                                                        <div className={`mt-4 p-4 rounded-lg border ${isCorrect
                                                            ? 'bg-green-500/10 border-green-500/20 text-green-300'
                                                            : 'bg-red-500/10 border-red-500/20 text-red-300'
                                                        } text-sm font-medium flex items-center`}>
                                                            {isCorrect ? (
                                                                <>
                                                                    <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                                                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                                                                    </svg>
                                                                    Correct Answer!
                                                                </>
                                                            ) : (
                                                                <>
                                                                    <svg className="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 20 20">
                                                                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                                                                    </svg>
                                                                    Incorrect
                                                                </>
                                                            )}
                                                        </div>

                                                        <div className="mt-3 p-4 rounded-lg bg-amber-500/10 border border-amber-500/20 text-amber-100 text-sm">
                                                            <p className="font-semibold text-amber-300 mb-1">Explanation:</p>
                                                            <p>{mcq.explanation}</p>
                                                        </div>
                                                    </>
                                                )}
                                            </div>
                                        );
                                    })}
                                </div>
                            ) : (
                                <div className="text-center py-12 text-gray-400">
                                    <svg className="w-16 h-16 mx-auto mb-4 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                    </svg>
                                    No MCQs available
                                </div>
                            )}
                        </div>
                    )}

                    {activeTab === 'flashcards' && (
                        <div className="animate-[fadeIn_0.5s_ease-out]">
                            <h3 className="text-2xl font-bold text-white mb-8 flex items-center">
                                <span className="w-8 h-1 bg-gradient-to-r from-neon-purple to-neon-pink mr-4 rounded-full"></span>
                                Flashcards
                            </h3>
                            {summary?.content?.flashcards && summary.content.flashcards.length > 0 ? (
                                <>
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                                        {summary.content.flashcards.map((card, index) => (
                                            <div key={index} className="group perspective-1000">
                                                <div className="relative p-6 h-64 rounded-xl bg-gradient-to-br from-white/5 to-white/0 border border-white/10 hover:border-neon-pink/50 transition-all duration-500 hover:shadow-2xl hover:shadow-pink-900/20 overflow-hidden flex flex-col justify-between group-hover:-translate-y-1">

                                                    {/* Card Back (Answer) - Revealed on hover via opacity/transform tricks usually, but keeping it simple layout for now */}
                                                    <div className="absolute inset-0 bg-black/90 flex flex-col items-center justify-center p-6 opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-20 text-center">
                                                        <h4 className="text-pink-400 font-bold text-sm uppercase tracking-wider mb-2">Answer</h4>
                                                        <p className="text-white font-medium text-lg leading-relaxed">{card.back}</p>
                                                    </div>

                                                    {/* Card Front */}
                                                    <div>
                                                        <h4 className="text-purple-400 font-bold text-sm uppercase tracking-wider mb-3">Question {index + 1}</h4>
                                                        <p className="text-white font-medium text-xl leading-relaxed">{card.front}</p>
                                                    </div>

                                                    <div className="text-center mt-auto opacity-50 text-xs text-gray-400 group-hover:opacity-0 transition-opacity">
                                                        Hover to reveal answer
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    <div className="flex gap-3">
                                        <Button
                                            onClick={() => handleGenerateMore('flashcards')}
                                            disabled={generatingMore === 'flashcards'}
                                            className="!px-6 !py-3"
                                        >
                                            {generatingMore === 'flashcards' ? (
                                                <>
                                                    <span className="inline-block animate-spin rounded-full h-4 w-4 border-t-2 border-b-2 border-white mr-2"></span>
                                                    Generating...
                                                </>
                                            ) : (
                                                '+ Generate More Flashcards'
                                            )}
                                        </Button>
                                    </div>
                                    {generateError && activeTab === 'flashcards' && (
                                        <div className="mt-4 p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-300 text-sm">
                                            {generateError}
                                        </div>
                                    )}
                                </>
                            ) : (
                                <div className="text-center py-12 text-gray-400">
                                    <svg className="w-16 h-16 mx-auto mb-4 opacity-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                                    </svg>
                                    No flashcards available
                                </div>
                            )}
                        </div>
                    )}
                </Card>
            </main>
        </div>
    );
};

export default Results;
