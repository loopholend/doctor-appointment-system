-- ============================================================
-- Full migration: test -> medical_appointment_system
-- ============================================================

-- Step 1: Create new database
CREATE DATABASE IF NOT EXISTS medical_appointment_system
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Step 2: Move and rename all tables across databases in one atomic statement
RENAME TABLE
  test.patient                  TO medical_appointment_system.patients,
  test.doctors                  TO medical_appointment_system.doctor_accounts,
  test.doctor_info              TO medical_appointment_system.doctor_profiles,
  test.medical_details          TO medical_appointment_system.patient_medical_records,
  test.appointments             TO medical_appointment_system.appointments,
  test.doctor_unavailable_dates TO medical_appointment_system.doctor_unavailable_dates,
  test.cancelled_dates          TO medical_appointment_system.cancelled_dates;

USE medical_appointment_system;

-- Step 3: patients (was: patient)
ALTER TABLE patients
  CHANGE COLUMN `name`     full_name      VARCHAR(100),
  CHANGE COLUMN CN         contact_number VARCHAR(15),
  CHANGE COLUMN email      email_address  VARCHAR(100),
  CHANGE COLUMN `password` password_hash  VARCHAR(255);

-- Step 4: doctor_accounts (was: doctors)
ALTER TABLE doctor_accounts
  CHANGE COLUMN `name`     full_name      VARCHAR(100),
  CHANGE COLUMN cn         contact_number VARCHAR(15),
  CHANGE COLUMN email      email_address  VARCHAR(100),
  CHANGE COLUMN `password` password_hash  VARCHAR(255);

-- Step 5: doctor_profiles (was: doctor_info)
ALTER TABLE doctor_profiles
  CHANGE COLUMN email                  email_address         VARCHAR(100),
  CHANGE COLUMN primary_speciality     primary_specialty     VARCHAR(100),
  CHANGE COLUMN secondary_speciality   secondary_specialty   VARCHAR(100),
  CHANGE COLUMN clinic_visit_time_slot clinic_visit_schedule VARCHAR(50),
  CHANGE COLUMN bio                    professional_bio      TEXT,
  CHANGE COLUMN approved_by           approved_by_admin     VARCHAR(100);

-- Step 6: patient_medical_records (was: medical_details)
ALTER TABLE patient_medical_records
  CHANGE COLUMN medical_id       record_id                INT AUTO_INCREMENT,
  CHANGE COLUMN patient_username patient_account_username VARCHAR(50),
  CHANGE COLUMN blood_group      blood_type               VARCHAR(10),
  CHANGE COLUMN diabetes         diabetes_status          VARCHAR(20),
  CHANGE COLUMN thyroid          thyroid_status           VARCHAR(30),
  CHANGE COLUMN bp               blood_pressure_status    VARCHAR(30),
  CHANGE COLUMN asthma           has_asthma               ENUM('yes','no'),
  CHANGE COLUMN allergies        has_allergies            ENUM('yes','no'),
  CHANGE COLUMN surgeries        has_previous_surgeries   ENUM('yes','no');

-- Step 7: appointments — rename patient_username and status columns
ALTER TABLE appointments
  CHANGE COLUMN patient_username patient_account_username VARCHAR(50),
  CHANGE COLUMN `status`         appointment_status       ENUM('booked','cancelled');

-- ============================================================
-- Verification
-- ============================================================
SELECT 'Migration complete!' AS result;
SHOW TABLES;
DESCRIBE patients;
DESCRIBE doctor_accounts;
DESCRIBE doctor_profiles;
DESCRIBE patient_medical_records;
DESCRIBE appointments;
