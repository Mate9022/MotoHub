import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-customer-search',
    standalone: true,
    imports: [FormsModule],
    templateUrl: './customer-search.component.html',
    styleUrls: ['./customer-search.component.css']
})
export class CustomerSearchComponent {

    @Input() search = '';

    @Output() readonly searchChange = new EventEmitter<string>();
    @Output() readonly searchSubmit = new EventEmitter<void>();

    onSearchChange(value: string): void {
        this.searchChange.emit(value);
    }
}
