# Industrial Asset Management Frontend

Backend-ready React frontend for an Industrial Asset Management system. The application is designed to support asset inventory, employee assignments, maintenance workflows, warranties, vendors, licenses, procurement, locations, reports, notifications, and settings.

## Tech Stack

- Frontend: React JSX with Vite
- Backend: Spring Boot
- Database: PostgreSQL
- Styling: Global CSS with reusable component classes
- Routing: React Router

## Current Status

The frontend is implemented as a production-oriented UI shell. The backend and database are not implemented yet.

Because backend APIs are not available yet, the frontend intentionally does not include mock business data, hard-coded records, fake CRUD behavior, localStorage-backed data, or simulated API responses. Screens render loading, error, empty, or unavailable states until real Spring Boot APIs are connected.

## Development

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Run lint checks:

```bash
npm run lint
```

Create a production build:

```bash
npm run build
```

## API Configuration

When the Spring Boot backend is available, create a local `.env` file using `.env.example`:

```bash
VITE_API_BASE_URL=http://localhost:8080/api
```

Frontend service functions already route requests through the API layer. Components should continue to call hooks and services instead of embedding API requests directly inside JSX.

## Project Structure

```text
src/
  api/                 API client and request helpers
  assets/              Static frontend assets
  components/
    common/            Reusable UI components
    domain/            Asset-management domain components
    layout/            Sidebar, topbar, and app shell
  constants/           Navigation, icons, and resource page configs
  hooks/               Reusable resource/action hooks
  pages/               Route-level screens
  services/            Backend-ready service modules
  utils/               Shared utilities when needed
  App.jsx              Route definitions
  main.jsx             React entry point
```

## Backend Integration Plan

The planned backend stack is Spring Boot with PostgreSQL. Recommended backend API areas:

- Asset inventory endpoints
- Employee and assignment endpoints
- Maintenance and work order endpoints
- Warranty, vendor, license, and procurement endpoints
- Location endpoints
- Dashboard and reporting endpoints
- Notification endpoints
- Settings endpoints

The frontend should consume these through the existing service layer so backend changes stay isolated from UI components.
