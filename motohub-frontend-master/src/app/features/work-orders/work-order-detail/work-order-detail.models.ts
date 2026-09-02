import {WorkOrderStatus} from "../../../core/models";


export interface WorkOrderEditForm {
  status: WorkOrderStatus;
  odometerKm: number;
  complaint: string;
  findings: string;
  recommendations: string;
}

export interface LaborFormValue {
  description: string;
  hours: number;
  hourlyRate: number;
}

export interface PartFormValue {
  description: string;
  sku: string;
  quantity: number;
  unitPrice: number;
}
