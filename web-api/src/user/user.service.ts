import { HttpStatus, Injectable } from '@nestjs/common'
import { User } from '../../types/user.types'
import prisma from '../service/prisma'
import { HttpError } from '../util/error'

const ENTITY_NAME = `user`;

@Injectable()
export class UserService {
  async loadManyById(ids: number[]): Promise<Record<number, User>> {
    const users: Record<number, User> = {};
    const results = await prisma.ravioli_user.findMany({
      where: {
        id: {
          in: ids,
        },
      },
    });

    results.forEach((row) => {
      const id = Number(row.id);

      users[id] = {
        id,
        uuid: row.uuid,
        username: row.username,
        createdAt: row.created_at,
        updatedAt: row.updated_at,
      };
    });
    return users;
  }

  async loadManyByUuid(uuids: string[]): Promise<Record<string, User>> {
    const users: Record<string, User> = {};
    const results = await prisma.ravioli_user.findMany({
      where: {
        uuid: {
          in: uuids,
        },
      },
    });

    results.forEach((row) => {
      users[row.uuid] = {
        id: Number(row.id),
        uuid: row.uuid,
        username: row.username,
        createdAt: row.created_at,
        updatedAt: row.updated_at,
      };
    });
    return users;
  }

  async loadManyByUsername(usernames: string[]): Promise<Record<string, User>> {
    const users: Record<string, User> = {};
    const results = await prisma.ravioli_user.findMany({
      where: {
        username: {
          in: usernames,
        },
      },
    });

    results.forEach((row) => {
      users[row.username] = {
        id: Number(row.id),
        uuid: row.uuid,
        username: row.username,
        createdAt: row.created_at,
        updatedAt: row.updated_at,
      };
    });
    return users;
  }

  async loadById(id: number): Promise<User | null> {
    const result = await prisma.ravioli_user.findFirst({
      where: {
        id,
      }
    });

    if (!result) {
      return null;
    }
    return {
      id: Number(result.id),
      username: result.username,
      uuid: result.uuid,
      createdAt: result.created_at,
      updatedAt: result.updated_at,
    };
  }

  async loadByUuid(uuid: string): Promise<User | null> {
    const result = await prisma.ravioli_user.findFirst({
      where: {
        uuid,
      }
    });

    if (!result) {
      return null;
    }
    return {
      id: Number(result.id),
      username: result.username,
      uuid: result.uuid,
      createdAt: result.created_at,
      updatedAt: result.updated_at,
    };
  }

  async loadByUsername(username: string): Promise<User | null> {
    const result = await prisma.ravioli_user.findFirst({
      where: {
        username,
      }
    });

    if (!result) {
      return null;
    }
    return {
      id: Number(result.id),
      username: result.username,
      uuid: result.uuid,
      createdAt: result.created_at,
      updatedAt: result.updated_at,
    };
  }

  async create(uuid: string, username: string): Promise<User> {
    const user = await prisma.ravioli_user.create({
      data: {
        uuid,
        username,
      }
    });

    if (!user) {
      throw new HttpError(HttpStatus.INTERNAL_SERVER_ERROR, 'Unable to create new user.');
    }
    return {
      id: Number(user.id),
      username: user.username,
      uuid: user.uuid,
      createdAt: user.created_at,
      updatedAt: user.updated_at,
    }
  }

  async updateUsername(userId: number, username: string): Promise<void> {
    await prisma.ravioli_user.update({
      data: {
        username,
      },
      where: {
        id: userId,
      },
    })
  }
}