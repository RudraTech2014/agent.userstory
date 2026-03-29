import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tech-reference-select',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <label class="field-label" for="tech-reference">Tech reference</label>
    <select
      id="tech-reference"
      [ngModel]="selected"
      (ngModelChange)="onSelected($event)"
      class="select"
    >
      <option [ngValue]="''">None</option>
      <option *ngFor="let option of options" [ngValue]="option.value">{{ option.label }}</option>
    </select>
  `,
  styles: [
    `
      .field-label {
        display: block;
        font-size: 0.85rem;
        color: #334155;
        margin-bottom: 0.35rem;
      }

      .select {
        width: 100%;
        padding: 0.6rem;
        border: 1px solid #cbd5e1;
        border-radius: 8px;
        background: #fff;
      }
    `
  ]
})
export class TechReferenceSelectComponent {
  @Input() selected = '';
  @Output() selectedChange = new EventEmitter<string>();

  readonly options = [{ value: 'JAVA_ANGULAR', label: 'Java + Angular' }];

  onSelected(value: string): void {
    this.selectedChange.emit(value);
  }
}
