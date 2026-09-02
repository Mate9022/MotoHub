import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {MotorcycleOwnerSectionComponent} from "../motorcycle-owner-section/motorcycle-owner-section.component";
import {MotorcycleServiceHistoryComponent} from "../motorcycle-service-history/motorcycle-service-history.component";
import {MotorcycleSystemInfoComponent} from "../motorcycle-system-info/motorcycle-system-info.component";
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {Motorcycle, WorkOrder} from "../../../core/models";

@Component({
    selector: 'app-motorcycle-detail-page',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        RouterLink,
        MotorcycleOwnerSectionComponent,
        MotorcycleServiceHistoryComponent,
        MotorcycleSystemInfoComponent
    ],
    templateUrl: './motorcycle-detail-page.component.html',
    styleUrls: ['./motorcycle-detail-page.component.css']
})
export class MotorcycleDetailPageComponent implements OnInit {

    private readonly api = inject(WorkshopApiService);
    private readonly route = inject(ActivatedRoute);

    readonly motorcycle = signal<Motorcycle | undefined>(undefined);
    readonly workOrders = signal<WorkOrder[]>([]);
    readonly workOrdersLoading = signal(false);
    readonly saving = signal(false);
    readonly saved = signal(false);
    readonly error = signal('');

    form = {
        brand: '',
        model: '',
        modelYear: undefined as number | undefined,
        licensePlate: '',
        vin: ''
    };

    ngOnInit(): void {
        this.load();
    }

    private load(): void {
        const id = this.route.snapshot.paramMap.get('id');

        if (!id) {
            this.error.set('Hiányzó motor azonosító.');
            return;
        }

        this.error.set('');
        this.loadMotorcycle(id);
        this.loadWorkOrders(id);
    }

    private loadMotorcycle(id: string): void {
        this.api
            .getMotorcycle(id)
            .subscribe({
                next: motorcycle => {
                    this.motorcycle.set(motorcycle);
                    this.form = {
                        brand: motorcycle.brand,
                        model: motorcycle.model,
                        modelYear: motorcycle.modelYear ?? undefined,
                        licensePlate: motorcycle.licensePlate ?? '',
                        vin: motorcycle.vin ?? ''
                    };
                },
                error: err => {
                    console.error('Motor betöltési hiba:', err);
                    this.error.set('Nem sikerült betölteni a motort.');
                }
            });
    }

    private loadWorkOrders(motorcycleId: string): void {
        this.workOrdersLoading.set(true);

        this.api
            .listMotorcycleWorkOrders(motorcycleId)
            .pipe(
                finalize(() => this.workOrdersLoading.set(false))
            )
            .subscribe({
                next: orders => {
                    this.workOrders.set(orders);
                },
                error: err => {
                    console.error('Szerviztörténet betöltési hiba:', err);
                    this.error.set('Nem sikerült betölteni a motor szerviztörténetét.');
                }
            });
    }

    save(motorcycleForm: NgForm): void {
        const motorcycle = this.motorcycle();

        if (!motorcycle || motorcycleForm.invalid || this.saving()) {
            return;
        }

        this.saving.set(true);
        this.saved.set(false);
        this.error.set('');

        this.api
            .updateMotorcycle(
                motorcycle.id,
                {
                    brand: this.form.brand.trim(),
                    model: this.form.model.trim(),
                    modelYear: this.form.modelYear ?? undefined,
                    licensePlate: this.form.licensePlate.trim().toUpperCase(),
                    vin: this.form.vin.trim().toUpperCase()
                }
            )
            .pipe(
                finalize(() => this.saving.set(false))
            )
            .subscribe({
                next: updated => {
                    this.motorcycle.set(updated);
                    this.saved.set(true);
                },
                error: err => {
                    console.error('Motor mentési hiba:', err);
                    this.error.set('Nem sikerült elmenteni a motort.');
                }
            });
    }
}
