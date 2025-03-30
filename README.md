# 🌍 InternSwiftCodesProject


An application for managing and searching SWIFT (BIC) codes of banks worldwide.
Allows importing data from CSV files, filtering, adding and deleting entries, and automatically linking branches to the bank's headquarters.
---
## 💡 Task Description

A SWIFT code, also known as a Bank Identifier Code (BIC), is a unique identifier of a bank's branch or headquarter.  
This application was built to:

✅ Parse CSV files with SWIFT data

✅ Recognize headquarters (XXX) and assign branches to them

✅ Normalize data (ISO2, country names, formatting)

✅ Store data in PostgreSQL database

✅ Provide a REST API for reading and managing
---

## 📦 Technologies

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL (for production)
- H2 (for tests)
- Docker + Docker Compose
- Maven
- Apache Commons CSV

---

### Endpoint description
#### 📤 POST /upload/swift

Description:
Uploads a CSV file with SWIFT data. Data is validated, parsed, and saved to the database.
Parameters:

    file (form-data): .csv file with headers:
    SWIFT CODE, NAME, ADDRESS, COUNTRY ISO2 CODE, COUNTRY NAME

#### Responses:
```http
200 OK – file processed correctly 
400 Bad Request – no file, wrong format or parsing error  
```
---
#### 🔍 GET /v1/swift-codes/{swiftCode}

Description:
Returns SWIFT code details.
If it's a headquarter (XXX), also returns related branches.

Parameters:

    {swiftCode} – SWIFT code (8–11 znaków)
#### Responses:
```http
200 OK – data found  
400 Bad Request – invalid code format  
404 Not Found – not found  
```

Example response:

```json
{
  "swiftCode": "AAAABBCCXXX",
  "bankName": "Bank A",
  "countryISO2": "PL",
  "countryName": "Poland",
  "headquarter": true,
  "branches": [
    {
      "swiftCode": "AAAABBCC111",
      "bankName": "Bank A Branch 1",
      "address": "Branch Street 1",
      "countryISO2": "PL",
      "countryName": "Poland",
      "headquarter": false
    }
  ]
}

```
---
#### 🌍 GET /v1/swift-codes/country/{countryISO2}

Description:
Returns all SWIFT codes from a given country.

Parameters:

    {countryISO2} – dtwo-letter country code (e.g., PL, US)

#### Responses:
```http
200 OK –  list of codes  
400 Bad Request – invalid ISO2 code  
404 Not Found – no data  

```
---
#### ➕ POST /v1/swift-codes

Description:
Adds a new SWIFT code to the database.
Automatically links branches to the headquarter (if exists), or the headquarter to orphans.

```json
{
  "swiftCode": "AAAABBCCXXX",
  "bankName": "Bank A",
  "address": "Main Street 1",
  "countryISO2": "PL",
  "countryName": "POLAND"
}
```
#### Response
```http 
200 OK – record added  
400 Bad Request – validation error  
409 Conflict – code already exists  

```
---
#### ❌ DELETE /v1/swift-codes/{swiftCode}

Description:
Deletes a SWIFT code from the database.
If it's a headquarter, the branches become “orphaned”.

Parameters:

    {swiftCode} – code to be deleted

#### Responses:
```http 
200 OK – deleted successfully  
404 Not Found – code not found in database  
500 Internal Server Error – error during deletion  


```
---
### 🧪 Input Data Validation

✅ SWIFT Code

- Must be 8 to 11 characters long

- Allowed characters: letters A-Z and digits 0–9

- Codes ending with XXX are treated as bank headquarters

- Others are treated as branches

- A branch can be saved without a headquarter, but will be automatically linked if one exists

✅ Country (ISO2 + name)

 - Country code (countryISO2) must be a two-letter ISO2 code, e.g., PL, US

 - Country name (countryName) must match the ISO2 code (e.g., PL → POLAND)

 - All country data is automatically converted to uppercase

✅ Address (address)

- Required field

- Length: 3 to 500 characters

- If not provided, value "No address provided" is assigned

✅ Bank name (bankName)

 - Required field

 - Cannot be empty





---

### ✅ Project Launch Instructions – InternSwiftCodesProject

#### 📦 Requirements
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

---

### 🚀 Quick Start

#### 1. 🔁 Clone the repository
```bash
git clone https://github.com/LawyerN/InterbSwiftCodesProject.git
cd InterbSwiftCodesProject
```

#### 2. 📄 Create .env file

Based on the sample `.env.example` file:

```bash
cp .env.example .env (Linux)
copy .env.example .env (Windows cmd)

```

You can edit database login data:
```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=haslo123
```

---

### Add CSV data file
I added example file in data/examplecsv.csv
Place your .csv file in the directory:
````bash
src/main/resources/data/
````
CSV file requirements:
- Any name, must end with .csv

- Headers must contain:
```bash
SWIFT CODE, NAME, ADDRESS, COUNTRY ISO2 CODE, COUNTRY NAME
```

- Data will be automatically imported on application start(it won't be loaded while app is running )



### 🐳 Running with Docker

#### 1. Clean previous builds (required to make step 2 work)
```bash
mvn clean package
```

#### 2. Build and run containers

```bash
docker-compose up --build
```

- The app will be avaliable at [http://localhost:8080](http://localhost:8080)
- Postgres listens on port: `5432`

If you add csv to data then run:
```bash
docker-compose down
docker-compose up --build
```
---

### 🥪 Running tests

Locally:
```bash
./mvnw test
```
#### ✅ The test suite covers:

- CSV Upload:

    - Successful upload

    - Empty file

    - Wrong file format (non-CSV)

- GET /v1/swift-codes/{swiftCode}:

    - Valid code

    - Non-existent code (404)

    - Invalid format (400)

- GET /v1/swift-codes/country/{ISO2}:

    - Valid country with data

    - Lowercase ISO2 support

    - No data for country

    - Invalid ISO2 format

- POST /v1/swift-codes:

    - Adding HQ or branch

    - Missing fields

    - Country name mismatch

    - Invalid characters in SWIFT code

    - Duplicate code (409)

    - Too short address

- DELETE /v1/swift-codes/{swiftCode}:

    - Delete branch

    - Delete HQ and orphan branches

    - Delete non-existing code (404)

#### 📦 Frameworks used:

- JUnit 5

- Spring Boot Test

- MockMvc

> The project uses H2 in-memory database for testing (no Docker or PostgreSQL required).

---
### 🖼️ Application Screenshots
#### Example adding a headquarter

![](https://i.imgur.com/fwP5RE0.png)

#### Example adding a branch to the headquarter

![](https://i.imgur.com/QjwgXGi.png)

#### get this code via localhost/v1/swift-codes

![](https://i.imgur.com/E4P1A5R.png)

#### deleting a headquarter
![](https://i.imgur.com/bkys9Mu.png)

#### getting list of banks from one country by code
![](https://i.imgur.com/01yy8b0.png)


---



### 🚠 File Structure

```
├── src/
│   ├── main/java/...      # Logika aplikacji
│   ├── test/java/...      # Testy jednostkowe/integracyjne
│   └── resources/
│       ├── application.properties
│       ├── application-test.properties
│       └── test.csv       # Przykładowe dane
├── db-init.sql            # Skrypt inicjalizujący bazę danych
├── .env                   # Konfiguracja środowiskowa (NIE dodawaj do Git)
├── .env.example           # Przykład - dołączony w repo
├── Dockerfile             # Build JAR w kontenerze
├── docker-compose.yml     # Usługi: app + db
└── README.md              # Ta instrukcja
```

---



Each user should create their own .env locally or in a CI/CD environment.
### 🤖 AI Support

During this project, I used ChatGPT to help with:

- refining validation logic,

- writing tests and describing endpoints,

- creating documentation (README).

The entire project was built and understood by me – ChatGPT served as an assistant.
