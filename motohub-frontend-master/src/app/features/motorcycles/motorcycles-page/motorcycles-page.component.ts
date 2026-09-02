import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import {MotorcycleSearchComponent} from "../motorcycle-search/motorcycle-search.component";
import {MotorcycleListComponent} from "../motorcycle-list/motorcycle-list.component";
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {Motorcycle} from "../../../core/models";

@Component({
    selector: 'app-motorcycles-page',
    standalone: true,
    imports: [
        CommonModule,
        MotorcycleSearchComponent,
        MotorcycleListComponent
    ],
    templateUrl: './motorcycles-page.component.html',
    styleUrls: ['./motorcycles-page.component.css']
})
export class MotorcyclesPageComponent implements OnInit {

    private readonly api = inject(WorkshopApiService);

    readonly motorcycles = signal<Motorcycle[]>([]);
    readonly loading = signal(true);
    readonly error = signal('');

    search = '';

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading.set(true);
        this.error.set('');

        this.api
            .listMotorcycles(this.search)
            .subscribe({
                next: motorcycles => {
                    this.motorcycles.set(motorcycles);
                    this.loading.set(false);
                },
                error: err => {
                    console.error('Motorlista betöltési hiba:', err);
                    this.error.set('Nem sikerült betölteni a motorokat.');
                    this.loading.set(false);
                }
            });
    }

    onSearchChange(search: string): void {
        this.search = search;
    }
}
