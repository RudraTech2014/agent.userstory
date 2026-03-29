import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CriticEvent,
  CriticResult,
  DoneEvent,
  ErrorEvent,
  FinalBundleEvent,
  SpecBundle,
  SpecMdDeltaEvent,
  SpecProSseEvent,
  StatusEvent
} from '../models/specpro.models';

@Injectable({ providedIn: 'root' })
export class SseClient {
  connect(runId: string): Observable<SpecProSseEvent> {
    return new Observable<SpecProSseEvent>((subscriber) => {
      const source = new EventSource(`/api/specpro/runs/${runId}/events`);

      const onStatus = (event: MessageEvent<string>) => {
        const parsed = this.safeParse<{ phase?: string; iteration?: number }>(event.data);
        const payload: StatusEvent = {
          type: 'status',
          phase: parsed?.phase ?? 'UNKNOWN',
          iteration: parsed?.iteration ?? 0
        };
        subscriber.next(payload);
      };

      const onSpecMdDelta = (event: MessageEvent<string>) => {
        const payload: SpecMdDeltaEvent = { type: 'spec_md_delta', delta: event.data ?? '' };
        subscriber.next(payload);
      };

      const onCritic = (event: MessageEvent<string>) => {
        const critic = this.safeParse<CriticResult>(event.data);
        if (!critic) {
          subscriber.next({ type: 'error', message: 'Unable to parse critic payload' } satisfies ErrorEvent);
          return;
        }

        subscriber.next({ type: 'critic', critic } satisfies CriticEvent);
      };

      const onFinalBundle = (event: MessageEvent<string>) => {
        const bundle = this.safeParse<SpecBundle>(event.data);
        if (!bundle) {
          subscriber.next({ type: 'error', message: 'Unable to parse final_bundle payload' } satisfies ErrorEvent);
          return;
        }

        subscriber.next({ type: 'final_bundle', bundle } satisfies FinalBundleEvent);
      };

      const onDone = (event: MessageEvent<string>) => {
        const done = this.safeParse<DoneEvent>(event.data) ?? { type: 'done' };
        subscriber.next({ type: 'done', runId: done.runId, phase: done.phase, iteration: done.iteration });
        subscriber.complete();
      };

      const onErrorEvent = (event: MessageEvent<string>) => {
        subscriber.next({ type: 'error', message: event.data ?? 'Unknown run error' } satisfies ErrorEvent);
      };

      const onSseError = () => {
        subscriber.error(new Error('SSE connection error'));
      };

      source.addEventListener('status', onStatus as EventListener);
      source.addEventListener('spec_md_delta', onSpecMdDelta as EventListener);
      source.addEventListener('critic', onCritic as EventListener);
      source.addEventListener('final_bundle', onFinalBundle as EventListener);
      source.addEventListener('done', onDone as EventListener);
      source.addEventListener('error', onErrorEvent as EventListener);
      source.onerror = onSseError;

      return () => {
        source.removeEventListener('status', onStatus as EventListener);
        source.removeEventListener('spec_md_delta', onSpecMdDelta as EventListener);
        source.removeEventListener('critic', onCritic as EventListener);
        source.removeEventListener('final_bundle', onFinalBundle as EventListener);
        source.removeEventListener('done', onDone as EventListener);
        source.removeEventListener('error', onErrorEvent as EventListener);
        source.close();
      };
    });
  }

  private safeParse<T>(value: string): T | null {
    try {
      return JSON.parse(value) as T;
    } catch {
      return null;
    }
  }
}
