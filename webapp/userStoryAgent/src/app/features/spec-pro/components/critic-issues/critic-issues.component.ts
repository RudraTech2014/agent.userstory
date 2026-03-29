import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { CriticResult } from '../../../../core/models/specpro.models';

@Component({
  selector: 'app-critic-issues',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="critic" *ngIf="critic; else empty">
      <h3>Critic ({{ critic.status }})</h3>
      <ul>
        <li *ngFor="let issue of critic.issues">
          <strong>[{{ issue.severity }}]</strong> {{ issue.file }}: {{ issue.problem }}
          <div class="fix">Fix: {{ issue.fix }}</div>
        </li>
      </ul>
    </section>

    <ng-template #empty>
      <section class="critic empty">No critic result yet.</section>
    </ng-template>
  `,
  styles: [
    `
      .critic {
        border: 1px solid #e2e8f0;
        border-radius: 10px;
        background: #fff;
        padding: 0.75rem;
      }

      .critic ul {
        margin: 0;
        padding-left: 1.1rem;
      }

      .critic li {
        margin-bottom: 0.4rem;
      }

      .fix {
        color: #475569;
        font-size: 0.85rem;
      }

      .empty {
        color: #64748b;
      }
    `
  ]
})
export class CriticIssuesComponent {
  @Input() critic: CriticResult | null = null;
}
