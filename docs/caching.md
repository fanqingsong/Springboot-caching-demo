# Caching

Caching stores frequently accessed data in a faster layer so later requests do not have to read the database every time.

## What it improves

- Less database load and shorter response time.
- More room to scale the application.
- Less time spent waiting on the database.

## Limits of a cache that lives in the application process

- It is bounded by the memory of that process.
- The entries disappear when the process exits, unless another store keeps them.
- Each application instance has its own copy, so instances do not see each other's updates.

This application uses Redis, which is outside the application process. Instances share one cache, and an application restart keeps the entries while Redis stays up. The choice among in-process caches and Redis is in [Cache providers](cache-providers.md).
