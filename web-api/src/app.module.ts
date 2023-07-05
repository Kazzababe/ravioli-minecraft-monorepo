import { Module } from '@nestjs/common';
import { MailService } from './mail/mail.service'
import { MailController } from './mail/mail.controller'
import { ConfigModule } from '@nestjs/config'
import { AuctionService } from './auction/auction.service'
import { AuctionController } from './auction/auction.controller'
import { CurrencyController } from './currency/currency.controller'
import { CurrencyService } from './currency/currency.service'
import { UserService } from './user/user.service'
import { UserController } from './user/user.controller'

@Module({
  imports: [ConfigModule.forRoot()],
  providers: [MailService, AuctionService, CurrencyService, UserService],
  controllers: [MailController, AuctionController, CurrencyController, UserController],
})
export class AppModule {}