import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import {Motorcycle} from "../../../core/models";

@Component({
    selector: 'app-motorcycle-owner-section',
    standalone: true,
    imports: [RouterLink],
    templateUrl: './motorcycle-owner-section.component.html',
    styleUrls: ['./motorcycle-owner-section.component.css']
})
export class MotorcycleOwnerSectionComponent {
    @Input({ required: true }) motorcycle!: Motorcycle;
}
