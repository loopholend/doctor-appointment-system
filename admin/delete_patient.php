<?php
// admin/delete_patient.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';
if ($_SERVER['REQUEST_METHOD'] !== 'POST') { header('Location: manage_patients.php'); exit; }
if (!validate_csrf($_POST['csrf_token'] ?? '')) { flash_set('error','CSRF'); header('Location: manage_patients.php'); exit; }
$id = intval($_POST['id'] ?? 0);
if ($id<=0) { flash_set('error','Invalid'); header('Location: manage_patients.php'); exit; }
$pdo->beginTransaction();
try {
    $pdo->prepare("DELETE FROM patients_profiles WHERE user_id=?")->execute([$id]);
    $pdo->prepare("DELETE FROM appointments WHERE patient_id=?")->execute([$id]);
    $pdo->prepare("DELETE FROM users WHERE id=? AND role='patient'")->execute([$id]);
    $pdo->prepare("INSERT INTO logs (actor_id,actor_role,event_type,detail,created_at) VALUES (?,?,?,?,NOW())")
        ->execute([current_user_id(),'admin','delete_patient',"patient_id={$id}"]);
    $pdo->commit();
    flash_set('success','Patient deleted');
} catch (Exception $e) {
    if ($pdo->inTransaction()) $pdo->rollBack();
    flash_set('error','Error: '.$e->getMessage());
}
header('Location: manage_patients.php'); exit;
