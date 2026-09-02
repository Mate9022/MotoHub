import {
  Component,
  Input
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {WorkOrder, WorkOrderStatus} from "../../../core/models";


@Component({
  selector: 'app-work-order-list',
  standalone: true,

  imports: [
    CommonModule,
    RouterLink
  ],

  templateUrl:
    './work-order-list.component.html',

  styleUrls: [
    './work-order-list.component.css'
  ]
})
export class WorkOrderListComponent {

  @Input({
    required: true
  })
  orders: WorkOrder[] = [];


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
