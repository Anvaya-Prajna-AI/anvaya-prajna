# Database Migration Strategy & Guide

This document outlines the Flyway database migration practices for Anvaya-Prajna-AI.

## Architecture & Principles
1. **Flyway Owns the Schema**: In production and development environments, Flyway handles all DDL migrations.
2. **Hibernate DDL Auto**: Set to `validate` in production to prevent unintended schema modifications and schema drift.
3. **Immutability**: Migration scripts once applied must never be edited. Any schema modifications must be applied via new incremental migration scripts (`V<version>__<description>.sql`).

## File Naming Convention
- Path: `services/explain-service/src/main/resources/db/migration/`
- Pattern: `V<version>__<short_description>.sql`
- Example:
  - `V1__create_explanation_schema.sql`
  - `V2__add_review_columns.sql`

## Handling Failed Migrations (Flyway Repair)
If a migration fails in non-production during development:
1. Fix the underlying SQL script or database state.
2. Run Flyway repair command or clean environment:
   ```bash
   ./gradlew flywayRepair
   ```
3. In PostgreSQL directly if needed:
   ```sql
   DELETE FROM flyway_schema_history WHERE success = false;
   ```

## Test Environments
In unit/slice testing with H2, tests use `spring.jpa.hibernate.ddl-auto: create-drop` or testcontainers with standard PostgreSQL Flyway migrations.
