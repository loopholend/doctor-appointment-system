-- Doctor Appointment System: Complete MySQL migration script
-- Step 1: Check current state
SHOW DATABASES;
SELECT 'Currently in test database' as status;
SHOW TABLES IN test;

-- Step 2: Create new database if it doesn't exist
CREATE DATABASE IF NOT EXISTS medical_appointment_system;

-- Step 3: Move tables if they exist in test database
SET @db_exists := (SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = 'test' AND TABLE_NAME = 'patient');

-- Conditional table rename
RENAME TABLE test.patient TO medical_appointment_system.patients;
RENAME TABLE test.doctors TO medical_appointment_system.doctor_accounts;
RENAME TABLE test.doctor_info TO medical_appointment_system.doctor_profiles;
RENAME TABLE test.medical_details TO medical_appointment_system.patient_medical_records;
RENAME TABLE test.appointments TO medical_appointment_system.appointments;

-- Step 4: Rename columns in each table
USE medical_appointment_system;

ALTER TABLE patients
  CHANGE COLUMN name full_name VARCHAR(100),
  CHANGE COLUMN CN contact_number VARCHAR(15),
  CHANGE COLUMN email email_address VARCHAR(100),
  CHANGE COLUMN password password_hash VARCHAR(255);

ALTER TABLE doctor_accounts
  CHANGE COLUMN name full_name VARCHAR(100),
  CHANGE COLUMN CN contact_number VARCHAR(15),
  CHANGE COLUMN email email_address VARCHAR(100),
  CHANGE COLUMN password password_hash VARCHAR(255);

ALTER TABLE doctor_profiles
  CHANGE COLUMN primary_speciality primary_specialty VARCHAR(100),
  CHANGE COLUMN secondary_speciality secondary_specialty VARCHAR(100),
  CHANGE COLUMN clinic_visit_time_slot clinic_visit_schedule VARCHAR(100),
  CHANGE COLUMN about_doctor professional_bio TEXT,
  CHANGE COLUMN approved_by approved_by_admin VARCHAR(100);

ALTER TABLE patient_medical_records
  CHANGE COLUMN medical_id record_id INT,
  CHANGE COLUMN patient_username patient_account_username VARCHAR(50),
  CHANGE COLUMN blood_group blood_type VARCHAR(10),
  CHANGE COLUMN diabetes diabetes_status VARCHAR(20),
  CHANGE COLUMN thyroid thyroid_status VARCHAR(20),
  CHANGE COLUMN bp blood_pressure_status VARCHAR(20),
  CHANGE COLUMN asthma has_asthma VARCHAR(10),
  CHANGE COLUMN allergies has_allergies VARCHAR(10),
  CHANGE COLUMN surgeries has_previous_surgeries VARCHAR(10);

ALTER TABLE appointments
  CHANGE COLUMN status appointment_status VARCHAR(20);

-- Final verification
SELECT 'Migration complete. Final table structure:' as status;
SHOW TABLES IN medical_appointment_system;
DESCRIBE patients;
DESCRIBE doctor_accounts;
DESCRIBE doctor_profiles;
DESCRIBE patient_medical_records;
DESCRIBE appointments;
