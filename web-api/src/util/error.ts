import { HttpStatus } from '@nestjs/common'

export class HttpError extends Error {
  constructor(private readonly statusCode: HttpStatus, message: string) {
    super(message);
  }

  get status(): HttpStatus {
    return this.statusCode;
  }
}