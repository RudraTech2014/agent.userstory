export interface SpecRunRequest {
  featureIdea: string;
  techReferenceKey?: string;
}

export interface SpecRunCreated {
  runId: string;
}

export interface SpecBundle {
  featureKey?: string;
  files: Record<string, string>;
}

export interface CriticIssue {
  severity: string;
  file: string;
  problem: string;
  fix: string;
}

export interface CriticResult {
  status: 'PASS' | 'FAIL' | string;
  issues: CriticIssue[];
}

export interface StatusEvent {
  event: 'status';
  phase?: string;
  message?: string;
}

export interface SpecMdDeltaEvent {
  event: 'spec_md_delta';
  path?: string;
  delta?: string;
}

export interface CriticEvent {
  event: 'critic';
  result: CriticResult;
}

export interface FinalBundleEvent {
  event: 'final_bundle';
  bundle: SpecBundle;
}

export interface ErrorEvent {
  event: 'error';
  message: string;
}

export interface DoneEvent {
  event: 'done';
}

export type SpecProSseEvent =
  | StatusEvent
  | SpecMdDeltaEvent
  | CriticEvent
  | FinalBundleEvent
  | ErrorEvent
  | DoneEvent;
