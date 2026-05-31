# jpa-spec-builder

`jpa-spec-builder` is a lightweight Java library for building dynamic Spring Data JPA queries with a small, readable fluent API. It helps you compose filters, nested conditions, joins, sorting, pagination, and common query operations without writing repetitive `Specification` or Criteria API boilerplate.

The library is designed for Spring Boot and Spring Data JPA projects that need flexible search screens, advanced filtering APIs, admin dashboards, back-office tools, or any feature where query conditions are assembled dynamically at runtime.

## Why This Library Exists

Spring Data JPA already provides powerful tools such as `JpaSpecificationExecutor`, `Specification`, and the Criteria API. Those tools are flexible, but they can become verbose quickly:

- Every condition requires manual access to `Root`, `CriteriaBuilder`, `Predicate`, and path objects.
- Nested `AND` / `OR` logic often becomes difficult to read.
- Dynamic filters require many null checks and repeated predicate list management.
- Joins, sorting, and pagination are often implemented separately from the filter definition.
- Query intent can be hidden behind low-level Criteria API mechanics.

`jpa-spec-builder` keeps the power of Spring Data JPA while giving developers a clearer way to express query intent:

```java
List<User> users = queryFactory.query(User.class, userRepository)
        .greaterThan("age", 18)
        .between("height", 160, 180)
        .ilike("name", "hieu")
        .findAll();
```

The result is shorter code, easier maintenance, and query definitions that are closer to the language developers use when describing business filters.

## Key Features

| Feature | Description |
| --- | --- |
| Fluent query builder | Compose filters through readable chained methods. |
| Spring Data JPA integration | Execute queries through repositories that implement `JpaSpecificationExecutor`. |
| Specification generation | Build reusable `QueryModel` objects and convert them to Spring Data JPA `Specification` instances. |
| Common filter operators | Supports equality, inequality, string matching, range queries, collection membership, comparison, and null checks. |
| Nested logical groups | Build grouped `AND` and `OR` conditions without manually managing predicate arrays. |
| Relationship joins | Add inner, left, and right joins, including scoped filters on joined associations. |
| Sorting | Sort by one or more paths with ascending, descending, and null-handling options. |
| Pagination | Fetch `Page`, `Slice`, or unpaged lists from the same fluent query. |
| Auto-configuration | Spring Boot automatically registers the core beans when the library is on the classpath. |
| Extensible internals | Override infrastructure beans such as `PathResolver`, `SpecificationBuilder`, or `OperatorRegistry`. |

## Requirements

- Java 17 or later
- Spring Boot 3.x
- Spring Framework 6.x
- Spring Data JPA 3.x
- Jakarta Persistence 3.x

The library declares Spring and Jakarta dependencies with `provided` scope. Your application should already include the normal Spring Data JPA stack, usually through `spring-boot-starter-data-jpa`.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.nn-hieu</groupId>
    <artifactId>jpa-spec-builder</artifactId>
    <version>0.0.1</version>
</dependency>
```

Most Spring Boot applications should also include:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.nn-hieu:jpa-spec-builder:0.0.1'
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

### Build From Source

```bash
git clone https://github.com/nn-hieu/jpa-spec-builder.git
cd jpa-spec-builder
mvn clean install
```

## Spring Boot Setup

When used in a Spring Boot application, the library auto-configures these beans:

- `JpaQueryFactory`
- `SpecificationBuilder`
- `PathResolver`
- `OperatorRegistry`

In most cases, no explicit configuration is required. Inject `JpaQueryFactory` into a service and start building queries.

```java
import io.github.nnhieu.jpaspecbuilder.JpaQueryFactory;
import org.springframework.stereotype.Service;

@Service
public class UserSearchService {

    private final JpaQueryFactory queryFactory;
    private final UserRepository userRepository;

    public UserSearchService(JpaQueryFactory queryFactory, UserRepository userRepository) {
        this.queryFactory = queryFactory;
        this.userRepository = userRepository;
    }
}
```

Your repository must implement `JpaSpecificationExecutor<T>` for executable queries:

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```

## Usage Guide

### 1. Execute a Simple Query

Use `JpaQueryFactory.query(...)` when you want to build and execute a query immediately through a Spring Data repository.

```java
import java.util.List;

public List<User> findAdultUsers() {
    return this.queryFactory.query(User.class, this.userRepository)
            .greaterThan("age", 18)
            .equals("active", true)
            .sortDesc("createdAt")
            .findAll();
}
```

This query:

- Filters users where `age > 18`.
- Filters users where `active = true`.
- Sorts the result by `createdAt` descending.
- Executes the query using `userRepository.findAll(specification, sort)`.

### 2. Use String Matching Operators

```java
public List<User> searchByName(String keyword) {
    return this.queryFactory.query(User.class, this.userRepository)
            .contains("name", keyword)
            .findAll();
}
```

Available string operators:

| Method | Meaning |
| --- | --- |
| `like(path, value)` | Case-sensitive contains match. |
| `ilike(path, value)` | Case-insensitive contains match. |
| `startsWith(path, value)` | Case-sensitive prefix match. |
| `endsWith(path, value)` | Case-sensitive suffix match. |
| `contains(path, value)` | Case-sensitive contains match. |

String operators escape `%`, `_`, and `\` automatically before building the Criteria API `LIKE` predicate.

### 3. Use Range and Comparison Filters

```java
public List<User> findUsersByAgeAndHeight() {
    return this.queryFactory.query(User.class, this.userRepository)
            .greaterThanOrEqual("age", 18)
            .between("height", 160, 180)
            .lessThan("failedLoginAttempts", 5)
            .findAll();
}
```

Supported comparison methods include:

- `greaterThan`
- `greaterThanOrEqual`
- `lessThan`
- `lessThanOrEqual`
- `between`

`between` accepts two comparable values. If the values are passed in reverse order, the library normalizes them before creating the predicate.

### 4. Use Collection Filters

```java
import java.util.List;

public List<User> findUsersByStatus() {
    return this.queryFactory.query(User.class, this.userRepository)
            .in("status", List.of(UserStatus.ACTIVE, UserStatus.PENDING))
            .notInIgnoreCase("email", List.of("root@example.com", "admin@example.com"))
            .findAll();
}
```

Collection operators:

- `in`
- `inIgnoreCase`
- `notIn`
- `notInIgnoreCase`

For empty collections, `in` returns no rows and `notIn` does not restrict the result.

### 5. Use Null Checks

```java
public List<User> findUsersWithoutDeletedAt() {
    return this.queryFactory.query(User.class, this.userRepository)
            .isNull("deletedAt")
            .isNotNull("email")
            .findAll();
}
```

### 6. Build Nested Logical Groups

Use `and(...)` and `or(...)` to express grouped conditions.

```java
public List<User> findVisibleUsers() {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("deleted", false)
            .or(group -> group
                    .equals("status", UserStatus.ACTIVE)
                    .equals("status", UserStatus.PENDING))
            .findAll();
}
```

This produces logic similar to:

```sql
where deleted = false
  and (status = 'ACTIVE' or status = 'PENDING')
```

### 7. Query Across Relationships

Use `join`, `leftJoin`, or `rightJoin` when filtering by related entities.

```java
public List<User> findUsersWithLargeOrders() {
    return this.queryFactory.query(User.class, this.userRepository)
            .leftJoin("orders", order -> order.greaterThan("totalAmount", 100000))
            .equals("active", true)
            .findAll();
}
```

You can also reference nested paths directly:

```java
public List<User> findUsersByProfileCity() {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("profile.city", "Singapore")
            .sortAsc("profile.lastName")
            .findAll();
}
```

The default path resolver creates left joins for intermediate path segments such as `profile` in `profile.city`.

### 8. Use Sorting and Null Handling

```java
import io.github.nnhieu.jpaspecbuilder.core.model.NullHandling;

public List<User> findRecentlyActiveUsers() {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("active", true)
            .sortDesc("lastLoginAt", NullHandling.NULLS_LAST)
            .sortAsc("name")
            .findAll();
}
```

Supported null handling options:

- `NullHandling.NATIVE`
- `NullHandling.NULLS_FIRST`
- `NullHandling.NULLS_LAST`

Sort paths are validated. Sorting by collection associations is rejected because it can produce ambiguous results.

### 9. Use Pagination

```java
import org.springframework.data.domain.Page;

public Page<User> findUserPage(int page, int size) {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("active", true)
            .sortDesc("createdAt")
            .page(page, size)
            .findPage();
}
```

For cursor-like "load more" screens, use `findSlice()` to avoid a count query:

```java
import org.springframework.data.domain.Slice;

public Slice<User> findUserSlice(int page, int size) {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("active", true)
            .page(page, size)
            .findSlice();
}
```

Executable query methods:

| Method | Result |
| --- | --- |
| `findAll()` | Returns a `List<T>`. |
| `findPage()` | Returns a Spring Data `Page<T>`. |
| `findSlice()` | Returns a Spring Data `Slice<T>`. |
| `findOne()` | Returns an `Optional<T>`. |
| `count()` | Returns the number of matching rows. |
| `exists()` | Returns whether at least one row matches. |

### 10. Build a Query Model Without Executing It

Use `builder(...)` when you want to create a reusable query model first. A `QueryModel` is only a query definition; it does not execute anything by itself.

```java
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;

public QueryModel<User> buildActiveUserModel() {
    return this.queryFactory.builder(User.class)
            .equals("active", true)
            .greaterThan("age", 18)
            .sortDesc("createdAt")
            .page(0, 20)
            .build();
}
```

To execute query models later, register a `QueryModelExecutor` bean. It uses the JPA `EntityManager` together with the library's `SpecificationBuilder` and `PathResolver`.

```java
import io.github.nnhieu.jpaspecbuilder.core.query.QueryModelExecutor;
import io.github.nnhieu.jpaspecbuilder.spring.support.PathResolver;
import io.github.nnhieu.jpaspecbuilder.spring.support.SpecificationBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryModelExecutorConfiguration {

    @Bean
    public QueryModelExecutor queryModelExecutor(
            EntityManager entityManager,
            SpecificationBuilder specificationBuilder,
            PathResolver pathResolver
    ) {
        return new QueryModelExecutor(entityManager, specificationBuilder, pathResolver);
    }
}
```

Then inject `QueryModelExecutor` wherever the model should be executed.

```java
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.core.query.QueryModelExecutor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class UserQueryRunner {

    private final QueryModelExecutor queryModelExecutor;
    private final UserQueryDefinitionFactory queryDefinitionFactory;

    public UserQueryRunner(QueryModelExecutor queryModelExecutor, UserQueryDefinitionFactory queryDefinitionFactory) {
        this.queryModelExecutor = queryModelExecutor;
        this.queryDefinitionFactory = queryDefinitionFactory;
    }

    public Page<User> findActiveUsers() {
        QueryModel<User> model = this.queryDefinitionFactory.buildActiveUserModel();
        return this.queryModelExecutor.executable(model).findPage();
    }
}
```

You can also call the executor methods directly:

```java
List<User> users = this.queryModelExecutor.findAll(model);
long total = this.queryModelExecutor.count(model);
boolean exists = this.queryModelExecutor.exists(model);
```

This is useful when query definitions need to be passed between layers, tested independently, or executed by infrastructure code later.

## Comparison With Traditional JPA Approaches

Consider this SQL query:

```sql
select *
from User u
where u.age > 18
  and u.height between 160 and 180
  and u.name ilike '%hieu'
```

The query has simple business intent:

- Users older than 18.
- Height between 160 and 180.
- Name matches `hieu` using case-insensitive text matching.

### Using JPA Specification

```java
import org.springframework.data.jpa.domain.Specification;

public Specification<User> adultUsersWithMatchingName() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.and(
            criteriaBuilder.greaterThan(root.get("age"), 18),
            criteriaBuilder.between(root.get("height"), 160, 180),
            criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%hieu"
            )
    );
}

public List<User> searchUsers() {
    return this.userRepository.findAll(this.adultUsersWithMatchingName());
}
```

This works, but the business logic is mixed with Criteria API mechanics. As soon as filters become optional, the implementation usually grows into manual predicate list management.

### Using JPA Predicate / Criteria API Directly

```java
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

public List<User> searchUsers(EntityManager entityManager) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
    Root<User> root = criteriaQuery.from(User.class);
    Predicate agePredicate = criteriaBuilder.greaterThan(root.get("age"), 18);
    Predicate heightPredicate = criteriaBuilder.between(root.get("height"), 160, 180);
    Predicate namePredicate = criteriaBuilder.like(
            criteriaBuilder.lower(root.get("name")),
            "%hieu"
    );
    criteriaQuery.select(root).where(criteriaBuilder.and(agePredicate, heightPredicate, namePredicate));
    TypedQuery<User> query = entityManager.createQuery(criteriaQuery);
    return query.getResultList();
}
```

The Criteria API version is explicit and powerful, but it is much more verbose. The developer has to manage the query object, root, predicates, selection, and execution flow manually.

### Using `jpa-spec-builder`

```java
import java.util.List;

public List<User> searchUsers() {
    return this.queryFactory.query(User.class, this.userRepository)
            .greaterThan("age", 18)
            .between("height", 160, 180)
            .ilike("name", "hieu")
            .findAll();
}
```

This version keeps the query focused on the business conditions. The library builds the `Specification`, creates the predicates, applies sorting if present, and delegates execution to the Spring Data repository.

> Note: The built-in `ilike(path, value)` operator performs a case-insensitive contains search by generating a `%value%` pattern. If you need strict suffix matching with the exact wildcard shape `%hieu`, use `endsWith("name", "hieu")` for suffix matching or keep that single condition as a custom `Specification` until a case-insensitive suffix operator is available.

### Comparison Summary

| Approach | Code Size | Readability | Maintainability | Developer Experience |
| --- | --- | --- | --- | --- |
| JPA Specification | Medium | Good for small queries, harder with dynamic filters | Requires manual predicate composition | Flexible but repetitive |
| Criteria API | Large | Low-level and verbose | Easy to introduce clutter in services | Powerful but ceremony-heavy |
| `jpa-spec-builder` | Small | Query intent is visible immediately | Conditions are easy to add, remove, and group | Fluent, concise, and Spring-friendly |

The main advantage is not only fewer lines of code. The bigger value is that query logic stays readable as requirements evolve.

## Real-World Examples

### Example 1: Dynamic Search Request

A common API endpoint receives optional request parameters. With Criteria API, this usually requires a mutable list of predicates. With `jpa-spec-builder`, you can add conditions only when values are present.

```java
import io.github.nnhieu.jpaspecbuilder.core.query.ExecutableQuery;
import org.springframework.data.domain.Page;

public Page<User> searchUsers(UserSearchRequest request) {
    ExecutableQuery<User> query = this.queryFactory.query(User.class, this.userRepository);
    if (request.getStatus() != null) {
        query.equals("status", request.getStatus());
    }
    if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
        query.ilike("name", request.getKeyword());
    }
    if (request.getMinAge() != null) {
        query.greaterThanOrEqual("age", request.getMinAge());
    }
    if (request.getMaxAge() != null) {
        query.lessThanOrEqual("age", request.getMaxAge());
    }
    return query.sortDesc("createdAt")
            .page(request.getPage(), request.getSize())
            .findPage();
}
```

Why this is easier:

- No `List<Predicate>` is required.
- Optional filters remain close to the request fields they represent.
- Sorting, pagination, filtering, and execution stay in one readable flow.

### Example 2: Search With Alternative Conditions

```java
public List<User> findUsersForNotification() {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("enabled", true)
            .or(group -> group
                    .equals("emailVerified", true)
                    .equals("phoneVerified", true))
            .findAll();
}
```

Equivalent logic:

```sql
where enabled = true
  and (email_verified = true or phone_verified = true)
```

Why this is easier:

- Grouping is visible in the structure of the Java code.
- There is no need to manually build nested `criteriaBuilder.or(...)` and `criteriaBuilder.and(...)` arrays.

### Example 3: Filter by Joined Collection

```java
public List<User> findCustomersWithPaidOrders() {
    return this.queryFactory.query(User.class, this.userRepository)
            .leftJoin("orders", order -> order
                    .equals("status", OrderStatus.PAID)
                    .greaterThan("totalAmount", 50000))
            .sortDesc("createdAt")
            .findAll();
}
```

Why this is easier:

- The join is declared as part of the query definition.
- Conditions scoped to the joined association stay inside the join block.
- The library applies `distinct` when collection joins are detected, reducing duplicate root entities.

### Example 4: Count and Exists Queries

```java
public boolean hasActiveAdminUsers() {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("role", UserRole.ADMIN)
            .equals("active", true)
            .exists();
}

public long countLockedUsers() {
    return this.queryFactory.query(User.class, this.userRepository)
            .equals("locked", true)
            .count();
}
```

Why this is easier:

- You can reuse the same fluent filter style for read, count, and existence checks.
- Service methods stay focused on the business question being asked.

### Example 5: Build a Reusable Query Definition

```java
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import java.time.LocalDateTime;
import java.time.Year;

public QueryModel<User> activeUsersCreatedThisYear() {
    LocalDateTime startOfYear = Year.now().atDay(1).atStartOfDay();
    return this.queryFactory.builder(User.class)
            .equals("active", true)
            .greaterThanOrEqual("createdAt", startOfYear)
            .sortDesc("createdAt")
            .build();
}
```

Later, another service can execute that definition with `QueryModelExecutor`.

```java
import io.github.nnhieu.jpaspecbuilder.core.model.QueryModel;
import io.github.nnhieu.jpaspecbuilder.core.query.QueryModelExecutor;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserReportService {

    private final QueryModelExecutor queryModelExecutor;
    private final UserQueryDefinitionFactory queryDefinitionFactory;

    public UserReportService(QueryModelExecutor queryModelExecutor, UserQueryDefinitionFactory queryDefinitionFactory) {
        this.queryModelExecutor = queryModelExecutor;
        this.queryDefinitionFactory = queryDefinitionFactory;
    }

    public List<User> findActiveUsersCreatedThisYear() {
        QueryModel<User> model = this.queryDefinitionFactory.activeUsersCreatedThisYear();
        return this.queryModelExecutor.findAll(model);
    }

    public long countActiveUsersCreatedThisYear() {
        QueryModel<User> model = this.queryDefinitionFactory.activeUsersCreatedThisYear();
        return this.queryModelExecutor.count(model);
    }
}
```

Why this is easier:

- Query definitions can be created without executing them immediately.
- Query models can be tested as plain objects.
- Infrastructure code can decide how and when to execute the model.
- The same `QueryModel` can be used for list, page, slice, count, one-result, and existence queries.

## Customization

### Override the Path Resolver

The default path resolver supports dot notation such as `profile.city` and creates left joins for intermediate path segments. If your application needs different path resolution behavior, define your own `PathResolver` bean.

```java
import io.github.nnhieu.jpaspecbuilder.spring.support.PathResolver;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaSpecBuilderConfiguration {

    @Bean
    public PathResolver pathResolver() {
        return new PathResolver() {
            @Override
            public Path<?> resolve(Root<?> root, String path) {
                return root.get(path);
            }

            @Override
            public Path<?> resolve(From<?, ?> from, String path) {
                return from.get(path);
            }
        };
    }
}
```

Spring Boot backs off because the auto-configuration uses `@ConditionalOnMissingBean`.

### Replace the Operator Registry

If your application needs different operator behavior, provide your own `OperatorRegistry` bean. This gives you full control over which `Operator` implementation is selected for each `FilterOperator`.

```java
import io.github.nnhieu.jpaspecbuilder.core.model.FilterOperator;
import io.github.nnhieu.jpaspecbuilder.spring.operator.Operator;
import io.github.nnhieu.jpaspecbuilder.spring.operator.registry.DefaultOperatorRegistry;
import io.github.nnhieu.jpaspecbuilder.spring.operator.registry.OperatorRegistry;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OperatorConfiguration {

    @Bean
    public OperatorRegistry operatorRegistry() {
        return new OperatorRegistry() {
            private final Operator likeOperator = new ExactLikeOperator();
            private final OperatorRegistry fallback = new DefaultOperatorRegistry();

            @Override
            public void register(Operator operator) {
                this.fallback.register(operator);
            }

            @Override
            public Operator find(FilterOperator operator) {
                if (operator == FilterOperator.LIKE) {
                    return this.likeOperator;
                }
                return this.fallback.find(operator);
            }
        };
    }

    private static final class ExactLikeOperator implements Operator {

        @Override
        public boolean supports(FilterOperator operator) {
            return operator == FilterOperator.LIKE;
        }

        @Override
        public Predicate toPredicate(FilterOperator operator, Path<?> path, CriteriaBuilder criteriaBuilder, Object value) {
            return criteriaBuilder.like(path.as(String.class), String.valueOf(value));
        }
    }
}
```

Spring Boot backs off from the default registry because the auto-configuration uses `@ConditionalOnMissingBean`.

## Best Practices

### Keep Query Builders Request-Scoped

Create a new builder for each query. Builders are mutable by design and should not be stored as shared singleton state.

```java
ExecutableQuery<User> query = this.queryFactory.query(User.class, this.userRepository);
```

### Validate Public API Inputs Before Adding Filters

Do not add filters for absent request values. Validate and normalize inputs before applying them.

```java
if (keyword != null && !keyword.isBlank()) {
    query.ilike("name", keyword.trim());
}
```

### Prefer Clear Field Paths

Use entity attribute names, not database column names.

```java
query.equals("profile.city", "Singapore");
```

### Use `findSlice()` for Infinite Scroll

Use `findPage()` when the client needs total counts. Use `findSlice()` when the client only needs to know whether another page exists.

### Keep Complex Business Rules Named

If a group of filters represents an important business concept, put it behind a method with a clear name.

```java
private void applyActiveCustomerFilters(ExecutableQuery<User> query) {
    query.equals("active", true)
            .equals("type", UserType.CUSTOMER)
            .isNull("deletedAt");
}
```

### Use Joins Intentionally

Joins are powerful, but they can affect result size and performance. Add explicit joins only when the query needs filters on relationships or when nested paths are required.

### Avoid Sorting by Collection Associations

Sorting by collection relationships is ambiguous and rejected by the library. Sort by scalar fields or singular relationship fields instead.

## Operator Reference

| Category | Methods |
| --- | --- |
| Equality | `equals`, `notEquals` |
| String | `like`, `ilike`, `startsWith`, `endsWith`, `contains` |
| Range and comparison | `between`, `greaterThan`, `greaterThanOrEqual`, `lessThan`, `lessThanOrEqual` |
| Collection | `in`, `inIgnoreCase`, `notIn`, `notInIgnoreCase` |
| Null checks | `isNull`, `isNotNull` |
| Joins | `join`, `leftJoin`, `rightJoin` |
| Logical groups | `and`, `or` |
| Sorting | `sortBy`, `sortAsc`, `sortDesc` |
| Pagination | `page` |
| Execution | `findAll`, `findPage`, `findSlice`, `findOne`, `count`, `exists` |

## Conclusion

`jpa-spec-builder` provides a concise and maintainable way to build dynamic Spring Data JPA queries. It reduces Criteria API boilerplate, keeps query intent readable, supports common filtering patterns, and integrates naturally with repositories that already use `JpaSpecificationExecutor`.

For teams building search-heavy applications with Spring Data JPA, the library offers a practical middle ground: the flexibility of JPA Specifications with a much cleaner developer experience.
