import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import {Motorcycle} from "../../../core/models";

@Component({
    selector: 'app-motorcycle-system-info',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './motorcycle-system-info.component.html',
    styleUrls: ['./motorcycle-system-info.component.css']
})
export class MotorcycleSystemInfoComponent {
    @Input({ required: true }) motorcycle!: Motorcycle;
}
