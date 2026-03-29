import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-spec-live-viewer',
  standalone: true,
  template: `
    <section class="viewer">
      <h3>Live spec.md</h3>
      <pre>{{ content || 'Waiting for generated spec...' }}</pre>
    </section>
  `,
  styles: [
    `
      .viewer {
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        background: #fff;
        min-height: 300px;
        padding: 0.75rem;
      }

      h3 {
        margin: 0 0 0.5rem;
        color: #0f172a;
      }

      pre {
        white-space: pre-wrap;
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono',
          'Courier New', monospace;
        font-size: 0.86rem;
        line-height: 1.4;
        margin: 0;
      }
    `
  ]
})
export class SpecLiveViewerComponent {
  @Input() content = '';
}
