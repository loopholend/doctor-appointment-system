<?php
// inc/db.php - PDO connection used by all scripts
date_default_timezone_set('Asia/Kolkata');

$DB_HOST = '127.0.0.1';
$DB_NAME = 'doctor_appointment';
$DB_USER = 'root';
$DB_PASS = ''; // XAMPP default; change if you set a root password.

$dsn = "mysql:host=$DB_HOST;dbname=$DB_NAME;charset=utf8mb4";
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES => false,
];

try {
    $pdo = new PDO($dsn, $DB_USER, $DB_PASS, $options);
} catch (Exception $e) {
    // Show error on local dev only
    die("DB Connection failed: " . $e->getMessage());
}
