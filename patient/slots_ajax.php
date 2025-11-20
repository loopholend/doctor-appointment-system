<?php
// patient/slots_ajax.php
header('Content-Type: application/json; charset=utf-8');

if (session_status() === PHP_SESSION_NONE) session_start();

require_once __DIR__ . '/../inc/auth_checks.php';
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

// require_role may block when testing in browser; leave it but be aware
require_role('patient');

// testing helper: add &debug=1 to see internals
$debug = isset($_GET['debug']) && $_GET['debug'] == '1';

// ensure server timezone is Kolkata
$tz = new DateTimeZone('Asia/Kolkata');

// input
$doctor_id = intval($_GET['doctor_id'] ?? 0);
$date_str  = $_GET['date'] ?? '';

// basic validation
if (!$doctor_id || !$date_str) {
    echo json_encode(['error' => 'Missing params: doctor_id and date required', 'slots' => []]);
    exit;
}

// strict date parse (YYYY-MM-DD)
$date_obj = DateTimeImmutable::createFromFormat('Y-m-d', $date_str, $tz);
$errors = DateTimeImmutable::getLastErrors();
if ($date_obj === false || $errors['warning_count'] > 0 || $errors['error_count'] > 0) {
    echo json_encode(['error' => 'Invalid date format. Use YYYY-MM-DD', 'slots' => []]);
    exit;
}

// check explicit day-off
$stmt = $pdo->prepare("SELECT COUNT(*) FROM doctor_dayoffs WHERE doctor_id = ? AND date = ?");
$stmt->execute([$doctor_id, $date_str]);
if ($stmt->fetchColumn() > 0) {
    echo json_encode(['error' => 'Doctor is off on this date', 'slots' => []]);
    exit;
}

// day of week 0..6 (Sunday=0)
$dow = (int) $date_obj->format('w');

// load availability: weekly repeating or date-specific
$sql = "SELECT id, doctor_id, day_of_week, date_specific, start_time, end_time, repeat_weekly
        FROM doctor_available_times
        WHERE doctor_id = ?
          AND (
               (repeat_weekly = 1 AND day_of_week = ?)
            OR (repeat_weekly = 0 AND date_specific = ?)
          )
        ORDER BY repeat_weekly DESC, start_time";
$stmt = $pdo->prepare($sql);
$stmt->execute([$doctor_id, $dow, $date_str]);
$rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

// if no availability rows, return empty slots (not an error)
if (empty($rows)) {
    echo json_encode(['slots' => [], 'debug' => $debug ? ['rows' => $rows] : null]);
    exit;
}

// helper to normalize TIME strings (allow "09:00" or "09:00:00")
function normalize_time($t) {
    if ($t === null) return null;
    if (preg_match('/^\d{2}:\d{2}$/', $t)) return $t . ':00';
    return $t;
}

// build 15-minute slots (end exclusive)
$slots = [];
foreach ($rows as $r) {
    $start_time = normalize_time($r['start_time']);
    $end_time   = normalize_time($r['end_time']);
    if (!$start_time || !$end_time) continue;

    // DateTimeImmutable in Kolkata
    $start_dt = DateTimeImmutable::createFromFormat('Y-m-d H:i:s', $date_str . ' ' . $start_time, $tz);
    $end_dt   = DateTimeImmutable::createFromFormat('Y-m-d H:i:s', $date_str . ' ' . $end_time, $tz);
    if ($start_dt === false || $end_dt === false) continue;
    if ($end_dt <= $start_dt) continue;

    $cur = $start_dt;
    while ($cur->add(new DateInterval('PT15M')) <= $end_dt) {
        $s = $cur->format('H:i');
        $e = $cur->add(new DateInterval('PT15M'))->format('H:i');
        $slots[] = ['start' => $s, 'end' => $e];
        $cur = $cur->add(new DateInterval('PT15M'));
    }
}

// get booked appointment start times (normalize to HH:MM)
$stmt = $pdo->prepare("SELECT DISTINCT TIME_FORMAT(start_time, '%H:%i') as st FROM appointments WHERE doctor_id = ? AND date = ? AND status = 'booked'");
$stmt->execute([$doctor_id, $date_str]);
$booked_rows = $stmt->fetchAll(PDO::FETCH_COLUMN, 0);
$booked = array_fill_keys($booked_rows ?: [], true);

// filter out booked slots (comparing start only)
$available = array_values(array_filter($slots, function($s) use ($booked) {
    return !isset($booked[$s['start']]);
}));

// if debug requested, include internal data
$output = ['slots' => $available];
if ($debug) {
    $output['debug'] = [
        'requested_date' => $date_str,
        'dow' => $dow,
        'availability_rows' => $rows,
        'all_generated_slots_count' => count($slots),
        'all_generated_slots_sample' => array_slice($slots, 0, 10),
        'booked' => array_values($booked_rows),
        'available_count' => count($available),
    ];
}

echo json_encode($output);
exit;
