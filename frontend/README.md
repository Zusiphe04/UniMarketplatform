# UniMarket Frontend

Responsive React and Vite storefront for the UniMarket Spring API.

## Setup

1. The development server proxies same-origin `/api` requests to `http://localhost:8080`; copy `.env.example` to `.env` only when a separately deployed API needs an explicit `VITE_API_BASE_URL`.
2. Run `npm install`.
3. Run `npm run dev` and keep the Spring backend on `http://localhost:8080`.

## Seed the supplied catalogue

For the complete role-aware frontend dataset, first run `../backend/http/seeding/00_frontend_platform_users.http` in IntelliJ. It creates the seller, student, faculty, resident, buyer, and moderator accounts through the real API and documents the administrator grants required for privileged roles.

After approving the seller, open `../backend/http/seeding/03_frontend_catalogue.http` and run it from top to bottom. The file logs in and captures the seller token and product IDs automatically before creating, imaging, and publishing each item. The live API listings automatically replace the labelled local preview. All successful requests persist through Spring/JPA to the configured database.

The seller form uses the backend-aligned categories Books, Tech, Clothing, Room & home, Service, and Other. It conditionally captures technology specifications such as storage, memory, processor, screen size, and color, plus clothing size/color and applicable room/home details.

## Structure

- `public/images/` — supplied brand, hero, category, product, and service artwork
- `src/js/` — API clients, hooks, constants, and formatters
- `src/jsx/` — React pages and components
- `src/css/` — global, component, and responsive styles
