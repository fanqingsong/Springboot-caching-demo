# Bean stereotypes

`@Component`, `@Service`, `@Repository`, `@RestController`, `@Configuration`, and `@Bean` all produce the same result: a bean. The annotation does not change the class. It records how the object is registered, and which role it plays.

A bean is an ordinary Java object whose creation, injection, initialization, and destruction are owned by the Spring container. `WeatherService` is still `WeatherService`. What changes is who calls `new` and who holds the instance. The lifecycle of that instance is covered in [Bean lifecycle](bean-lifecycle.md).

## Two ways to register

Component scan registers a class you own. A factory method registers an object the container cannot construct from a stereotype annotation alone.

| | Stereotype on a class | `@Bean` method |
|---|---|---|
| Written on | `@Component` or a specialized form of it | A method inside a `@Configuration` class |
| How Spring creates it | Calls the class constructor | Calls the method and stores the return value |
| Fits | Application types such as controllers and services | Third-party types, or objects that need hand-written setup |

`@Service`, `@Repository`, `@Controller`, `@RestController`, and `@Configuration` are specialized forms of `@Component`. Component scan turns them into beans the same way. The annotation name records the role.

## Roles in this project

| Annotation | Class | Role |
|---|---|---|
| `@Component` | `CacheInitializer`, `WeatherDataInitializer` | Startup helpers that are not a service or a controller |
| `@Service` | `WeatherService`, `CacheInspectionService` | Business logic |
| `@RestController` | `WeatherController`, `CacheController` | HTTP API |
| `@Configuration` | `CacheConfig` | Declares other beans with `@Bean` methods |
| `@Bean` | `redisTemplate`, `redisCacheConfiguration` | Factory methods; the return value is the bean |
| Spring Data | `WeatherRepository` | Interface extending `JpaRepository`; Spring Data creates the implementation and registers it |

`WeatherRepository` carries none of these annotations. Extending `JpaRepository` is enough for Spring Data to generate the bean at startup. The generated object is still a bean.

`RedisTemplate` and `RedisCacheConfiguration` are registered from methods because the classes are not application types and their setup is written by hand. The method is not the bean. The object it returns is. See [Cache configuration beans](cache-config.md).

## Why the names differ

Most of the names are layer labels. Opening the class shows whether it is business logic, persistence, the web API, configuration, or a general component, without reading the method bodies. `@Service` has no extra container behavior. A `@Service` class and a `@Component` class are registered and injected the same way.

A few annotations add behavior on top of registration:

- `@RestController` combines `@Controller` and `@ResponseBody`. Spring MVC treats a bean annotated with `@Controller` as a request entry point, and writes the return value into the HTTP response body.
- `@Repository` marks a persistence component. The container translates persistence exceptions from that bean into Spring's data-access exception hierarchy. Spring Data applies the same translation to repository beans it generates, including `WeatherRepository`.
- `@Configuration` makes the container proxy the class. When one `@Bean` method in that class calls another, the call returns the single instance stored in the container. A `@Bean` method placed on a `@Component` class does not get that proxy.

The scan and injection mechanism is one mechanism. The annotation names mark the layer, and a few of those layers attach extra container behavior.
