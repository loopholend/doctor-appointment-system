<?php
// patient/book_action.php
header('Content-Type: application/json');
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('patient');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/csrf.php';
require_once __DIR__ . '/../inc/functions.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') { echo json_encode(['error'=>'POST required']); exit; }
if (!validate_csrf($_POST['csrf_token'] ?? '')) { echo json_encode(['error'=>'CSRF']); exit; }

$patient_id = current_user_id();
$doctor_id = intval($_POST['doctor_id'] ?? 0);
$date = $_POST['date'] ?? '';
$start = $_POST['start_time'] ?? '';
$end = $_POST['end_time'] ?? '';

if (!$doctor_id || !$date || !$start || !$end) { echo json_encode(['error'=>'Missing fields']); exit; }

// normalize times to H:i:s
$start_ts = date('H:i:s', strtotime($start));
$end_ts = date('H:i:s', strtotime($end));

try {
  // transaction for concurrency
  $pdo->beginTransaction();

  // lock any existing appointment rows for that doctor/date/start to prevent race
  $q = $pdo->prepare("SELECT COUNT(*) as cnt FROM appointments WHERE doctor_id=? AND date=? AND start_time = ? FOR UPDATE");
  $q->execute([$doctor_id,$date,$start_ts]);
  $row = $q->fetch();
  if ($row['cnt'] > 0) {
    $pdo->rollBack();
    echo json_encode(['error'=>'Slot already booked']);
    exit;
  }

  // insert appointment
  $ins = $pdo->prepare("INSERT INTO appointments (patient_id, doctor_id, date, start_time, end_time, status, created_at, created_by) VALUES (?,?,?,?,?,'booked',NOW(),?)");
  $ins->execute([$patient_id,$doctor_id,$date,$start_ts,$end_ts,'patient']);

  // create notification for doctor (simple)
  $nid = $pdo->lastInsertId();
  $note = $pdo->prepare("INSERT INTO notifications (user_id,title,message,created_at) VALUES (?,?,?,NOW())");
  $note->execute([$doctor_id,'New appointment','Patient booked an appointment on '.$date.' at '.$start.' .']);

  $pdo->commit();
  echo json_encode(['ok'=>true]);
} catch (Exception $e) {
  if ($pdo->inTransaction()) $pdo->rollBack();
  echo json_encode(['error'=>'Server error: '.$e->getMessage()]);
}
