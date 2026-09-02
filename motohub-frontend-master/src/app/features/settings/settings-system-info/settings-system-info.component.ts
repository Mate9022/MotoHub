import { CommonModule } from '@angular/common';

import {
    Component,
    Input
} from '@angular/core';


@Component({
    selector: 'app-settings-system-info',
    standalone: true,

    imports: [
        CommonModule
    ],

    templateUrl:
        './settings-system-info.component.html',

    styleUrls: [
        './settings-system-info.component.css'
    ]
})
export class SettingsSystemInfoComponent {

    @Input({
        required: true
    })
    updatedAt!: string;

}
