import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-motorcycle-search',
    standalone: true,
    imports: [FormsModule],
    templateUrl: './motorcycle-search.component.html',
    styleUrls: ['./motorcycle-search.component.css']
})
export class MotorcycleSearchComponent {

    @Input() search = '';

    @Output() readonly searchChange = new EventEmitter<string>();
    @Output() readonly searchSubmit = new EventEmitter<void>();

    onSearchChange(value: string): void {
        this.searchChange.emit(value);
    }
}
