import { Injectable } from '@nestjs/common';
import RedisCache, { CollectionFetchResult } from '../cache/cache'
import {
  CreateMailboxItemType,
  MailboxItem,
  MailboxItemCategory,
  MailboxItemItemStacks,
  MailboxItemType,
  MailboxType,
} from '../../types/mail.types'
import prisma from '../service/prisma'

const PRIMARY_ID = (type: MailboxType) => `USER_${type}_MAILBOX`;
const PARSERS: Record<MailboxItemType, (data: any) => MailboxItem> = {
  [MailboxItemType.ITEM]: (data: any): MailboxItemItemStacks => {
    const itemJson = JSON.parse(data.item);

    return {
      serialize: function() {
        return this;
      },
      name: 'mail-item-stacks',
      id: BigInt(data.id),
      userId: BigInt(data.user_id),
      mailboxType: data.mailboxType as MailboxType,
      mailboxItemCategory: data.category as MailboxItemCategory,
      title: data.title!,
      message: data.message,
      createdOn: data.created_at!,
      updatedOn: data.updated_at!,
      opened: data.opened,
      type: MailboxItemType.ITEM,
      items: itemJson.map((item: any) => ({
        item: item.item,
        amount: Number.parseInt(item.amount),
        data: item.data,
      })),
    };
  }
}

@Injectable()
export class MailService {
  private readonly cache: RedisCache<MailboxItem>;

  constructor() {
    this.cache = new RedisCache<MailboxItem>(`mail`, 1000 * 60 * 5);

    this.cache.register(`mail-item-stacks`, (data) => ({
      id: BigInt(data.id),
      userId: BigInt(data.userId),
      mailboxType: data.mailboxType as MailboxType,
      mailboxItemCategory: data.mailboxItemCategory as MailboxItemCategory,
      message: data.message,
      title: data.title,
      createdOn: new Date(Number.parseInt(data.createdOn)),
      updatedOn: new Date(Number.parseInt(data.updatedOn)),
      opened: data.opened === "opened",
      type: data.type as MailboxItemType,
      items: data.items.map((item: any) => ({
        item: item.item,
        amount: Number.parseInt(item.amount),
        data: item.data,
      })),
    }));
  }
  
  async fetchRange(userId: number, start: number, end: number, type: MailboxType): Promise<CollectionFetchResult<Omit<MailboxItem, "serialize" | "name">>> {
    const keys = [userId, type, start, end];
    const size = end - start;
    const collectionId = PRIMARY_ID(type);

    return await this.cache.getCollectionOr(collectionId, keys, async () => {
      const [items] = await prisma.user_mailbox.findMany({
        select: {
          id: true,
          user_id: true,
          type: true,
          mailbox_item: {
            select: {
              id: true,
              title: true,
              item: true,
              message: true,
              opened: true,
              type: true,
              category: true,
              group_id: true,
              created_at: true,
              updated_at: true,
            },
          },
        },
        where: {
          user_id: userId,
          type,
        },
        orderBy: {
          id: 'desc',
        },
        skip: start,
        take: size + 1,
      });
      const parsedItems: MailboxItem[] = items.mailbox_item.map((data) => {
        const type = data.type as MailboxItemType;

        return PARSERS[type]({
          ...data,
          user_id: userId,
          mailboxType: items.type as MailboxType,
        }) as MailboxItem;
      });
      const next = items.mailbox_item.length >= size;

      await this.cache.cacheCollection(collectionId, keys, parsedItems, next);

      return {
        items: parsedItems,
        next,
      }
    });
  }

  async create(item: CreateMailboxItemType): Promise<BigInt> {
    const userMailbox = await prisma.user_mailbox.upsert({
      where: {
        user_id_type: {
          user_id: item.userId,
          type: item.mailboxType,
        }
      },
      create: {
        user_id: item.userId,
        type: item.mailboxType,
      },
      update: {}
    });
    const itemData = item.data;
    const normalizedItemData = Array.isArray(itemData) ? itemData : [itemData];
    const result = await prisma.mailbox_item.create({
      data: {
        mailbox_id: userMailbox.id,
        item: JSON.stringify(normalizedItemData),
        message: item.message,
        title: item.title,
        category: item.mailboxItemCategory,
        type: item.type,
        group_id: item.groupId,
      },
    });
    await this.cache.invalidateCollection(PRIMARY_ID(item.mailboxType));

    return result.id;
  }

  async changeOpened(id: number, opened: boolean, mailboxType: MailboxType): Promise<void> {
    await this.cache.invalidateCollection(PRIMARY_ID(mailboxType));
    await prisma.mailbox_item.update({
      where: {
        id,
      },
      data: {
        opened,
      }
    });
  }

  async delete(id: number, mailboxType: MailboxType): Promise<void> {
    await this.cache.invalidateCollection(PRIMARY_ID(mailboxType));
    await prisma.mailbox_item.delete({
      where: {
        id,
      }
    });
  }
}