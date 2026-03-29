import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SpecProSseEvent, SpecRunCreated, SpecRunRequest } from '../models/specpro.models';
import { SseClient } from '../sse/sse-client';

@Injectable({ providedIn: 'root' })
export class SpecproApiService {
  constructor(
    private readonly http: HttpClient,
    private readonly sseClient: SseClient
  ) {}

  createRun(payload: SpecRunRequest): Observable<SpecRunCreated> {
    return this.http.post<SpecRunCreated>('/api/specpro/runs', payload);
  }

  streamRun(runId: string): Observable<SpecProSseEvent> {
    return this.sseClient.connect(runId);
  }
}
