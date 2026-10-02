# WorkFlowX — Enterprise Project & Collaboration Platform

> A modern, Jira/Linear-inspired full-stack enterprise project and task management application built with **Spring Boot 3** and **React (Vite)**.

---

## 🚀 Overview

**WorkFlowX** is an enterprise-grade project management system engineered for high-performance software engineering and product teams. It provides workspaces, multi-project coordination, interactive 5-stage Kanban boards, sprint lifecycle management, team collaboration, and real-time activity feeds with a clean, light Jira-inspired design system.

---

## ✨ Features

- 🔐 **Authentication & Security**: JWT-based authentication (Access & Refresh tokens), role-based access control, password hashing with BCrypt.
- 🏢 **Multi-Workspace Hierarchy**: Create and manage multiple workspaces with isolated projects and members.
- 📁 **Project Management**: Project portfolio tracking with status badges (Planning, Active, Completed, On Hold).
- 📋 **Interactive Kanban Board**: 5-column Jira workflow: `BACKLOG`, `TODO`, `IN_PROGRESS`, `CODE_REVIEW`, and `DONE`.
- ⚡ **Sprint Planning**: Time-boxed iteration cycles with status lifecycle (`PLANNED` → `ACTIVE` → `COMPLETED`) and sprint goal tracking.
- ✅ **Issue / Task Navigator**: Full-text filtering, search by title/summary, priority levels (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`), and due dates.
- 👥 **Teams & Collaboration**: Organization squads, member management, and task comments.
- 🔔 **Activity Feed & Notifications**: Real-time notifications for task assignments and status updates.
- 🎨 **Jira-Inspired Light UI**: Clean, accessible design system with Atlassian Blue accents (`#0052CC`), neutral surfaces (`#F4F5F7`), and status lozenges.

---

## 🛠️ Tech Stack

### Backend
- **Framework**: Java 21, Spring Boot 3.3
- **Security**: Spring Security 6, JWT (jjwt 0.12)
- **Data Persistence**: Spring Data JPA, Hibernate, PostgreSQL / H2 Database
- **Validation & Mapping**: Jakarta Bean Validation, Lombok
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18, Vite
- **State & Data Fetching**: TanStack React Query v5, Zustand
- **Routing**: React Router v6
- **Styling**: Modern CSS Design System (Jira / Atlassian Design Language)
- **Icons & UI**: Lucide React, React Hot Toast

---

## 📂 Project Structure

```
WorkFlowX/
├── workflowx-backend/          # Spring Boot 3 Backend
│   ├── src/main/java/com/workflowx/
│   │   ├── auth/               # Authentication & JWT tokens
│   │   ├── user/               # User entities & services
│   │   ├── workspace/          # Workspaces & membership
│   │   ├── project/            # Projects
│   │   ├── sprint/             # Sprints & iterations
│   │   ├── task/               # Tasks & Kanban logic
│   │   ├── comment/            # Task comments
│   │   ├── notification/       # Notifications
│   │   ├── activity/           # Audit trail & activities
│   │   └── security/           # Security filters & configs
│   ├── src/main/resources/     # Application configurations
│   └── pom.xml
│
├── workflowx-frontend/         # React + Vite Frontend
│   ├── src/
│   │   ├── api/                # Axios API services
│   │   ├── components/         # Layout, Modal, Avatar, Badges, Spinner
│   │   ├── pages/              # Dashboard, Workspaces, Projects, Kanban, Tasks, Sprints, Teams
│   │   ├── store/              # Zustand authentication store
│   │   └── index.css           # Jira light design tokens & styles
│   └── package.json
│
├── .gitignore                  # Monorepo gitignore
└── README.md
```

---

## 🏃 Getting Started

### Prerequisites
- **JDK 17+** (JDK 21 recommended)
- **Node.js 18+** & **npm**
- **Git**

### 1. Clone the repository
```bash
git clone <your-repository-url>
cd WorkFlowX
```

### 2. Start the Backend
```bash
cd workflowx-backend
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local"
```
*Backend runs on `http://localhost:8080` (API base: `http://localhost:8080/api/v1`)*

### 3. Start the Frontend
```bash
cd ../workflowx-frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`*

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
