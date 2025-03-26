# 🌍 InternSwiftCodesProject

Aplikacja do zarządzania i przeszukiwania kodów SWIFT (BIC) banków z całego świata.  
Umożliwia importowanie danych z plików CSV, filtrowanie, dodawanie i usuwanie wpisów oraz automatyczne łączenie oddziałów z centralą banku.

---
## 💡 Opis zadania

A SWIFT code, also known as a Bank Identifier Code (BIC), is a unique identifier of a bank's branch or headquarter.  
This application was built to:

- ✅ Parsować pliki CSV z danymi SWIFT-owymi
- ✅ Rozpoznawać centrale (`XXX`) i przypisywać im oddziały
- ✅ Normalizować dane (ISO2, country names, formatowanie)
- ✅ Przechowywać dane w bazie PostgreSQL
- ✅ Udostępniać REST API do odczytu i zarządzania

---

## 📦 Technologie

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL (produkcyjnie)
- H2 (do testów)
- Docker + Docker Compose
- Maven
- Apache Commons CSV

---

### Opis endpointów
📤 POST /upload/swift

Opis:
Przesyła plik CSV z danymi SWIFT. Dane są walidowane, parsowane i zapisywane do bazy.

Parametry:

    file (form-data): plik .csv z nagłówkami:
    SWIFT CODE, NAME, ADDRESS, COUNTRY ISO2 CODE, COUNTRY NAME

#### Odpowiedzi:
```http
200 OK – plik poprawnie przetworzony
400 Bad Request – brak pliku, zły format lub błąd parsowania
```
---
🔍 GET /v1/swift-codes/{swiftCode}

Opis:
Zwraca szczegóły kodu SWIFT.
Jeśli to centrala (XXX), zwraca też powiązane oddziały.

Parametry:

    {swiftCode} – kod SWIFT (8–11 znaków)
#### Odpowiedzi:
```http
200 OK – dane znalezione
400 Bad Request – błędny format kodu
404 Not Found – nie znaleziono
```

Przykład odpowiedzi:

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
🌍 GET /v1/swift-codes/country/{countryISO2}

Opis:
Zwraca wszystkie kody SWIFT z danego kraju.

Parametry:

    {countryISO2} – dwuliterowy kod kraju (np. PL, US)

#### Odpowiedzi:
```http
200 OK – lista kodów
400 Bad Request – niepoprawny kod ISO2
404 Not Found – brak danych

```
---
➕ POST /v1/swift-codes

Opis:
Dodaje nowy kod SWIFT do bazy.
Automatycznie łączy oddziały z centralą (jeśli istnieje), lub centralę z sierotami.

```json
{
  "swiftCode": "AAAABBCCXXX",
  "bankName": "Bank A",
  "address": "Main Street 1",
  "countryISO2": "PL",
  "countryName": "POLAND"
}
```
#### Odpowiedzi
```http 
200 OK – dodano rekord
400 Bad Request – błąd walidacji
409 Conflict – kod już istnieje

```
---
❌ DELETE /v1/swift-codes/{swiftCode}

Opis:
Usuwa kod SWIFT z bazy.
Jeśli to centrala, oddziały zostają „osierocone”.

Parametry:

    {swiftCode} – kod do usunięcia

#### Odpowiedzi:
```http 
200 OK – usunięto poprawnie
404 Not Found – brak kodu w bazie
500 Internal Server Error – błąd podczas usuwania


```
---
### 🧪 Walidacja danych wejściowych

✅ SWIFT Code

   - Musi mieć od 8 do 11 znaków

    Dozwolone znaki: litery A-Z oraz cyfry 0–9

    Kody kończące się na XXX są traktowane jako centrala banku (headquarter)

    Pozostałe traktowane są jako oddziały (branch)

    Branch może być zapisany bez centrali, ale zostanie automatycznie powiązany z centralą, jeśli taka istnieje

✅ Kraj (ISO2 + nazwa)

    Kod kraju (countryISO2) musi być dokładnie 2-literowym kodem ISO2, np. PL, US

    Nazwa kraju (countryName) musi pasować do kodu ISO2 (np. PL → POLAND)

    Wszelkie dane dotyczące kraju są automatycznie konwertowane do wielkich liter

✅ Adres (address)

    Pole wymagane

    Długość: od 3 do 500 znaków

    Jeśli adres nie zostanie podany, zostanie przypisana wartość "No address provided"

✅ Nazwa banku (bankName)

    Pole wymagane

    Nie może być puste





---

### ✅ Instrukcja uruchomienia projektu – InternSwiftCodesProject

#### 📦 Wymagania
- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Git

---

### 🚀 Szybki start

#### 1. 🔁 Sklonuj repozytorium
```bash
git clone https://github.com/LawyerN/InterbSwiftCodesProject.git
cd InternSwiftCodesProject
```

#### 2. 📄 Utwórz plik `.env`

Na podstawie przykładowego pliku `.env.example`:

```bash
cp .env.example .env
```

Możesz edytować dane logowania do bazy:
```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=haslo123
```

---

### Dodaj plik z danymi CSV
Umieść swój plik .csv w katalogu
````bash
src/main/resources/data/
````
Wymagania dla pliku CSV:

- Nazwa dowolna, musi kończyć się na .csv

- Nagłówki muszą zawierać:
```bash
SWIFT CODE, NAME, ADDRESS, COUNTRY ISO2 CODE, COUNTRY NAME
- ```
- Dane zostaną automatycznie zaimportowane przy starcie aplikacji



### 🐳 Uruchamianie przez Docker

#### 1. Wyczyść poprzednie buildy(jest to konieczne, żeby potem zadziałało polecenie z punktu 2)
```bash
mvn clean
```

#### 2. Zbuduj i uruchom kontenery

```bash
docker-compose up --build
```

- Aplikacja będzie dostępna pod: [http://localhost:8080](http://localhost:8080)
- PostgreSQL nasłuchuje na porcie: `5432`

---

### 🥪 Uruchamianie testów

Lokalnie:
```bash
./mvnw test
```

> Projekt używa bazy **H2 in-memory** do testów (nie wymaga Dockera ani PostgreSQL).

---



### 🚠 Struktura plików

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



Każdy użytkownik powinien sam stworzyć `.env` lokalnie lub w środowisku CI/CD.

### 🤖 Wsparcie AI

Podczas pracy nad tym projektem wspierałem się ChatGPT – m.in. przy:

- dopracowywaniu logiki walidacji,
- pisaniu testów i opisie endpointów,
- tworzeniu dokumentacji (README).

Całość projektu została zbudowana i zrozumiana przeze mnie – ChatGPT służył jako asystent

