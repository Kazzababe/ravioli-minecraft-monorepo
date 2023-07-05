import { Injectable } from '@nestjs/common';
import RedisCache, { CollectionFetchResult } from '../cache/cache'
import { AuctionHouseItem, SortDirection, SortType } from '../../types/auction.types'
import prisma from '../service/prisma'
import { MailService } from '../mail/mail.service'
import { ItemStack, MailboxItemCategory, MailboxItemType, MailboxType } from '../../types/mail.types'

const PRIMARY_ID = 'AUCTION_HOUSE';
const USER_ID = (id: number | bigint) => `USER_${id}_AUCTION_HOUSE`;
const ENTITY_NAME = `auction-house-item`;

type NormalAuctionHouseItem = Omit<AuctionHouseItem, "serialize" | "name">

@Injectable()
export class AuctionService {
  private readonly cache: RedisCache<AuctionHouseItem>;

  constructor(private readonly mailService: MailService) {
    this.cache = new RedisCache<AuctionHouseItem>(`auction_house`, 1000 * 60 * 5);

    this.cache.register(ENTITY_NAME, (data) => ({
      id: BigInt(data.id),
      userId: BigInt(data.userId),
      item: data.item,
      createdOn: new Date(Number.parseInt(data.createdOn)),
      expiration: new Date(Number.parseInt(data.expiration)),
      currency: data.currency,
      cost: Number.parseInt(`${data.cost}`),
    }));
  }

  async create(userId: number, item: ItemStack, expiration: Date, currency: string, cost: number, displayName: string): Promise<bigint> {
    const auctionHouseItem = await prisma.auction_house_item.create({
      data: {
        item: JSON.stringify(item),
        expiration,
        currency,
        cost,
        display_name: displayName,
        user_id: userId,
      }
    });

    await this.cache.invalidateCollection(PRIMARY_ID);
    await this.cache.invalidateCollection(USER_ID(userId));

    return auctionHouseItem.id;
  }

  async delete(ids: number[] | bigint[]): Promise<void> {
    for (const id of ids) {
      await this.cache.invalidateCollection(USER_ID(id))
    }
    await this.cache.invalidateCollection(PRIMARY_ID);
    await prisma.auction_house_item.deleteMany({
      where: {
        id: {
          in: ids,
        }
      }
    });
  }

  async fetchRange(direction: SortDirection, type: SortType, start: number, end: number): Promise<CollectionFetchResult<NormalAuctionHouseItem>> {
    const keys = [direction, type, start, end];
    
    return await this.cache.getCollectionOr(PRIMARY_ID, keys, () => {
      return this._fetchRange(direction, type, start, end, true);
    });
  }

  private async _fetchRange(direction: SortDirection, type: SortType, start: number, end: number, cache: boolean): Promise<CollectionFetchResult<NormalAuctionHouseItem>> {
    const keys = [direction, type, start, end];
    const size = end - start;
    const results = await prisma.auction_house_item.findMany({
      skip: start,
      take: size + 1,
      orderBy: {
        display_name: 'asc',
      },
    });
    const auctionHouseItems = results.map((item) => {
      const itemJson = JSON.parse(item.item);

      return {
        id: item.id,
        userId: item.user_id,
        item: {
          item: itemJson.item,
          amount: Number.parseInt(itemJson.amount),
          data: itemJson.data || {},
        },
        createdOn: new Date(item.created_at),
        expiration: new Date(item.expiration),
        currency: item.currency,
        cost: item.cost,
        name: ENTITY_NAME,
        serialize: function() {
          return this;
        }
      }
    });

    if (cache) {
      await this.cache.cacheCollection(PRIMARY_ID, keys, auctionHouseItems, auctionHouseItems.length >= size);
    }
    return {
      items: auctionHouseItems.map((item) => {
        const { serialize, name, ...auctionHouseItem } = item;

        return auctionHouseItem;
      }),
      next: auctionHouseItems.length >= size,
    }
  }

  async checkExpirations(): Promise<void> {
    const expiredItems = await prisma.auction_house_item.findMany({
      where: {
        expiration: {
          lt: new Date(),
        },
      },
    });

    if (!expiredItems) {
      return;
    }
    const uniqueUserIds = new Set(expiredItems.map((row) => row.user_id));

    for (const userId of uniqueUserIds) {
      await this.cache.invalidateCollection(USER_ID(userId))
    }
    await this.cache.invalidateCollection(PRIMARY_ID);
    await prisma.auction_house_item.deleteMany({
      where: {
        id: {
          in: expiredItems.map((row) => row.id),
        },
      },
    });
    expiredItems.forEach((item) => {
      this.mailService.create({
        userId: item.user_id,
        mailboxType: MailboxType.AUCTION_HOUSE,
        mailboxItemCategory: MailboxItemCategory.GENERAL,
        groupId: "all",
        title: 'string',
        data: [JSON.parse(item.item)],
        message: '',
        type: MailboxItemType.ITEM,
      });
    });
  }

  async fetchUserRange(userId: number, direction: SortDirection, type: SortType, start: number, end: number): Promise<CollectionFetchResult<NormalAuctionHouseItem>> {
    const keys = [direction, type, start, end];
    const collectionId = USER_ID(userId);

    return await this.cache.getCollectionOr(collectionId, keys, () => {
      return this._fetchUserRange(userId, direction, type, start, end, true);
    });
  }

  private async _fetchUserRange(userId: number, direction: SortDirection, type: SortType, start: number, end: number, cache: boolean): Promise<CollectionFetchResult<NormalAuctionHouseItem>> {
    const collectionId = USER_ID(userId);
    const keys = [direction, type, start, end];
    const size = end - start;
    const results = await prisma.auction_house_item.findMany({
      where: {
        user_id: userId,
      },
      skip: start,
      take: size + 1,
      orderBy: {
        display_name: 'asc',
      },
    });
    const auctionHouseItems = results.map((item) => {
      const itemJson = JSON.parse(item.item);

      return {
        id: item.id,
        userId: item.user_id,
        item: {
          item: itemJson.item,
          amount: Number.parseInt(itemJson.amount),
          data: itemJson.data || {},
        },
        createdOn: new Date(item.created_at),
        expiration: new Date(item.expiration),
        currency: item.currency,
        cost: item.cost,
        name: ENTITY_NAME,
        serialize: function() {
          return this;
        }
      }
    });

    if (cache) {
      await this.cache.cacheCollection(collectionId, keys, auctionHouseItems, auctionHouseItems.length >= size);
    }
    return {
      items: auctionHouseItems.map((item) => {
        const { serialize, name, ...auctionHouseItem } = item;

        return auctionHouseItem;
      }),
      next: auctionHouseItems.length >= size,
    }
  }
}