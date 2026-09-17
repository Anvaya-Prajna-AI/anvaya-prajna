import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { VerificationPanel } from '../VerificationPanel';
import { VerificationResult } from '../../types/ir';

describe('VerificationPanel Component', () => {
  it('renders null when verification is not provided', () => {
    const { container } = render(<VerificationPanel />);
    expect(container.firstChild).toBeNull();
  });

  it('renders verified badge and details when passed is true', () => {
    const verification: VerificationResult = {
      passed: true,
      type: 'SUBSTITUTION',
      expression: '3(5) + 5 = 20',
      expected: true,
      actual: true,
      details: 'Evaluated to equal values on both sides',
    };

    render(<VerificationPanel verification={verification} />);
    expect(screen.getByText(/Mathematically Verified/i)).toBeInTheDocument();
    expect(screen.getByText(/SUBSTITUTION/i)).toBeInTheDocument();
    expect(screen.getByText(/3\(5\) \+ 5 = 20/i)).toBeInTheDocument();
  });
});
