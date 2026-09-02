import { CommonModule } from '@angular/common';

import {
    Component,
    OnInit,
    inject,
    signal
} from '@angular/core';

import {
    FormsModule,
    NgForm
} from '@angular/forms';

import { finalize } from 'rxjs';
import {SettingsSystemInfoComponent} from "../settings-system-info/settings-system-info.component";
import {WorkshopApiService} from "../../../core/workshop-api.service";


@Component({
    selector: 'app-settings-page',
    standalone: true,

    imports: [
        CommonModule,
        FormsModule,
        SettingsSystemInfoComponent
    ],

    templateUrl:
        './settings-page.component.html',

    styleUrls: [
        './settings-page.component.css'
    ]
})
export class SettingsPageComponent
    implements OnInit {

    private readonly api =
        inject(WorkshopApiService);


    readonly loading =
        signal(true);


    readonly saving =
        signal(false);


    readonly saved =
        signal(false);


    readonly error =
        signal('');


    readonly updatedAt =
        signal<string | null>(
            null
        );


    form = {

        workshopName:
            '',

        address:
            '',

        phone:
            '',

        email:
            '',

        taxNumber:
            '',

        defaultHourlyRate:
            0

    };


    ngOnInit(): void {

        this.load();

    }


    private load(): void {

        this.loading.set(
            true
        );


        this.error.set('');


        this.api
            .getSettings()
            .pipe(

                finalize(() => {

                    this.loading.set(
                        false
                    );

                })

            )
            .subscribe({

                next: settings => {

                    this.form = {

                        workshopName:
                        settings.workshopName,

                        address:
                            settings.address ?? '',

                        phone:
                            settings.phone ?? '',

                        email:
                            settings.email ?? '',

                        taxNumber:
                            settings.taxNumber ?? '',

                        defaultHourlyRate:
                        settings.defaultHourlyRate

                    };


                    this.updatedAt.set(
                        settings.updatedAt
                    );

                },


                error: err => {

                    console.error(
                        'Beállítások betöltési hiba:',
                        err
                    );


                    this.error.set(
                        'Nem sikerült betölteni a műhelybeállításokat.'
                    );

                }

            });

    }


    save(
        settingsForm: NgForm
    ): void {

        if (
            settingsForm.invalid ||
            this.saving()
        ) {

            return;

        }


        this.saving.set(
            true
        );


        this.saved.set(
            false
        );


        this.error.set('');


        this.api
            .updateSettings({

                workshopName:
                    this.form
                        .workshopName
                        .trim(),

                address:
                    this.form
                        .address
                        .trim(),

                phone:
                    this.form
                        .phone
                        .trim(),

                email:
                    this.form
                        .email
                        .trim(),

                taxNumber:
                    this.form
                        .taxNumber
                        .trim(),

                defaultHourlyRate:
                this.form
                    .defaultHourlyRate

            })
            .pipe(

                finalize(() => {

                    this.saving.set(
                        false
                    );

                })

            )
            .subscribe({

                next: settings => {

                    this.form = {

                        workshopName:
                        settings.workshopName,

                        address:
                            settings.address ?? '',

                        phone:
                            settings.phone ?? '',

                        email:
                            settings.email ?? '',

                        taxNumber:
                            settings.taxNumber ?? '',

                        defaultHourlyRate:
                        settings.defaultHourlyRate

                    };


                    this.updatedAt.set(
                        settings.updatedAt
                    );


                    this.saved.set(
                        true
                    );

                },


                error: err => {

                    console.error(
                        'Beállítások mentési hiba:',
                        err
                    );


                    this.error.set(
                        'Nem sikerült elmenteni a műhelybeállításokat.'
                    );

                }

            });

    }

}
