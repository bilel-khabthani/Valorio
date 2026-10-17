export interface CodeGenerationRequest {
  task: string;
  language: string;
  context?: string;
}

export interface CodeGenerationResponse {
  code: string;
  language: string;
  explanation: string;
  assumptions: string[];
  potentialRisks: string[];
  suggestedImprovements: string[];
}

export interface TestGenerationRequest {
  code: string;
  language: string;
  framework?: string;
  testType?: string;
  context?: string;
}

export interface TestGenerationResponse {
  generatedTests: string;
  framework: string;
  explanation: string;
  testCasesCovered: string[];
  edgeCases: string[];
  suggestions: string[];
}

export interface ReviewFinding {
  id: string;
  category: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  description: string;
  evidence: string;
  lineReference?: string;
  suggestedCorrection: string;
  source: string;
  confidenceLevel: number;
}

export interface CodeReviewRequest {
  code: string;
  language: string;
  context?: string;
}

export interface CodeReviewResponse {
  summary: string;
  overallScore: number;
  findings: ReviewFinding[];
  recommendations: string[];
}

export interface QualityFinding {
  id: string;
  category: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  description: string;
  evidence: string;
  lineReference?: string;
  suggestedCorrection: string;
  source: 'STATIC_ANALYSIS' | 'AI_PREDICTED';
  confidenceLevel: number;
}

export interface QualityReportRequest {
  code: string;
  language: string;
  includeStaticAnalysis?: boolean;
  includeAiReview?: boolean;
}

export interface QualityReportResponse {
  reportId: string;
  timestamp: string;
  summary: string;
  status: 'PASSED' | 'WARNING' | 'FAILED';
  metrics: {
    totalFindings: number;
    criticalCount: number;
    highCount: number;
    mediumCount: number;
    lowCount: number;
  };
  findings: QualityFinding[];
}
