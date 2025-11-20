<?php
// admin/add_patient_save.php
// Purpose: POST handler to create a patient (admin only). Debug-friendly (temporarily shows errors).
if (session_status() === PHP_SESSION_NONE && !headers_sent()) session_start();

// Debug mode: set to false once stable
$DEBUG = true;
if ($DEBUG) {
    ini_set('display_errors', 1);
    ini_set('display_startup_errors', 1);
    error_reporting(E_ALL);
}

require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');

require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    flash_set('error','Invalid request method.');
    header('Location: add_patient.php');
    exit;
}

// CSRF
if (!function_exists('validate_csrf') || !validate_csrf($_POST['csrf_token'] ?? '')) {
    flash_set('error','Invalid CSRF token.');
    header('Location: add_patient.php');
    exit;
}

// collect & sanitize
$username  = trim($_POST['username'] ?? '');
$full_name = trim($_POST['full_name'] ?? '');
$email     = trim($_POST['email'] ?? '');
$contact   = trim($_POST['contact'] ?? '');
$password  = $_POST['password'] ?? '';

$age       = isset($_POST['age']) && $_POST['age'] !== '' ? intval($_POST['age']) : null;
$blood_grp = trim($_POST['blood_group'] ?? null);

// minimal validation
$errs = [];
if ($username === '') $errs[] = 'Username is required.';
if ($full_name === '') $errs[] = 'Full name is required.';
if ($email === '') $errs[] = 'Email is required.';
if ($contact === '') $errs[] = 'Contact is required.';
if ($password === '') $errs[] = 'Password is required.';

if (!empty($errs)) {
    foreach ($errs as $e) flash_set('error', $e);
    header('Location: add_patient.php');
    exit;
}

try {
    // Check duplicates
    $chk = $pdo->prepare("SELECT id FROM users WHERE username = ? OR email = ? LIMIT 1");
    $chk->execute([$username, $email]);
    if ($chk->fetch()) {
        flash_set('error', 'A user with that username or email already exists.');
        header('Location: add_patient.php');
        exit;
    }

    $pdo->beginTransaction();

    $hash = password_hash($password, PASSWORD_DEFAULT);

    $ins = $pdo->prepare("INSERT INTO users (role, username, full_name, email, contact, password_hash, status, created_at, created_by) VALUES ('patient', ?, ?, ?, ?, ?, 'active', NOW(), ?)");
    $ins->execute([$username, $full_name, $email, $contact, $hash, current_user_id()]);
    $uid = $pdo->lastInsertId();

    // Insert into patients_profiles - be defensive: list columns we know exist
    // If your patients_profiles table has additional NOT NULL columns without defaults, this may fail.
    $pp = $pdo->prepare("INSERT INTO patients_profiles (user_id, age, blood_group) VALUES (?, ?, ?)");
    $pp->execute([$uid, $age, $blood_grp]);

    // Log
    $lg = $pdo->prepare("INSERT INTO logs (actor_id, actor_role, event_type, detail, created_at) VALUES (?, ?, ?, ?, NOW())");
    $lg->execute([current_user_id(), 'admin', 'create_patient', "patient_id={$uid}"]);

    // Notification (optional)
    if (function_exists('create_notification')) {
        create_notification($uid, 'Account created', 'Your patient account was created by admin.');
    }

    $pdo->commit();

    flash_set('success', 'Patient created successfully (ID: ' . intval($uid) . ').');
    header('Location: manage_patients.php');
    exit;

} catch (Throwable $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    // Log error to Apache error log and flash message
    error_log("add_patient_save error: " . $e->getMessage());
    if ($DEBUG) {
        // show the error on page so you don't get a blank page
        echo "<h2>Debug error</h2><pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
        echo "<p><a href='add_patient.php'>Back</a></p>";
    } else {
        flash_set('error', 'Failed to create patient. See logs.');
        header('Location: add_patient.php');
    }
    exit;
}
