import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  FormsModule,
  NgForm
} from '@angular/forms';
import {LaborItem} from "../../../core/models";
import {LaborFormValue} from "../work-order-detail/work-order-detail.models";


@Component({
  selector: 'app-work-order-labor-section',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './work-order-labor-section.component.html',

  styleUrls: [
    './work-order-labor-section.component.css'
  ]
})
export class WorkOrderLaborSectionComponent {

  @Input({
    required: true
  })
  items: LaborItem[] = [];


  @Input()
  defaultHourlyRate =
    0;


  @Input()
  completed =
    false;


  @Input()
  actionBusy =
    false;


  @Input()
  editingLaborId:
    string | null =
    null;


  @Input({
    required: true
  })
  labor!: LaborFormValue;


  @Input({
    required: true
  })
  laborEdit!: LaborFormValue;


  @Output()
  readonly addRequested =
    new EventEmitter<NgForm>();


  @Output()
  readonly startEditRequested =
    new EventEmitter<LaborItem>();


  @Output()
  readonly cancelEditRequested =
    new EventEmitter<void>();


  @Output()
  readonly saveEditRequested =
    new EventEmitter<LaborItem>();


  @Output()
  readonly deleteRequested =
    new EventEmitter<LaborItem>();

}
