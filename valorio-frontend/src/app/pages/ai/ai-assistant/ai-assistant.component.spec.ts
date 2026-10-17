import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { AiAssistantComponent } from './ai-assistant.component';
import { AiService, CodeGenerationResponse } from '@valorio/shared';

describe('AiAssistantComponent', () => {
  let component: AiAssistantComponent;
  let fixture: ComponentFixture<AiAssistantComponent>;
  let aiService: AiService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AiAssistantComponent],
      providers: [AiService, provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AiAssistantComponent);
    component = fixture.componentInstance;
    aiService = TestBed.inject(AiService);
    fixture.detectChanges();
  });

  it('doit être initialisé avec les valeurs par défaut', () => {
    expect(component).toBeTruthy();
    expect(component.language).toBe('java');
    expect(component.task).toContain('service');
    expect(component.result()).toBeNull();
  });

  it('génère du code et stocke le résultat', () => {
    const mockRes: CodeGenerationResponse = {
      code: 'public class Demo {}',
      language: 'java',
      explanation: 'Code de test',
      assumptions: ['Hypothèse 1'],
      potentialRisks: ['Risque 1'],
      suggestedImprovements: ['Amélioration 1'],
    };

    spyOn(aiService, 'generateCode').and.returnValue(of(mockRes));

    component.task = 'Créer une classe Demo';
    component.onSubmit();

    expect(aiService.generateCode).toHaveBeenCalled();
    expect(component.result()).toEqual(mockRes);
    expect(component.loading()).toBeFalse();
  });

  it('affiche une erreur si la tâche est vide', () => {
    component.task = '';
    component.onSubmit();
    expect(component.errorMessage()).toContain('décrire la tâche');
  });
});
