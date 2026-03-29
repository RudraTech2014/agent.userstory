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

export interface SpecRunResult {
  runId: string;
  phase?: string;
  iteration?: number;
  specMd?: string;
  critic?: CriticResult;
  finalBundle?: SpecBundle;
}

export interface StatusEvent {
  type: 'status';
  phase: string;
  iteration: number;
}

export interface SpecMdDeltaEvent {
  type: 'spec_md_delta';
  delta: string;
}

export interface CriticEvent {
  type: 'critic';
  critic: CriticResult;
}

export interface FinalBundleEvent {
  type: 'final_bundle';
  bundle: SpecBundle;
}

export interface ErrorEvent {
  type: 'error';
  message: string;
}

export interface DoneEvent {
  type: 'done';
  runId?: string;
  phase?: string;
  iteration?: number;
}

export type SpecProSseEvent =
  | StatusEvent
  | SpecMdDeltaEvent
  | CriticEvent
  | FinalBundleEvent
  | ErrorEvent
  | DoneEvent;
