- Event receiver is a just a separate service that exposes an endpoint to publish various payloads through redis.
- The web api is a nest.js project that contains the various api endpoints for our plugins
- Everything else is a self-contained papermc plugin

Note: The web api exposes an endpoint for handling expired items in the auction house. I would personally just create a cron job or something similar that runs every minute and just round expiration times to the nearest minute.

Note: None of the endpoints in the web api have an authorization surrounding them so if anyone actually does use this, that would need ot be handled before this ever sees a production environment.