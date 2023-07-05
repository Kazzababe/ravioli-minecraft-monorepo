import { HttpStatus, Injectable } from '@nestjs/common';
import RedisCache from '../cache/cache'
import prisma from '../service/prisma'
import { CurrencyData, TransferResult, UpdateResult } from '../../types/currency.types'
import { HttpError } from '../util/error'
import { publishToMinecraft } from '../service/event-receiver'

const ENTITY_NAME = `currency`;

type NormalCurrencyData = Omit<CurrencyData, "serialize" | "name">

@Injectable()
export class CurrencyService {
  private readonly cache: RedisCache<CurrencyData>;

  constructor() {
    this.cache = new RedisCache<CurrencyData>(`currency`, 1000 * 60 * 5);

    this.cache.register(ENTITY_NAME, (data) => ({
      userId: data.userId,
      currency: data.currency,
      balance: data.balance,
    }));
  }

  async transfer(senderId: bigint, receiverId: bigint, currency: string, amount: number): Promise<TransferResult> {
    const [result] = await prisma.$queryRaw<TransferResult[]>`SELECT * FROM "transferCurrency"(${senderId}::BIGINT, ${receiverId}::BIGINT, ${currency}::VARCHAR, ${amount}::NUMERIC);`

    if (!result) {
      throw new HttpError(HttpStatus.INTERNAL_SERVER_ERROR, 'No data was returned attempting to update transfer balance to another user.');
    }
    if (result.updated_rows == null) {
      throw new HttpError(HttpStatus.BAD_REQUEST, 'User cannot have a negative balance.');
    }
    return result;
  }

  async update(userId: bigint, currency: string, amount: number): Promise<UpdateResult> {
    const [result] = await prisma.$queryRaw<UpdateResult[]>`SELECT * FROM "updateCurrency"(${userId}::BIGINT, ${currency}::VARCHAR, ${amount}::NUMERIC);`;

    if (!result) {
      throw new HttpError(HttpStatus.INTERNAL_SERVER_ERROR, 'No data was returned attempting to update user balance.');
    }
    if (result.updated_rows == null) {
      throw new HttpError(HttpStatus.BAD_REQUEST, 'User cannot have a negative balance.');
    }
    await publishToMinecraft('minecraft-event:user-currency-balance-update', {
      userId,
      currency,
      amount,
      balance: result.new_amount,
    });

    return result;
  }

  async fetch(userId: bigint, currency: string): Promise<number> {
    const keys = [userId, currency];
    const result = await this.cache.getOr(keys, async () => {
      const data = await prisma.user_currency.findFirst({
        where: {
          user_id: userId,
          currency,
        },
      });

      if (!data) {
        return {
          userId,
          currency,
          balance: 0,
        }
      }
      return {
        userId,
        currency,
        balance: Number(data.amount),
      }
    });

    if (!result) {
      return 0;
    }
    return Number(result.balance);
  }
}