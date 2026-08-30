# Core Banking

A small core banking platform built for the Tuum Software Engineer Test Assignment: account
management with multi-currency balances, transaction processing with row-level concurrency
safety, RabbitMQ event publishing on every insert/update, and an admin console for the same API.

The repository is a monorepo:

```
.
├── docker-compose.yml   # Postgres, RabbitMQ, API, and frontend
├── backend/             # Spring Boot REST API
└── frontend/            # React admin console (Vite + TypeScript)
```

**Backend stack:** Java 21, Spring Boot 4.1.1, MyBatis, Gradle (Kotlin DSL), PostgreSQL, RabbitMQ, JUnit 5 + Testcontainers.

**Frontend stack:** React, TypeScript, Vite, Tailwind CSS, shadcn/ui, Redux Toolkit Query, React Router, React Hook Form + Zod.

## Prerequisites

Just Docker and Docker Compose. Nothing else needs to be installed — both the API and the
frontend are built and run inside Docker, so there are no local JDK/Node version requirements
on the reviewer's machine.

Optional local tooling (only if you want to run a service outside Docker):

- Java 21 + the Gradle wrapper in `backend/`
- Node.js 22+ in `frontend/`

## How to run it

From the **repository root**:

```
docker compose up --build
```

This builds the API and frontend images, then starts four containers: Postgres, RabbitMQ, the
Spring Boot app, and an Nginx container that serves the compiled React UI. The app waits for
Postgres and RabbitMQ to report healthy before starting, and Flyway creates the database schema
on first boot.

Once it's up:

- Admin console: `http://localhost:3000` (use `http://127.0.0.1:3000` on Windows if `localhost` fails)
- API: `http://localhost:8080`
- API via the UI proxy: `http://localhost:3000/api/...` (Nginx forwards `/api/` to the app)
- RabbitMQ management UI: `http://localhost:15672` (user/pass: `corebanking` / `corebanking`)
- Postgres: `localhost:5432` (user/pass/db: `corebanking` / `corebanking` / `corebanking`)

To stop everything: `docker compose down` (add `-v` if you also want to wipe the database volume).

### What the frontend is

The console is a thin admin UI over the assignment API. It does not add banking rules of its own.

- **Accounts hub** (`/accounts`): create an account (customer id, country, currencies) or open an existing account by UUID
- **Account workspace** (`/accounts/{accountId}`): balances, post IN/OUT transactions, list transactions
- **Documentation** (`/docs`): the four HTTP endpoints, example bodies, and validation rules
- Light/dark theme, recent-account shortcuts, and RTK Query caching so a created transaction refreshes both the list and the balances

The browser never talks to Postgres or RabbitMQ. It only calls the REST API. In Docker, Vite is
built with `VITE_API_URL=/api`, and Nginx proxies `/api/accounts` to `http://app:8080/accounts`.
That avoids CORS and keeps frontend routes like `/accounts/:id` from colliding with API paths
on refresh.

### Local development (optional)

**API only**, with containerized Postgres and RabbitMQ:

```
docker compose up postgres rabbitmq
cd backend
./gradlew bootRun      # or .\gradlew.bat bootRun on Windows
```

**Frontend only**, against that API (or against `docker compose up app`):

```
cd frontend
npm install
npm run dev
```

Vite serves the UI at `http://localhost:5173`. Point `VITE_API_URL` at `http://localhost:8080`
and allow that origin in Spring CORS, or keep using the Docker frontend on port 3000.

### Running the tests

Tests live in `backend/`. You need:

- **JDK 21** on the machine that runs Gradle (`JAVA_HOME` must point at that JDK)
- **Docker Desktop running** — integration tests use Testcontainers for real Postgres and RabbitMQ

From the repository root:

```
cd backend
```

macOS / Linux:

```
./gradlew test
```

Windows (PowerShell or cmd) — use the batch wrapper, not `./gradlew`:

```
.\gradlew.bat test
```

`./gradlew` on Windows is the Unix script. It will fail with a broken `JAVA_HOME` even if a JDK is installed.

Do not run a Dockerized `gradlew test` against the same `backend/` folder at the same time as a local run. Both write to `backend/build` and can fail with `NoSuchFileException` on Gradle’s test-result files. If that happens:

```
.\gradlew.bat --stop
# delete backend/build, then run tests again
.\gradlew.bat test
```

A coverage report is generated at `backend/build/reports/jacoco/test/html/index.html`.
`./gradlew jacocoTestCoverageVerification` (also run as part of `check`) enforces the assignment's
80% minimum coverage requirement at build time.

## API reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/accounts` | Create an account with balances in given currencies |
| GET | `/accounts/{accountId}` | Get an account and its balances |
| POST | `/accounts/{accountId}/transactions` | Create a transaction (IN/OUT) on an account |
| GET | `/accounts/{accountId}/transactions` | List all transactions for an account |

Example:

```
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-001","country":"EE","currencies":["EUR","USD"]}'
```

From the Docker UI the same call is `POST http://localhost:3000/api/accounts`.

Allowed currencies: `EUR`, `SEK`, `GBP`, `USD`. Transaction direction: `IN` (increases balance),
`OUT` (decreases balance).

## Important design choices

**Package-by-feature, not package-by-layer.** `account/`, `transaction/`, `messaging/`, and
`common/` each contain their own controller, service, domain, DTOs and exceptions, rather than
a global `controllers/`, `services/`, `dtos/` split. Keeps everything related to one concept
in one place.

**Pessimistic row locking for balance updates.** `TransactionService` acquires a
`SELECT ... FOR UPDATE` lock on the specific balance row before reading and updating it. This was
a deliberate choice over optimistic locking: for financial balance updates, correctness under
concurrent access matters more than throughput on the (rare) case of two simultaneous
transactions against the *same* account and currency, and pessimistic locking makes the
guarantee straightforward to reason about and test. This is directly verified by a dedicated
concurrency integration test that fires 10 simultaneous withdrawal requests at an account with
enough balance for exactly 5, and asserts exactly 5 succeed with a final balance of zero.

**Assembler classes instead of inline DTO construction.** `AccountAssembler`/`TransactionAssembler`
are stateless static-method classes that convert between domain objects and DTOs, keeping that
construction logic out of the services. Named "Assembler" rather than "Mapper" specifically to
avoid collision with MyBatis's own `@Mapper` annotation used on the persistence interfaces.

**Shared `Money` value object.** `amount` + `currencyCode` are bundled into a single `Money` DTO
reused across balance and transaction responses, matching the convention used in Tuum's own
public API (checked against their developer portal). Bean Validation on `Money` is limited to
`@NotNull` — it does **not** enforce positivity, because `Money` is reused for both a balance
(which is legitimately zero, and must never be negative) and a transaction amount (which must be
strictly positive). The "amount must be > 0" rule instead lives as an explicit business-rule
check in `TransactionService`, thrown as `InvalidAmountException`.

**Event publishing decoupled from the HTTP request via `@TransactionalEventListener`.**
`AccountService`/`TransactionService` publish an in-process event via
`ApplicationEventPublisher` inside their `@Transactional` methods; a separate
`RabbitMQEventListener`, registered with `@TransactionalEventListener(phase = AFTER_COMMIT)`,
does the actual RabbitMQ publish only after the database transaction has successfully committed.
This guarantees a message is never published for a transaction that later rolls back. A RabbitMQ
publish failure (e.g. broker unreachable) is caught and logged rather than propagated — by the
time this listener runs, the banking operation has already committed successfully, so a broker
outage should never turn an already-successful request into a client-visible 500. Automatic
retry (3 attempts, exponential backoff) is configured on top of this as a first line of defense.

**Structured error responses.** A single `@RestControllerAdvice` (`GlobalExceptionHandler`) maps
every domain exception and Spring binding exception to a consistent `ErrorResponse` shape,
including a structured `fieldErrors` list (field + message) for Bean Validation failures, rather
than string-concatenating field names into a single message.

**Frontend talks to the API only through RTK Query.** Account and transaction endpoints live in
separate `injectEndpoints` files on one API slice. Cache tags (`Account`, `Transaction`) refetch
the workspace after a create. Forms use Zod for client-side shape checks; the server remains the
source of business-rule errors (`fieldErrors` mapped onto the form).

**Testing: two layers, not three.** Mockito-based unit tests (no Spring context) cover
service-layer business logic, the exception handler, and the RabbitMQ listener's failure-handling
behavior. Testcontainers-backed integration tests (real Postgres + RabbitMQ, shared as singleton
containers across the whole test run for speed) cover the full HTTP-to-database path through
`TestRestTemplate`, plus the concurrency test described above. `@WebMvcTest`/`@MybatisTest` slice
tests were deliberately skipped as redundant, since the unit and integration layers already
exercise that wiring from both directions.

**A few explicit extensions beyond the literal spec**, worth calling out honestly:

- Account creation rejects duplicate currencies in the request (e.g. `["EUR", "EUR"]`) with a
  dedicated 400, rather than silently creating two balance rows.
- Transaction amount of exactly zero is rejected alongside negative amounts, since a zero-amount
  transaction has no banking meaning.
- A transaction in a currency the account doesn't hold a balance in returns its own 400
  (`Account does not hold a balance in <currency>`) rather than failing some other way.
- `GET /accounts/{id}/transactions` includes `balanceAfter` on every transaction in the list, even
  though the spec's example output for this endpoint doesn't list it — it's harmless, useful, and
  falls out naturally from reusing the same response shape as the create endpoint.

## Transaction throughput estimate

_[to be filled in from a real load test against the running Docker stack]_

## Horizontal scaling considerations

**The application itself is stateless.** No in-memory session state, no server-side caching that
would need to be shared — any number of instances can run behind a load balancer immediately
without sticky sessions.

**Correctness under concurrency doesn't depend on running a single instance.** The
`SELECT ... FOR UPDATE` lock that protects balance updates lives in Postgres, not in the JVM's
memory. That's precisely why it's safe to run N instances of this app against the same database:
two requests hitting *different* app instances but the *same* account/currency still serialize
correctly at the database row level. A naive in-process lock (e.g. a `synchronized` block or an
in-memory mutex) would have looked correct in this test's single-instance setup but would have
silently broken the moment a second instance was added — this is exactly the kind of bug that
only surfaces under horizontal scaling, so it was worth designing around from the start rather
than retrofitting later.

**Connection pool sizing multiplies with instance count.** Each instance runs its own HikariCP
pool; N instances means roughly N × pool-size connections against Postgres. This needs to be
sized against Postgres's own `max_connections` limit — it's easy to scale app instances past the
point where the database itself becomes the bottleneck on connection count alone, well before
CPU or disk I/O become the limiting factor. A connection pooler (e.g. PgBouncer) in front of
Postgres is the standard mitigation once instance count grows.

**The database write path is the real ceiling, not the app.** Reads (`GET` endpoints) scale
horizontally trivially — read replicas can absorb that load. Writes are a different story:
transactions against the same account/currency fundamentally serialize on that row's lock
regardless of how many app instances are running. Scaling write throughput further than a single
Postgres primary allows would require a different strategy entirely — sharding accounts across
multiple database instances by customer or account ID, for example — not just adding more app
containers.

**The frontend is a static bundle.** Nginx serves compiled assets. Scaling the UI is a matter of
replicating that container or putting the `dist/` files on any static host. It does not hold
account state; RTK Query cache lives in the browser tab.

**Configuration must be fully externalized, which it already is here.** Datasource URL, RabbitMQ
host, and credentials are all environment variables (see `docker-compose.yml`), not hardcoded —
a prerequisite for instances being interchangeable rather than each carrying baked-in identity.

**Schema migrations need coordination when multiple instances start simultaneously.** Flyway
takes a lock during migration to prevent two instances from racing to apply the same migration,
but it's worth being aware of — in a real rolling deployment, this is often handled by running
migrations as a separate step/job before the new app instances start, rather than letting every
instance attempt it on boot.

**A real deployment needs health/readiness probes**, not just "container started." An
orchestrator (Kubernetes, ECS, etc.) needs to know an instance has actually finished startup —
Flyway migration done, database and RabbitMQ connections established — before routing traffic to
it, the same problem this project's own `docker-compose.yml` healthchecks solve at a smaller scale.

## AI usage

This project was built through an extended, iterative collaboration with Claude (Anthropic) and
Grok (xAI), used throughout the development process rather than for one-shot code generation.
A few concrete examples of how it was actually used, for transparency:

- **Architecture decisions were discussed and reasoned through, not just generated.** For
  example, the endpoint structure for transactions (nested under `/accounts/{id}/transactions`
  rather than a flat `/transactions` resource) was decided after researching Tuum's own public
  API structure via their developer portal, discussing the tradeoffs, and correcting an earlier,
  less-considered design once the actual assignment PDF was available.
- **Real bugs were found and fixed through iterative review, not accepted blindly.** Examples
  include a bug where account-creation events silently never reached RabbitMQ (traced to a
  missing `@Transactional` annotation, confirmed via the RabbitMQ management UI), a case where a
  RabbitMQ publish failure could have turned an already-successful, already-committed banking
  operation into a client-facing 500 error, and dead code left over after a refactor that would
  have shown up as false gaps in the coverage report.
- **AI was used to explain unfamiliar concepts, not just produce code.** Spring's exception
  handling pipeline, how `@TransactionalEventListener` and RabbitMQ actually work end-to-end, and
  the mechanics of the concurrency test were each explained in depth on request, since parts of
  this stack were newer territory going in.
- **Current library documentation was checked directly rather than relying on possibly-outdated
  training knowledge**, particularly for Spring Boot 4's several breaking module reorganizations
  (Testcontainers artifact renames, `TestRestTemplate`'s move to a separate module and package,
  the Testcontainers 2.0 API changes) — each was verified against official docs or release notes
  before being applied, rather than guessed.
- **Static analysis findings (IntelliJ inspections, nullability warnings, Java 21 modernization
  suggestions) were reviewed and applied individually**, with each fix's reasoning understood
  rather than accepted as an opaque diff.
- **The admin console was added the same way:** Vite + TypeScript scaffolding, shadcn/ui, RTK
  Query endpoint modules matching the Java DTOs, Docker/Nginx packaging so `docker compose up`
  starts the UI next to the API, and fixes for SPA/API path collisions on refresh.
