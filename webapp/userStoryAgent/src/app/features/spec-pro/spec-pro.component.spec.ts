import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, Subject } from 'rxjs';
import { SpecproApiService } from '../../core/api/specpro-api.service';
import { SpecProSseEvent } from '../../core/models/specpro.models';
import { SpecProComponent } from './spec-pro.component';

describe('SpecProComponent', () => {
  let fixture: ComponentFixture<SpecProComponent>;
  let component: SpecProComponent;
  let events$: Subject<SpecProSseEvent>;

  const apiMock = {
    createRun: vi.fn(),
    streamRun: vi.fn()
  };

  beforeEach(async () => {
    events$ = new Subject<SpecProSseEvent>();
    apiMock.createRun.mockReturnValue(of({ runId: 'run-123' }));
    apiMock.streamRun.mockReturnValue(events$.asObservable());

    await TestBed.configureTestingModule({
      imports: [SpecProComponent],
      providers: [{ provide: SpecproApiService, useValue: apiMock }]
    }).compileComponents();

    fixture = TestBed.createComponent(SpecProComponent);
    component = fixture.componentInstance;
    component.featureIdea = 'Build a checkout feature';
    fixture.detectChanges();
  });

  it('updates state from SSE event sequence', () => {
    component.generate();

    events$.next({ type: 'status', phase: 'CRITIC', iteration: 1 });
    events$.next({ type: 'spec_md_delta', delta: '# Spec\n' });
    events$.next({
      type: 'critic',
      critic: { status: 'FAIL', issues: [{ severity: 'MAJOR', file: 'spec.md', problem: 'x', fix: 'y' }] }
    });
    events$.next({ type: 'final_bundle', bundle: { files: { 'spec.md': '# Final' } } });
    events$.next({ type: 'done', phase: 'DONE', iteration: 2 });
    events$.complete();

    expect(component.runId).toBe('run-123');
    expect(component.phase).toBe('DONE');
    expect(component.iteration).toBe(2);
    expect(component.liveSpecMd).toContain('# Spec');
    expect(component.criticResult?.status).toBe('FAIL');
    expect(component.finalBundle?.files['spec.md']).toBe('# Final');
    expect(component.isGenerating).toBe(false);
  });
});
