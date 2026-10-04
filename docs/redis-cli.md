# Redis CLI

Commands for looking at the cache this application writes. Key names look like `weatherCache::Bangalore`. The same keys can be browsed in [Redis Insight](redis-insight.md). Lifetime and the memory ceiling are explained in [Redis lifetime and memory limits](redis-limits.md).

## Keys written by this application

```sh
redis-cli
KEYS *
GET "weatherCache::Bangalore"
TTL "weatherCache::Bangalore"
```

## Basic commands

```sh
PING                 # Check the Redis connection
SET key value        # Store a key-value pair
GET key              # Retrieve the value of a key
DEL key              # Delete a key
EXISTS key           # Check if a key exists
KEYS *               # List all keys
FLUSHALL             # Clear all data in Redis
```

## Expiration

```sh
TTL key              # Remaining time-to-live of a key, in seconds
EXPIRE key seconds   # Set an expiration time on a key
PERSIST key          # Remove the expiration from a key
```

## Lists

```sh
LPUSH key value      # Insert a value at the beginning of a list
RPUSH key value      # Insert a value at the end of a list
LPOP key             # Remove and return the first element
RPOP key             # Remove and return the last element
LRANGE key start stop  # Read a range of elements from a list
```

## Hashes

```sh
HSET key field value  # Set a field in a hash
HGET key field        # Read a field from a hash
HGETALL key           # Read every field in a hash
```

This application stores each cache entry as a string value, not as a list or a hash. The list and hash commands are here as general Redis reference.
