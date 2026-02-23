# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/com/enumerate/disease_detection` contains the Spring Boot application and modules such as `Controller`, `Service`, `Mapper`, `Tools`, `Configurations`, and `ModelInterfaces`.
- `src/main/resources` holds configuration and assets: `application.yml`, MyBatis XML in `mapper/`, and web assets in `static/` and `templates/`.
- `src/test/java/com/enumerate/disease_detection` contains test classes (currently minimal).
- `sql/` stores database scripts; `target/` is build output.

## Build, Test, and Development Commands
- `mvn clean package` builds the project and produces the JAR.
- `mvn spring-boot:run` starts the app locally using `application.yml`.
- `mvn test` runs the unit/integration tests.

## Coding Style & Naming Conventions
- Java 17 with Spring Boot 3.x; prefer 4-space indentation and standard Java conventions.
- Packages use lowercase (e.g., `com.enumerate.disease_detection`); classes use PascalCase; methods/fields use camelCase.
- Keep module names aligned to folders (`Controller`, `Service`, `Mapper`, `Utils`) and avoid mixing responsibilities.
- Lombok is enabled; use it consistently rather than manual boilerplate.

## Testing Guidelines
- Tests use `spring-boot-starter-test` (JUnit 5).
- Name test classes `*Tests.java` and colocate under `src/test/java` mirroring the main package.
- Run focused tests with `mvn test -Dtest=ClassNameTests`.

## Commit & Pull Request Guidelines
- Recent commits use version-style messages (e.g., `1.7.9`, `1.7.2-农场管理`). Follow that pattern for releases; use short, scoped descriptions for non-release work.
- PRs should include: purpose, key changes, how to verify, and any config or schema updates.
- If UI/static assets change, include before/after screenshots where applicable.

## Configuration & Security Tips
- `application.yml` uses profiles (`dev/test/prod`). Prefer environment variables or local overrides for secrets (DB, mail, API keys).
- Do not commit real credentials or tokens; replace with placeholders and document required env vars in the PR.
