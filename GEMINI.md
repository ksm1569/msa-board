# MSA Board Project Context

## Project Overview
This project, `msa-board`, is a Microservices Architecture (MSA) implementation for a bulletin board system. It is built using Java and Spring Boot, managed by Gradle. The project is currently structured as a multi-module Gradle project.

## Technology Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.3.2
- **Build Tool:** Gradle
- **Key Libraries:** 
  - Lombok (for reducing boilerplate code)
  - JUnit 5 (for testing)
  - Spring Web (in sub-modules)

## Project Structure
The project is organized into a root project with shared configuration and several sub-modules representing different microservices:

- **Root:** Contains global build configuration (`build.gradle`, `settings.gradle`) and the Gradle wrapper.
- **`service/`**: Contains the individual microservice modules:
  - `article`: Manages article creation and updates.
  - `article-read`: Handles reading articles (CQRS pattern likely intended).
  - `comment`: Manages comments on articles.
  - `hot-article`: Handles "hot" or popular articles.
  - `like`: Manages likes/reactions.
  - `view`: Handles view counts or view-related logic.
- **`common/`**: Intended for shared utilities and domain objects (currently empty).

## Building and Running

### Prerequisites
- JDK 21 installed.
- Terminal with access to the project root.

### Build Commands
Use the provided Gradle Wrapper (`gradlew`) for all build tasks to ensure version consistency.

*   **Build the entire project:**
    ```bash
    ./gradlew build
    ```

*   **Clean the project:**
    ```bash
    ./gradlew clean
    ```

### Running Services
Since this is a multi-module project, you can run individual services using their specific Gradle tasks.

*   **Run the Article Service:**
    ```bash
    ./gradlew :service:article:bootRun
    ```

*   **Run the Comment Service:**
    ```bash
    ./gradlew :service:comment:bootRun
    ```
    *(Replace `article` or `comment` with the name of the service you wish to run: `article-read`, `hot-article`, `like`, `view`)*.

## Development Conventions
- **Code Style:** Standard Java conventions. Lombok is used extensively (`@Getter`, `@Setter`, `@RequiredArgsConstructor`, etc.).
- **Dependency Management:** Common dependencies (Lombok, Test starters) are defined in the root `build.gradle` under `allprojects`. Service-specific dependencies are in their respective `build.gradle` files.
- **Configuration:** Spring Boot configuration is located in `src/main/resources/application.yml` for each service.
- **Testing:** JUnit 5 is configured for testing. Run tests via `./gradlew test`.

## Future Considerations
- The `common` module is currently empty and will likely be populated with shared DTOs, utilities, or exceptions.
- Database and messaging dependencies (e.g., JPA, Kafka) are not yet explicitly visible in the inspected files and may need to be added as the project matures.
