import express from 'express';
import redis from './service/redis'

const app = express();

app.use(express.json());

app.post(`/publish`, async (req, res) => {
  const channel = req.body.channel;
  const payload = req.body.payload;

  if (!channel) {
    return res
      .status(500)
      .json({
        message: `No "channel" specified in request body.`
      })
  }
  if (!payload) {
    return res
      .status(500)
      .json({
        message: `No "payload" specified in request body.`
      })
  }
  await redis.publish(channel, JSON.stringify(payload));

  console.log(`Received payload:`, { channel, payload })

  return res.sendStatus(200);
})

app.listen(Number.parseInt(process.env.PORT as string || `3000`), () => {
  console.info("Started event-receiver.")
})