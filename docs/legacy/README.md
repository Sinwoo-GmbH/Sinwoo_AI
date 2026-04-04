# SINWOO Legacy Upgrade Docs

## Purpose

This directory contains the official analysis and upgrade documents for migrating the legacy `Sinwoo-New.zip` project into the next-generation SINWOO platform.

The goal is not a blind rewrite.

The goal is:

- preserve usable business assets
- move into the next-generation standard architecture
- add stronger compliance, audit, and UI foundations
- keep the migration executable and reviewable

## Document Index

- `01_sinwoo_new_upgrade_assessment.md`
  - legacy stack, asset scale, structural problems, and keep-improve-risk analysis

- `02_nextgen_upgrade_strategy.md`
  - next-generation upgrade strategy, target architecture, and migration principles

- `03_legacy_asset_inventory.md`
  - controller, screen, mapper, SQL, and static-asset inventory

- `04_domain_porting_matrix.md`
  - domain-by-domain keep, bridge, port, and standardize matrix

- `05_core_domain_field_mapping.md`
  - first mapping draft for user, company, role, and logging models

- `06_phase1_execution_backlog.md`
  - Phase 1 execution backlog and ordered delivery targets

- `07_phase1_porting_progress.md`
  - actual implementation progress for the next-generation common axis

- `08_german_compliance_baseline.md`
  - Germany-first legal compliance baseline for the product

## Operating Rules

- The legacy project remains a reference asset, not the new source of truth.
- The upgrade is treated as a next-generation migration, not a direct clone.
- Running business behavior should be preserved where possible.
- New engineering work must align with the next-generation naming, history, menu authorization, and compliance standards.
- German-law compliance is now a first-class engineering baseline for Germany-facing domains.
