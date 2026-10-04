# API

## Weather

- `GET /weather/{city}` — fetch weather data for a city.
- `PUT /weather/{city}` — update weather data for a city.
- `DELETE /weather/{city}` — remove weather data for a city.
- `GET /weather` — get all weather data.
- `POST /weather` — add new weather data.

`GET /weather/{city}` and `GET /weather` are cached. `POST` and `PUT` refresh the city entry. `DELETE` drops it. The paths through Redis and PostgreSQL are in [Architecture](architecture.md).

## Cache

- `GET /cache/names` — list cache names.
- `GET /cache/contents/{cacheName}` — fetch every entry in a cache.
- `GET /cache/contents/{cacheName}/{key}` — fetch one entry.
- `DELETE /cache/evict/{cacheName}` — clear a cache by name.

## Example weather data

```json
[
    {"city": "Bangalore", "temperature": 28.5, "humidity": 65, "condition": "Cloudy"},
    {"city": "Mysore", "temperature": 30.2, "humidity": 60, "condition": "Sunny"},
    {"city": "Hubli", "temperature": 32.8, "humidity": 55, "condition": "Partly Cloudy"}
]
```
