<?php
// admin/reports.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

// totals
$total_users = $pdo->query("SELECT COUNT(*) FROM users")->fetchColumn();
$total_patients = $pdo->query("SELECT COUNT(*) FROM users WHERE role='patient'")->fetchColumn();
$total_doctors = $pdo->query("SELECT COUNT(*) FROM users WHERE role='doctor'")->fetchColumn();
$total_appointments = $pdo->query("SELECT COUNT(*) FROM appointments")->fetchColumn();
$total_cancelled = $pdo->query("SELECT COUNT(*) FROM appointments WHERE status='cancelled'")->fetchColumn();
$revenue = $pdo->query("SELECT IFNULL(SUM(fee),0) FROM appointments a JOIN doctors_profiles dp ON dp.user_id=a.doctor_id WHERE a.status='booked' OR a.status='completed'")->fetchColumn();
?>
<!doctype html><html><head><meta charset="utf-8"><title>Admin Reports</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:1000px;margin:18px auto;padding:12px">
  <h1>Reports</h1>
  <?php flash_render(); ?>
  <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:12px">
    <div class="card"><h3>Users</h3><div><?php echo e($total_users); ?></div></div>
    <div class="card"><h3>Patients</h3><div><?php echo e($total_patients); ?></div></div>
    <div class="card"><h3>Doctors</h3><div><?php echo e($total_doctors); ?></div></div>
    <div class="card"><h3>Appointments</h3><div><?php echo e($total_appointments); ?></div></div>
    <div class="card"><h3>Cancelled</h3><div><?php echo e($total_cancelled); ?></div></div>
    <div class="card"><h3>Revenue (stub)</h3><div>₹ <?php echo number_format($revenue,2); ?></div></div>
  </div>

  <div style="margin-bottom:12px">
    <a class="role-btn" href="reports_export_csv.php">Export full CSV</a>
  </div>

  <h2>Recent logs</h2>
  <table class="wide-table">
    <thead><tr><th>Time</th><th>Actor</th><th>Role</th><th>Event</th><th>Detail</th></tr></thead>
    <tbody>
    <?php
    $logs = $pdo->query("SELECT l.*, u.full_name AS actor_name FROM logs l LEFT JOIN users u ON u.id=l.actor_id ORDER BY l.id DESC LIMIT 50")->fetchAll();
    foreach($logs as $lg){
        echo '<tr><td>'.e($lg['created_at']).'</td><td>'.e($lg['actor_name']??'system').'</td><td>'.e($lg['actor_role']).'</td><td>'.e($lg['event_type']).'</td><td>'.e($lg['detail']).'</td></tr>';
    }
    ?>
    </tbody>
  </table>
</div>
</body></html>
