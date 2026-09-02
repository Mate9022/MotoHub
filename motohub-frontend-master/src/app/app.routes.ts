import {Routes} from '@angular/router';
import {CustomersPageComponent} from "./features/customers/customers-page/customers-page.component";
import {NewCustomerPageComponent} from "./features/customers/new-customer-page/new-customer-page.component";
import {CustomerDetailPageComponent} from "./features/customers/customer-detail-page/customer-detail-page.component";
import {MotorcyclesPageComponent} from "./features/motorcycles/motorcycles-page/motorcycles-page.component";
import {
    MotorcycleDetailPageComponent
} from "./features/motorcycles/motorcycle-detail-page/motorcycle-detail-page.component";
import {NewMotorcyclePageComponent} from "./features/motorcycles/new-motorcycle-page/new-motorcycle-page.component";
import {SettingsPageComponent} from "./features/settings/settings-page/settings-page.component";
import {WorkOrdersPageComponent} from "./features/work-orders/work-orders-page/work-orders-page.component";
import {NewWorkOrderPageComponent} from "./features/work-orders/new-work-order-page/new-work-order-page.component";
import {
    WorkOrderDetailPageComponent
} from "./features/work-orders/work-order-detail-page/work-order-detail-page.component";

export const routes: Routes = [
    {path: '', pathMatch: 'full', redirectTo: 'work-orders'},
    {path: 'work-orders', component: WorkOrdersPageComponent},
    {path: 'work-orders/new', component: NewWorkOrderPageComponent},
    {path: 'work-orders/:id', component: WorkOrderDetailPageComponent},
    {path: 'customers', component: CustomersPageComponent},
    {path: 'customers/new', component: NewCustomerPageComponent},
    {path: 'customers/:id', component: CustomerDetailPageComponent},
    {
        path: 'motorcycles',
        component: MotorcyclesPageComponent
    },

    {
        path: 'motorcycles/:id',
        component: MotorcycleDetailPageComponent
    },

    {
        path: 'customers/:customerId/motorcycles/new',
        component: NewMotorcyclePageComponent
    },
    {
        path: 'settings',
        component: SettingsPageComponent
    },
    {path: '**', redirectTo: 'work-orders'}
];
