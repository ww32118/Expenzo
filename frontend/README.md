# Expenzo Frontend

Vue 3 + Bootstrap 5 UI for the Expenzo Spring Boot backend, with a light/dark theme toggle.

## Setup

```bash
npm install
cp .env.example .env   # adjust if your backend isn't on localhost:8080
npm run dev
```

App runs at `http://localhost:5173`.

## What's included

- Auth: Login / Register (session kept in `localStorage`, no JWT — see note below)
- Dashboard: totals, budget vs spend, "Spend Pulse" category breakdown
- Transactions: full CRUD, category picker
- Categories: full CRUD
- Budgets: add/edit/delete (see backend note below)
- Bank Accounts: add/edit/delete, type switch, masked account numbers
- Export: CSV / PDF download
- Profile: update details, change password
- Light/dark theme toggle (navbar sun/moon switch), persisted in `localStorage`

## ⚠️ Backend items to fix before this is fully wired up

1. **CORS** — add a CORS config in Spring Boot allowing `http://localhost:5173`, or the
   browser will block every request. Example:
   ```java
   @Bean
   public WebMvcConfigurer corsConfigurer() {
     return new WebMvcConfigurer() {
       @Override
       public void addCorsMappings(CorsRegistry registry) {
         registry.addMapping("/**").allowedOrigins("http://localhost:5173").allowedMethods("*");
       }
     };
   }
   ```

2. **`UserResponseDTO` has no `id` field.** This is the biggest one — the frontend
   needs the logged-in user's `id` to create transactions, categories, budgets, and
   bank accounts (they all take a `userId`). Right now `authStore.userId` will be
   `null` after login/register until you add `id` to `UserResponseDTO` and populate
   it in `UserService`.

3. **`CategoryResponse`, `BudgetResponse`, `BankAccountResponse` have no `id` field**,
   which means edit/delete buttons for budgets and bank accounts can't target the
   right record yet (categories currently do return enough since the frontend
   fetches by list — but confirm your `CategoryService` mapping includes `id`).
   Add `id` (and ideally `categoryId`/`categoryName` on `BudgetResponse`) to fix this.

4. **List endpoints aren't scoped per user** (`/transactions/all-transactions`,
   `/category/all-categories`, `/budget/all-users`, `/bank-account`) — they return
   every record for every user. The frontend currently just displays whatever comes
   back. Consider adding `/transactions/user/{userId}`-style endpoints, or filtering
   server-side by the authenticated user.

5. **Security** — `spring.security.user.name` / `password` in `application.properties`
   is Spring Security's *default* built-in user, separate from your app's own
   `User` entity/login. The frontend sends this as a Basic Auth header on every
   request just to get past default Spring Security (see `src/services/api.js`).
   Replace with real per-user auth (JWT is the common choice) when ready.

## Theme

Colors live in `src/assets/theme.css` as CSS variables (`--color-*`), switched via
`data-theme="light"` / `data-theme="dark"` on `<html>`. Toggle button is in
`src/components/ThemeToggle.vue`.

Fonts: **Germania One** (`--font-brand`) is used for the sidebar brand mark and every
page's big `page-title` heading — kept out of body copy and buttons for readability.
Sora (`--font-display`) handles module sub-headings, Inter is body text, JetBrains
Mono is used for amounts/currency (`.font-mono`).

## Design touches added

- Gradient hero banner on the Dashboard with a faint ₹ watermark in Germania One
- Blurred gradient shapes + a giant translucent "Expenzo" watermark behind Login/Register
- Hover-lift on cards (`.surface-card--interactive`)
- Avatar initials chip in the topbar linking to Profile
- Mobile: sidebar becomes an off-canvas drawer with a tap-to-close overlay
- Responsive type scale (`clamp()`) on page titles, tighter paddings under 576px
- Visible keyboard focus rings and `prefers-reduced-motion` support
- Staggered fade-in-up entrance animation on every page's cards/sections

## Dashboard charts

Built with lightweight, dependency-free SVG/CSS (no charting library needed —
respects your theme colors automatically and animates on load / theme switch):

- **`DonutChart.vue`** — spend by category, animated stroke-draw, hover to spotlight a slice
- **`BudgetBarChart.vue`** — per-category budget vs. actual spend, animated fill bars,
  turns red when a category goes over budget
- **`TrendBarChart.vue`** — last 6 months of total spend, animated column growth
- Stat cards (Total Spent, Monthly Budget, Transactions, Avg/Transaction) count up
  from 0 on load via `useCountUp.js`

Note: `BudgetBarChart` groups by `categoryName` from the budget if present, falling
back to the budget's `month` — once `BudgetResponse` returns `categoryId`/`categoryName`
(see backend note above) it'll automatically group by category correctly.
