import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ExplanationPlayer } from '../ExplanationPlayer';
import { ExplanationIR } from '../../types/ir';

describe('ExplanationPlayer Component', () => {
  const mockExplanation: ExplanationIR = {
    schemaVersion: '1.0.0',
    explanationId: 'exp-test-01',
    questionId: 'q-test-01',
    status: 'APPROVED',
    problem: {
      statement: 'Solve 2x + 4 = 10',
      domain: 'GENERAL',
    },
    concepts: ['linear-equation'],
    steps: [
      {
        id: 'step-1',
        sequence: 1,
        type: 'TRANSFORM',
        before: '2x + 4 = 10',
        after: '2x = 6',
        justification: {
          text: 'Subtract 4 from both sides',
        },
      },
    ],
  };

  it('renders complete explanation player layout', () => {
    render(<ExplanationPlayer explanation={mockExplanation} />);
    expect(screen.getByText(/Solve 2x \+ 4 = 10/i)).toBeInTheDocument();
    expect(screen.getByText(/linear-equation/i)).toBeInTheDocument();
    expect(screen.getByText(/APPROVED/i)).toBeInTheDocument();
  });
});
