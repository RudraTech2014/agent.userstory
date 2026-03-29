import { NgZone } from '@angular/core';
import { SseClient } from './sse-client';

type Handler = (event: MessageEvent<string>) => void;

class MockEventSource {
  static instance: MockEventSource;

  handlers = new Map<string, Handler[]>();
  onerror: ((this: EventSource, ev: Event) => unknown) | null = null;

  constructor(public readonly url: string) {
    MockEventSource.instance = this;
  }

  addEventListener(type: string, listener: EventListener): void {
    const list = this.handlers.get(type) ?? [];
    list.push(listener as Handler);
    this.handlers.set(type, list);
  }

  removeEventListener(type: string, listener: EventListener): void {
    const list = this.handlers.get(type) ?? [];
    this.handlers.set(
      type,
      list.filter((h) => h !== (listener as Handler))
    );
  }

  emit(type: string, data: string): void {
    const list = this.handlers.get(type) ?? [];
    for (const handler of list) {
      handler({ data } as MessageEvent<string>);
    }
  }

  close(): void {}
}

describe('SseClient', () => {
  it('appends spec_md_delta events and handles final_bundle', () => {
    const original = globalThis.EventSource;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    globalThis.EventSource = MockEventSource as any;

    const zone = { run: (fn: () => void) => fn() } as NgZone;
    const client = new SseClient(zone);
    const output: string[] = [];
    let bundleSpec = '';

    const subscription = client.connect('run-abc').subscribe((event) => {
      if (event.type === 'spec_md_delta') {
        output.push(event.delta);
      }
      if (event.type === 'final_bundle') {
        bundleSpec = event.bundle.files['spec.md'];
      }
    });

    const source = MockEventSource.instance;
    source.emit('spec_md_delta', 'Hello ');
    source.emit('spec_md_delta', 'world');
    source.emit('final_bundle', JSON.stringify({ files: { 'spec.md': '# Final spec' } }));

    expect(output.join('')).toBe('Hello world');
    expect(bundleSpec).toBe('# Final spec');

    subscription.unsubscribe();
    globalThis.EventSource = original;
  });
});
