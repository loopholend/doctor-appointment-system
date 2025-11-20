<?php
// admin/dashboard.php (upgraded)
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');

require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

// Metrics
$month_start = date('Y-m-01');
$stmt = $pdo->prepare("SELECT COUNT(*) FROM users WHERE role='patient' AND created_at >= ?");
$stmt->execute([$month_start]); $users_this_month = (int)$stmt->fetchColumn();

$stmt = $pdo->query("SELECT COUNT(*) FROM users WHERE role='patient'"); $total_patients = (int)$stmt->fetchColumn();
$stmt = $pdo->query("SELECT COUNT(*) FROM users WHERE role='doctor'"); $total_doctors = (int)$stmt->fetchColumn();

$stmt = $pdo->prepare("SELECT COALESCE(SUM(dp.fee),0) FROM appointments a JOIN doctors_profiles dp ON a.doctor_id=dp.user_id WHERE a.status IN ('booked','completed')");
$stmt->execute(); $revenue = (float)$stmt->fetchColumn();

$stmt = $pdo->query("SELECT COUNT(*) FROM users WHERE role='doctor' AND status='pending'"); $pending_doctors = (int)$stmt->fetchColumn();

// recent cancelled appointments
$stmt = $pdo->query("SELECT a.id,a.date,a.start_time,a.end_time,a.cancel_reason,a.cancelled_at,u.full_name AS patient_name, ud.full_name AS doctor_name FROM appointments a JOIN users u ON a.patient_id=u.id JOIN users ud ON a.doctor_id=ud.id WHERE a.status='cancelled' ORDER BY a.cancelled_at DESC LIMIT 6");
$recent_cancelled = $stmt->fetchAll();

// appointments per day (last 7 days)
$days = [];
$counts = [];
for ($i = 6; $i >= 0; $i--) {
    $d = date('Y-m-d', strtotime("-{$i} days"));
    $days[] = $d;
    $q = $pdo->prepare("SELECT COUNT(*) FROM appointments WHERE date = ?");
    $q->execute([$d]);
    $counts[] = (int)$q->fetchColumn();
}

?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>Admin Dashboard</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
<script defer src="/doctor-appointment/assets/js/admin.js"></script>
<script defer src="/doctor-appointment/assets/js/admin_charts.js"></script>
<style>
/* admin dashboard grid */
.admin-wrap { display:flex; gap:18px; max-width:1200px; margin:20px auto; }
.admin-main { flex:1; }
.admin-cards { display:flex; gap:12px; margin-bottom:18px; flex-wrap:wrap; }
.admin-card { background:#fff; padding:16px; border-radius:12px; box-shadow:0 8px 26px rgba(20,40,80,0.04); min-width:160px; flex:1; }
.admin-card .num { font-size:22px; font-weight:700; }
.chart-box { background:#fff; padding:16px; border-radius:12px; box-shadow:0 10px 30px rgba(10,30,60,0.04); margin-top:12px; }
.admin-aside { width:320px; }
.recent-list li { padding:8px 0; border-bottom:1px solid #f3f6fb; }
</style>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="admin-wrap">
  <?php include __DIR__ . '/_sidebar.php'; ?>

  <main class="admin-main">
    <div class="center-card">
      <h2>Dashboard</h2>

      <div class="admin-cards">
        <div class="admin-card">
          <div class="num"><?php echo $users_this_month; ?></div>
          <div class="muted">New patients this month</div>
        </div>
        <div class="admin-card">
          <div class="num"><?php echo $total_patients; ?></div>
          <div class="muted">Total patients</div>
        </div>
        <div class="admin-card">
          <div class="num"><?php echo $total_doctors; ?></div>
          <div class="muted">Total doctors</div>
        </div>
        <div class="admin-card">
          <div class="num">₹ <?php echo number_format($revenue,2); ?></div>
          <div class="muted">Revenue (booked/completed)</div>
        </div>
        <div class="admin-card">
          <div class="num"><?php echo $pending_doctors; ?></div>
          <div class="muted">Pending doctor requests</div>
        </div>
      </div>

      <div class="chart-box">
        <h3>Appointments — last 7 days</h3>
        <canvas id="apptsChart" width="800" height="200" style="width:100%;max-width:100%;"></canvas>
        <script>
          window.__APPT_CHART = {
            labels: <?php echo json_encode($days); ?>,
            data: <?php echo json_encode($counts); ?>
          };
        </script>
      </div>

      <div style="display:flex;gap:18px;margin-top:18px;flex-wrap:wrap">
        <div style="flex:1">
          <h3 style="margin-top:12px">Recent cancellations</h3>
          <?php if (empty($recent_cancelled)): ?>
            <div class="muted">No cancellations recently.</div>
          <?php else: ?>
            <ul class="recent-list">
              <?php foreach($recent_cancelled as $c): ?>
                <li>
                  <div><strong><?php echo e($c['patient_name']); ?></strong> — <?php echo e($c['date'].' '.$c['start_time']); ?></div>
                  <div class="muted"><?php echo e($c['cancel_reason']); ?></div>
                </li>
              <?php endforeach; ?>
            </ul>
          <?php endif; ?>
        </div>

        <aside class="admin-aside">
          <div class="center-card">
            <h3>Quick links</h3>
            <p><a href="/doctor-appointment/admin/requests.php">Doctor Requests</a></p>
            <p><a href="/doctor-appointment/admin/manage_doctors.php">Manage Doctors</a></p>
            <p><a href="/doctor-appointment/admin/manage_patients.php">Manage Patients</a></p>
            <p><a href="/doctor-appointment/admin/appointments.php">Appointments</a></p>
            <p><a href="/doctor-appointment/admin/reports.php">Reports</a></p>
          </div>
        </aside>
      </div>
    </div>
  </main>
</div>

<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
