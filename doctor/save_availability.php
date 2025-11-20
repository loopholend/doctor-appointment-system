<?php
header('Content-Type: application/json');
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('doctor');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/csrf.php';

$data = $_POST;
if (!validate_csrf($data['csrf_token'] ?? '')) {
    echo json_encode(['error'=>'Invalid CSRF']); exit;
}

$doctor_id = current_user_id();
$repeat_weekly = isset($data['repeat_weekly']) ? intval($data['repeat_weekly']) : 1;
$day_of_week = intval($data['day_of_week'] ?? 1);
$date_for = $data['date_for'] ?: null;
$start_time = $data['start_time'] ?? '';
$end_time = $data['end_time'] ?? '';

if (!$start_time || !$end_time) {
    echo json_encode(['error'=>'Start and end required']); exit;
}

try {
    $stmt = $pdo->prepare("INSERT INTO doctor_available_times (doctor_id, day_of_week, date_for, start_time, end_time, repeat_weekly, created_at) VALUES (?,?,?,?,?,?,NOW())");
    $stmt->execute([$doctor_id, $day_of_week, $date_for, $start_time, $end_time, $repeat_weekly]);
    echo json_encode(['success'=>true, 'id'=>$pdo->lastInsertId()]);
} catch (Exception $e) {
    echo json_encode(['error'=>'Server error: '.$e->getMessage()]);
}
