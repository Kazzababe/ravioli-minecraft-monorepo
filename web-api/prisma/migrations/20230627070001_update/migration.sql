/*
  Warnings:

  - You are about to drop the column `deleted` on the `mailbox_item` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "mailbox_item" DROP COLUMN "deleted";
