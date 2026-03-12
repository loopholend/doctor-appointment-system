# 🏥 MediCare+ — Doctor Appointment System

A full-stack **Java EE web application** for managing doctor appointments, built with Java Servlets on GlassFish 4.1 and MySQL 8.0.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [URL Routes](#-url-routes)
- [Roles & Capabilities](#-roles--capabilities)
- [Database](#-database)
- [Admin Access](#-admin-access)
- [Build & Deploy](#-build--deploy)

---

## ✨ Features

- **Patient Portal** — Register, book/cancel appointments, view medical history
- **Doctor Portal** — Manage profile, view patients, set unavailable dates
- **Admin Panel** — Approve doctors, manage all users, generate reports, add doctors/patients directly
- Role-based session authentication with 30-minute timeout
- Profile image upload (stored as BLOB in DB)
- Medical License Number immutable after doctor registration
- Password hashing via PBKDF2WithHmacSHA256
- SQL injection prevention via prepared statements

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 8 |
| **Server** | GlassFish 4.1 (Servlet 3.1) |
| **Database** | MySQL 8.0 |
| **JDBC Driver** | MySQL Connector/J 9.1.0 |
| **Build Tool** | Apache Ant |
| **IDE** | NetBeans 8.x |
| **Frontend** | HTML5, CSS3 (inline via NavHelper), Google Fonts (Inter/Poppins) |

---

## 📁 Project Structure

```
Doctor-Appointment-System/
├── src/
│   └── java/
│       ├── db.properties                    # DB credentials & admin password
│       ├── DBConnection.java                # JDBC connection utility
│       ├── NavHelper.java                   # Shared navbar/sidebar/CSS/session helpers
│       ├── PasswordUtil.java                # PBKDF2 password hashing
│       ├── LogoutServlet.java               # Shared logout (all roles)
│       ├── patient/                         # Patient servlets (11)
│       ├── doctor/                          # Doctor servlets (10)
│       └── admin/                           # Admin servlets (13)
├── web/
│   ├── index.html                           # Landing page
│   ├── WEB-INF/
│   │   ├── web.xml                          # Servlet mappings & security config
│   │   ├── glassfish-web.xml                # GlassFish context root config
│   │   └── lib/                             # Runtime JARs (mysql-connector, etc.)
│   ├── patient/                             # Patient static HTML pages
│   ├── doctor/                              # Doctor static HTML pages
│   └── admin/                              # Admin static HTML pages
├── build.xml                                # Ant build script
├── nbproject/                               # NetBeans project config
└── DATABASE.md                              # Full DB schema with CREATE TABLE SQL
```

---

## 🚀 Getting Started

### Prerequisites

- Java JDK 8
- GlassFish Server 4.1
- MySQL Server 8.0
- Apache Ant (bundled with NetBeans 8.x)

### 1. Clone the Repository

```bash
git clone https://github.com/loopholend/doctor-appointment-.git
cd doctor-appointment-
```

### 2. Set Up the Database

Create the database and all tables using the schema file:

```bash
mysql -u root -p < DATABASE.md
```

Or copy the SQL from [`DATABASE.md`](DATABASE.md) and run it in MySQL Workbench / CLI.

### 3. Configure Database Credentials

Edit `src/java/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/medical_appointment_system?useSSL=false
db.user=root
db.password=your_password
admin.password=Admin@1234
```

### 4. Add MySQL Connector JAR

Place `mysql-connector-j-9.1.0.jar` in `web/WEB-INF/lib/`.

### 5. Build the WAR

```bash
"C:\Program Files\NetBeans 8.0.2\extide\ant\bin\ant.bat" dist
```

Output: `dist/Doctor-Appointment-System.war`

### 6. Deploy to GlassFish

- Open GlassFish Admin Console: `http://localhost:4848`
- Go to **Applications → Deploy**
- Upload `dist/Doctor-Appointment-System.war`

### 7. Access the App

```
http://localhost:8080/Doctor-Appointment-System/
```

---

## 🔗 URL Routes

### Patient (`/patient/`)

| URL | Purpose |
|-----|---------|
| `/patient/login` | Login page |
| `/patient/register` | Register new account |
| `/patient/dashboard` | View available doctors |
| `/patient/medical-details` | View/edit medical info |
| `/patient/medical-info` | Submit medical info (POST) |
| `/patient/appointments` | View my appointments |
| `/patient/book-appointment` | Book a new appointment |
| `/patient/cancel-appointment` | Cancel appointment |
| `/patient/delete-account` | Delete account |
| `/patient/logout` | Logout |

### Doctor (`/doctor/`)

| URL | Purpose |
|-----|---------|
| `/doctor/login` | Login page |
| `/doctor/register` | Register new account |
| `/doctor/dashboard` | View patients & appointments |
| `/doctor/profile-edit` | Edit professional profile |
| `/doctor/day-off` | View unavailable dates calendar |
| `/doctor/save-day-off` | Mark unavailable dates |
| `/doctor/remove-day-off` | Remove unavailable dates |
| `/doctor/patient-details` | View a patient's medical record |
| `/doctor/delete-account` | Delete account |
| `/doctor/logout` | Logout |

### Admin (`/admin/`)

| URL | Purpose |
|-----|---------|
| `/admin/login` | Admin login |
| `/admin/dashboard` | Admin home (6 quick-access cards) |
| `/admin/doctor-requests` | Review pending doctor registrations |
| `/admin/approve-doctor` | Approve/reject a doctor |
| `/admin/doctors` | List all approved doctors |
| `/admin/add-doctor` | Add a doctor directly (pre-approved) |
| `/admin/patients` | List all patients |
| `/admin/add-patient` | Add a patient directly |
| `/admin/delete-doctor` | Remove a doctor |
| `/admin/delete-patient` | Remove a patient |
| `/admin/report` | Generate system report |
| `/admin/logout` | Logout |

---

## 👥 Roles & Capabilities

### 🧑‍⚕️ Patient
- Self-register with username, full name, contact, email, password
- Browse all approved doctors with specialties and fees
- Book appointments on available dates
- View and cancel appointments
- Submit personal medical info (blood type, age, conditions, allergies, surgeries)
- Delete own account

### 👨‍⚕️ Doctor
- Self-register (pending admin approval)
- Complete professional profile: specialty, license, fee, schedule, bio, profile photo
- Medical License Number is locked after first submission
- Mark unavailable / day-off dates
- View patients' medical records
- Delete own account

### 🔑 Admin
- Password-only login (no DB record required)
- Approve or reject pending doctor registrations
- Add doctors directly (immediately approved, supports profile image)
- Add patients directly
- Delete any doctor or patient account
- View all doctors and patients
- Generate downloadable system report

---

## 🗄 Database

**Database name:** `medical_appointment_system`

**Tables:**

| Table | Description |
|-------|-------------|
| `patients` | Patient accounts |
| `doctor_accounts` | Doctor login credentials |
| `doctor_profiles` | Doctor professional details + profile image |
| `patient_medical_records` | Patient health info |
| `appointments` | Booked appointments |
| `doctor_unavailable_dates` | Doctor day-off/unavailable dates |
| `cancelled_dates` | System-level cancelled dates |

See [`DATABASE.md`](DATABASE.md) for full schema with `CREATE TABLE` statements.

---

## 🔐 Admin Access

Admin login does **not** use the database. Credentials are stored in `db.properties`:

```properties
admin.password=Admin@1234
```

To change the admin password, update `admin.password` in `db.properties` and redeploy.

> **Default admin URL:** `http://localhost:8080/Doctor-Appointment-System/admin/login`

---

## 🔨 Build & Deploy

| Command | Description |
|---------|-------------|
| `ant dist` | Build WAR to `dist/` |
| `ant clean` | Clean build artifacts |
| `ant compile` | Compile Java sources only |

The NetBeans IDE project can also be opened directly — right-click → **Run** to auto-deploy to a configured GlassFish instance.

---

## 🔒 Security Notes

- Passwords hashed with **PBKDF2WithHmacSHA256** (salt + hash stored as `base64(salt):base64(hash)`)
- All DB queries use **PreparedStatement** (no string concatenation)
- Session invalidated on logout; 30-minute idle timeout
- `metadata-complete="true"` in `web.xml` — annotation scanning disabled; all routes declared explicitly
- `useSSL=false` in JDBC URL — **enable SSL for production**

---

## 📄 License

This project is for educational purposes.
