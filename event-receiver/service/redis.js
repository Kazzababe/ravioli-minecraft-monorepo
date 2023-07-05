"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var ioredis_1 = require("ioredis");
var redis = new ioredis_1.default({
    port: Number.parseInt(process.env.REDIS_PORT),
    host: process.env.REDIS_HOST || "127.0.0.1",
    password: process.env.REDIS_PASSWORD
});
exports.default = redis;
