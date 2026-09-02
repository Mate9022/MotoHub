import { CommonModule } from '@angular/common';

import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  FormsModule,
  NgForm
} from '@angular/forms';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import { finalize } from 'rxjs';
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {Customer, Motorcycle} from "../../../core/models";


@Component({
  selector: 'app-new-work-order-page',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule
  ],

  templateUrl:
    './new-work-order-page.component.html',

  styleUrls: [
    './new-work-order-page.component.css'
  ]
})
export class NewWorkOrderPageComponent
  implements OnInit {

  private readonly api =
    inject(WorkshopApiService);

  private readonly router =
    inject(Router);

  private readonly route =
    inject(ActivatedRoute);


  readonly customers =
    signal<Customer[]>([]);


  readonly motorcycles =
    signal<Motorcycle[]>([]);


  readonly customerId =
    signal('');


  readonly motorcycleId =
    signal('');


  readonly motorcyclesLoading =
    signal(false);


  readonly saving =
    signal(false);


  readonly error =
    signal('');


  form = {

    odometerKm:
      0,

    complaint:
      ''

  };


  ngOnInit(): void {

    this.loadCustomers();

    this.initializePreselectedMotorcycle();

  }


  private loadCustomers(): void {

    this.api
      .listCustomers()
      .subscribe({

        next: customers => {

          this.customers.set(
            customers
          );

        },


        error: err => {

          console.error(
            'Ügyfelek betöltési hiba:',
            err
          );


          this.error.set(
            'Nem sikerült betölteni az ügyfeleket.'
          );

        }

      });

  }


  private initializePreselectedMotorcycle(): void {

    const motorcycleId =
      this.route
        .snapshot
        .queryParamMap
        .get('motorcycleId');


    if (!motorcycleId) {

      return;

    }


    this.motorcyclesLoading.set(
      true
    );


    this.api
      .getMotorcycle(
        motorcycleId
      )
      .subscribe({

        next: motorcycle => {

          this.customerId.set(
            motorcycle.customerId
          );


          this.loadMotorcyclesForCustomer(
            motorcycle.customerId,
            motorcycle.id
          );

        },


        error: err => {

          this.motorcyclesLoading.set(
            false
          );


          console.error(
            'Előre kiválasztott motor betöltési hiba:',
            err
          );


          this.error.set(
            'A kiválasztott motor nem található.'
          );

        }

      });

  }


  customerChanged(
    customerId: string
  ): void {

    this.customerId.set(
      customerId
    );


    this.motorcycleId.set(
      ''
    );


    this.motorcycles.set(
      []
    );


    this.error.set('');


    if (!customerId) {

      return;

    }


    this.loadMotorcyclesForCustomer(
      customerId
    );

  }


  private loadMotorcyclesForCustomer(
    customerId: string,
    preselectedMotorcycleId?: string
  ): void {

    this.motorcyclesLoading.set(
      true
    );


    this.api
      .listCustomerMotorcycles(
        customerId
      )
      .pipe(

        finalize(() => {

          this.motorcyclesLoading.set(
            false
          );

        })

      )
      .subscribe({

        next: motorcycles => {

          this.motorcycles.set(
            motorcycles
          );


          if (
            preselectedMotorcycleId &&
            motorcycles.some(
              motorcycle =>
                motorcycle.id ===
                preselectedMotorcycleId
            )
          ) {

            this.motorcycleId.set(
              preselectedMotorcycleId
            );

          }

        },


        error: err => {

          console.error(
            'Motorok betöltési hiba:',
            err
          );


          this.error.set(
            'Nem sikerült betölteni az ügyfél motorjait.'
          );

        }

      });

  }


  selectedMotorcycle():
    Motorcycle | undefined {

    const id =
      this.motorcycleId();


    if (!id) {

      return undefined;

    }


    return this.motorcycles()
      .find(
        motorcycle =>
          motorcycle.id === id
      );

  }


  submit(
    formRef: NgForm
  ): void {

    const motorcycleId =
      this.motorcycleId();


    if (
      formRef.invalid ||
      this.saving() ||
      !motorcycleId
    ) {

      return;

    }


    this.saving.set(
      true
    );


    this.error.set('');


    this.api
      .createWorkOrder({

        motorcycleId,

        odometerKm:
          this.form.odometerKm,

        complaint:
          this.form.complaint.trim()

      })
      .pipe(

        finalize(() => {

          this.saving.set(
            false
          );

        })

      )
      .subscribe({

        next: order => {

          void this.router.navigate([
            '/work-orders',
            order.id
          ]);

        },


        error: err => {

          console.error(
            'Munkalap létrehozási hiba:',
            err
          );


          this.error.set(
            'Nem sikerült létrehozni a munkalapot.'
          );

        }

      });

  }

}
