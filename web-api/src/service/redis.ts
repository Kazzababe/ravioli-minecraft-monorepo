import Redis from 'ioredis';

const redis = new Redis({
  port: Number.parseInt(process.env.REDIS_PORT as string), // Redis port
  host: process.env.REDIS_HOST as string || "127.0.0.1", // Redis host
  password: process.env.REDIS_PASSWORD as string
});

export default redis;