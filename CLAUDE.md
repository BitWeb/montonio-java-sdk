# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Montonio Java SDK — a type-safe Java client for the Montonio payment gateway REST API (V2 + Stargate). Covers payment order lifecycle, payment method discovery, and JWT webhook/return validation. MIT licensed, owned by Bitweb.

## Build & Test Commands

Requires Java 17 (managed via `.sdkmanrc`). Uses Gradle 9.4.1 with Groovy DSL.

```bash
./gradlew build                # full build
./gradlew test                 # all tests
./gradlew unitTest             # unit tests only (excludes @Tag("integration"))
./gradlew integrationTest      # integration tests only (@Tag("integration"))
./gradlew testAndReport        # all tests + jacoco reports
./gradlew test --tests 'com.example.MyTest.myMethod'  # single test
```

## Devcontainer

A devcontainer configuration is provided in `.devcontainer/devcontainer.json`. It includes Java 17 (Temurin), Gradle (via wrapper), and GitHub CLI. Open the project in any devcontainer-compatible tool (VS Code, IntelliJ, GitHub Codespaces, Claude Code) to get a ready-to-use environment.

## Gradle Structure

Build is split across files following BitWeb conventions:
- `build.gradle` — plugins, dependencies, Java config
- `test.gradle` — JUnit Platform, jacoco, unit/integration test tasks
- `library.gradle` — java-library, maven-publish, signing, nexus publishing
