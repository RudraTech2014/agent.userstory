export interface SpecRunRequest {
  featureIdea: string;
  techReference?: string;
  constraints?: string;
}

export interface SpecRunCreated {
  runId: string;
}

export interface SpecProSseEvent {
  event: string;
  data: unknown;
}
