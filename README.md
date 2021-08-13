# veo-history
REST API providing archived revisions of resources from veo REST services. Uses Spring boot.


## Runtime dependencies
* OAuth server


## Build

    ./gradlew build

For verification, I recommend this as a `pre-commit` git hook.


## Config & Launch
### Create PostgreSQL DB
Install postgres and create veo-history database:

    su postgres
    createuser -S -D -R -P verinice
    # when prompted set password to "verinice"
    createdb -O verinice veo-history
    exit

You can customize connection settings in `application.properties` > `spring.datasource.[...]`.

### Configure OAuth
Setup OAuth server URLs (`application.properties` > `spring.security.oauth2.resourceserver.jwt.[...]`).

### Run

    ./gradlew bootRun

(default port: 8084)


## API docs
Launch and visit <http://localhost:8084/swagger-ui.html>


## Code format
Spotless is used for linting and license-gradle-plugin is used to apply license headers. The following task applies
spotless code format & adds missing license headers to new files:

    ./gradlew formatApply

The Kotlin lint configuration does not allow wildcard imports. Spotless cannot fix wildcard imports automatically, so
you should setup your IDE to avoid them.

## Database migrations
Veo-history uses [flyway](https://github.com/flyway/flyway/) for DB migrations. It runs kotlin migration scripts from [org.veo.history.migrations](src/main/kotlin/org/veo/history/migrations) when starting the service / spring test environment before JPA is initialized.

### Creating a migration
1. Modify DB model code (JPA entity classes).
2. `./gradlew bootRun`. The service might complain that the DB doesn't match the model but will silently generate the update DDL in `schema.local.sql`.
3. Copy SQL from `schema.local.sql`.
4. Create a new migration script (e.g. `src/main/kotlin/org/veo/history/migrations/V3__add_fancy_new_columns.kt`) and let it execute the SQL you copied (see existing migration scripts).
5. Append a semicolon to every SQL command
6. Add some DML to your migration if necessary.
