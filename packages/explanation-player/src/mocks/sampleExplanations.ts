import { ExplanationIR } from '../types/ir';

export const SAMPLE_ALGEBRA: ExplanationIR = {
  schemaVersion: '1.0',
  explanationId: 'exp-alg-001',
  questionId: 'q-alg-101',
  status: 'APPROVED',
  problem: {
    statement: 'Solve for x: 3x + 5 = 20',
    domain: 'ALGEBRA',
    difficulty: 'MEDIUM',
    authoritativeAnswer: '5',
  },
  concepts: ['linear-equation', 'inverse-operation', 'variable-isolation'],
  facts: ['Given equation: 3x + 5 = 20'],
  steps: [
    {
      id: 's1',
      sequence: 1,
      type: 'TRANSFORM',
      operation: { name: 'SUBTRACT', target: 'both-sides', value: 5 },
      before: '3x + 5 = 20',
      after: '3x = 15',
      justification: { text: 'Subtract 5 from both sides to isolate the variable term 3x.' },
      representations: ['TEXT', 'MATH', 'ANIMATION'],
      math: { latex: '3x + 5 - 5 = 20 - 5 \\implies 3x = 15' },
    },
    {
      id: 's2',
      sequence: 2,
      type: 'TRANSFORM',
      operation: { name: 'DIVIDE', target: 'both-sides', value: 3 },
      before: '3x = 15',
      after: 'x = 5',
      justification: { text: 'Divide both sides by 3 to solve for x.' },
      representations: ['TEXT', 'MATH', 'ANIMATION'],
      math: { latex: '\\frac{3x}{3} = \\frac{15}{3} \\implies x = 5' },
    },
    {
      id: 's3',
      sequence: 3,
      type: 'VERIFY',
      before: 'x = 5',
      after: '3 * 5 + 5 = 20',
      justification: { text: 'Substitute x = 5 back into original equation to confirm equality.' },
      representations: ['TEXT', 'MATH'],
      math: { latex: '3(5) + 5 = 15 + 5 = 20' },
    },
  ],
  reasoningGraph: {
    nodes: [
      { id: 'f1', type: 'FACT', content: '3x + 5 = 20' },
      { id: 's1', type: 'RESULT', content: '3x = 15' },
      { id: 's2', type: 'CONCLUSION', content: 'x = 5' },
      { id: 's3', type: 'VERIFICATION', content: '20 = 20' },
    ],
    edges: [
      { from: 'f1', to: 's1', relationship: 'DERIVED_FROM' },
      { from: 's1', to: 's2', relationship: 'DERIVED_FROM' },
      { from: 's2', to: 's3', relationship: 'SUPPORTS' },
    ],
  },
  verification: {
    type: 'SUBSTITUTION',
    expression: '3 * 5 + 5 = 20',
    passed: true,
    details: 'Verified: 3(5) + 5 evaluates exactly to 20.',
  },
  hints: [
    { id: 'h1', level: 1, text: 'Look at the constant term on the side with x.' },
    { id: 'h2', level: 2, text: 'Use the inverse operation of addition (subtract 5 from both sides).' },
    { id: 'h3', level: 3, text: 'Divide both sides by the coefficient of x (3).' },
  ],
  misconceptions: [
    {
      id: 'm1',
      incorrectReasoning: 'Adding 5 to 20 resulting in 3x = 25',
      whyWrong: 'Adding does not cancel out the +5; inverse subtraction is required to balance the equation.',
      correctedReasoning: 'Subtract 5 from both sides: 20 - 5 = 15.',
    },
  ],
  metadata: {
    language: 'en',
    studentLevel: 'INTERMEDIATE',
    compilerVersion: '1.0.0',
    qualityScore: 0.98,
  },
};

export const SAMPLE_SYLLOGISM: ExplanationIR = {
  schemaVersion: '1.0',
  explanationId: 'exp-syl-002',
  questionId: 'q-syl-202',
  status: 'APPROVED',
  problem: {
    statement: 'John is taller than Alice. Alice is taller than Bob. Who is the tallest?',
    domain: 'LOGICAL_REASONING',
    difficulty: 'EASY',
    authoritativeAnswer: 'John',
  },
  concepts: ['transitive-relation', 'comparative-reasoning', 'deductive-logic'],
  facts: ['John > Alice', 'Alice > Bob'],
  steps: [
    {
      id: 's1',
      sequence: 1,
      type: 'GIVEN',
      after: 'Premise 1: John > Alice. Premise 2: Alice > Bob.',
      justification: { text: 'Extract comparative premises from statement.' },
      representations: ['TEXT', 'REASONING_GRAPH'],
    },
    {
      id: 's2',
      sequence: 2,
      type: 'INFER',
      before: 'John > Alice and Alice > Bob',
      after: 'Chain: John > Alice > Bob',
      justification: { text: 'Apply transitive property: if A > B and B > C then A > B > C.' },
      representations: ['TEXT', 'DIAGRAM'],
    },
    {
      id: 's3',
      sequence: 3,
      type: 'CONCLUDE',
      before: 'Chain: John > Alice > Bob',
      after: 'John is the tallest.',
      justification: { text: 'John occupies the highest position in the strict ordering chain.' },
      representations: ['TEXT'],
    },
  ],
  diagrams: [
    {
      id: 'diag-height',
      type: 'bar',
      title: 'Height Hierarchy Comparison',
      objects: [
        { id: 'John', type: 'bar', label: 'John', properties: { rank: 3 } },
        { id: 'Alice', type: 'bar', label: 'Alice', properties: { rank: 2 } },
        { id: 'Bob', type: 'bar', label: 'Bob', properties: { rank: 1 } },
      ],
      relationships: [
        { type: 'GREATER_THAN', from: 'John', to: 'Alice', label: 'taller' },
        { type: 'GREATER_THAN', from: 'Alice', to: 'Bob', label: 'taller' },
      ],
    },
  ],
  reasoningGraph: {
    nodes: [
      { id: 'f1', type: 'FACT', content: 'John > Alice' },
      { id: 'f2', type: 'FACT', content: 'Alice > Bob' },
      { id: 's2', type: 'RESULT', content: 'John > Alice > Bob' },
      { id: 's3', type: 'CONCLUSION', content: 'John is tallest' },
    ],
    edges: [
      { from: 'f1', to: 's2', relationship: 'DEPENDS_ON' },
      { from: 'f2', to: 's2', relationship: 'DEPENDS_ON' },
      { from: 's2', to: 's3', relationship: 'SUPPORTS' },
    ],
  },
  verification: {
    type: 'LOGICAL_CONSISTENCY',
    expression: 'John > Alice > Bob',
    passed: true,
  },
  hints: [
    { id: 'h1', level: 1, text: 'Connect the two sentences together into a single comparison chain.' },
    { id: 'h2', level: 2, text: 'Look at who has no one taller than them in the chain.' },
  ],
  metadata: {
    studentLevel: 'BEGINNER',
    qualityScore: 0.96,
  },
};