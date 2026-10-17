export type ItemType = 'LOST' | 'FOUND';
export type ItemStatus = 'ACTIVE' | 'CLAIMED' | 'RETURNED';

export interface Item {
  id?: number;
  title: string;
  description?: string;
  category: string;
  location: string;
  date: string;          // ISO date: "2026-10-17"
  photoUrl?: string;
  type: ItemType;
  status: ItemStatus;
  reporterName?: string;
  reporterContact?: string;
  createdAt?: string;
  updatedAt?: string;
  claimsCount?: number;
}

export interface ItemSearchParams {
  keyword?: string;
  category?: string;
  location?: string;
  type?: ItemType;
  status?: ItemStatus;
}
