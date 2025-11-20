<?php
// admin/approve_doctor.php - ajax handler for approve/reject
// Robust: captures accidental output so we always return valid JSON.

ob_start(); // capture any accidental output

// ensure a JSON response even if PHP prints warnings later
header('Content-Type: application/json; charset=utf-8');

ini_set('display_errors', 0); // DO NOT echo PHP errors to client
ini_set('display_startup_errors', 0);
error_reporting(E_ALL);

if (session_status() === PHP_SESSION_NONE) session_start();

require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/csrf.php';
require_once __DIR__ . '/../inc/functions.php';

function json_exit($payload, int $http_status = 200) {
    http_response_code($http_status);
    // discard any buffered accidental output (but capture it for debug when needed)
    $extra = ob_get_clean();
    if ($extra !== '') {
        // if debug mode via ?debug=1, include that stray output in response for troubleshooting
        if (isset($_GET['debug']) && $_GET['debug'] == '1') {
            $payload['_debug_output'] = $extra;
        } else {
            // otherwise log the stray output so it doesn't break JSON
            error_log("approve_doctor stray output: " . $extra);
        }
    }
    echo json_encode($payload, JSON_UNESCAPED_UNICODE|JSON_UNESCAPED_SLASHES);
    exit;
}

// read POST data
$id = isset($_POST['id']) ? intval($_POST['id']) : 0;
$action = $_POST['action'] ?? '';
$reason = trim($_POST['reason'] ?? '');
$csrf = $_POST['csrf_token'] ?? '';

// basic validation
if (!$id || !in_array($action, ['approve','reject'])) {
    json_exit(['error' => 'Invalid request parameters'], 400);
}

// validate CSRF - adapt to whichever function your app uses
$valid_csrf = false;
if (function_exists('validate_csrf')) {
    $valid_csrf = validate_csrf($csrf);
} elseif (function_exists('csrf_validate')) {
    $valid_csrf = csrf_validate($csrf);
} elseif (function_exists('csrf_token') && !empty($_SESSION['_csrf'])) {
    $valid_csrf = hash_equals($_SESSION['_csrf'], (string)$csrf);
} else {
    // fallback compare session token
    $valid_csrf = (isset($_SESSION['_csrf']) && hash_equals($_SESSION['_csrf'], (string)$csrf));
}

if (!$valid_csrf) {
    error_log("CSRF failed in admin/approve_doctor.php admin_id={$_SESSION['user_id'] ?? 'unknown'} target={$id}");
    json_exit(['error' => 'Invalid CSRF'], 403);
}

// perform DB changes
try {
    if ($action === 'approve') {
        $stmt = $pdo->prepare("UPDATE doctors_profiles SET status='approved' WHERE user_id=?");
        $stmt->execute([$id]);
        $pdo->prepare("UPDATE users SET status='active' WHERE id=?")->execute([$id]);
        if (function_exists('create_notification')) {
            create_notification($id, "Doctor account approved", "Your doctor registration was approved by admin.");
        }
        json_exit(['success' => true], 200);
    } else {
        $stmt = $pdo->prepare("UPDATE doctors_profiles SET status='rejected' WHERE user_id=?");
        $stmt->execute([$id]);
        $pdo->prepare("UPDATE users SET status='disabled' WHERE id=?")->execute([$id]);
        if (function_exists('create_notification')) {
            create_notification($id, "Doctor account rejected", "Your registration was rejected. Reason: " . ($reason ?: 'No reason provided'));
        }
        json_exit(['success' => true], 200);
    }
} catch (Exception $e) {
    error_log("DB error in admin/approve_doctor.php: " . $e->getMessage());
    json_exit(['error' => 'Server error', 'exception' => (isset($_GET['debug']) && $_GET['debug']=='1') ? $e->getMessage() : null], 500);
}
