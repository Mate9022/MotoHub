import {
  Component,
  EventEmitter,
  Input,
  Output,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import {WorkshopApiService} from "../../../core/workshop-api.service";
import {WorkOrderPhoto} from "../../../core/models";


@Component({
  selector: 'app-work-order-photos-section',
  standalone: true,

  imports: [
    CommonModule
  ],

  templateUrl:
    './work-order-photos-section.component.html',

  styleUrls: [
    './work-order-photos-section.component.css'
  ]
})
export class WorkOrderPhotosSectionComponent {

  readonly api =
    inject(WorkshopApiService);


  @Input({
    required: true
  })
  workOrderId =
    '';


  @Input({
    required: true
  })
  photos: WorkOrderPhoto[] = [];


  @Input()
  loading =
    false;


  @Input()
  uploading =
    false;


  @Input()
  completed =
    false;


  @Input()
  error =
    '';


  @Output()
  readonly uploadRequested =
    new EventEmitter<Event>();


  @Output()
  readonly deleteRequested =
    new EventEmitter<WorkOrderPhoto>();


  formatFileSize(
    bytes: number
  ): string {

    if (
      bytes < 1024
    ) {

      return `${bytes} B`;

    }


    const kilobytes =
      bytes / 1024;


    if (
      kilobytes < 1024
    ) {

      return `${kilobytes.toLocaleString(
        'hu-HU',
        {
          maximumFractionDigits:
            0
        }
      )} KB`;

    }


    const megabytes =
      kilobytes / 1024;


    return `${megabytes.toLocaleString(
      'hu-HU',
      {
        minimumFractionDigits:
          1,

        maximumFractionDigits:
          2
      }
    )} MB`;

  }

}
