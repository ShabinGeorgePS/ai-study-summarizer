import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { documentService } from '../services/documentService';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Card from '../components/common/Card';

const Upload = () => {
    const navigate = useNavigate();
    const { logout } = useAuth();

    const [uploadType, setUploadType] = useState('pdf'); // 'pdf', 'image', 'url'
    const [file, setFile] = useState(null);
    const [url, setUrl] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];
        setFile(selectedFile);
        setError('');
    };

    const handleUrlChange = (e) => {
        setUrl(e.target.value);
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            let response;

            if (uploadType === 'url') {
                if (!url.trim()) {
                    setError('Please enter a URL');
                    setLoading(false);
                    return;
                }
                response = await documentService.uploadURL(url);
            } else {
                if (!file) {
                    setError('Please select a file');
                    setLoading(false);
                    return;
                }
                response = await documentService.uploadPDF(file);
            }

            // After upload, trigger summarization
            if (response.documentId) {
                const summaryResponse = await documentService.summarizeDocument(response.documentId);
                // Navigate to results page
                navigate(`/results/${summaryResponse.id}`);
            }
        } catch (err) {
            setError(err.message || 'Upload failed. Please try again.');
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
            <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
                <div className="text-center mb-8">
                    <h2 className="text-3xl font-bold text-gray-900 mb-2">Upload Study Material</h2>
                    <p className="text-gray-600">Upload a PDF, image, or provide a URL to generate a summary</p>
                </div>

                <Card className="p-8">
                    {/* Upload Type Selector */}
                    <div className="flex space-x-2 mb-6 bg-gray-100 p-1 rounded-lg">
                        <button
                            onClick={() => setUploadType('pdf')}
                            className={`flex-1 py-2 px-4 rounded-md font-medium transition-all ${uploadType === 'pdf'
                                    ? 'bg-white text-indigo-600 shadow-sm'
                                    : 'text-gray-600 hover:text-gray-900'
                                }`}
                        >
                            PDF
                        </button>
                        <button
                            onClick={() => setUploadType('image')}
                            className={`flex-1 py-2 px-4 rounded-md font-medium transition-all ${uploadType === 'image'
                                    ? 'bg-white text-indigo-600 shadow-sm'
                                    : 'text-gray-600 hover:text-gray-900'
                                }`}
                        >
                            Image
                        </button>
                        <button
                            onClick={() => setUploadType('url')}
                            className={`flex-1 py-2 px-4 rounded-md font-medium transition-all ${uploadType === 'url'
                                    ? 'bg-white text-indigo-600 shadow-sm'
                                    : 'text-gray-600 hover:text-gray-900'
                                }`}
                        >
                            URL
                        </button>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        {error && (
                            <div className="bg-red-50 border-2 border-red-200 text-red-700 px-4 py-3 rounded-lg">
                                {error}
                            </div>
                        )}

                        {uploadType === 'url' ? (
                            <Input
                                label="Document URL"
                                type="url"
                                value={url}
                                onChange={handleUrlChange}
                                placeholder="https://example.com/document.pdf"
                            />
                        ) : (
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    Select {uploadType === 'pdf' ? 'PDF' : 'Image'} File
                                </label>
                                <div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-indigo-500 transition-colors">
                                    <input
                                        type="file"
                                        accept={uploadType === 'pdf' ? '.pdf' : 'image/*'}
                                        onChange={handleFileChange}
                                        className="hidden"
                                        id="file-upload"
                                    />
                                    <label htmlFor="file-upload" className="cursor-pointer">
                                        <svg className="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                                        </svg>
                                        <p className="text-gray-600 mb-2">
                                            {file ? file.name : 'Click to upload or drag and drop'}
                                        </p>
                                        <p className="text-sm text-gray-500">
                                            {uploadType === 'pdf' ? 'PDF files only' : 'PNG, JPG, JPEG up to 10MB'}
                                        </p>
                                    </label>
                                </div>
                            </div>
                        )}

                        <Button
                            type="submit"
                            disabled={loading}
                            className="w-full"
                        >
                            {loading ? (
                                <span className="flex items-center justify-center">
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Processing...
                                </span>
                            ) : (
                                'Generate Summary'
                            )}
                        </Button>
                    </form>
                </Card>
            </main>
        </div>
    );
};

export default Upload;
