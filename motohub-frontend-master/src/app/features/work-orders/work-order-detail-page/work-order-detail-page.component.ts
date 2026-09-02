import { CommonModule } from '@angular/common';

import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import { NgForm } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { finalize } from 'rxjs';



import {
  WorkOrderEditForm,
  LaborFormValue,
  PartFormValue
} from '../work-order-detail/work-order-detail.models';
import {WorkOrderHeaderComponent} from "../work-order-header/work-order-header.component";
import {WorkOrderInfoSectionComponent} from "../work-order-info-section/work-order-info-section.component";
import {WorkOrderLaborSectionComponent} from "../work-order-labor-section/work-order-labor-section.component";
import {WorkOrderPartsSectionComponent} from "../work-order-parts-section/work-order-parts-section.component";
import {WorkOrderPhotosSectionComponent} from "../work-order-photos-section/work-order-photos-section.component";
import {WorkOrderTotalsComponent} from "../work-order-totals/work-order-totals.component";
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {LaborItem, PartItem, WorkOrder, WorkOrderPhoto} from "../../../core/models";


@Component({
  selector: 'app-work-order-detail-page',
  standalone: true,

  imports: [
    CommonModule,
    WorkOrderHeaderComponent,
    WorkOrderInfoSectionComponent,
    WorkOrderLaborSectionComponent,
    WorkOrderPartsSectionComponent,
    WorkOrderPhotosSectionComponent,
    WorkOrderTotalsComponent
  ],

  templateUrl:
    './work-order-detail-page.component.html',

  styleUrls: [
    './work-order-detail-page.component.css'
  ]
})
export class WorkOrderDetailPageComponent
  implements OnInit {

  private readonly api =
    inject(WorkshopApiService);


  private readonly route =
    inject(ActivatedRoute);


  readonly order =
    signal<WorkOrder | undefined>(
      undefined
    );


  readonly error =
    signal('');


  readonly saved =
    signal(false);


  readonly actionBusy =
    signal(false);


  readonly editingLaborId =
    signal<string | null>(
      null
    );


  readonly editingPartId =
    signal<string | null>(
      null
    );


  readonly defaultHourlyRate =
    signal(0);


  readonly photos =
    signal<WorkOrderPhoto[]>([]);


  readonly photosLoading =
    signal(false);


  readonly photoUploading =
    signal(false);


  readonly photoError =
    signal('');


  handOverOdometerKm:
    number | null =
    null;


  edit: WorkOrderEditForm = {

    status:
      'OPEN',

    odometerKm:
      0,

    complaint:
      '',

    findings:
      '',

    recommendations:
      ''

  };


  labor: LaborFormValue = {

    description:
      '',

    hours:
      1,

    hourlyRate:
      0

  };


  laborEdit: LaborFormValue = {

    description:
      '',

    hours:
      1,

    hourlyRate:
      0

  };


  part: PartFormValue = {

    description:
      '',

    sku:
      '',

    quantity:
      1,

    unitPrice:
      0

  };


  partEdit: PartFormValue = {

    description:
      '',

    sku:
      '',

    quantity:
      1,

    unitPrice:
      0

  };


  ngOnInit(): void {

    this.load();

    this.loadSettings();

  }


  private loadSettings(): void {

    this.api
      .getSettings()
      .subscribe({

        next: settings => {

          const rate =
            settings.defaultHourlyRate
            ?? 0;


          this.labor = {

            ...this.labor,

            hourlyRate:
              rate

          };


          this.defaultHourlyRate.set(
            rate
          );

        },


        error: err => {

          console.error(
            'Műhelybeállítás betöltési hiba:',
            err
          );

        }

      });

  }


  private load(): void {

    const id =
      this.route
        .snapshot
        .paramMap
        .get('id');


    if (!id) {

      this.error.set(
        'Hiányzó munkalap azonosító.'
      );

      return;

    }


    this.error.set('');


    this.loadPhotos(
      id
    );


    this.api
      .getWorkOrder(id)
      .subscribe({

        next: order => {

          this.setOrder(
            order
          );

        },


        error: err => {

          console.error(
            'Munkalap betöltési hiba:',
            err
          );


          this.error.set(
            'Nem sikerült betölteni a munkalapot.'
          );

        }

      });

  }


  private loadPhotos(
    workOrderId: string
  ): void {

    this.photosLoading.set(
      true
    );


    this.photoError.set('');


    this.api
      .listWorkOrderPhotos(
        workOrderId
      )
      .pipe(

        finalize(() => {

          this.photosLoading.set(
            false
          );

        })

      )
      .subscribe({

        next: photos => {

          this.photos.set(
            photos
          );

        },


        error: err => {

          console.error(
            'Fotók betöltési hiba:',
            err
          );


          this.photoError.set(
            'Nem sikerült betölteni a fotókat.'
          );

        }

      });

  }


  uploadPhoto(
    event: Event
  ): void {

    const order =
      this.order();


    if (
      !order ||
      this.isCompleted() ||
      this.photoUploading()
    ) {

      return;

    }


    const input =
      event.target;


    if (
      !(input instanceof HTMLInputElement)
    ) {

      return;

    }


    const file =
      input.files?.[0];


    if (!file) {

      return;

    }


    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'image/webp'
    ];


    if (
      !allowedTypes.includes(
        file.type
      )
    ) {

      this.photoError.set(
        'Csak JPG, PNG vagy WEBP kép tölthető fel.'
      );


      input.value =
        '';

      return;

    }


    const maxSize =
      15 * 1024 * 1024;


    if (
      file.size > maxSize
    ) {

      this.photoError.set(
        'A kép legfeljebb 15 MB lehet.'
      );


      input.value =
        '';

      return;

    }


    this.photoUploading.set(
      true
    );


    this.photoError.set('');


    this.api
      .uploadWorkOrderPhoto(
        order.id,
        file
      )
      .pipe(

        finalize(() => {

          this.photoUploading.set(
            false
          );


          input.value =
            '';

        })

      )
      .subscribe({

        next: photo => {

          this.photos.update(
            current => [
              photo,
              ...current
            ]
          );

        },


        error: err => {

          console.error(
            'Fotó feltöltési hiba:',
            err
          );


          this.photoError.set(
            err?.error?.message
            ??
            'Nem sikerült feltölteni a fotót.'
          );

        }

      });

  }


  deletePhoto(
    photo: WorkOrderPhoto
  ): void {

    const order =
      this.order();


    if (
      !order ||
      this.isCompleted() ||
      this.photoUploading()
    ) {

      return;

    }


    const confirmed =
      confirm(
        `Biztosan törlöd ezt a fotót?\n\n${photo.originalFileName}`
      );


    if (!confirmed) {

      return;

    }


    this.photoUploading.set(
      true
    );


    this.photoError.set('');


    this.api
      .deleteWorkOrderPhoto(
        order.id,
        photo.id
      )
      .pipe(

        finalize(() => {

          this.photoUploading.set(
            false
          );

        })

      )
      .subscribe({

        next: () => {

          this.photos.update(
            current =>
              current.filter(
                item =>
                  item.id !==
                  photo.id
              )
          );

        },


        error: err => {

          console.error(
            'Fotó törlési hiba:',
            err
          );


          this.photoError.set(
            err?.error?.message
            ??
            'Nem sikerült törölni a fotót.'
          );

        }

      });

  }


  private setOrder(
    order: WorkOrder
  ): void {

    this.order.set(
      order
    );


    this.edit = {

      status:
        order.status,

      odometerKm:
        order.odometerKm,

      complaint:
        order.complaint
        ?? '',

      findings:
        order.findings
        ?? '',

      recommendations:
        order.recommendations
        ?? ''

    };


    if (
      order.status ===
      'READY_FOR_PICKUP'
    ) {

      this.handOverOdometerKm =
        order.handedOverOdometerKm
        ?? order.odometerKm;

    }


    if (
      order.status ===
        'COMPLETED' &&
      order.handedOverOdometerKm != null
    ) {

      this.handOverOdometerKm =
        order.handedOverOdometerKm;

    }

  }


  isCompleted(): boolean {

    return this.order()?.status
      === 'COMPLETED';

  }


  saveWorkOrder(): void {

    const order =
      this.order();


    if (
      !order ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    this.beginAction();


    this.api
      .updateWorkOrder(
        order.id,
        {

          status:
            this.edit.status,

          odometerKm:
            this.edit.odometerKm,

          complaint:
            this.edit
              .complaint
              .trim(),

          findings:
            this.edit
              .findings
              .trim(),

          recommendations:
            this.edit
              .recommendations
              .trim()

        }
      )
      .subscribe({

        next: updated => {

          this.setOrder(
            updated
          );


          this.saved.set(
            true
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Munkalap mentési hiba:',
            err
          );


          this.error.set(
            'Nem sikerült elmenteni a munkalapot.'
          );


          this.endAction();

        }

      });

  }


  markReady(): void {

    const order =
      this.order();


    if (
      !order ||
      this.actionBusy() ||
      order.status === 'COMPLETED' ||
      order.status === 'READY_FOR_PICKUP'
    ) {

      return;

    }


    const confirmed =
      confirm(
        'Biztosan készre állítod a javítást? A motor ezután átadásra kész állapotba kerül.'
      );


    if (!confirmed) {

      return;

    }


    this.beginAction();


    this.api
      .markWorkOrderReady(
        order.id
      )
      .subscribe({

        next: updated => {

          this.setOrder(
            updated
          );


          this.saved.set(
            true
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Javítás készre állítási hiba:',
            err
          );


          this.error.set(
            err?.error?.message
            ??
            'Nem sikerült átadásra készre állítani a munkalapot.'
          );


          this.endAction();

        }

      });

  }


  handOver(): void {

    const order =
      this.order();


    if (
      !order ||
      this.actionBusy() ||
      order.status !==
        'READY_FOR_PICKUP'
    ) {

      return;

    }


    const odometerKm =
      this.handOverOdometerKm;


    if (
      odometerKm == null ||
      !Number.isFinite(
        odometerKm
      ) ||
      odometerKm < 0
    ) {

      this.error.set(
        'Add meg az átadáskori kilométeróra állást.'
      );

      return;

    }


    if (
      odometerKm <
      order.odometerKm
    ) {

      this.error.set(
        'Az átadáskori kilométeróra nem lehet kisebb a beérkezési kilométeróra értékénél.'
      );

      return;

    }


    const confirmed =
      confirm(
        `A motorkerékpár ténylegesen átadásra került az ügyfélnek?\n\nÁtadáskori kilométeróra: ${odometerKm.toLocaleString('hu-HU')} km\n\nEz lezárja a munkalapot.`
      );


    if (!confirmed) {

      return;

    }


    this.beginAction();


    this.api
      .handOverWorkOrder(
        order.id,
        odometerKm
      )
      .subscribe({

        next: updated => {

          this.editingLaborId.set(
            null
          );


          this.editingPartId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.saved.set(
            true
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Motorkerékpár átadási hiba:',
            err
          );


          this.error.set(
            err?.error?.message
            ??
            'Nem sikerült lezárni a motorkerékpár átadását.'
          );


          this.endAction();

        }

      });

  }


  addLabor(
    form: NgForm
  ): void {

    const order =
      this.order();


    if (
      !order ||
      form.invalid ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    this.beginAction();


    this.api
      .addLabor(
        order.id,
        {

          description:
            this.labor
              .description
              .trim(),

          hours:
            this.labor.hours,

          hourlyRate:
            this.labor.hourlyRate

        }
      )
      .subscribe({

        next: updated => {

          this.editingLaborId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.labor = {

            description:
              '',

            hours:
              1,

            hourlyRate:
              this.defaultHourlyRate()

          };


          form.resetForm(
            this.labor
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Munkadíj hozzáadási hiba:',
            err
          );


          this.error.set(
            'Nem sikerült hozzáadni a munkadíjat.'
          );


          this.endAction();

        }

      });

  }


  startLaborEdit(
    item: LaborItem
  ): void {

    if (!item.id) {

      return;

    }


    this.editingPartId.set(
      null
    );


    this.editingLaborId.set(
      item.id
    );


    this.laborEdit = {

      description:
        item.description,

      hours:
        item.hours,

      hourlyRate:
        item.hourlyRate

    };

  }


  cancelLaborEdit(): void {

    this.editingLaborId.set(
      null
    );

  }


  saveLaborEdit(
    item: LaborItem
  ): void {

    const order =
      this.order();


    if (
      !order ||
      !item.id ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    if (
      !this.laborEdit
        .description
        .trim()
    ) {

      this.error.set(
        'A munka megnevezése kötelező.'
      );

      return;

    }


    if (
      this.laborEdit.hours <= 0
    ) {

      this.error.set(
        'A munkaóra legyen nagyobb nullánál.'
      );

      return;

    }


    if (
      this.laborEdit.hourlyRate < 0
    ) {

      this.error.set(
        'Az óradíj nem lehet negatív.'
      );

      return;

    }


    this.beginAction();


    this.api
      .updateLabor(
        order.id,
        item.id,
        {

          description:
            this.laborEdit
              .description
              .trim(),

          hours:
            this.laborEdit.hours,

          hourlyRate:
            this.laborEdit.hourlyRate

        }
      )
      .subscribe({

        next: updated => {

          this.editingLaborId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Munkadíj módosítási hiba:',
            err
          );


          this.error.set(
            'Nem sikerült módosítani a munkadíjat.'
          );


          this.endAction();

        }

      });

  }


  deleteLabor(
    item: LaborItem
  ): void {

    const order =
      this.order();


    if (
      !order ||
      !item.id ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    const confirmed =
      confirm(
        `Biztosan törlöd ezt a munkát?\n\n${item.description}`
      );


    if (!confirmed) {

      return;

    }


    this.beginAction();


    this.api
      .deleteLabor(
        order.id,
        item.id
      )
      .subscribe({

        next: updated => {

          this.editingLaborId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Munkadíj törlési hiba:',
            err
          );


          this.error.set(
            'Nem sikerült törölni a munkadíjat.'
          );


          this.endAction();

        }

      });

  }


  addPart(
    form: NgForm
  ): void {

    const order =
      this.order();


    if (
      !order ||
      form.invalid ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    this.beginAction();


    this.api
      .addPart(
        order.id,
        {

          description:
            this.part
              .description
              .trim(),

          sku:
            this.part
              .sku
              .trim(),

          quantity:
            this.part.quantity,

          unitPrice:
            this.part.unitPrice

        }
      )
      .subscribe({

        next: updated => {

          this.editingPartId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.part = {

            description:
              '',

            sku:
              '',

            quantity:
              1,

            unitPrice:
              0

          };


          form.resetForm(
            this.part
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Alkatrész hozzáadási hiba:',
            err
          );


          this.error.set(
            'Nem sikerült hozzáadni az alkatrészt.'
          );


          this.endAction();

        }

      });

  }


  startPartEdit(
    item: PartItem
  ): void {

    if (!item.id) {

      return;

    }


    this.editingLaborId.set(
      null
    );


    this.editingPartId.set(
      item.id
    );


    this.partEdit = {

      description:
        item.description,

      sku:
        item.sku ?? '',

      quantity:
        item.quantity,

      unitPrice:
        item.unitPrice

    };

  }


  cancelPartEdit(): void {

    this.editingPartId.set(
      null
    );

  }


  savePartEdit(
    item: PartItem
  ): void {

    const order =
      this.order();


    if (
      !order ||
      !item.id ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    if (
      !this.partEdit
        .description
        .trim()
    ) {

      this.error.set(
        'Az alkatrész megnevezése kötelező.'
      );

      return;

    }


    if (
      this.partEdit.quantity <= 0
    ) {

      this.error.set(
        'A mennyiség legyen nagyobb nullánál.'
      );

      return;

    }


    if (
      this.partEdit.unitPrice < 0
    ) {

      this.error.set(
        'Az egységár nem lehet negatív.'
      );

      return;

    }


    this.beginAction();


    this.api
      .updatePart(
        order.id,
        item.id,
        {

          description:
            this.partEdit
              .description
              .trim(),

          sku:
            this.partEdit
              .sku
              .trim(),

          quantity:
            this.partEdit.quantity,

          unitPrice:
            this.partEdit.unitPrice

        }
      )
      .subscribe({

        next: updated => {

          this.editingPartId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Alkatrész módosítási hiba:',
            err
          );


          this.error.set(
            'Nem sikerült módosítani az alkatrészt.'
          );


          this.endAction();

        }

      });

  }


  deletePart(
    item: PartItem
  ): void {

    const order =
      this.order();


    if (
      !order ||
      !item.id ||
      this.actionBusy() ||
      this.isCompleted()
    ) {

      return;

    }


    const confirmed =
      confirm(
        `Biztosan törlöd ezt a tételt?\n\n${item.description}`
      );


    if (!confirmed) {

      return;

    }


    this.beginAction();


    this.api
      .deletePart(
        order.id,
        item.id
      )
      .subscribe({

        next: updated => {

          this.editingPartId.set(
            null
          );


          this.setOrder(
            updated
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Alkatrész törlési hiba:',
            err
          );


          this.error.set(
            'Nem sikerült törölni az alkatrészt.'
          );


          this.endAction();

        }

      });

  }


  reopenOrder(): void {

    const order =
      this.order();


    if (
      !order ||
      this.actionBusy()
    ) {

      return;

    }


    const confirmed =
      confirm(
        'Biztosan újranyitod a lezárt munkalapot?'
      );


    if (!confirmed) {

      return;

    }


    this.beginAction();


    this.api
      .reopenWorkOrder(
        order.id
      )
      .subscribe({

        next: updated => {

          this.handOverOdometerKm =
            null;


          this.setOrder(
            updated
          );


          this.saved.set(
            true
          );


          this.endAction();

        },


        error: err => {

          console.error(
            'Munkalap újranyitási hiba:',
            err
          );


          this.error.set(
            'Nem sikerült újranyitni a munkalapot.'
          );


          this.endAction();

        }

      });

  }


  private beginAction(): void {

    this.actionBusy.set(
      true
    );


    this.saved.set(
      false
    );


    this.error.set('');

  }


  private endAction(): void {

    this.actionBusy.set(
      false
    );

  }

}
