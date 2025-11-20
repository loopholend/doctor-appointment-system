<?php
// admin/appointments_create_ajax_slots.php
header('Content-Type: application/json');
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

$doctor_id = intval($_GET['doctor_id'] ?? 0);
$date = $_GET['date'] ?? '';
if ($doctor_id<=0 || !preg_match('/^\d{4}-\d{2}-\d{2}$/',$date)) { echo json_encode(['error'=>'Invalid input']); exit; }

// read doctor's weekly availability and day-offs and generate slots
$day = date('D', strtotime($date)); // Mon, Tue
$map = ['Mon'=>'mon','Tue'=>'tue','Wed'=>'wed','Thu'=>'thu','Fri'=>'fri','Sat'=>'sat','Sun'=>'sun'];
$dow = $map[$day] ?? null;

// 1) check dayoff
$doff = $pdo->prepare("SELECT id FROM doctor_dayoffs WHERE doctor_id=? AND date=? LIMIT 1");
$doff->execute([$doctor_id,$date]);
if ($doff->fetch()) { echo json_encode(['slots'=>[]]); exit; }

// 2) get available ranges (weekly)
$avail = $pdo->prepare("SELECT start_time,end_time FROM doctor_available_times WHERE doctor_id=? AND day_of_week=? AND repeat_weekly=1");
$avail->execute([$doctor_id,$dow]);
$ranges = $avail->fetchAll();
if (empty($ranges)) { echo json_encode(['slots'=>[]]); exit; }

// 3) get booked slots
$booked = $pdo->prepare("SELECT start_time FROM appointments WHERE doctor_id=? AND date=? AND status='booked'");
$booked->execute([$doctor_id,$date]);
$bookedArr = array_column($booked->fetchAll(),'start_time');

// 4) generate 15-min slots
$slots=[];
foreach($ranges as $rg){
    $start = strtotime($date . ' ' . $rg['start_time']);
    $end = strtotime($date . ' ' . $rg['end_time']);
    for($t=$start;$t+15*60<=$end;$t+=15*60){
        $s = date('H:i', $t);
        if (!in_array($s,$bookedArr)){
            $slots[]=['start'=>$s,'end'=>date('H:i',$t+15*60)];
        }
    }
}
echo json_encode(['slots'=>$slots]);
