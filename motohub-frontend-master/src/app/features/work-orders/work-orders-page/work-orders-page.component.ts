import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {WorkOrderSearchComponent} from "../work-order-search/work-order-search.component";
import {WorkOrderListComponent} from "../work-order-list/work-order-list.component";
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {WorkOrder} from "../../../core/models";


@Component({
  selector: 'app-work-orders-page',
  standalone: true,

  imports: [
    CommonModule,
    RouterLink,
    WorkOrderSearchComponent,
    WorkOrderListComponent
  ],

  templateUrl:
    './work-orders-page.component.html',

  styleUrls: [
    './work-orders-page.component.css'
  ]
})
export class WorkOrdersPageComponent
  implements OnInit {

  private readonly api =
    inject(WorkshopApiService);


  readonly orders =
    signal<WorkOrder[]>([]);

  readonly loading =
    signal(true);

  readonly error =
    signal('');


  ngOnInit(): void {
    this.load();
  }


  load(
    search = ''
  ): void {

    this.loading.set(true);
    this.error.set('');


    this.api
      .listWorkOrders(
        search
      )
      .subscribe({

        next: orders => {

          this.orders.set(
            orders
          );

          this.loading.set(
            false
          );

        },


        error: err => {

          console.error(
            'Munkalap lista hiba:',
            err
          );

          this.error.set(
            'Nem sikerült betölteni a munkalapokat.'
          );

          this.loading.set(
            false
          );

        }

      });

  }

}
