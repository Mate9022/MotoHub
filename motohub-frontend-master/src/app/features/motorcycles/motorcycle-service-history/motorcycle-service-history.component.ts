import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {WorkOrder, WorkOrderStatus} from "../../../core/models";


@Component({
    selector: 'app-motorcycle-service-history',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink
    ],
    templateUrl: './motorcycle-service-history.component.html',
    styleUrls: ['./motorcycle-service-history.component.css']
})
export class MotorcycleServiceHistoryComponent {

    @Input({ required: true }) motorcycleId!: string;
    @Input() workOrders: WorkOrder[] = [];
    @Input() loading = false;

    statusLabel(status: WorkOrderStatus): string {
        return {
            OPEN: 'Nyitott',
            IN_PROGRESS: 'Javítás alatt',
            WAITING_PARTS: 'Alkatrészre vár',
            READY_FOR_PICKUP: 'Átadásra kész',
            COMPLETED: 'Átadva / lezárva'
        }[status];
    }
}
