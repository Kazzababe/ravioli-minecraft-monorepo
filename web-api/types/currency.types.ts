import { CacheableEntity } from '../src/cache/cache'
import { ItemStack } from './mail.types'

export interface CurrencyData extends CacheableEntity {
  userId: bigint;
  currency: string;
  balance: number;
}

export interface UpdateResult {
  updated_rows: number;
  new_amount: number;
}

export interface TransferResult {
  updated_rows: number;
  new_from_amount: number;
  new_to_amount: number;
}