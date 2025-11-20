<?php
// admin/delete_doctor.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';
if ($_SERVER['REQUEST_METHOD'] !== 'POST') { header('Location: manage_doctors.php'); exit; }
if (!validate_csrf($_POST['csrf_token'] ?? '')) { flash_set('error','CSRF'); header('Location: manage_doctors.php'); exit; }

$id = intval($_POST['id'] ?? 0);
if ($id<=0) { flash_set('error','Invalid id'); header('Location: manage_doctors.php'); exit; }

$pdo->beginTransaction();
try {
    $pdo->prepare("DELETE FROM doctors_profiles WHERE user_id=?")->execute([$id]);
    $pdo->prepare("DELETE FROM appointments WHERE doctor_id=?")->execute([$id]);
    $pdo->prepare("DELETE FROM users WHERE id=? AND role='doctor'")->execute([$id]);
    $pdo->prepare("INSERT INTO logs (actor_id,actor_role,event_type,detail,created_at) VALUES (?,?,?,?,NOW())")
        ->execute([current_user_id(),'admin','delete_doctor',"doctor_id={$id}"]);
    $pdo->commit();
    flash_set('success','Doctor deleted');
} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    flash_set('error','Error deleting doctor: '.$e->getMessage());
}
header('Location: manage_doctors.php'); exit;
