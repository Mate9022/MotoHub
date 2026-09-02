import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {WorkOrder, WorkOrderStatus} from "../../../core/models";
import {WorkOrderEditForm} from "../work-order-detail/work-order-detail.models";


@Component({
  selector: 'app-work-order-info-section',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './work-order-info-section.component.html',

  styleUrls: [
    './work-order-info-section.component.css'
  ]
})
export class WorkOrderInfoSectionComponent {

  @Input({
    required: true
  })
  order!: WorkOrder;


  @Input({
    required: true
  })
  edit!: WorkOrderEditForm;


  @Input()
  handOverOdometerKm:
    number | null =
    null;


  @Input()
  actionBusy =
    false;


  @Output()
  readonly handOverOdometerKmChange =
    new EventEmitter<number | null>();


  @Output()
  readonly saveRequested =
    new EventEmitter<void>();


  isCompleted(): boolean {

    return this.order.status ===
      'COMPLETED';

  }


  statusLabel(
    status: WorkOrderStatus
  ): string {

    return {

      OPEN:
        'Nyitott',

      IN_PROGRESS:
        'Javítás alatt',

      WAITING_PARTS:
        'Alkatrészre vár',

      READY_FOR_PICKUP:
        'Átadásra kész',

      COMPLETED:
        'Átadva / lezárva'

    }[status];

  }

}
