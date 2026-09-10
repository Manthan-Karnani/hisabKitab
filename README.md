# Hisab Kitab

Hisab Kitab is an expense tracking website I built with my teammates Rahul and Aarush for our DBMS project. The idea is simple: you note down what you spend, category by category, set a monthly budget, and the site shows you where your money is actually going.

## What it does

- Register and login with a username and password (no OTP, passwords are stored hashed)
- Add expenses under categories like Food, Travel, Shopping, Rent, Utilities, Health, Entertainment and Others, or create your own category
- Set a monthly budget and watch every expense get deducted from it, with remaining balance shown
- See a category-wise summary chart for the month, along with total spent and remaining figures
- Everything is saved per user, so each person's data stays separate and is there when they log back in

## Tech stack

- Backend: Spring Boot (Java 17) with Spring Data JPA
- Database: MySQL
- Frontend: plain HTML, CSS and JavaScript (served by Spring Boot itself), Chart.js for the summary chart

## Project structure

```
dbmsProject/
  backend/
    pom.xml
    src/main/java/com/hisabkitab/
      HisabKitabApplication.java
      model/         (User, Category, Budget, Expense entities)
      repository/    (Spring Data JPA repositories)
      controller/    (Auth, Category, Budget, Expense, Summary APIs)
      service/       (register/login logic, default categories)
      util/          (salted SHA-256 password hashing)
      config/        (CORS config)
    src/main/resources/
      application.properties   (DB config, reads env vars on Render)
      static/                  (the frontend)
        index.html             (login / register page)
        dashboard.html         (budget, expenses, chart)
        css/style.css          (banknote green-and-gold theme)
        js/                    (api.js, auth.js, dashboard.js)
  database/
    schema.sql       (table definitions and sample report queries)
  Dockerfile         (used for the Render deployment)
  render.yaml        (Render blueprint)
```

## Running it locally

You need Java 17+, Maven and MySQL running on your machine.

```bash
cd backend
mvn spring-boot:run
```

Then open http://localhost:8080 in your browser, register an account and start adding expenses. If your MySQL root user has a password, update it in `backend/src/main/resources/application.properties` first. The `hisabkitab` database and all tables are created automatically on the first run.

## Live demo

The project is hosted here: https://hisab-kitab-rate.onrender.com

Made by Manthan, Rahul and Aarush.
