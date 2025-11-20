<?php
// admin/add_doctor_save.php
if (session_status() === PHP_SESSION_NONE && !headers_sent()) session_start();
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    flash_set('error','Invalid request');
    header('Location: add_doctor.php'); exit;
}
if (!validate_csrf($_POST['csrf_token'] ?? '')) {
    flash_set('error','Invalid CSRF'); header('Location: add_doctor.php'); exit;
}

// basic fields
$username = trim($_POST['username'] ?? '');
$full_name = trim($_POST['full_name'] ?? '');
$email = trim($_POST['email'] ?? '');
$contact = trim($_POST['contact'] ?? '');
$password = $_POST['password'] ?? '';
$license_no = trim($_POST['license_no'] ?? '');
$experience = intval($_POST['experience'] ?? 0);
$specialty = trim($_POST['specialty'] ?? '');
$fee = floatval($_POST['fee'] ?? 0);
$bio = trim($_POST['bio'] ?? '');
$created_by = current_user_id();

if ($username === '' || $full_name === '' || $email === '' || $password === '') {
    flash_set('error','Missing required fields'); header('Location:add_doctor.php'); exit;
}

// image upload (optional)
$upload_dir = __DIR__ . '/../uploads/';
if (!is_dir($upload_dir)) mkdir($upload_dir,0755,true);
$image_path = null;
if (!empty($_FILES['image']['name'])) {
    $f = $_FILES['image'];
    $allowed = ['image/jpeg','image/png'];
    if ($f['error'] === 0 && in_array($f['type'],$allowed) && $f['size'] <= 2*1024*1024) {
        $ext = ($f['type']==='image/png') ? '.png' : '.jpg';
        $fname = uniqid('drimg_').$ext;
        move_uploaded_file($f['tmp_name'], $upload_dir.$fname);
        $image_path = 'uploads/'.$fname;
    }
}

// check duplicates
$chk = $pdo->prepare("SELECT id FROM users WHERE username=? OR email=? LIMIT 1");
$chk->execute([$username,$email]);
if ($chk->fetch()) { flash_set('error','Doctor username or email exists'); header('Location:add_doctor.php'); exit; }

try {
    $pdo->beginTransaction();

    $hash = password_hash($password,PASSWORD_DEFAULT);
    $ins = $pdo->prepare("INSERT INTO users (role,username,full_name,email,contact,password_hash,status,created_at,created_by) VALUES ('doctor',?,?,?,?,?,'active',NOW(),?)");
    // admin-created doctors are approved immediately
    $ins->execute([$username,$full_name,$email,$contact,$hash,$created_by]);
    $uid = $pdo->lastInsertId();

    $dp = $pdo->prepare("INSERT INTO doctors_profiles (user_id, license_no, experience, specialty, fee, bio, image, status, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, 'approved', ?)");
    $dp->execute([$uid, $license_no, $experience, $specialty, $fee, $bio, $image_path, $created_by]);

    // Insert weekly availability
    for ($d=0;$d<=6;$d++) {
        $enabled = isset($_POST["day_{$d}_enabled"]);
        $start = $_POST["day_{$d}_start"] ?? '';
        $end = $_POST["day_{$d}_end"] ?? '';
        if ($enabled && $start !== '' && $end !== '') {
            // validate time format (HH:MM)
            $stmt = $pdo->prepare("INSERT INTO doctor_available_times (doctor_id, day_of_week, date_specific, start_time, end_time, repeat_weekly, created_by) VALUES (?, ?, NULL, ?, ?, 1, ?)");
            $stmt->execute([$uid, $d, $start, $end, $created_by]);
        }
    }

    $pdo->prepare("INSERT INTO logs (actor_id,actor_role,event_type,detail,created_at) VALUES (?, 'admin', 'create_doctor', ?, NOW())")
        ->execute([$created_by, "doctor_id={$uid}"]);

    $pdo->commit();

    flash_set('success','Doctor created and availability saved.');
    header('Location: manage_doctors.php'); exit;

} catch (Throwable $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    error_log("add_doctor_save error: ".$e->getMessage());
    flash_set('error','Failed: '.$e->getMessage());
    header('Location:add_doctor.php'); exit;
}
