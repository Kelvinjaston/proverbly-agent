# Proverbly Agent

##  Overview

The Proverbly Agent is a Spring Boot application designed to manage, retrieve, and disseminate inspiring quotes and proverbs. It acts as a central service for two primary functions: fetching fresh content from external APIs and managing a local store of proverbs, with built-in scheduling and integration capabilities for internal messaging platforms (like a "Telex" system).

---

##  Key Features

- **External Quote Retrieval**: Connects to a third-party API to fetch a new, random quote on demand
- **Local Proverb Management**: Serves proverbs from a local data source (proverbs.json)
- **Scheduled Delivery**: Uses a scheduler to periodically perform tasks, such as sending daily proverbs or quotes
- **Telex Integration**: Includes webhook support and client logic for receiving and sending messages on an internal messaging system
- **Robust Fallback**: Provides a safe, internal fallback quote if the external API call fails

---

##  Technology Stack

| Category | Technology |
|----------|------------|
| Framework | Spring Boot |
| Language | Java 17+ |
| Build Tool | Maven |
| APIs | RESTful Controllers and Services |
| Logging | SLF4J / Logback |

---

##  Getting Started

### Prerequisites

To run this project locally, you need:

- **Java Development Kit (JDK)** 17 or newer
- **Maven** 3.6 or newer
- **Git** (for cloning the repository)

### Local Setup

**1. Clone the repository:**

```bash
git clone https://github.com/Kelvinjaston/proverbly-agent.git
cd proverbly-agent
```

**2. Build the project:**

```bash
./mvnw clean package
```

**3. Run the application:**

```bash
java -jar target/proverbly-agent-0.0.1-SNAPSHOT.jar
```

> **Note:** The JAR name may vary based on your `pom.xml` version.

The application will start on `http://localhost:8080`.

### Environment Variables

Before running, you must configure the following variables, typically in an `application.properties` file or by setting environment variables in your terminal/deployment platform (like Railway):

| Variable Name | Description | Example Value |
|--------------|-------------|---------------|
| `SERVER_PORT` | The port the application runs on | `8080` |
| `EXTERNAL_QUOTE_API_BASE_URL` | Base URL for the external quote service | `https://api.zenquotes.io/v1/` |

---

##  API Endpoints

Once the application is running, you can access the following primary endpoints:

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/quote/random-external` | Fetches a random quote from the configured external API. Includes a system fallback. |
| `GET` | `/api/proverb/random` | Fetches a random proverb from the local `proverbs.json` data source. |
| `POST` | `/api/telex/webhook` | Endpoint to receive webhook notifications (e.g., new messages or commands) from the Telex platform. |

---
