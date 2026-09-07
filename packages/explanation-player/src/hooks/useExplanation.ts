import { useState, useEffect } from 'react';
import { ExplanationIR } from '../types/ir';

export function useExplanation(initialExplanation?: ExplanationIR, questionId?: string) {
  const [explanation, setExplanation] = useState<ExplanationIR | null>(initialExplanation || null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (initialExplanation) {
      setExplanation(initialExplanation);
      return;
    }

    if (questionId) {
      setLoading(true);
      setError(null);
      fetch(`/api/v1/explanations/${questionId}`)
        .then((res) => {
          if (!res.ok) {
            throw new Error(`Failed to load explanation (${res.status} ${res.statusText})`);
          }
          return res.json();
        })
        .then((data: ExplanationIR) => {
          setExplanation(data);
          setLoading(false);
        })
        .catch((err) => {
          setError(err.message || 'Error fetching explanation');
          setLoading(false);
        });
    }
  }, [initialExplanation, questionId]);

  return { explanation, loading, error, setExplanation };
}