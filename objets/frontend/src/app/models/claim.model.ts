export type ClaimStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Claim {
  id?: number;
  itemId: number;
  itemTitle?: string;
  claimantName: string;
  claimantContact: string;
  description?: string;
  status: ClaimStatus;
  createdAt?: string;
  updatedAt?: string;
}
