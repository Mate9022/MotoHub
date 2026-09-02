import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {Motorcycle} from "../../../core/models";


@Component({
    selector: 'app-customer-motorcycles-section',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink
    ],
    templateUrl: './customer-motorcycles-section.component.html',
    styleUrls: ['./customer-motorcycles-section.component.css']
})
export class CustomerMotorcyclesSectionComponent {
    @Input({ required: true }) customerId!: string;
    @Input() motorcycles: Motorcycle[] = [];
    @Input() loading = false;
}
