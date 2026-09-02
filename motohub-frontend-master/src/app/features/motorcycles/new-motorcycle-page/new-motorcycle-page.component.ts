import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {Customer} from "../../../core/models";

@Component({
    selector: 'app-new-motorcycle-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule
    ],
    templateUrl: './new-motorcycle-page.component.html',
    styleUrls: ['./new-motorcycle-page.component.css']
})
export class NewMotorcyclePageComponent implements OnInit {

    private readonly api = inject(WorkshopApiService);
    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);

    readonly customer = signal<Customer | undefined>(undefined);
    readonly saving = signal(false);
    readonly error = signal('');

    private customerId = '';

    form = {
        brand: '',
        model: '',
        modelYear: undefined as number | undefined,
        licensePlate: '',
        vin: ''
    };

    ngOnInit(): void {
        const customerId = this.route.snapshot.paramMap.get('customerId');

        if (!customerId) {
            this.error.set('Hiányzó ügyfél azonosító.');
            return;
        }

        this.customerId = customerId;

        this.api
            .getCustomer(customerId)
            .subscribe({
                next: customer => {
                    this.customer.set(customer);
                },
                error: err => {
                    console.error('Ügyfél betöltési hiba:', err);
                    this.error.set('Nem sikerült betölteni az ügyfelet.');
                }
            });
    }

    submit(motorcycleForm: NgForm): void {
        if (motorcycleForm.invalid || this.saving() || !this.customerId) {
            return;
        }

        this.saving.set(true);
        this.error.set('');

        this.api
            .createMotorcycle({
                customerId: this.customerId,
                brand: this.form.brand.trim(),
                model: this.form.model.trim(),
                modelYear: this.form.modelYear ?? undefined,
                licensePlate: this.form.licensePlate.trim().toUpperCase(),
                vin: this.form.vin.trim().toUpperCase()
            })
            .pipe(
                finalize(() => this.saving.set(false))
            )
            .subscribe({
                next: motorcycle => {
                    void this.router.navigate([
                        '/motorcycles',
                        motorcycle.id
                    ]);
                },
                error: err => {
                    console.error('Motor létrehozási hiba:', err);
                    this.error.set('Nem sikerült létrehozni a motort.');
                }
            });
    }
}
