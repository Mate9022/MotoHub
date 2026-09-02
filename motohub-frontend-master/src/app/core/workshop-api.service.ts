import { Injectable, inject } from '@angular/core';
import {
    HttpClient,
    HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';

import {
    Customer,
    Motorcycle,

    WorkOrder,

    CreateWorkOrderRequest,
    UpdateWorkOrderRequest,

    CreateLaborItemRequest,
    UpdateLaborItemRequest,

    CreatePartItemRequest,
    UpdatePartItemRequest,

    CreateCustomerRequest,
    UpdateCustomerRequest,

    CreateMotorcycleRequest,
    UpdateMotorcycleRequest,

    WorkshopSettings,
    UpdateWorkshopSettingsRequest, WorkOrderPhoto
} from './models';


@Injectable({
    providedIn: 'root'
})
export class WorkshopApiService {

    private readonly http =
        inject(HttpClient);

    // private readonly apiUrl =
    //     'http://localhost:8080/api';

    private readonly apiUrl = '/api';


    markWorkOrderReady(
        id: string
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders/${id}/ready`,
            {}
        );

    }


    handOverWorkOrder(
        id: string,
        odometerKm: number
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders/${id}/handover`,
            {
                odometerKm
            }
        );
    }

    getSettings(): Observable<WorkshopSettings> {

        return this.http.get<WorkshopSettings>(
            `${this.apiUrl}/settings`
        );
    }

    listWorkOrderPhotos(
        workOrderId: string
    ): Observable<WorkOrderPhoto[]> {

        return this.http.get<WorkOrderPhoto[]>(
            `${this.apiUrl}/work-orders/${workOrderId}/photos`
        );
    }


    uploadWorkOrderPhoto(
        workOrderId: string,
        file: File
    ): Observable<WorkOrderPhoto> {

        const formData =
            new FormData();


        formData.append(
            'file',
            file
        );


        return this.http.post<WorkOrderPhoto>(
            `${this.apiUrl}/work-orders/${workOrderId}/photos`,
            formData
        );
    }


    deleteWorkOrderPhoto(
        workOrderId: string,
        photoId: string
    ): Observable<void> {

        return this.http.delete<void>(
            `${this.apiUrl}/work-orders/${workOrderId}/photos/${photoId}`
        );
    }


    workOrderPhotoUrl(
        workOrderId: string,
        photoId: string
    ): string {

        return `${this.apiUrl}/work-orders/${workOrderId}/photos/${photoId}/content`;
    }

    updateSettings(
        request: UpdateWorkshopSettingsRequest
    ): Observable<WorkshopSettings> {

        return this.http.put<WorkshopSettings>(
            `${this.apiUrl}/settings`,
            request
        );
    }

    listCustomers(
        search = ''
    ): Observable<Customer[]> {

        let params =
            new HttpParams();

        if (search.trim()) {

            params = params.set(
                'search',
                search.trim()
            );
        }

        return this.http.get<Customer[]>(
            `${this.apiUrl}/customers`,
            { params }
        );
    }


    getCustomer(
        id: string
    ): Observable<Customer> {

        return this.http.get<Customer>(
            `${this.apiUrl}/customers/${id}`
        );
    }


    createCustomer(
        request: CreateCustomerRequest
    ): Observable<Customer> {

        return this.http.post<Customer>(
            `${this.apiUrl}/customers`,
            request
        );
    }


    updateCustomer(
        id: string,
        request: UpdateCustomerRequest
    ): Observable<Customer> {

        return this.http.put<Customer>(
            `${this.apiUrl}/customers/${id}`,
            request
        );
    }


    // =====================================================
    // MOTORCYCLE
    // =====================================================

    listMotorcycles(
        search = ''
    ): Observable<Motorcycle[]> {

        let params =
            new HttpParams();

        if (search.trim()) {

            params = params.set(
                'search',
                search.trim()
            );
        }

        return this.http.get<Motorcycle[]>(
            `${this.apiUrl}/motorcycles`,
            { params }
        );
    }


    listCustomerMotorcycles(
        customerId: string
    ): Observable<Motorcycle[]> {

        return this.http.get<Motorcycle[]>(
            `${this.apiUrl}/motorcycles/customer/${customerId}`
        );
    }


    getMotorcycle(
        id: string
    ): Observable<Motorcycle> {

        return this.http.get<Motorcycle>(
            `${this.apiUrl}/motorcycles/${id}`
        );
    }


    createMotorcycle(
        request: CreateMotorcycleRequest
    ): Observable<Motorcycle> {

        return this.http.post<Motorcycle>(
            `${this.apiUrl}/motorcycles`,
            request
        );
    }


    updateMotorcycle(
        id: string,
        request: UpdateMotorcycleRequest
    ): Observable<Motorcycle> {

        return this.http.put<Motorcycle>(
            `${this.apiUrl}/motorcycles/${id}`,
            request
        );
    }


    // =====================================================
    // WORK ORDER
    // =====================================================

    listWorkOrders(
        search = ''
    ): Observable<WorkOrder[]> {

        let params =
            new HttpParams();

        if (search.trim()) {

            params = params.set(
                'search',
                search.trim()
            );
        }

        return this.http.get<WorkOrder[]>(
            `${this.apiUrl}/work-orders`,
            { params }
        );
    }


    getWorkOrder(
        id: string
    ): Observable<WorkOrder> {

        return this.http.get<WorkOrder>(
            `${this.apiUrl}/work-orders/${id}`
        );
    }


    listMotorcycleWorkOrders(
        motorcycleId: string
    ): Observable<WorkOrder[]> {

        return this.http.get<WorkOrder[]>(
            `${this.apiUrl}/work-orders/motorcycle/${motorcycleId}`
        );
    }


    createWorkOrder(
        request: CreateWorkOrderRequest
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders`,
            request
        );
    }


    updateWorkOrder(
        id: string,
        request: UpdateWorkOrderRequest
    ): Observable<WorkOrder> {

        return this.http.put<WorkOrder>(
            `${this.apiUrl}/work-orders/${id}`,
            request
        );
    }


    closeWorkOrder(
        id: string
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders/${id}/close`,
            {}
        );
    }


    reopenWorkOrder(
        id: string
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders/${id}/reopen`,
            {}
        );
    }


    // =====================================================
    // LABOR
    // =====================================================

    addLabor(
        workOrderId: string,
        request: CreateLaborItemRequest
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders/${workOrderId}/labor-items`,
            request
        );
    }


    updateLabor(
        workOrderId: string,
        itemId: string,
        request: UpdateLaborItemRequest
    ): Observable<WorkOrder> {

        return this.http.put<WorkOrder>(
            `${this.apiUrl}/work-orders/${workOrderId}/labor-items/${itemId}`,
            request
        );
    }


    deleteLabor(
        workOrderId: string,
        itemId: string
    ): Observable<WorkOrder> {

        return this.http.delete<WorkOrder>(
            `${this.apiUrl}/work-orders/${workOrderId}/labor-items/${itemId}`
        );
    }


    // =====================================================
    // PARTS
    // =====================================================

    addPart(
        workOrderId: string,
        request: CreatePartItemRequest
    ): Observable<WorkOrder> {

        return this.http.post<WorkOrder>(
            `${this.apiUrl}/work-orders/${workOrderId}/part-items`,
            request
        );
    }


    updatePart(
        workOrderId: string,
        itemId: string,
        request: UpdatePartItemRequest
    ): Observable<WorkOrder> {

        return this.http.put<WorkOrder>(
            `${this.apiUrl}/work-orders/${workOrderId}/part-items/${itemId}`,
            request
        );
    }


    deletePart(
        workOrderId: string,
        itemId: string
    ): Observable<WorkOrder> {

        return this.http.delete<WorkOrder>(
            `${this.apiUrl}/work-orders/${workOrderId}/part-items/${itemId}`
        );
    }


    // =====================================================
    // PDF
    // =====================================================

    pdfUrl(
        workOrderId: string
    ): string {

        return `${this.apiUrl}/work-orders/${workOrderId}/pdf`;
    }
}