<?php
// inc/functions.php
// Common helpers used across the app.
// Saved as UTF-8 without BOM and must be included with require_once.

if (session_status() === PHP_SESSION_NONE) session_start();

function e($s) {
    return htmlspecialchars($s ?? '', ENT_QUOTES | ENT_SUBSTITUTE, 'UTF-8');
}

function get_int($k, $default = 0) {
    return isset($_GET[$k]) ? intval($_GET[$k]) : $default;
}

function paginate_params($total, $per_page, $current_page, $base_url) {
    $pages = max(1, (int)ceil($total / $per_page));
    $current_page = max(1, min($pages, (int)$current_page));
    return [
        'pages' => $pages,
        'current' => $current_page,
        'prev' => $current_page > 1 ? $current_page - 1 : null,
        'next' => $current_page < $pages ? $current_page + 1 : null,
        'base_url' => $base_url
    ];
}

function create_notification($user_id, $title, $message) {
    global $pdo;
    if (!isset($pdo)) return false;
    $stmt = $pdo->prepare("INSERT INTO notifications (user_id, title, message, is_read, created_at) VALUES (?, ?, ?, 0, NOW())");
    return $stmt->execute([$user_id, $title, $message]);
}

if (!function_exists('flash_set')) {
    function flash_set($type, $msg) {
        if (session_status() === PHP_SESSION_NONE) session_start();
        if (!isset($_SESSION['flash_messages'])) $_SESSION['flash_messages'] = [];
        $_SESSION['flash_messages'][] = ['type' => $type, 'msg' => $msg];
    }
}

if (!function_exists('flash_render')) {
    function flash_render() {
        if (session_status() === PHP_SESSION_NONE) session_start();
        if (!empty($_SESSION['flash_messages'])) {
            foreach ($_SESSION['flash_messages'] as $f) {
                $t = $f['type'] ?? 'info';
                $msg = e($f['msg'] ?? '');
                $class = ($t === 'success') ? 'flash-msg success' : (($t === 'error') ? 'flash-msg error' : 'flash-msg info');
                echo "<div class='$class'>$msg</div>";
            }
            unset($_SESSION['flash_messages']);
        }
    }
}

/*
 * CSRF helpers
 * Guarded with function_exists to avoid redeclare fatal errors.
 * Prefer keeping CSRF functions in one file (inc/csrf.php) and removing duplicates,
 * but these guarded helpers are safe if included multiple times.
 */
if (!function_exists('csrf_token')) {
    function csrf_token(): string {
        if (session_status() === PHP_SESSION_NONE) session_start();
        if (empty($_SESSION['_csrf'])) {
            $_SESSION['_csrf'] = bin2hex(random_bytes(32));
        }
        return $_SESSION['_csrf'];
    }
}

if (!function_exists('csrf_field')) {
    function csrf_field(): string {
        return '<input type="hidden" name="_csrf" value="' . htmlspecialchars(csrf_token(), ENT_QUOTES, 'UTF-8') . '">';
    }
}

if (!function_exists('csrf_validate')) {
    function csrf_validate($token): bool {
        if (session_status() === PHP_SESSION_NONE) session_start();
        if (empty($token) || empty($_SESSION['_csrf'])) return false;
        return hash_equals($_SESSION['_csrf'], (string)$token);
    }
}
