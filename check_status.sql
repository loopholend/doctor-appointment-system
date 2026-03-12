-- Step 1: Check if tables exist in 'test' database
SELECT 'Checking test database tables:' as status;
SELECT TABLE_NAME FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'test' 
AND TABLE_NAME IN ('patient', 'doctors', 'doctor_info', 'medical_details', 'appointments');

-- Step 2: Check if medical_appointment_system database exists
SELECT 'Checking medical_appointment_system database:' as status;
SELECT SCHEMA_NAME FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'medical_appointment_system';
