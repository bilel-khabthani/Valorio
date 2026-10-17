import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AiService,
  CodeGenerationRequest,
  CodeGenerationResponse,
  messageErreur,
} from '@valorio/shared';

@Component({
  selector: 'app-ai-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-assistant.component.html',
  styleUrl: './ai-assistant.component.css',
})
export class AiAssistantComponent {
  private readonly aiService = inject(AiService);

  task = 'Créer un service de suivi d\'audit pour le module Réparation avec Spring Boot 3.3 et JdbcClient';
  language = 'java';
  context = 'Java 17, Spring Boot 3.3.5, JdbcClient, PostgreSQL, schéma reparation';

  readonly languages = [
    { value: 'java', label: 'Java (Spring Boot 3.3)' },
    { value: 'typescript', label: 'TypeScript (Angular 20)' },
    { value: 'sql', label: 'SQL (PostgreSQL)' },
    { value: 'python', label: 'Python' },
  ];

  loading = signal(false);
  errorMessage = signal<string | null>(null);
  result = signal<CodeGenerationResponse | null>(null);
  copied = signal(false);

  onSubmit(): void {
    if (!this.task.trim()) {
      this.errorMessage.set('Veuillez décrire la tâche à générer.');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const payload: CodeGenerationRequest = {
      task: this.task.trim(),
      language: this.language,
      context: this.context.trim() || undefined,
    };

    this.aiService.generateCode(payload).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.result.set(res);
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(messageErreur(err, 'Échec de la génération de code.'));
      },
    });
  }

  copyCode(): void {
    const res = this.result();
    if (res && res.code) {
      navigator.clipboard.writeText(res.code).then(() => {
        this.copied.set(true);
        setTimeout(() => this.copied.set(false), 2000);
      });
    }
  }

  exportFile(): void {
    const res = this.result();
    if (!res || !res.code) return;

    let extension = '.txt';
    switch (res.language.toLowerCase()) {
      case 'java': extension = '.java'; break;
      case 'typescript':
      case 'angular': extension = '.ts'; break;
      case 'sql': extension = '.sql'; break;
      case 'python': extension = '.py'; break;
    }

    const blob = new Blob([res.code], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `valorio-ai-code${extension}`;
    a.click();
    URL.revokeObjectURL(url);
  }
}
