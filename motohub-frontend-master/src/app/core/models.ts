export interface LaborItem {
  id: string;
  description: string;
  hours: number;
  hourlyRate: number;
  total: number;
}


export interface Customer {
  id: string;
  name: string;
  phone: string | null;
  email: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCustomerRequest {
  name: string;
  phone?: string;
  email?: string;
}

export interface UpdateCustomerRequest {
  name: string;
  phone?: string;
  email?: string;
}

export interface Motorcycle {
  id: string;
  customerId: string;
  customerName: string;
  brand: string;
  model: string;
  modelYear: number | null;
  licensePlate: string | null;
  vin: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMotorcycleRequest {
  customerId: string;
  brand: string;
  model: string;
  modelYear?: number;
  licensePlate?: string;
  vin?: string;
}

export interface UpdateMotorcycleRequest {
  brand: string;
  model: string;
  modelYear?: number;
  licensePlate?: string;
  vin?: string;
}

export type WorkOrderStatus =
    | 'OPEN'
    | 'IN_PROGRESS'
    | 'WAITING_PARTS'
    | 'READY_FOR_PICKUP'
    | 'COMPLETED';


export interface LaborItem {
  id: string;
  description: string;
  hours: number;
  hourlyRate: number;
  total: number;
}


export interface PartItem {
  id: string;
  description: string;
  sku: string | null;
  quantity: number;
  unitPrice: number;
  total: number;
}


export interface WorkOrder {

  id: string;

  workOrderNumber: string;

  status: WorkOrderStatus;

  odometerKm: number;

  handedOverOdometerKm: number | null;

  complaint: string | null;

  findings: string | null;

  recommendations: string | null;

  receivedAt: string;

  readyAt: string | null;

  handedOverAt: string | null;

  closedAt: string | null;

  createdAt: string;

  updatedAt: string;

  motorcycleId: string;

  motorcycle: string;

  licensePlate: string | null;

  customerId: string;

  customerName: string;

  laborItems: LaborItem[];

  partItems: PartItem[];

  laborTotal: number;

  partsTotal: number;

  grandTotal: number;
}


export interface CreateWorkOrderRequest {
  motorcycleId: string;
  odometerKm: number;
  complaint?: string;
}


export interface UpdateWorkOrderRequest {
  status: WorkOrderStatus;
  odometerKm: number;
  complaint?: string;
  findings?: string;
  recommendations?: string;
}


export interface CreateLaborItemRequest {
  description: string;
  hours: number;
  hourlyRate: number;
}


export interface UpdateLaborItemRequest {
  description: string;
  hours: number;
  hourlyRate: number;
}


export interface CreatePartItemRequest {
  description: string;
  sku?: string;
  quantity: number;
  unitPrice: number;
}


export interface UpdatePartItemRequest {
  description: string;
  sku?: string;
  quantity: number;
  unitPrice: number;
}

export interface WorkshopSettings {
  workshopName: string;
  address: string | null;
  phone: string | null;
  email: string | null;
  taxNumber: string | null;
  defaultHourlyRate: number;
  createdAt: string | null;
  updatedAt: string | null;
}


export interface UpdateWorkshopSettingsRequest {
  workshopName: string;
  address?: string;
  phone?: string;
  email?: string;
  taxNumber?: string;
  defaultHourlyRate: number;
}

export interface WorkOrderPhoto {

  id: string;

  originalFileName: string;

  contentType: string;

  fileSize: number;

  createdAt: string;
}
