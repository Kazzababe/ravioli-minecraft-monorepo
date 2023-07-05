import {
  Body,
  Controller,
  Get,
  HttpStatus,
  Param,
  ParseArrayPipe,
  ParseEnumPipe,
  ParseIntPipe,
  Post,
  Res,
} from '@nestjs/common'
import { AuctionService } from './auction.service'
import { ItemStack } from '../../types/mail.types'
import { Response } from 'express'
import { SortDirection, SortType } from '../../types/auction.types'

@Controller('api/auction')
export class AuctionController {
  constructor(private readonly auctionService: AuctionService) {
  }

  @Post('expiration-check')
  async expirationCheck(@Res() res: Response) {
    try {
      await this.auctionService.checkExpirations();
      
      return res.status(HttpStatus.OK).json({
        message: 'Success.',
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }

  @Get('global/:direction/:type/:start/:end')
  async fetchRange(
    @Res() res: Response,
    @Param('direction', new ParseEnumPipe(SortDirection)) direction: SortDirection,
    @Param('type', new ParseEnumPipe(SortType)) type: SortType,
    @Param('start', ParseIntPipe) start: number,
    @Param('end', ParseIntPipe) end: number,
  ) {
    try {
      const fetchResults = await this.auctionService.fetchRange(
        direction,
        type,
        start,
        end,
      );

      return res.status(HttpStatus.OK).json({
        message: 'Success',
        ...fetchResults,
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }

  @Get('user/:userId/:direction/:type/:start/:end')
  async fetchUserRange(
    @Res() res: Response,
    @Param('userId', ParseIntPipe) userId: number,
    @Param('direction', new ParseEnumPipe(SortDirection)) direction: SortDirection,
    @Param('type', new ParseEnumPipe(SortType)) type: SortType,
    @Param('start', ParseIntPipe) start: number,
    @Param('end', ParseIntPipe) end: number,
  ) {
    try {
      const fetchResults = await this.auctionService.fetchUserRange(
        userId,
        direction,
        type,
        start,
        end,
      );

      return res.status(HttpStatus.OK).json({
        message: 'Success',
        ...fetchResults,
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }

  @Post('create')
  async create(
    @Res() res: Response,
    @Body('userId', ParseIntPipe) userId: number,
    @Body('item') item: ItemStack,
    @Body('expiration') expiration: Date,
    @Body('currency') currency: string,
    @Body('cost', ParseIntPipe) cost: number,
    @Body('displayName') displayName: string,
  ) {
    try {
      const data = await this.auctionService.create(
        userId,
        item,
        expiration,
        currency,
        cost,
        displayName,
      );

      return res.status(HttpStatus.OK).json({
        message: 'Success',
        data,
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }

  @Post('delete')
  async delete(
    @Res() res: Response,
    @Body('ids', new ParseArrayPipe({ items: Number })) ids: number[]
  ) {
    try {
      await this.auctionService.delete(ids);
    } catch (error: any) {
      return res.status(error.status ?? HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return res.status(HttpStatus.OK).json({
      message: 'Success.',
    })
  }
}