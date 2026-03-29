import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SpecRunCreated, SpecRunRequest, SpecProSseEvent } from '../models/specpro.models';
import { SseClient } from '../sse/sse-client';

@Injectable({ providedIn: 'root' })
export class SpecproApiService {
  private readonly runsPath = '/api/specpro/runs';

  constructor(
    private readonly http: HttpClient,
    private readonly sseClient: SseClient,
  ) {}

  createRun(req: SpecRunRequest): Observable<SpecRunCreated> {
    return this.http.post<SpecRunCreated>(this.runsPath, req);
  }

  streamEvents(runId: string): Observable<SpecProSseEvent> {
    return this.sseClient.stream<SpecProSseEvent>(`${this.runsPath}/${runId}/events`);
  }

  // TODO(backend): Add GET /api/specpro/runs/{runId} in controller before implementing polling fallback/getResult().
}
