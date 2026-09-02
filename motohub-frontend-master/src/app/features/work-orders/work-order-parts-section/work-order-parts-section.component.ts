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
import {PartItem} from "../../../core/models";
import {PartFormValue} from "../work-order-detail/work-order-detail.models";


@Component({
  selector: 'app-work-order-parts-section',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './work-order-parts-section.component.html',

  styleUrls: [
    './work-order-parts-section.component.css'
  ]
})
export class WorkOrderPartsSectionComponent {

  @Input({
    required: true
  })
  items: PartItem[] = [];


  @Input()
  completed =
    false;


  @Input()
  actionBusy =
    false;


  @Input()
  editingPartId:
    string | null =
    null;


  @Input({
    required: true
  })
  part!: PartFormValue;


  @Input({
    required: true
  })
  partEdit!: PartFormValue;


  @Output()
  readonly addRequested =
    new EventEmitter<NgForm>();


  @Output()
  readonly startEditRequested =
    new EventEmitter<PartItem>();


  @Output()
  readonly cancelEditRequested =
    new EventEmitter<void>();


  @Output()
  readonly saveEditRequested =
    new EventEmitter<PartItem>();


  @Output()
  readonly deleteRequested =
    new EventEmitter<PartItem>();

}
