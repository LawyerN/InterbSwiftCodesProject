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
git clone https://github.com/twoj-login/InternSwiftCodesProject.git
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

### 🐳 Uruchamianie przez Docker

#### 1. Zbuduj i uruchom kontenery

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

### 🔧 Budowanie JAR

Jeśli chcesz zbudować aplikację poza Dockerem:

```bash
./mvnw clean package
```

Gotowy plik `.jar` znajdziesz w `target/app.jar`.

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

### 🛡️ Bezpieczeństwo `.env`

> Nie wrzucaj `.env` do repo!  
W `.gitignore` masz już wpis:

```
.env
```

Każdy użytkownik powinien sam stworzyć `.env` lokalnie lub w środowisku CI/CD.

