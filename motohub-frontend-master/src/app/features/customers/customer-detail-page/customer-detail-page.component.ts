import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {Customer, Motorcycle} from "../../../core/models";
import {
    CustomerMotorcyclesSectionComponent
} from "../customer-motorcycles-section/customer-motorcycles-section.component";
import {CustomerSystemInfoComponent} from "../customer-system-info/customer-system-info.component";


@Component({
    selector: 'app-customer-detail-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        CustomerMotorcyclesSectionComponent,
        CustomerSystemInfoComponent
    ],
    templateUrl: './customer-detail-page.component.html',
    styleUrls: ['./customer-detail-page.component.css']
})
export class CustomerDetailPageComponent implements OnInit {

    private readonly api = inject(WorkshopApiService);
    private readonly route = inject(ActivatedRoute);

    readonly customer = signal<Customer | undefined>(undefined);
    readonly motorcycles = signal<Motorcycle[]>([]);
    readonly motorcyclesLoading = signal(false);
    readonly saving = signal(false);
    readonly saved = signal(false);
    readonly error = signal('');

    form = {
        name: '',
        phone: '',
        email: ''
    };

    ngOnInit(): void {
        this.load();
    }

    private load(): void {
        const id = this.route.snapshot.paramMap.get('id');

        if (!id) {
            this.error.set('Hiányzó ügyfél azonosító.');
            return;
        }

        this.error.set('');
        this.loadCustomer(id);
        this.loadMotorcycles(id);
    }

    private loadCustomer(id: string): void {
        this.api
            .getCustomer(id)
            .subscribe({
                next: customer => {
                    this.customer.set(customer);
                    this.form = {
                        name: customer.name,
                        phone: customer.phone ?? '',
                        email: customer.email ?? ''
                    };
                },
                error: err => {
                    console.error('Ügyfél betöltési hiba:', err);
                    this.error.set('Nem sikerült betölteni az ügyfelet.');
                }
            });
    }

    private loadMotorcycles(customerId: string): void {
        this.motorcyclesLoading.set(true);

        this.api
            .listCustomerMotorcycles(customerId)
            .pipe(
                finalize(() => this.motorcyclesLoading.set(false))
            )
            .subscribe({
                next: motorcycles => {
                    this.motorcycles.set(motorcycles);
                },
                error: err => {
                    console.error('Ügyfél motorjainak betöltési hibája:', err);
                }
            });
    }

    save(customerForm: NgForm): void {
        const customer = this.customer();

        if (!customer || customerForm.invalid || this.saving()) {
            return;
        }

        this.saving.set(true);
        this.saved.set(false);
        this.error.set('');

        this.api
            .updateCustomer(
                customer.id,
                {
                    name: this.form.name.trim(),
                    phone: this.form.phone.trim(),
                    email: this.form.email.trim()
                }
            )
            .pipe(
                finalize(() => this.saving.set(false))
            )
            .subscribe({
                next: updatedCustomer => {
                    this.customer.set(updatedCustomer);
                    this.saved.set(true);
                },
                error: err => {
                    console.error('Ügyfél mentési hiba:', err);
                    this.error.set('Nem sikerült elmenteni az ügyfelet.');
                }
            });
    }
}
