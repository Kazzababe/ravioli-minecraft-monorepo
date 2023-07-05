import 'dotenv/config';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import prisma from './service/prisma';
import postgresClient from './service/postgres'

(BigInt.prototype as any).toJSON = function() {
  return this.toString();
}

async function initPostgres() {
  await postgresClient.file('prisma/init/init.sql');
  await prisma.$executeRaw`
    CREATE UNIQUE INDEX IF NOT EXISTS friends_low_high_idx
    ON friends (LEAST(first_friend_id, second_friend_id), GREATEST(first_friend_id, second_friend_id));
  `;
  await prisma.$executeRaw`
    CREATE UNIQUE INDEX IF NOT EXISTS friend_requests_low_high_idx
    ON friend_requests (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id));
  `;
}

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  await app.listen(3000);
}

void initPostgres();
void bootstrap();
