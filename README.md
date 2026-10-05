# 💰 Expenzo

> A full-stack expense management application with local + Google OAuth authentication.

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen)
![Vue.js](https://img.shields.io/badge/Vue-3-4FC08D)
![Java](https://img.shields.io/badge/Java-21-orange)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)

Expenzo is a personal finance tracker that helps you log daily transactions, set monthly budgets,
categorize expenses, manage multiple bank accounts, and export your data. Built with a
Spring Boot backend and a Vue 3 frontend, it supports both traditional email/password
login (with OTP verification) and one-click Google sign-in.

---

## ✨ Features

### Authentication
- Email/password registration with **OTP email verification**
- **Google OAuth2** one-click login
- **JWT-based** stateless authentication (7-day expiry)
- Password reset via email link (30-min token)
- Auto-generated temporary password on signup
- Separate handling for LOCAL and GOOGLE users

### Expense Management
- **Transactions** — add, edit, delete with full audit trail
- **Categories** — organize spending per user
- **Bank Accounts** — track balances across multiple accounts
- **Budgets** — set monthly limits per category, get overspend alerts
- **Recurring Transactions** — automated monthly transactions via scheduler

### Import & Export
- **Import** transactions from CSV, Excel (.xlsx), or PDF
- **Export** to CSV or PDF with proper null-safe handling
- Bank-account auto-matching during import

### UI / UX
- **Dark mode** with persistent theme
- **Fully responsive** (mobile, tablet, desktop)
- Real-time charts (spend by category, budget vs actual, 6-month trend)
- Animated counters, skeleton loaders, toast notifications
- Global error boundary with graceful fallback

---

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 4.1.1 |
| **Security** | Spring Security 7, JWT (JJWT 0.12.6), OAuth2 Client, BCrypt |
| **Persistence** | Spring Data JPA, Hibernate 7, MySQL 8 |
| **Email** | Spring Boot Mail (Gmail SMTP) |
| **File Processing** | Apache POI (Excel), PDFBox (PDF), OpenCSV (CSV) |
| **Frontend** | Vue 3 (Composition API), Vite |
| **State** | Pinia |
| **Routing** | Vue Router 4 |
| **Styling** | Bootstrap 5, Bootstrap Icons, custom theme |
| **Charts** | Chart.js |
| **HTTP** | Axios |

---

## 📁 Project Structure


---

## 🚀 Setup

### Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Node.js | 20+ |
| MySQL | 8+ |
| Gmail account | for sending OTP/reset emails |
| Google Cloud project | for OAuth2 login (optional) |

### 1. Clone the repo

```bash
git clone https://github.com/YOUR-USERNAME/Expenzo.git
cd Expenzo