import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import {WorkshopApiService} from "../../../core/workshop-api.service";

@Component({
    selector: 'app-new-customer-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule
    ],
    templateUrl: './new-customer-page.component.html',
    styleUrls: ['./new-customer-page.component.css']
})
export class NewCustomerPageComponent {

    private readonly api = inject(WorkshopApiService);
    private readonly router = inject(Router);

    readonly saving = signal(false);
    readonly error = signal('');

    form = {
        name: '',
        phone: '',
        email: ''
    };

    submit(customerForm: NgForm): void {
        if (customerForm.invalid || this.saving()) {
            return;
        }

        this.saving.set(true);
        this.error.set('');

        this.api
            .createCustomer({
                name: this.form.name.trim(),
                phone: this.form.phone.trim(),
                email: this.form.email.trim()
            })
            .pipe(
                finalize(() => this.saving.set(false))
            )
            .subscribe({
                next: customer => {
                    void this.router.navigate([
                        '/customers',
                        customer.id
                    ]);
                },
                error: err => {
                    console.error('Ügyfél létrehozási hiba:', err);
                    this.error.set('Nem sikerült létrehozni az ügyfelet.');
                }
            });
    }
}
