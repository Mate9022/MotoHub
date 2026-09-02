import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import {Customer} from "../../../core/models";


@Component({
    selector: 'app-customer-system-info',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './customer-system-info.component.html',
    styleUrls: ['./customer-system-info.component.css']
})
export class CustomerSystemInfoComponent {
    @Input({ required: true }) customer!: Customer;
}
