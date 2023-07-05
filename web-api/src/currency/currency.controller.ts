import { Body, Controller, Get, HttpStatus, Param, ParseFloatPipe, ParseIntPipe, Post, Res } from '@nestjs/common'
import { CurrencyService } from './currency.service'
import { Response } from 'express'
import prisma from '../service/prisma'

interface UpdateResponse {
  success: boolean;
  amount: number;
  balance: number;
}

interface TransferResponse {
  success: boolean;
  senderBalance: number;
  receiverBalance: number;
  amount: number;
}

@Controller('api/currency')
export class CurrencyController {
  constructor(private readonly currencyService: CurrencyService) {
  }

  @Post('update')
  async update(
    @Res() res: Response,
    @Body('userId', ParseIntPipe) userId: number,
    @Body('currency') currency: string,
    @Body('amount', ParseFloatPipe) amount: number
  ) {
    try {
      const data = await this.currencyService.update(BigInt(userId), currency, amount);
      const response: UpdateResponse = {
        success: data.updated_rows > 0,
        balance: data.new_amount,
        amount,
      };
      
      return res.status(HttpStatus.OK).json({
        message: 'Success.',
        ...response,
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }

  @Post('transfer')
  async transfer(
    @Res() res: Response,
    @Body('senderId', ParseIntPipe) senderId: number,
    @Body('receiverId', ParseIntPipe) receiverId: number,
    @Body('currency') currency: string,
    @Body('amount', ParseFloatPipe) amount: number
  ) {
    try {
      const data = await this.currencyService.transfer(BigInt(senderId), BigInt(receiverId), currency, amount);
      const response: TransferResponse = {
        success: data.updated_rows > 0,
        receiverBalance: data.new_to_amount,
        senderBalance: data.new_from_amount,
        amount,
      }

      return res.status(HttpStatus.OK).json({
        message: 'Success.',
        ...response,
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }

  @Get(':userId/:currency')
  async fetch(
    @Res() res: Response,
    @Param('userId', ParseIntPipe) userId: number,
    @Param('currency') currency: string
  ) {
    try {
      const data = await this.currencyService.fetch(BigInt(userId), currency);

      return res.status(HttpStatus.OK).json({
        message: 'Success.',
        data,
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }
}