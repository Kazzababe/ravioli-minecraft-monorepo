-- CreateEnum
CREATE TYPE "quest_status" AS ENUM ('ACTIVE', 'COMPLETED');

-- CreateTable
CREATE TABLE "auction_house_item" (
    "id" BIGSERIAL NOT NULL,
    "user_id" BIGINT NOT NULL,
    "item" TEXT NOT NULL,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "expiration" TIMESTAMP(3) NOT NULL,
    "currency" VARCHAR(64) NOT NULL,
    "cost" INTEGER NOT NULL,
    "display_name" VARCHAR(64) NOT NULL,

    CONSTRAINT "auction_house_item_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auction_house_sell_log" (
    "user_id" BIGSERIAL NOT NULL,
    "item" VARCHAR(1024) NOT NULL,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "currency" VARCHAR(64) NOT NULL,
    "cost" INTEGER NOT NULL,
    "tsv" tsvector,

    CONSTRAINT "auction_house_sell_log_pkey" PRIMARY KEY ("user_id")
);

-- CreateTable
CREATE TABLE "dialogue_state" (
    "user_id" UUID NOT NULL,
    "dialogue" VARCHAR(64) NOT NULL,
    "state" INTEGER NOT NULL
);

-- CreateTable
CREATE TABLE "friend_requests" (
    "id" BIGSERIAL NOT NULL,
    "sender_id" BIGINT NOT NULL,
    "receiver_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "friend_requests_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "friends" (
    "id" BIGSERIAL NOT NULL,
    "first_friend_id" BIGINT NOT NULL,
    "second_friend_id" BIGINT NOT NULL,
    "created_at" TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "friends_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "mailbox_item" (
    "id" BIGSERIAL NOT NULL,
    "mailbox_id" BIGINT NOT NULL,
    "item" TEXT NOT NULL,
    "message" TEXT,
    "opened" BOOLEAN NOT NULL DEFAULT false,
    "deleted" BOOLEAN NOT NULL DEFAULT false,
    "type" VARCHAR(32) NOT NULL,
    "category" VARCHAR(32) NOT NULL,
    "group_id" VARCHAR(32),
    "title" VARCHAR(64),
    "created_at" TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "mailbox_item_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "quest_stages" (
    "id" BIGSERIAL NOT NULL,
    "stage_id" VARCHAR(64) NOT NULL,
    "quest_id" BIGINT NOT NULL,
    "status" "quest_status" DEFAULT 'ACTIVE',
    "created_at" TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "quest_stages_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "quest_tasks" (
    "id" BIGSERIAL NOT NULL,
    "task_id" VARCHAR(64) NOT NULL,
    "stage_id" BIGINT NOT NULL,
    "progress" DOUBLE PRECISION NOT NULL,

    CONSTRAINT "quest_tasks_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "quests" (
    "id" BIGSERIAL NOT NULL,
    "quest_id" VARCHAR(64) NOT NULL,
    "status" "quest_status" DEFAULT 'ACTIVE',
    "user_id" BIGINT,
    "created_at" TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "quests_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ravioli_user" (
    "id" BIGSERIAL NOT NULL,
    "uuid" UUID NOT NULL,
    "username" VARCHAR(16) NOT NULL,
    "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "ravioli_user_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "tracked_quest" (
    "user_id" BIGINT NOT NULL,
    "quest_id" BIGINT NOT NULL
);

-- CreateTable
CREATE TABLE "user_currency" (
    "id" BIGSERIAL NOT NULL,
    "user_id" BIGINT NOT NULL,
    "currency" VARCHAR(32) NOT NULL,
    "amount" DECIMAL(14,2) NOT NULL DEFAULT 0,

    CONSTRAINT "user_currency_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "user_mailbox" (
    "id" BIGSERIAL NOT NULL,
    "user_id" BIGINT NOT NULL,
    "type" VARCHAR(32) NOT NULL,

    CONSTRAINT "user_mailbox_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "idx_auction_house_item_user_id" ON "auction_house_item"("user_id");

-- CreateIndex
CREATE INDEX "auction_house_sell_log_tsv_idx" ON "auction_house_sell_log" USING GIN ("tsv");

-- CreateIndex
CREATE INDEX "idx_auction_house_sell_log_user_id" ON "auction_house_sell_log"("user_id");

-- CreateIndex
CREATE UNIQUE INDEX "dialogue_state_user_id_dialogue_key" ON "dialogue_state"("user_id", "dialogue");

-- CreateIndex
CREATE UNIQUE INDEX "quest_stages_quest_id_stage_id_key" ON "quest_stages"("quest_id", "stage_id");

-- CreateIndex
CREATE UNIQUE INDEX "quest_tasks_stage_id_task_id_key" ON "quest_tasks"("stage_id", "task_id");

-- CreateIndex
CREATE UNIQUE INDEX "quests_quest_id_user_id_key" ON "quests"("quest_id", "user_id");

-- CreateIndex
CREATE UNIQUE INDEX "ravioli_user_uuid_key" ON "ravioli_user"("uuid");

-- CreateIndex
CREATE UNIQUE INDEX "ravioli_user_username_key" ON "ravioli_user"("username");

-- CreateIndex
CREATE UNIQUE INDEX "tracked_quest_user_id_key" ON "tracked_quest"("user_id");

-- CreateIndex
CREATE UNIQUE INDEX "user_currency_user_id_currency_key" ON "user_currency"("user_id", "currency");

-- CreateIndex
CREATE UNIQUE INDEX "user_mailbox_user_id_type_key" ON "user_mailbox"("user_id", "type");

-- AddForeignKey
ALTER TABLE "auction_house_item" ADD CONSTRAINT "auction_house_item_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "ravioli_user"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "auction_house_sell_log" ADD CONSTRAINT "auction_house_sell_log_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "ravioli_user"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "mailbox_item" ADD CONSTRAINT "mailbox_item_mailbox_id_fkey" FOREIGN KEY ("mailbox_id") REFERENCES "user_mailbox"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "quest_stages" ADD CONSTRAINT "quest_stages_quest_id_fkey" FOREIGN KEY ("quest_id") REFERENCES "quests"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "quest_tasks" ADD CONSTRAINT "quest_tasks_stage_id_fkey" FOREIGN KEY ("stage_id") REFERENCES "quest_stages"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "quests" ADD CONSTRAINT "quests_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "ravioli_user"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "tracked_quest" ADD CONSTRAINT "tracked_quest_quest_id_fkey" FOREIGN KEY ("quest_id") REFERENCES "quests"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "tracked_quest" ADD CONSTRAINT "tracked_quest_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "ravioli_user"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "user_currency" ADD CONSTRAINT "user_currency_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "ravioli_user"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- AddForeignKey
ALTER TABLE "user_mailbox" ADD CONSTRAINT "user_mailbox_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "ravioli_user"("id") ON DELETE NO ACTION ON UPDATE NO ACTION;
