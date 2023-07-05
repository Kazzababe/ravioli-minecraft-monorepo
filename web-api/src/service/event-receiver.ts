const EVENT_RECEIVER_URL = process.env.EVENT_RECEIVER_URL as string;

export async function publishToMinecraft(channel: string, payload: any): Promise<void> {
  await fetch(`${EVENT_RECEIVER_URL}/publish`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      channel,
      payload,
    }),
  });
}