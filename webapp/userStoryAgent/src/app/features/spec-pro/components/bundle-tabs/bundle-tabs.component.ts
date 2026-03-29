import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { SpecBundle } from '../../../../core/models/specpro.models';

@Component({
  selector: 'app-bundle-tabs',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="bundle" *ngIf="bundle; else empty">
      <h3>Final bundle files</h3>
      <div class="tabs">
        <button
          type="button"
          *ngFor="let file of orderedFiles"
          class="tab"
          [class.active]="file === selectedFile"
          (click)="selectedFile = file"
        >
          {{ file }}
        </button>
      </div>

      <pre class="tab-content">{{ selectedContent }}</pre>
    </section>

    <ng-template #empty>
      <section class="bundle empty">Final bundle will appear here.</section>
    </ng-template>
  `,
  styles: [
    `
      .bundle {
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        background: #fff;
        padding: 0.75rem;
      }

      .tabs {
        display: flex;
        gap: 0.35rem;
        flex-wrap: wrap;
        margin-bottom: 0.75rem;
      }

      .tab {
        border: 1px solid #cbd5e1;
        background: #f8fafc;
        border-radius: 999px;
        padding: 0.35rem 0.75rem;
        cursor: pointer;
      }

      .tab.active {
        background: #dbeafe;
        border-color: #93c5fd;
      }

      .tab-content {
        white-space: pre-wrap;
        margin: 0;
        min-height: 150px;
      }

      .empty {
        color: #64748b;
      }
    `
  ]
})
export class BundleTabsComponent {
  private _bundle: SpecBundle | null = null;

  @Input() set bundle(value: SpecBundle | null) {
    this._bundle = value;
    const first = this.orderedFiles[0];
    if (first) {
      this.selectedFile = first;
    }
  }

  get bundle(): SpecBundle | null {
    return this._bundle;
  }

  selectedFile = 'spec.md';

  readonly preferredOrder = [
    'spec.md',
    'plan.md',
    'data-model.md',
    'contracts/api-spec.json',
    'quickstart.md',
    'research.md'
  ];

  get orderedFiles(): string[] {
    const files = this._bundle?.files ?? {};
    return this.preferredOrder.filter((name) => files[name] !== undefined);
  }

  get selectedContent(): string {
    const files = this._bundle?.files ?? {};
    return files[this.selectedFile] ?? '';
  }
}
