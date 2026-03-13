# 🗄 Database Schema — MediCare+ Doctor Appointment System

**Database:** `medical_appointment_system`  
**Charset:** `utf8mb4`  
**Collation:** `utf8mb4_unicode_ci`  
**Engine:** InnoDB

---

## Quick Setup

Run all commands below in order to create the database and all tables from scratch.

```sql
CREATE DATABASE IF NOT EXISTS medical_appointment_system
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE medical_appointment_system;
```

---

## Tables

### 1. `patients`

Stores patient login credentials and basic contact information.

```sql
CREATE TABLE patients (
  id              INT          NOT NULL AUTO_INCREMENT,
  username        VARCHAR(50)  NOT NULL,
  full_name       VARCHAR(100) DEFAULT NULL,
  contact_number  VARCHAR(15)  DEFAULT NULL,
  email_address   VARCHAR(100) DEFAULT NULL,
  password_hash   VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (username),
  UNIQUE KEY id   (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `id` | INT AUTO_INCREMENT | Unique numeric ID |
| `username` | VARCHAR(50) | **Primary Key** — used for all FK references |
| `full_name` | VARCHAR(100) | Patient's full name |
| `contact_number` | VARCHAR(15) | Phone number |
| `email_address` | VARCHAR(100) | Email |
| `password_hash` | VARCHAR(255) | PBKDF2 hash: `base64(salt):base64(hash)` |

---

### 2. `doctor_accounts`

Stores doctor login credentials. Linked to `doctor_profiles` by `username`.

```sql
CREATE TABLE doctor_accounts (
  id              INT          NOT NULL AUTO_INCREMENT,
  username        VARCHAR(50)  NOT NULL,
  full_name       VARCHAR(100) DEFAULT NULL,
  contact_number  VARCHAR(15)  DEFAULT NULL,
  email_address   VARCHAR(100) DEFAULT NULL,
  password_hash   VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (username),
  UNIQUE KEY id   (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `id` | INT AUTO_INCREMENT | Unique numeric ID |
| `username` | VARCHAR(50) | **Primary Key** |
| `full_name` | VARCHAR(100) | Doctor's full name |
| `contact_number` | VARCHAR(15) | Phone number |
| `email_address` | VARCHAR(100) | Email |
| `password_hash` | VARCHAR(255) | PBKDF2 hash |

---

### 3. `doctor_profiles`

Stores doctor professional details, profile image, and approval status.

```sql
CREATE TABLE doctor_profiles (
  doctor_id              INT            NOT NULL AUTO_INCREMENT,
  username               VARCHAR(50)    DEFAULT NULL,
  email_address          VARCHAR(100)   DEFAULT NULL,
  full_name              VARCHAR(100)   NOT NULL,
  gender                 ENUM('male','female','other') NOT NULL,
  date_of_birth          DATE           NOT NULL,
  medical_license_number VARCHAR(50)    NOT NULL,
  years_of_experience    VARCHAR(20)    NOT NULL,
  primary_specialty      VARCHAR(100)   DEFAULT NULL,
  secondary_specialty    VARCHAR(100)   DEFAULT NULL,
  consultation_fee       DECIMAL(10,2)  NOT NULL,
  clinic_visit_schedule  VARCHAR(50)    DEFAULT NULL,
  professional_bio       TEXT,
  profile_image          MEDIUMBLOB     NOT NULL,
  image_type             VARCHAR(20)    DEFAULT NULL,
  created_at             TIMESTAMP      NULL DEFAULT CURRENT_TIMESTAMP,
  approval_status        VARCHAR(20)    DEFAULT 'pending',
  approved_by_admin      VARCHAR(100)   DEFAULT NULL,
  approval_date          TIMESTAMP      NULL DEFAULT NULL,
  PRIMARY KEY (doctor_id),
  UNIQUE KEY medical_license_number (medical_license_number),
  KEY idx_approval_status (approval_status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `doctor_id` | INT AUTO_INCREMENT | **Primary Key** — used for all FK references |
| `username` | VARCHAR(50) | Links to `doctor_accounts.username` |
| `email_address` | VARCHAR(100) | |
| `full_name` | VARCHAR(100) | |
| `gender` | ENUM | `male` / `female` / `other` |
| `date_of_birth` | DATE | |
| `medical_license_number` | VARCHAR(50) | **Unique** — immutable after creation |
| `years_of_experience` | VARCHAR(20) | e.g. `"8 years"` |
| `primary_specialty` | VARCHAR(100) | e.g. `"Cardiologist"` |
| `secondary_specialty` | VARCHAR(100) | Optional, defaults to `"none"` |
| `consultation_fee` | DECIMAL(10,2) | In ₹ |
| `clinic_visit_schedule` | VARCHAR(50) | e.g. `"10am–2pm"` |
| `professional_bio` | TEXT | |
| `profile_image` | MEDIUMBLOB | Raw image bytes |
| `image_type` | VARCHAR(20) | MIME type e.g. `"image/jpeg"` |
| `created_at` | TIMESTAMP | Auto-set on insert |
| `approval_status` | VARCHAR(20) | `pending` / `approved` / `rejected` |
| `approved_by_admin` | VARCHAR(100) | Admin username who approved |
| `approval_date` | TIMESTAMP | When approved |

---

### 4. `patient_medical_records`

Stores health/medical information submitted by the patient.

```sql
CREATE TABLE patient_medical_records (
  record_id                INT           NOT NULL AUTO_INCREMENT,
  patient_account_username VARCHAR(50)   DEFAULT NULL,
  blood_type               VARCHAR(10)   DEFAULT NULL,
  age                      INT           NOT NULL,
  diabetes_status          VARCHAR(20)   DEFAULT NULL,
  thyroid_status           VARCHAR(30)   DEFAULT NULL,
  blood_pressure_status    VARCHAR(30)   DEFAULT NULL,
  has_asthma               ENUM('yes','no') DEFAULT NULL,
  has_allergies            ENUM('yes','no') DEFAULT NULL,
  has_previous_surgeries   ENUM('yes','no') DEFAULT NULL,
  created_at               TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP
                                              ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (record_id),
  KEY patient_username (patient_account_username)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `record_id` | INT AUTO_INCREMENT | **Primary Key** |
| `patient_account_username` | VARCHAR(50) | FK → `patients.username` |
| `blood_type` | VARCHAR(10) | e.g. `"A+"`, `"O-"` |
| `age` | INT | |
| `diabetes_status` | VARCHAR(20) | e.g. `"Type 2"`, `"None"` |
| `thyroid_status` | VARCHAR(30) | |
| `blood_pressure_status` | VARCHAR(30) | |
| `has_asthma` | ENUM | `yes` / `no` |
| `has_allergies` | ENUM | `yes` / `no` |
| `has_previous_surgeries` | ENUM | `yes` / `no` |
| `created_at` | TIMESTAMP | Auto-set on insert |
| `updated_at` | TIMESTAMP | Auto-updated on any change |

---

### 5. `appointments`

Stores booked appointments between patients and doctors.

```sql
CREATE TABLE appointments (
  appointment_id           INT           NOT NULL AUTO_INCREMENT,
  doctor_id                INT           NOT NULL,
  patient_account_username VARCHAR(50)   DEFAULT NULL,
  appointment_time         VARCHAR(10)   NOT NULL,
  appointment_date         DATE          NOT NULL,
  appointment_status       ENUM('booked','cancelled','completed','no_show') DEFAULT 'booked',
  created_at               TIMESTAMP     NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (appointment_id),
  KEY doctor_id          (doctor_id),
  KEY patient_username   (patient_account_username),
  CONSTRAINT appointments_ibfk_1
    FOREIGN KEY (doctor_id)
    REFERENCES doctor_profiles (doctor_id)
    ON DELETE CASCADE,
  CONSTRAINT appointments_ibfk_2
    FOREIGN KEY (patient_account_username)
    REFERENCES patients (username)
    ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `appointment_id` | INT AUTO_INCREMENT | **Primary Key** |
| `doctor_id` | INT | FK → `doctor_profiles.doctor_id` (CASCADE DELETE) |
| `patient_account_username` | VARCHAR(50) | FK → `patients.username` (CASCADE DELETE) |
| `appointment_time` | VARCHAR(10) | e.g. `"10:30 AM"` |
| `appointment_date` | DATE | |
| `appointment_status` | ENUM | `booked` / `cancelled` / `completed` / `no_show` |
| `created_at` | TIMESTAMP | Auto-set on insert |

---

### 6. `doctor_unavailable_dates`

Tracks dates when a doctor is unavailable (day-off management).

```sql
CREATE TABLE doctor_unavailable_dates (
  id               INT          NOT NULL AUTO_INCREMENT,
  doctor_id        INT          NOT NULL,
  unavailable_date DATE         NOT NULL,
  reason           VARCHAR(100) DEFAULT NULL,
  created_at       TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_doctor_date (doctor_id, unavailable_date),
  CONSTRAINT doctor_unavailable_dates_ibfk_1
    FOREIGN KEY (doctor_id)
    REFERENCES doctor_profiles (doctor_id)
    ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `id` | INT AUTO_INCREMENT | **Primary Key** |
| `doctor_id` | INT | FK → `doctor_profiles.doctor_id` (CASCADE DELETE) |
| `unavailable_date` | DATE | |
| `reason` | VARCHAR(100) | Optional reason e.g. `"Personal"` |
| `created_at` | TIMESTAMP | Auto-set on insert |

> **Unique constraint** on `(doctor_id, unavailable_date)` prevents duplicate entries.

---

### 7. `cancelled_dates`

System-level cancelled dates (e.g. holidays).

```sql
CREATE TABLE cancelled_dates (
  id          INT  NOT NULL AUTO_INCREMENT,
  cancel_date DATE NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

| Column | Type | Notes |
|--------|------|-------|
| `id` | INT AUTO_INCREMENT | **Primary Key** |
| `cancel_date` | DATE | The cancelled date |

---

## Entity Relationships

```
patients ──────────────────────────┐
  │ username (PK)                   │ (FK: patient_account_username)
  │                                 ▼
  ├──► patient_medical_records    appointments
  │      patient_account_username   │
  │                                 │ (FK: doctor_id)
doctor_accounts                     │
  │ username                        ▼
  │                           doctor_profiles
  └──────────────────────────►  doctor_id (PK)
        username                    │
                                    ├──► doctor_unavailable_dates
                                    │      doctor_id
                                    └──► appointments
                                           doctor_id
```

---

## Full Setup Script (Copy & Run)

```sql
-- ============================================================
-- MediCare+ — Full Database Setup
-- ============================================================

CREATE DATABASE IF NOT EXISTS medical_appointment_system
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medical_appointment_system;

-- 1. patients
CREATE TABLE IF NOT EXISTS patients (
  id              INT          NOT NULL AUTO_INCREMENT,
  username        VARCHAR(50)  NOT NULL,
  full_name       VARCHAR(100) DEFAULT NULL,
  contact_number  VARCHAR(15)  DEFAULT NULL,
  email_address   VARCHAR(100) DEFAULT NULL,
  password_hash   VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (username),
  UNIQUE KEY id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. doctor_accounts
CREATE TABLE IF NOT EXISTS doctor_accounts (
  id              INT          NOT NULL AUTO_INCREMENT,
  username        VARCHAR(50)  NOT NULL,
  full_name       VARCHAR(100) DEFAULT NULL,
  contact_number  VARCHAR(15)  DEFAULT NULL,
  email_address   VARCHAR(100) DEFAULT NULL,
  password_hash   VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (username),
  UNIQUE KEY id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. doctor_profiles
CREATE TABLE IF NOT EXISTS doctor_profiles (
  doctor_id              INT            NOT NULL AUTO_INCREMENT,
  username               VARCHAR(50)    DEFAULT NULL,
  email_address          VARCHAR(100)   DEFAULT NULL,
  full_name              VARCHAR(100)   NOT NULL,
  gender                 ENUM('male','female','other') NOT NULL,
  date_of_birth          DATE           NOT NULL,
  medical_license_number VARCHAR(50)    NOT NULL,
  years_of_experience    VARCHAR(20)    NOT NULL,
  primary_specialty      VARCHAR(100)   DEFAULT NULL,
  secondary_specialty    VARCHAR(100)   DEFAULT NULL,
  consultation_fee       DECIMAL(10,2)  NOT NULL,
  clinic_visit_schedule  VARCHAR(50)    DEFAULT NULL,
  professional_bio       TEXT,
  profile_image          MEDIUMBLOB     NOT NULL,
  image_type             VARCHAR(20)    DEFAULT NULL,
  created_at             TIMESTAMP      NULL DEFAULT CURRENT_TIMESTAMP,
  approval_status        VARCHAR(20)    DEFAULT 'pending',
  approved_by_admin      VARCHAR(100)   DEFAULT NULL,
  approval_date          TIMESTAMP      NULL DEFAULT NULL,
  PRIMARY KEY (doctor_id),
  UNIQUE KEY medical_license_number (medical_license_number),
  KEY idx_approval_status (approval_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. patient_medical_records
CREATE TABLE IF NOT EXISTS patient_medical_records (
  record_id                INT              NOT NULL AUTO_INCREMENT,
  patient_account_username VARCHAR(50)      DEFAULT NULL,
  blood_type               VARCHAR(10)      DEFAULT NULL,
  age                      INT              NOT NULL,
  diabetes_status          VARCHAR(20)      DEFAULT NULL,
  thyroid_status           VARCHAR(30)      DEFAULT NULL,
  blood_pressure_status    VARCHAR(30)      DEFAULT NULL,
  has_asthma               ENUM('yes','no') DEFAULT NULL,
  has_allergies            ENUM('yes','no') DEFAULT NULL,
  has_previous_surgeries   ENUM('yes','no') DEFAULT NULL,
  created_at               TIMESTAMP        NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP        NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (record_id),
  KEY patient_username (patient_account_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. appointments
CREATE TABLE IF NOT EXISTS appointments (
  appointment_id           INT                        NOT NULL AUTO_INCREMENT,
  doctor_id                INT                        NOT NULL,
  patient_account_username VARCHAR(50)                DEFAULT NULL,
  appointment_time         VARCHAR(10)                NOT NULL,
  appointment_date         DATE                       NOT NULL,
  appointment_status       ENUM('booked','cancelled','completed','no_show') DEFAULT 'booked',
  created_at               TIMESTAMP                  NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (appointment_id),
  KEY doctor_id        (doctor_id),
  KEY patient_username (patient_account_username),
  CONSTRAINT appointments_ibfk_1 FOREIGN KEY (doctor_id)
    REFERENCES doctor_profiles (doctor_id) ON DELETE CASCADE,
  CONSTRAINT appointments_ibfk_2 FOREIGN KEY (patient_account_username)
    REFERENCES patients (username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. doctor_unavailable_dates
CREATE TABLE IF NOT EXISTS doctor_unavailable_dates (
  id               INT          NOT NULL AUTO_INCREMENT,
  doctor_id        INT          NOT NULL,
  unavailable_date DATE         NOT NULL,
  reason           VARCHAR(100) DEFAULT NULL,
  created_at       TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY unique_doctor_date (doctor_id, unavailable_date),
  CONSTRAINT doctor_unavailable_dates_ibfk_1 FOREIGN KEY (doctor_id)
    REFERENCES doctor_profiles (doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. cancelled_dates
CREATE TABLE IF NOT EXISTS cancelled_dates (
  id          INT  NOT NULL AUTO_INCREMENT,
  cancel_date DATE NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'MediCare+ database setup complete!' AS status;
SHOW TABLES;
```
