import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {Customer} from "../../../core/models";

@Component({
    selector: 'app-customer-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink
    ],
    templateUrl: './customer-list.component.html',
    styleUrls: ['./customer-list.component.css']
})
export class CustomerListComponent {
    @Input() customers: Customer[] = [];
}
