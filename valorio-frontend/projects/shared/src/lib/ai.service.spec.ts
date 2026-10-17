import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AiService } from './ai.service';
import { CodeGenerationRequest, QualityReportRequest, TestGenerationRequest } from './ai-models';

describe('AiService', () => {
  let service: AiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AiService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('appelle l endpoint de génération de code avec la charge utile attendue', () => {
    const reqData: CodeGenerationRequest = {
      task: 'Créer un service Spring Boot',
      language: 'java',
      context: 'JDBC',
    };

    service.generateCode(reqData).subscribe((res) => {
      expect(res.code).toContain('public class Service');
      expect(res.language).toBe('java');
    });

    const req = httpMock.expectOne('/api/ai/code-generation');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.task).toBe('Créer un service Spring Boot');

    req.flush({
      code: 'public class Service {}',
      language: 'java',
      explanation: 'Service créé',
      assumptions: [],
      potentialRisks: [],
      suggestedImprovements: [],
    });
  });

  it('appelle l endpoint de génération de tests', () => {
    const reqData: TestGenerationRequest = {
      code: 'class A {}',
      language: 'java',
      framework: 'JUnit 5',
    };

    service.generateTests(reqData).subscribe((res) => {
      expect(res.framework).toBe('JUnit 5');
    });

    const req = httpMock.expectOne('/api/ai/tests/generate');
    expect(req.request.method).toBe('POST');
    req.flush({
      generatedTests: 'class ATest {}',
      framework: 'JUnit 5',
      explanation: 'Suite de tests',
      testCasesCovered: ['Cas 1'],
      edgeCases: [],
      suggestions: [],
    });
  });

  it('génère un rapport de qualité complet', () => {
    const reqData: QualityReportRequest = {
      code: 'class Demo {}',
      language: 'java',
      includeStaticAnalysis: true,
      includeAiReview: true,
    };

    service.generateQualityReport(reqData).subscribe((res) => {
      expect(res.status).toBe('PASSED');
      expect(res.reportId).toBe('REP-TEST-123');
    });

    const req = httpMock.expectOne('/api/ai/quality-report');
    expect(req.request.method).toBe('POST');
    req.flush({
      reportId: 'REP-TEST-123',
      timestamp: '2026-10-17T12:00:00Z',
      summary: 'Audit réussi',
      status: 'PASSED',
      metrics: { totalFindings: 0, criticalCount: 0, highCount: 0, mediumCount: 0, lowCount: 0 },
      findings: [],
    });
  });
});
