import {
  Component,
  EventEmitter,
  Output
} from '@angular/core';

import { FormsModule } from '@angular/forms';


@Component({
  selector: 'app-work-order-search',
  standalone: true,

  imports: [
    FormsModule
  ],

  templateUrl:
    './work-order-search.component.html',

  styleUrls: [
    './work-order-search.component.css'
  ]
})
export class WorkOrderSearchComponent {

  @Output()
  readonly searchRequested =
    new EventEmitter<string>();


  search = '';


  submit(): void {

    this.searchRequested.emit(
      this.search
    );

  }

}
