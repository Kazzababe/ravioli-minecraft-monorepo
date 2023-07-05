import { Body, Controller, Get, HttpStatus, Param, ParseEnumPipe, ParseIntPipe, Post, Res } from '@nestjs/common';
import { CreateMailboxItemType, MailboxItemCategory, MailboxItemType, MailboxType } from '../../types/mail.types'
import { Response } from 'express';
import { MailService } from './mail.service'

interface ChangeOpenedBody {
  id: number;
  opened: boolean;
  mailboxType: MailboxType;
}

interface DeleteBody {
  id: number;
  mailboxType: MailboxType;
}

@Controller('api/mail')
export class MailController {
  constructor(private readonly mailService: MailService) {}

  @Get(':userId/:start/:end/:type')
  async fetchRange(
    @Res() res: Response,
    @Param('userId', ParseIntPipe) userId: number,
    @Param('start', ParseIntPipe) start: number,
    @Param('end', ParseIntPipe) end: number,
    @Param('type', new ParseEnumPipe(MailboxType)) type: MailboxType,
  ) {
    try {
      const results = await this.mailService.fetchRange(
        userId,
        start,
        end,
        type,
      );

      return res.status(HttpStatus.OK).json({
        message: 'Success.',
        ...results || {
          items: [],
          next: false,
        },
      })
    } catch (error: any) {
      return res.status(error.status ?? HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Post('create')
  async create(@Res() res: Response, @Body() body: CreateMailboxItemType) {
    try {
      const data = this.mailService.create(body);

      return res.status(HttpStatus.OK).json({
        message: 'Success.',
        data,
      })
    } catch (error: any) {
      return res.status(error.status ?? HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Post('set-opened')
  async changeOpen(@Res() res: Response, @Body() body: ChangeOpenedBody) {
    try {
      await this.mailService.changeOpened(
        Number.parseInt(`${body.id}`),
        `${body.opened}` === "true",
        body.mailboxType as MailboxType,
      );
    } catch (error: any) {
      return res.status(error.status ?? HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return res.status(HttpStatus.OK).json({
      message: 'Success.',
    })
  }

  @Post('delete')
  async delete(@Res() res: Response, @Body() body: DeleteBody) {
    try {
      await this.mailService.delete(
        Number.parseInt(`${body.id}`),
        body.mailboxType as MailboxType,
      );
    } catch (error: any) {
      return res.status(error.status ?? HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return res.status(HttpStatus.OK).json({
      message: 'Success.',
    })
  }
}
