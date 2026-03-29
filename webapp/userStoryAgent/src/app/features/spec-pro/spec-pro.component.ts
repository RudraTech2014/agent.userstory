import { CommonModule } from '@angular/common';
import { Component, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { SpecproApiService } from '../../core/api/specpro-api.service';
import {
  CriticResult,
  SpecBundle,
  SpecProSseEvent,
  SpecRunRequest
} from '../../core/models/specpro.models';
import { BundleTabsComponent } from './components/bundle-tabs/bundle-tabs.component';
import { CriticIssuesComponent } from './components/critic-issues/critic-issues.component';
import { SpecLiveViewerComponent } from './components/spec-live-viewer/spec-live-viewer.component';
import { TechReferenceSelectComponent } from './components/tech-reference-select/tech-reference-select.component';

@Component({
  selector: 'app-spec-pro',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TechReferenceSelectComponent,
    SpecLiveViewerComponent,
    BundleTabsComponent,
    CriticIssuesComponent
  ],
  templateUrl: './spec-pro.component.html',
  styleUrl: './spec-pro.component.scss'
})
export class SpecProComponent implements OnDestroy {
  featureIdea = '';
  constraints = '';
  techReferenceKey = 'JAVA_ANGULAR';

  runId = '';
  phase = 'IDLE';
  iteration = 0;
  isGenerating = false;

  liveSpecMd = '';
  finalBundle: SpecBundle | null = null;
  criticResult: CriticResult | null = null;
  errorMessage = '';

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly api: SpecproApiService) {}

  generate(): void {
    const trimmedIdea = this.featureIdea.trim();
    if (!trimmedIdea) {
      this.errorMessage = 'Feature idea is required.';
      return;
    }

    this.resetRunState();
    this.isGenerating = true;

    const payload: SpecRunRequest = {
      featureIdea: trimmedIdea,
      techReferenceKey: this.techReferenceKey || undefined
    };

    this.api
      .createRun(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ runId }) => {
          this.runId = runId;
          this.startStream(runId);
        },
        error: () => {
          this.errorMessage = 'Unable to create run.';
          this.isGenerating = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private startStream(runId: string): void {
    this.api
      .streamRun(runId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (event) => this.handleEvent(event),
        error: () => {
          this.errorMessage = 'Stream disconnected unexpectedly.';
          this.isGenerating = false;
        },
        complete: () => {
          this.isGenerating = false;
        }
      });
  }

  private handleEvent(event: SpecProSseEvent): void {
    switch (event.type) {
      case 'status':
        this.phase = event.phase;
        this.iteration = event.iteration;
        break;
      case 'spec_md_delta':
        this.liveSpecMd += event.delta;
        break;
      case 'critic':
        this.criticResult = event.critic;
        break;
      case 'final_bundle':
        this.finalBundle = event.bundle;
        break;
      case 'error':
        this.errorMessage = event.message;
        this.isGenerating = false;
        break;
      case 'done':
        this.phase = event.phase ?? 'DONE';
        if (event.iteration !== undefined) {
          this.iteration = event.iteration;
        }
        this.isGenerating = false;
        break;
      default:
        break;
    }
  }

  private resetRunState(): void {
    this.runId = '';
    this.phase = 'STARTING';
    this.iteration = 0;
    this.liveSpecMd = '';
    this.finalBundle = null;
    this.criticResult = null;
    this.errorMessage = '';
  }
}
