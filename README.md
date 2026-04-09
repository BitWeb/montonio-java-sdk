# Montonio Java SDK

A type-safe Java client for the [Montonio](https://montonio.com) payment gateway REST API (V2 + Stargate). Covers payment order lifecycle, payment method discovery, and JWT webhook/return validation.

## Requirements

- Java 17+
- Gradle 9.4.1 (included via wrapper)

## Getting Started

### Using a Devcontainer (recommended)

The project includes a [devcontainer](.devcontainer/devcontainer.json) configuration with Java 17, Gradle, and GitHub CLI pre-installed. Open the project in any devcontainer-compatible tool:

- **VS Code** — install the [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers) extension, then _Reopen in Container_
- **IntelliJ IDEA** — open the project and follow the [Dev Containers integration](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html) guide
- **GitHub Codespaces** — click _Code > Codespaces > New codespace_ on the repository page
- **CLI** — using the [Dev Container CLI](https://github.com/devcontainers/cli):
  ```bash
  devcontainer up --workspace-folder .
  devcontainer exec --workspace-folder . bash
  ```

### Local Setup

Install Java 17 via [SDKMAN](https://sdkman.io/):

```bash
sdk env install
```

## Build & Test

```bash
./gradlew build                # full build
./gradlew test                 # all tests
./gradlew unitTest             # unit tests only
./gradlew integrationTest      # integration tests only
./gradlew testAndReport        # all tests + JaCoCo coverage reports
```

## License

[MIT](LICENSE)
