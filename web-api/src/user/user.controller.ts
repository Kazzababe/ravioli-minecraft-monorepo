import {
  Body,
  Controller,
  Get,
  HttpStatus,
  Param,
  ParseArrayPipe,
  ParseIntPipe,
  ParseUUIDPipe,
  Post,
  Res,
} from '@nestjs/common'
import { UserService } from './user.service'
import { Response } from 'express'

@Controller('api/user')
export class UserController {
  constructor(private readonly userService: UserService) {
  }

  @Get('ids/:userIds')
  async manyUsersById(
    @Res() res: Response,
    @Param('userIds', new ParseArrayPipe({ separator: ',', items: Number })) userIds: number[]
  ) {
    try {
      const data = await this.userService.loadManyById(userIds);

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

  @Get('uuids/:userIds')
  async manyUsersByUuid(
    @Res() res: Response,
    @Param('userIds', new ParseArrayPipe({ separator: ',', items: String })) userIds: string[]
  ) {
    try {
      const data = await this.userService.loadManyByUuid(userIds);

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

  @Get('usernames/:userNames')
  async manyUsersByUsername(
    @Res() res: Response,
    @Param('userNames', new ParseArrayPipe({ separator: ',', items: String })) usernames: string[]
  ) {
    try {
      const data = await this.userService.loadManyByUsername(usernames);

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

  @Get('id/:userId')
  async userById(
    @Res() res: Response,
    @Param('userId', ParseIntPipe) userId: number,
  ) {
    try {
      const data = await this.userService.loadById(userId);

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

  @Get('uuid/:userId')
  async userByUuid(
    @Res() res: Response,
    @Param('userId', ParseUUIDPipe) userId: string
  ) {
    try {
      const data = await this.userService.loadByUuid(userId);

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

  @Get('username/:userName')
  async userByUserName(
    @Res() res: Response,
    @Param('userName') username: string,
  ) {
    try {
      const data = await this.userService.loadByUsername(username);

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

  @Post('create')
  async create(
    @Res() res: Response,
    @Body('uuid', ParseUUIDPipe) uuid: string,
    @Body('username') username: string,
  ) {
    try {
      const data = await this.userService.create(uuid, username);

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

  @Post('update-username')
  async updateUsername(
    @Res() res: Response,
    @Body('id', ParseIntPipe) userId: number,
    @Body('username') username: string,
  ) {
    try {
      await this.userService.updateUsername(userId, username);

      return res.status(HttpStatus.OK).json({
        message: 'Success.',
      });
    } catch (error: any) {
      return res.status(error.status || HttpStatus.INTERNAL_SERVER_ERROR).json({
        message: error.message,
      });
    }
  }
}