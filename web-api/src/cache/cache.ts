import redis from '../service/redis';

export default class RedisCache<T extends CacheableEntity> {
  private readonly deserializers: Map<string, (data: any) => Omit<T, "serialize" | "name">>;

  constructor(
    private readonly key: string,
    private readonly expiry: number,
  ) {
    this.deserializers = new Map();
  }

  register(entityName: string, deserializer: (data: any) => Omit<T, "serialize" | "name">) {
    this.deserializers.set(entityName.toLowerCase(), deserializer);
  }

  async invalidateKeys(keys: any[]): Promise<void> {
    const pipeline = redis.pipeline();

    keys.forEach((key) => pipeline.del(`${this.key}:${key}`));

    await pipeline.exec();
  }

  async invalidateCollection(collectionId: string): Promise<void> {
    const collectionKey = `${this.key}::${collectionId}`;
    const members = await redis.smembers(collectionKey);

    if (members.length === 0) {
      return;
    }
    const pipeline = redis.pipeline();

    pipeline.del(members);
    pipeline.del(collectionKey);

    await pipeline.exec();
  }

  async cache(keys: any[], entity: T): Promise<void> {
    const entityName = entity.name.toLowerCase();
    const data = entity.serialize();
    const redisKey = `${this.key}::${keys.join(':')}`;

    await redis.psetex(redisKey, this.expiry, JSON.stringify({
      ...data,
      entityName,
    }));
  }

  async cacheCollection(collectionId: string, keys: any[], entities: T[], hasNext: boolean = false): Promise<void> {
    const data = {
      items: entities.map((entity) => {
        const entityData = entity.serialize();
        const entityName = entity.name.toLowerCase();

        return {
          ...entityData,
          entityName,
        };
      }),
      hasNext,
    }
    const redisPrefixKey = `${this.key}::${collectionId}`;
    const redisKey = `${redisPrefixKey}::${keys.join(':')}`
    const pipeline = redis.pipeline();

    pipeline.psetex(redisKey, this.expiry, JSON.stringify(data));
    pipeline.sadd(redisPrefixKey, redisKey);

    await pipeline.exec();
  }

  async getOr(keys: any[], mapFunction: () => Promise<Omit<T, "serialize" | "name">>): Promise<Omit<T, "serialize" | "name">> {
    const rawData = await redis.get(`${this.key}:${keys.join(':')}`);

    if (rawData == null) {
      return mapFunction();
    }
    const data = JSON.parse(rawData);
    const { entityName } = data;
    const deserializer = this.deserializers.get(entityName)!;

    return deserializer(data);
  }

  async getCollectionOr(collectionId: string, keys: any[], mapFunction: () => Promise<CollectionFetchResult<Omit<T, "serialize" | "name">>>): Promise<CollectionFetchResult<Omit<T, "serialize" | "name">>> {
    const redisPrefixKey = `${this.key}::${collectionId}`;
    const jsonData = await redis.get(`${redisPrefixKey}::${keys.join(':')}`);

    if (jsonData == null) {
      return await mapFunction();
    }
    const data = JSON.parse(jsonData);
    const { items, hasNext: next } = data;

    return {
      items: items.map((entityData: any) => {
        const entityName = entityData.entityName;
        const deserializer = this.deserializers.get(entityName)!;

        return deserializer(entityData);
      }),
      next,
    }
  }
}

export interface CacheableEntity {
  serialize: () => any;

  name: string;
}

export interface CollectionFetchResult<T> {
  items: T[];

  next: boolean;
}