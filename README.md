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
