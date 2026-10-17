import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RecyclageApiService } from './recyclage-api.service';

describe('RecyclageApiService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  it('should be created', () => {
    expect(TestBed.inject(RecyclageApiService)).toBeTruthy();
  });
});
