<?php
// C:\xampp\htdocs\doctor-appointment\inc\csrf.php
// Central CSRF helper. Safe to include multiple times because of function_exists guards.

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

if (!function_exists('csrf_token')) {
    /**
     * Return the current CSRF token, generating a new one if missing.
     * Stored in $_SESSION['csrf_token'].
     */
    function csrf_token(): string
    {
        if (empty($_SESSION['csrf_token']) || !is_string($_SESSION['csrf_token'])) {
            // 32 bytes = 64 hex chars
            $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
        }
        return $_SESSION['csrf_token'];
    }
}

if (!function_exists('csrf_field')) {
    /**
     * Return an HTML hidden input for forms.
     */
    function csrf_field(): string
    {
        $token = htmlspecialchars(csrf_token(), ENT_QUOTES, 'UTF-8');
        return '<input type="hidden" name="csrf_token" value="' . $token . '">';
    }
}

if (!function_exists('csrf_validate')) {
    /**
     * Validate CSRF token from user input. Returns boolean.
     */
    function csrf_validate(?string $tokenFromRequest): bool
    {
        if (empty($tokenFromRequest) || !isset($_SESSION['csrf_token'])) {
            return false;
        }
        // Use hash_equals to avoid timing attacks
        return hash_equals((string)$_SESSION['csrf_token'], (string)$tokenFromRequest);
    }
}
