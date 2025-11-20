<?php
// admin/requests.php - pending doctor approvals (updated with sidebar)
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$stmt = $pdo->prepare("SELECT u.id,u.username,u.full_name,u.email,u.contact, dp.license_no, dp.specialty, dp.bio FROM users u JOIN doctors_profiles dp ON u.id=dp.user_id WHERE u.role='doctor' AND dp.status='pending'");
$stmt->execute();
$requests = $stmt->fetchAll();

// reliably obtain a CSRF token string for client use
// prefer function names used in your project; try multiple fallbacks
if (function_exists('csrf_token')) {
    $csrf_token = csrf_token();
} elseif (function_exists('get_csrf_token')) {
    $csrf_token = get_csrf_token();
} else {
    // fallback: ensure session and a token in session
    if (session_status() === PHP_SESSION_NONE) session_start();
    if (empty($_SESSION['_csrf'])) $_SESSION['_csrf'] = bin2hex(random_bytes(32));
    $csrf_token = $_SESSION['_csrf'];
}
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Doctor Requests</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <script defer src="/doctor-appointment/assets/js/admin.js"></script>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="admin-wrap">
  <?php include __DIR__ . '/_sidebar.php'; ?>

  <main style="flex:1;padding:20px;">
    <div class="center-card" style="padding:18px;">
      <h2>Doctor Registration Requests</h2>

      <?php if (!$requests): ?>
        <div class="muted">No pending requests.</div>
      <?php else: ?>
        <table style="width:100%;margin-top:10px">
          <thead><tr style="background:#f8fafc"><th>Doctor</th><th>License</th><th>Specialty</th><th>Contact</th><th>Actions</th></tr></thead>
          <tbody>
            <?php foreach($requests as $r): ?>
            <tr data-id="<?php echo (int)$r['id']; ?>">
              <td><?php echo htmlspecialchars($r['full_name'].' ('.$r['username'].')'); ?></td>
              <td><?php echo htmlspecialchars($r['license_no']); ?></td>
              <td><?php echo htmlspecialchars($r['specialty']); ?></td>
              <td><?php echo htmlspecialchars($r['contact']); ?></td>
              <td>
                <button class="approve-doctor" data-id="<?php echo (int)$r['id']; ?>">Approve</button>
                <button class="reject-doctor" data-id="<?php echo (int)$r['id']; ?>">Reject</button>
              </td>
            </tr>
            <?php endforeach; ?>
          </tbody>
        </table>
      <?php endif; ?>

    </div>
  </main>
</div>

<!-- Expose CSRF token to JS securely (short-lived per-session token) -->
<script>
  window.ADMIN_CSRF_TOKEN = <?= json_encode($csrf_token, JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE) ?>;
  (function(){
    async function sendAction(id, action, reason='') {
      const body = new URLSearchParams();
      body.append('id', id);
      body.append('action', action);
      body.append('csrf_token', window.ADMIN_CSRF_TOKEN);
      if (reason) body.append('reason', reason);

      const res = await fetch('/doctor-appointment/admin/approve_doctor.php', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: body.toString(),
        credentials: 'same-origin'
      });
      let data;
      try { data = await res.json(); } catch (e) { data = {error: 'Invalid JSON response'}; }
      return {ok: res.ok, status: res.status, data};
    }

    function showMessage(msg) {
      // quick UX: alert for now; replace with flash_render or toast if you have one
      alert(msg);
    }

    document.addEventListener('click', function(e){
      // Approve
      if (e.target && e.target.matches('.approve-doctor')) {
        const btn = e.target;
        const id = btn.dataset.id;
        if (!confirm('Approve this doctor?')) return;
        btn.disabled = true;
        sendAction(id, 'approve').then(res => {
          btn.disabled = false;
          if (!res.ok || res.data.error) {
            showMessage(res.data.error || 'Server error while approving');
            return;
          }
          // success: remove the row or mark approved
          const row = btn.closest('tr');
          if (row) row.remove();
          showMessage('Doctor approved.');
        }).catch(err => {
          btn.disabled = false;
          console.error(err);
          showMessage('Network error.');
        });
      }

      // Reject
      if (e.target && e.target.matches('.reject-doctor')) {
        const btn = e.target;
        const id = btn.dataset.id;
        const reason = prompt('Reason for rejection (optional):', '');
        if (reason === null) return; // cancelled
        btn.disabled = true;
        sendAction(id, 'reject', reason).then(res => {
          btn.disabled = false;
          if (!res.ok || res.data.error) {
            showMessage(res.data.error || 'Server error while rejecting');
            return;
          }
          const row = btn.closest('tr');
          if (row) row.remove();
          showMessage('Doctor rejected.');
        }).catch(err => {
          btn.disabled = false;
          console.error(err);
          showMessage('Network error.');
        });
      }
    });
  })();
</script>

<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
