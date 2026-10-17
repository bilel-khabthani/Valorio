import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CodeGenerationRequest,
  CodeGenerationResponse,
  CodeReviewRequest,
  CodeReviewResponse,
  QualityReportRequest,
  QualityReportResponse,
  TestGenerationRequest,
  TestGenerationResponse,
} from './ai-models';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/ai';

  generateCode(request: CodeGenerationRequest): Observable<CodeGenerationResponse> {
    return this.http.post<CodeGenerationResponse>(`${this.baseUrl}/code-generation`, request);
  }

  generateTests(request: TestGenerationRequest): Observable<TestGenerationResponse> {
    return this.http.post<TestGenerationResponse>(`${this.baseUrl}/tests/generate`, request);
  }

  reviewCode(request: CodeReviewRequest): Observable<CodeReviewResponse> {
    return this.http.post<CodeReviewResponse>(`${this.baseUrl}/code-review`, request);
  }

  generateQualityReport(request: QualityReportRequest): Observable<QualityReportResponse> {
    return this.http.post<QualityReportResponse>(`${this.baseUrl}/quality-report`, request);
  }
}
