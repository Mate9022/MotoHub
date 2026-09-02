import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {WorkOrder} from "../../../core/models";



@Component({
  selector: 'app-work-order-header',
  standalone: true,

  imports: [
    CommonModule,
    RouterLink
  ],

  templateUrl:
    './work-order-header.component.html',

  styleUrls: [
    './work-order-header.component.css'
  ]
})
export class WorkOrderHeaderComponent {

  readonly api =
    inject(WorkshopApiService);


  @Input({
    required: true
  })
  order!: WorkOrder;


  @Input()
  actionBusy =
    false;


  @Output()
  readonly markReadyRequested =
    new EventEmitter<void>();


  @Output()
  readonly handOverRequested =
    new EventEmitter<void>();


  @Output()
  readonly reopenRequested =
    new EventEmitter<void>();

}
