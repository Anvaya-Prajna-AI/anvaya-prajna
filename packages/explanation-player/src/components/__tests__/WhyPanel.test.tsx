import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { WhyPanel } from '../WhyPanel';
import { ReasoningStep } from '../../types/ir';

describe('WhyPanel Component', () => {
  it('renders null when currentStep is missing', () => {
    const { container } = render(<WhyPanel />);
    expect(container.firstChild).toBeNull();
  });

  it('renders justification text when toggled open', () => {
    const step: ReasoningStep = {
      id: 'step-1',
      sequence: 1,
      type: 'TRANSFORM',
      justification: {
        text: 'Subtraction Property of Equality',
        depth: 1,
      },
    };

    render(<WhyPanel currentStep={step} />);
    expect(screen.getByText(/Why this step\?/i)).toBeInTheDocument();

    // Click to toggle open
    fireEvent.click(screen.getByRole('button'));
    expect(screen.getByText(/Subtraction Property of Equality/i)).toBeInTheDocument();
  });
});
