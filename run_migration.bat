@echo off
echo ===================================================
echo  Doctor Appointment System - MySQL Migration
echo  (All data is preserved - RENAME TABLE moves data)
echo ===================================================

set MYSQL=mysql -u root -proot --force

echo.
echo [1/7] Creating database medical_appointment_system...
%MYSQL% -e "CREATE DATABASE IF NOT EXISTS medical_appointment_system;"

echo.
echo [2/7] Row counts in OLD 'test' database (before migration):
%MYSQL% test -e "SELECT 'patient' as tbl, COUNT(*) as rows FROM patient UNION ALL SELECT 'doctors', COUNT(*) FROM doctors UNION ALL SELECT 'doctor_info', COUNT(*) FROM doctor_info UNION ALL SELECT 'medical_details', COUNT(*) FROM medical_details UNION ALL SELECT 'appointments', COUNT(*) FROM appointments;" 2>nul

echo.
echo [3/7] Moving tables WITH ALL DATA from 'test' to 'medical_appointment_system'...
echo   (RENAME TABLE across databases moves the entire table including all rows)
%MYSQL% -e "RENAME TABLE test.patient TO medical_appointment_system.patients;" 2>nul
%MYSQL% -e "RENAME TABLE test.doctors TO medical_appointment_system.doctor_accounts;" 2>nul
%MYSQL% -e "RENAME TABLE test.doctor_info TO medical_appointment_system.doctor_profiles;" 2>nul
%MYSQL% -e "RENAME TABLE test.medical_details TO medical_appointment_system.patient_medical_records;" 2>nul
%MYSQL% -e "RENAME TABLE test.appointments TO medical_appointment_system.appointments;" 2>nul

echo.
echo [4/7] Renaming tables already inside medical_appointment_system (if needed)...
%MYSQL% medical_appointment_system -e "RENAME TABLE patient TO patients;" 2>nul
%MYSQL% medical_appointment_system -e "RENAME TABLE doctors TO doctor_accounts;" 2>nul
%MYSQL% medical_appointment_system -e "RENAME TABLE doctor_info TO doctor_profiles;" 2>nul
%MYSQL% medical_appointment_system -e "RENAME TABLE medical_details TO patient_medical_records;" 2>nul

echo.
echo [5/7] Renaming columns in patients and doctor_accounts...
%MYSQL% medical_appointment_system -e "ALTER TABLE patients CHANGE COLUMN name full_name VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patients CHANGE COLUMN CN contact_number VARCHAR(15);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patients CHANGE COLUMN email email_address VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patients CHANGE COLUMN password password_hash VARCHAR(255);" 2>nul

%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_accounts CHANGE COLUMN name full_name VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_accounts CHANGE COLUMN CN contact_number VARCHAR(15);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_accounts CHANGE COLUMN email email_address VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_accounts CHANGE COLUMN password password_hash VARCHAR(255);" 2>nul

echo.
echo [6/7] Renaming columns in doctor_profiles, patient_medical_records, appointments...
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_profiles CHANGE COLUMN primary_speciality primary_specialty VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_profiles CHANGE COLUMN secondary_speciality secondary_specialty VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_profiles CHANGE COLUMN clinic_visit_time_slot clinic_visit_schedule VARCHAR(100);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_profiles CHANGE COLUMN about_doctor professional_bio TEXT;" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE doctor_profiles CHANGE COLUMN approved_by approved_by_admin VARCHAR(100);" 2>nul

%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN medical_id record_id INT;" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN patient_username patient_account_username VARCHAR(50);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN blood_group blood_type VARCHAR(10);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN diabetes diabetes_status VARCHAR(20);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN thyroid thyroid_status VARCHAR(20);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN bp blood_pressure_status VARCHAR(20);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN asthma has_asthma VARCHAR(10);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN allergies has_allergies VARCHAR(10);" 2>nul
%MYSQL% medical_appointment_system -e "ALTER TABLE patient_medical_records CHANGE COLUMN surgeries has_previous_surgeries VARCHAR(10);" 2>nul

%MYSQL% medical_appointment_system -e "ALTER TABLE appointments CHANGE COLUMN status appointment_status VARCHAR(20);" 2>nul

echo.
echo [7/7] VERIFICATION - Row counts in NEW 'medical_appointment_system' (should match above):
%MYSQL% medical_appointment_system -e "SELECT 'patients' as table_name, COUNT(*) as total_rows FROM patients UNION ALL SELECT 'doctor_accounts', COUNT(*) FROM doctor_accounts UNION ALL SELECT 'doctor_profiles', COUNT(*) FROM doctor_profiles UNION ALL SELECT 'patient_medical_records', COUNT(*) FROM patient_medical_records UNION ALL SELECT 'appointments', COUNT(*) FROM appointments;"

echo.
echo Final table list:
%MYSQL% medical_appointment_system -e "SHOW TABLES;"

echo.
echo ===================================================
echo  Done! Compare row counts above - they should match.
echo  If counts match, ALL your data was preserved.
echo ===================================================
pause
