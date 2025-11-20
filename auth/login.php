<?php
// auth/login.php - Minimal login (server-side checks)
session_start();
require_once __DIR__ . '/../inc/db.php';

$role = isset($_GET['role']) ? $_GET['role'] : '';

$error = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $username = trim($_POST['username'] ?? '');
    $password = $_POST['password'] ?? '';
    $role_post = $_POST['role'] ?? '';

    if (!$username || !$password) {
        $error = 'Enter username and password.';
    } else {
        $stmt = $pdo->prepare("SELECT id, username, role, password_hash, status FROM users WHERE username = ? LIMIT 1");
        $stmt->execute([$username]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        if ($user && password_verify($password, $user['password_hash'])) {
            if ($user['status'] !== 'active') {
                $error = 'Account not active. Status: ' . htmlspecialchars($user['status']);
            } elseif ($role_post !== $user['role']) {
                $error = 'Role mismatch. Please choose correct role.';
            } else {
                // success
                session_regenerate_id(true);
                $_SESSION['user_id'] = $user['id'];
                $_SESSION['username'] = $user['username'];
                $_SESSION['role'] = $user['role'];

                // redirect based on role
                if ($user['role'] === 'admin') {
                    header('Location: ../admin/dashboard.php'); exit;
                } elseif ($user['role'] === 'doctor') {
                    header('Location: ../doctor/dashboard.php'); exit;
                } else {
                    header('Location: ../patient/dashboard.php'); exit;
                }
            }
        } else {
            $error = 'Invalid credentials.';
        }
    }
}
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Login - <?php echo htmlspecialchars($role ?: ''); ?></title>
  <link rel="stylesheet" href="../assets/css/style.css">
</head>
<body>
  <div class="center-card">
    <h2>Login <?php echo ($role ? 'as '.htmlspecialchars($role) : ''); ?></h2>
    <?php if ($error): ?><div class="error"><?php echo htmlspecialchars($error); ?></div><?php endif; ?>
    <form method="post" action="">
      <input type="hidden" name="role" value="<?php echo htmlspecialchars($role); ?>">
      <label>Username<br><input name="username" required></label><br>
      <label>Password<br><input name="password" type="password" required></label><br>
      <button type="submit">Login</button>
    </form>
    <p style="margin-top:12px;"><a href="../role-select.php">Back</a></p>
  </div>
</body>
</html>
