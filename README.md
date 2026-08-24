# AssetFlow Industrial Asset Management System

## Project Overview

AssetFlow is a full-stack industrial and IT asset lifecycle management system. It combines a Spring Boot REST API, PostgreSQL database, and React/Vite frontend to help organizations manage assets from procurement and assignment through maintenance, warranty, transfer, valuation, and disposal.

The backend provides the complete business API and persistence layer. The frontend is an API-ready user interface whose backend integration is planned as a separate phase.

## Tech Stack

- Frontend: React, JSX, Vite, React Router, and reusable CSS
- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA, and Maven
- Database: PostgreSQL with JSONB, constraints, triggers, indexes, and reporting views
- Authentication: JWT-based authentication with secure cookies and role-based authorization

## Features

- Asset inventory with categories, vendors, locations, conditions, statuses, valuations, transfers, lifecycle events, and disposal records
- Employee, department, application user, role, and permission management
- Asset assignments, returns, transfers, and assignment/status consistency rules
- Maintenance tickets with priorities, vendors, costs, due dates, and resolution tracking
- Warranty policies and claims
- Software licenses and employee seat assignments
- Purchase orders, purchase-order items, invoices, and vendor contracts
- Notifications, notification preferences, organization settings, system settings, and email templates
- Dashboard summaries, activity feeds, reports, audit logging, validation, and structured API errors
- PostgreSQL initialization through [Database-Initialize.sql](Database-Initialize.sql)

## Project Structure

```text
Asset_Management/
├── Backend/
│   └── AssetsFlow/
│       ├── src/main/java/com/binuwara/AssetsFlow/
│       │   ├── Config/          Application and security configuration
│       │   ├── Controller/      REST API controllers
│       │   ├── DTO/             Request and response records
│       │   ├── Entity/          JPA entities and domain enums
│       │   ├── Exception/       API exceptions and error handling
│       │   ├── Repository/      Spring Data repositories
│       │   └── Service/         Business logic and reporting services
│       ├── src/main/resources/  Backend configuration
│       └── src/test/             Backend tests
├── Frontend/
│   ├── src/
│   │   ├── api/                API client and request helpers
│   │   ├── components/         Shared, domain, and layout components
│   │   ├── constants/           Navigation and application configuration
│   │   ├── hooks/               Reusable resource and action hooks
│   │   ├── pages/               Route-level screens
│   │   ├── services/            API-ready service modules
│   │   └── utils/               Shared utilities
│   └── package.json
└── Database-Initialize.sql      PostgreSQL database initialization script
```
