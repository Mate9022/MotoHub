import {
  Component,
  Input
} from '@angular/core';

import { CommonModule } from '@angular/common';
import {WorkOrder} from "../../../core/models";


@Component({
  selector: 'app-work-order-totals',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './work-order-totals.component.html',

  styleUrls: [
    './work-order-totals.component.css'
  ]
})
export class WorkOrderTotalsComponent {

  @Input({
    required: true
  })
  order!: WorkOrder;


  @Input()
  defaultHourlyRate =
    0;


  @Input()
  photoCount =
    0;

}
