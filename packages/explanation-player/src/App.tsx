import React, { useState } from 'react';
import { ExplanationPlayer } from './components/ExplanationPlayer';
import {
  SAMPLE_ALGEBRA,
  SAMPLE_SYLLOGISM,
  SAMPLE_PHYSICS,
  SAMPLE_CHEMISTRY,
} from './mocks/sampleExplanations';
import { ExplanationIR } from './types/ir';
import './styles/player.css';

export const App: React.FC = () => {
  const [selectedSample, setSelectedSample] = useState<
    'algebra' | 'syllogism' | 'physics' | 'chemistry' | 'custom'
  >('algebra');
  const [customExplanation, setCustomExplanation] = useState<ExplanationIR | null>(null);
  const [questionIdInput, setQuestionIdInput] = useState<string>('q-alg-101');
  const [loading, setLoading] = useState<boolean>(false);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [showJson, setShowJson] = useState<boolean>(false);

  const currentExplanation: ExplanationIR =
    selectedSample === 'algebra'
      ? SAMPLE_ALGEBRA
      : selectedSample === 'syllogism'
      ? SAMPLE_SYLLOGISM
      : selectedSample === 'physics'
      ? SAMPLE_PHYSICS
      : selectedSample === 'chemistry'
      ? SAMPLE_CHEMISTRY
      : customExplanation || SAMPLE_ALGEBRA;

  const handleSelectSample = (sample: 'algebra' | 'syllogism' | 'physics' | 'chemistry') => {
    setSelectedSample(sample);
    if (sample === 'algebra') setQuestionIdInput('q-alg-101');
    else if (sample === 'syllogism') setQuestionIdInput('q-syl-202');
    else if (sample === 'physics') setQuestionIdInput('q-phys-mechanics');
    else if (sample === 'chemistry') setQuestionIdInput('q-chem-stoich');
  };

  const handleFetchFromBackend = async () => {
    if (!questionIdInput.trim()) return;
    setLoading(true);
    setFetchError(null);
    try {
      const res = await fetch(`/api/v1/explanations/${encodeURIComponent(questionIdInput.trim())}`);
      if (!res.ok) {
        throw new Error(`API error ${res.status}: ${res.statusText}`);
      }
      const data: ExplanationIR = await res.json();
      setCustomExplanation(data);
      setSelectedSample('custom');
    } catch (err: any) {
      setFetchError(err.message || 'Failed to fetch explanation from Spring Boot service');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f1f5f9', padding: '32px 16px' }}>
      {/* Demo Top Control Bar */}
      <div
        style={{
          maxWidth: '880px',
          margin: '0 auto 20px auto',
          background: '#ffffff',
          padding: '16px 20px',
          borderRadius: '12px',
          border: '1px solid #e2e8f0',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '12px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          <span style={{ fontWeight: 700, color: '#334155', fontSize: '0.9rem' }}>Sample:</span>
          <button
            onClick={() => handleSelectSample('algebra')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              border: selectedSample === 'algebra' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
              background: selectedSample === 'algebra' ? '#eef2ff' : '#ffffff',
              color: selectedSample === 'algebra' ? '#4f46e5' : '#475569',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Algebra (3x + 5 = 20)
          </button>
          <button
            onClick={() => handleSelectSample('syllogism')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              border: selectedSample === 'syllogism' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
              background: selectedSample === 'syllogism' ? '#eef2ff' : '#ffffff',
              color: selectedSample === 'syllogism' ? '#4f46e5' : '#475569',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Syllogism (Heights)
          </button>
          <button
            onClick={() => handleSelectSample('physics')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              border: selectedSample === 'physics' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
              background: selectedSample === 'physics' ? '#eef2ff' : '#ffffff',
              color: selectedSample === 'physics' ? '#4f46e5' : '#475569',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Physics (FBD Mechanics)
          </button>
          <button
            onClick={() => handleSelectSample('chemistry')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              border: selectedSample === 'chemistry' ? '2px solid #4f46e5' : '1px solid #cbd5e1',
              background: selectedSample === 'chemistry' ? '#eef2ff' : '#ffffff',
              color: selectedSample === 'chemistry' ? '#4f46e5' : '#475569',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Chemistry (Stoichiometry & Energy)
          </button>
        </div>

        {/* Live API Fetcher */}
        <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
          <input
            type="text"
            placeholder="Live Question ID (e.g. q-alg-101)"
            value={questionIdInput}
            onChange={(e) => setQuestionIdInput(e.target.value)}
            style={{
              padding: '6px 10px',
              border: '1px solid #cbd5e1',
              borderRadius: '6px',
              fontSize: '0.85rem',
            }}
          />
          <button
            onClick={handleFetchFromBackend}
            disabled={loading}
            style={{
              padding: '6px 12px',
              background: '#0ea5e9',
              color: '#ffffff',
              border: 'none',
              borderRadius: '6px',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            {loading ? 'Fetching...' : 'Fetch from Backend'}
          </button>
          <button
            onClick={() => setShowJson(!showJson)}
            style={{
              padding: '6px 10px',
              background: '#f8fafc',
              border: '1px solid #cbd5e1',
              borderRadius: '6px',
              fontSize: '0.8rem',
              color: '#475569',
              cursor: 'pointer',
            }}
          >
            {showJson ? 'Hide IR JSON' : 'View IR JSON'}
          </button>
        </div>
      </div>

      {fetchError && (
        <div
          style={{
            maxWidth: '880px',
            margin: '0 auto 16px auto',
            padding: '12px 16px',
            background: '#fef2f2',
            border: '1px solid #fecaca',
            color: '#991b1b',
            borderRadius: '8px',
            fontSize: '0.85rem',
          }}
        >
          {fetchError} (Make sure the Spring Boot service is running on http://localhost:8080)
        </div>
      )}

      {/* JSON Viewer */}
      {showJson && (
        <pre
          style={{
            maxWidth: '880px',
            margin: '0 auto 20px auto',
            padding: '16px',
            background: '#0f172a',
            color: '#38bdf8',
            borderRadius: '10px',
            overflowX: 'auto',
            fontSize: '0.8rem',
            maxHeight: '300px',
          }}
        >
          {JSON.stringify(currentExplanation, null, 2)}
        </pre>
      )}

      {/* The Explanation Player Component */}
      <ExplanationPlayer explanation={currentExplanation} />
    </div>
  );
};
