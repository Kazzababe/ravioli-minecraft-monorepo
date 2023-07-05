import { CacheableEntity } from '../src/cache/cache'

export enum MailboxType {
  SERVER = 'SERVER',
  AUCTION_HOUSE = 'AUCTION_HOUSE',
}

export enum MailboxItemType {
  ITEM = 'ITEM',
}

export enum MailboxItemCategory {
  GENERAL = 'GENERAL',
  PATCH_NOTES = 'PATCH_NOTES',
  ANNOUNCEMENTS = 'ANNOUNCEMENTS',
}

export interface CreateMailboxItemType {
  userId: number | bigint;
  mailboxType: MailboxType;
  mailboxItemCategory: MailboxItemCategory;
  groupId: string;
  title: string;
  data: MailboxItemData;
  message: string | null;
  type: MailboxItemType;
}

export type MailboxItemData = ItemStack[];

export interface MailboxItem extends CacheableEntity {
  id: bigint;
  userId: bigint;
  mailboxType: MailboxType;
  type: MailboxItemType;
  mailboxItemCategory: MailboxItemCategory;
  message: string | null;
  title: string;
  createdOn: Date;
  updatedOn: Date;
  opened: boolean;
}

export interface ItemStack {
  item: string;
  amount: number;
  data: object;
}

export interface MailboxItemItemStacks extends MailboxItem {
  items: ItemStack[];
}