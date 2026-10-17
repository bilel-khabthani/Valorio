import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AiService,
  QualityFinding,
  QualityReportRequest,
  QualityReportResponse,
  TestGenerationRequest,
  TestGenerationResponse,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'app-ai-quality',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-quality.component.html',
  styleUrl: './ai-quality.component.css',
})
export class AiQualityComponent {
  private readonly aiService = inject(AiService);

  code = `package com.valorio.reparation.service;

import org.springframework.stereotype.Service;

@Service
public class DevisService {
    public void traiterDevis(Long demandeId, Double montant) {
        if (montant < 0) {
            throw new IllegalArgumentException("Montant négatif");
        }
        System.out.println("Devis créé pour demande " + demandeId);
    }
}`;

  language = 'java';
  activeTab = signal<'TESTS' | 'REPORT'>('REPORT');

  loadingTests = signal(false);
  loadingReport = signal(false);
  errorMessage = signal<string | null>(null);

  testResult = signal<TestGenerationResponse | null>(null);
  reportResult = signal<QualityReportResponse | null>(null);

  severityFilter = signal<'ALL' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL');
  copiedTests = signal(false);

  readonly filteredFindings = computed(() => {
    const rep = this.reportResult();
    if (!rep) return [];
    const filter = this.severityFilter();
    if (filter === 'ALL') return rep.findings;
    return rep.findings.filter((f) => f.severity.toUpperCase() === filter);
  });

  generateTests(): void {
    if (!this.code.trim()) {
      this.errorMessage.set('Veuillez fournir du code source à tester.');
      return;
    }

    this.loadingTests.set(true);
    this.errorMessage.set(null);
    this.activeTab.set('TESTS');

    const req: TestGenerationRequest = {
      code: this.code.trim(),
      language: this.language,
      framework: this.language === 'typescript' ? 'Jasmine' : 'JUnit 5',
    };

    this.aiService.generateTests(req).subscribe({
      next: (res) => {
        this.loadingTests.set(false);
        this.testResult.set(res);
      },
      error: (err) => {
        this.loadingTests.set(false);
        this.errorMessage.set(messageErreur(err, 'Échec de la génération des tests.'));
      },
    });
  }

  generateQualityReport(): void {
    if (!this.code.trim()) {
      this.errorMessage.set('Veuillez fournir du code source à analyser.');
      return;
    }

    this.loadingReport.set(true);
    this.errorMessage.set(null);
    this.activeTab.set('REPORT');

    const req: QualityReportRequest = {
      code: this.code.trim(),
      language: this.language,
      includeStaticAnalysis: true,
      includeAiReview: true,
    };

    this.aiService.generateQualityReport(req).subscribe({
      next: (res) => {
        this.loadingReport.set(false);
        this.reportResult.set(res);
      },
      error: (err) => {
        this.loadingReport.set(false);
        this.errorMessage.set(messageErreur(err, 'Échec de l\'analyse de qualité.'));
      },
    });
  }

  setFilter(filter: 'ALL' | 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'): void {
    this.severityFilter.set(filter);
  }

  copyTests(): void {
    const res = this.testResult();
    if (res && res.generatedTests) {
      navigator.clipboard.writeText(res.generatedTests).then(() => {
        this.copiedTests.set(true);
        setTimeout(() => this.copiedTests.set(false), 2000);
      });
    }
  }

  exportReport(): void {
    const res = this.reportResult();
    if (!res) return;

    const data = JSON.stringify(res, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `valorio-quality-report-${res.reportId}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
