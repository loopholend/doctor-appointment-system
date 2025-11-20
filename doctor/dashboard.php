<?php
// doctor/dashboard.php — Doctor Home Dashboard (date-picker + appointments by date + hamburger quick actions)
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('doctor');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$doctor_id = current_user_id();

// fetch doctor info
$infoQ = $pdo->prepare("SELECT u.full_name, COALESCE(dp.specialty,'') AS specialty FROM users u LEFT JOIN doctors_profiles dp ON dp.user_id=u.id WHERE u.id=?");
$infoQ->execute([$doctor_id]);
$doc = $infoQ->fetch();

// notification count
$ncQ = $pdo->prepare("SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0");
$ncQ->execute([$doctor_id]);
$notif_count = (int)$ncQ->fetchColumn();

// which date? default today or GET param
$sel_date = $_GET['date'] ?? date('Y-m-d');
// if page requested with date param we will render server-side for progressive enhancement

// fetch appointments for selected date (server-side fallback)
$apptQ = $pdo->prepare("
    SELECT a.id, a.patient_id, a.start_time, a.end_time, a.status, a.cancel_reason,
           u.full_name AS patient_name
    FROM appointments a
    JOIN users u ON u.id = a.patient_id
    WHERE a.doctor_id = ? AND a.date = ?
    ORDER BY a.start_time
");
$apptQ->execute([$doctor_id, $sel_date]);
$appts = $apptQ->fetchAll();

?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>Doctor Dashboard</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
<script defer src="/doctor-appointment/assets/js/doctor_ui.js"></script>
</head>
<body>

<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="page-wrap" style="max-width:1000px;margin:20px auto;padding:12px;">

  <div style="display:flex;justify-content:space-between;align-items:center;">
    <div>
      <h1 style="margin:0"><?php echo e($doc['full_name'] ?: 'Doctor'); ?></h1>
      <div class="muted"><?php echo e($doc['specialty']); ?></div>
    </div>

    <div style="display:flex;gap:12px;align-items:center">
      <a id="notif-link" href="/doctor-appointment/doctor/notifications.php" class="icon-btn">🔔<?php if($notif_count>0) echo "<span class='badge'>{$notif_count}</span>"; ?></a>

      <!-- hamburger -->
              <div id="hamburger-menu" class="hamburger-menu" aria-hidden="true">
          <a href="/doctor-appointment/doctor/availability.php">Manage Availability</a>
          <a href="/doctor-appointment/doctor/dayoff.php">Mark Day Off</a>
          <a href="/doctor-appointment/doctor/profile_edit.php">Edit Profile</a>
        </div>

      </div>
    </div>
  </div>

  <hr style="margin:18px 0;">

  <div style="display:flex;gap:18px;align-items:center;margin-bottom:12px;">
    <label style="display:flex;flex-direction:column">
      <span class="muted">Select date</span>
      <input id="date-picker" type="date" value="<?php echo e($sel_date); ?>">
    </label>

    <button id="load-btn" class="role-btn" type="button">Load</button>

    <div style="margin-left:18px" class="muted">Viewing appointments for <strong id="date-label"><?php echo e($sel_date); ?></strong></div>
  </div>

  <div id="appointments-area">
    <?php if(empty($appts)): ?>
      <div class="muted">No appointments for <?php echo e($sel_date); ?>.</div>
    <?php else: ?>
      <table class="wide-table">
        <thead><tr><th>Time</th><th>Patient</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>
        <?php foreach($appts as $a): ?>
          <tr>
            <td><?php echo e(substr($a['start_time'],0,5)); ?> - <?php echo e(substr($a['end_time'],0,5)); ?></td>
            <td><?php echo e($a['patient_name']); ?></td>
            <td><span class="status <?php echo e($a['status']); ?>"><?php echo e(ucfirst($a['status'])); ?></span></td>
            <td>
              <a class="role-btn small-btn" href="/doctor-appointment/doctor/patient_view.php?patient_id=<?php echo (int)$a['patient_id']; ?>&appointment_id=<?php echo (int)$a['id']; ?>">View medical details</a>
              <!-- Doctor cannot cancel here by business rule -->
            </td>
          </tr>
        <?php endforeach; ?>
        </tbody>
      </table>
    <?php endif; ?>
  </div>

</div>

<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
