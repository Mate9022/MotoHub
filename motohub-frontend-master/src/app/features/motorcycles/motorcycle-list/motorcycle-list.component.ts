import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {Motorcycle} from "../../../core/models";

@Component({
    selector: 'app-motorcycle-list',
    standalone: true,
    imports: [
        CommonModule,
        RouterLink
    ],
    templateUrl: './motorcycle-list.component.html',
    styleUrls: ['./motorcycle-list.component.css']
})
export class MotorcycleListComponent {
    @Input() motorcycles: Motorcycle[] = [];
}
