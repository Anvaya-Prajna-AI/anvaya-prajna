export type ExplanationStatus =
  | 'DRAFT'
  | 'VALIDATING'
  | 'NEEDS_REVIEW'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'DEPRECATED'
  | 'REJECTED';

export type StudentLevel =
  | 'BEGINNER'
  | 'INTERMEDIATE'
  | 'ADVANCED'
  | 'EXAM'
  | 'EXPERT';

export type StepType =
  | 'GIVEN'
  | 'DEFINE'
  | 'OBSERVE'
  | 'IDENTIFY'
  | 'APPLY_RULE'
  | 'SUBSTITUTE'
  | 'CALCULATE'
  | 'TRANSFORM'
  | 'COMPARE'
  | 'ELIMINATE'
  | 'INFER'
  | 'CONCLUDE'
  | 'VERIFY';

export type RepresentationType =
  | 'TEXT'
  | 'MATH'
  | 'TABLE'
  | 'SVG'
  | 'DIAGRAM'
  | 'GEOMETRY'
  | 'REASONING_GRAPH'
  | 'MERMAID'
  | 'ANIMATION'
  | 'HIGHLIGHT'
  | 'AUDIO';

export type AnimationAction =
  | 'SHOW'
  | 'HIDE'
  | 'HIGHLIGHT'
  | 'EMPHASIZE'
  | 'FOCUS'
  | 'MOVE'
  | 'DRAW'
  | 'TRANSFORM'
  | 'REVEAL'
  | 'PAUSE'
  | 'RESET';

export interface AnimationEvent {
  at: number; // in milliseconds
  action: AnimationAction;
  target?: string;
  operation?: string;
  value?: any;
}

export interface AnimationIR {
  durationMs: number;
  timeline: AnimationEvent[];
}

export interface DiagramObject {
  id: string;
  type: string;
  label?: string;
  position?: number[];
  properties?: Record<string, any>;
}

export interface DiagramRelationship {
  type: string;
  from: string;
  to: string;
  label?: string;
}

export interface DiagramIR {
  id: string;
  type: string;
  title?: string;
  objects?: DiagramObject[];
  relationships?: DiagramRelationship[];
}

export interface MathExpression {
  rawText?: string;
  latex?: string;
  mathml?: string;
  semanticAst?: any;
}

export interface StepOperation {
  name: string;
  target?: string;
  value?: any;
}

export interface StepJustification {
  text: string;
  depth?: number;
}

export interface ReasoningStep {
  id: string;
  sequence: number;
  type: StepType;
  inputs?: string[];
  operation?: StepOperation;
  before?: string;
  after?: string;
  outputs?: string[];
  justification?: StepJustification;
  representations?: RepresentationType[];
  math?: MathExpression;
  verification?: VerificationResult;
  misconception?: Misconception;
}

export interface VerificationResult {
  type?: string;
  expression: string;
  expected?: boolean;
  actual?: boolean;
  passed: boolean;
  details?: string;
}

export interface Hint {
  id: string;
  level: number;
  text: string;
  revealStep?: number;
}

export interface Misconception {
  id: string;
  incorrectReasoning: string;
  whyWrong: string;
  correctedReasoning?: string;
}

export interface ReasoningGraphNode {
  id: string;
  type: 'FACT' | 'RULE' | 'RESULT' | 'CONCLUSION' | 'VERIFICATION';
  content: string;
  source?: string;
  confidence?: number;
  validationStatus?: string;
}

export interface ReasoningGraphEdge {
  from: string;
  to: string;
  relationship:
    | 'DEPENDS_ON'
    | 'DERIVED_FROM'
    | 'CONTRADICTS'
    | 'SUPPORTS'
    | 'REQUIRES'
    | 'ELIMINATES'
    | 'VERIFIES';
  justification?: string;
}

export interface ReasoningGraphIR {
  nodes: ReasoningGraphNode[];
  edges: ReasoningGraphEdge[];
}

export interface Problem {
  statement: string;
  domain?: string;
  difficulty?: string;
  authoritativeAnswer?: string;
  options?: Record<string, string>;
}

export interface ExplanationMetadata {
  language?: string;
  studentLevel?: StudentLevel;
  compilerVersion?: string;
  modelVersion?: string;
  generationLatencyMs?: number;
  qualityScore?: number;
}

export interface ExplanationPolicy {
  level?: StudentLevel;
  showFormula?: boolean;
  showEveryCalculation?: boolean;
  showWhy?: boolean;
  showVerification?: boolean;
  showMisconceptions?: boolean;
  animation?: 'NONE' | 'LIGHT' | 'FULL';
}

export interface ExplanationIR {
  schemaVersion: string;
  explanationId: string;
  questionId: string;
  status: ExplanationStatus;
  problem: Problem;
  concepts?: string[];
  facts?: string[];
  assumptions?: string[];
  reasoningGraph?: ReasoningGraphIR;
  steps: ReasoningStep[];
  verification?: VerificationResult;
  hints?: Hint[];
  misconceptions?: Misconception[];
  diagrams?: DiagramIR[];
  animation?: AnimationIR;
  metadata?: ExplanationMetadata;
}