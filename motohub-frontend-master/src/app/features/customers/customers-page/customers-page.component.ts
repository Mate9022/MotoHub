import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {Customer} from "../../../core/models";
import {CustomerSearchComponent} from "../customer-search/customer-search.component";
import {CustomerListComponent} from "../customer-list/customer-list.component";

@Component({
    selector: 'app-customers-page',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink,
        CustomerSearchComponent,
        CustomerListComponent
    ],
    templateUrl: './customers-page.component.html',
    styleUrls: ['./customers-page.component.css']
})
export class CustomersPageComponent implements OnInit {

    private readonly api = inject(WorkshopApiService);

    readonly customers = signal<Customer[]>([]);
    readonly loading = signal(true);
    readonly error = signal('');

    search = '';

    ngOnInit(): void {
        this.loadCustomers();
    }

    loadCustomers(): void {
        this.loading.set(true);
        this.error.set('');

        this.api
            .listCustomers(this.search)
            .subscribe({
                next: customers => {
                    this.customers.set(customers);
                    this.loading.set(false);
                },
                error: err => {
                    console.error('Ügyféllista betöltési hiba:', err);
                    this.error.set('Nem sikerült betölteni az ügyfeleket.');
                    this.loading.set(false);
                }
            });
    }

    onSearchChange(search: string): void {
        this.search = search;
        this.loadCustomers();
    }
}
