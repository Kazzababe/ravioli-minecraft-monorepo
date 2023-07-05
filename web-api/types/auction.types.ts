import { CacheableEntity } from '../src/cache/cache'
import { ItemStack } from './mail.types'

export enum SortDirection {
  ASCENDING = 'ASCENDING',
  DESCENDING = 'DESCENDING',
}

export enum SortType {
  CREATED_AT = 'CREATED_AT',
  ALPHABETICAL = 'ALPHABETICAL',
  COST = 'COST',
}

export interface AuctionHouseItem extends CacheableEntity {
  id: bigint;
  userId: bigint;
  item: ItemStack;
  createdOn: Date;
  expiration: Date;
  currency: string;
  cost: number;
}