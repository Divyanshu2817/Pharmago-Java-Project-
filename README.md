# PharmaGo

PharmaGo is an MCA-level Java desktop project for managing medicine inventory, purchases, sales, and expiry alerts using Java Swing, AWT, JDBC, and MySQL.

## Highlights

- Layered architecture: `config`, `dao`, `model`, `service`, `ui`, `util`
- Desktop dashboard built with Swing and AWT
- Low-stock and near-expiry intelligence
- Purchase and sales workflows that update stock automatically
- MySQL schema and seed data included

## Tech Stack

- Java 17
- Swing and AWT
- JDBC
- MySQL 8+
- Maven

## Project Structure

```text
pharmago/
├── pom.xml
├── README.md
├── database/
│   ├── schema.sql
│   └── seed.sql
└── src/
    └── main/
        ├── java/com/pharmago/
        │   ├── PharmagoApplication.java
        │   ├── config/
        │   ├── dao/
        │   ├── model/
        │   ├── service/
        │   ├── ui/
        │   └── util/
        └── resources/
            └── application.example.properties
```

## Database Setup

1. Create the database in MySQL.
2. Run `database/schema.sql`
3. Optionally run `database/seed.sql`
4. Copy `src/main/resources/application.example.properties` to `src/main/resources/application.properties`
5. Update your MySQL username and password

## Run

```bash
mvn compile
mvn exec:java
```

The application opens as a desktop window after the database connection is established.

## Default Features

- Add and view medicines
- Track purchases and sales
- Monitor low-stock inventory
- Show expiry alerts for upcoming expiries
- Generate a live business summary dashboard
- Use desktop forms and tables for data entry and records review
