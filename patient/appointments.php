<?php
// patient/appointments.php - tabs for Upcoming / Past
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('patient');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$uid = current_user_id();
$now = date('Y-m-d H:i:s');

// All appointments
$stmt = $pdo->prepare("SELECT a.*, ud.full_name AS doctor_name, dp.specialty FROM appointments a JOIN users ud ON a.doctor_id=ud.id JOIN doctors_profiles dp ON dp.user_id=ud.id WHERE a.patient_id=? ORDER BY a.date DESC, a.start_time DESC");
$stmt->execute([$uid]);
$appts = $stmt->fetchAll();

// split
$upcoming = $past = [];
foreach($appts as $a){
  $dt = strtotime($a['date'].' '.$a['start_time']);
  if ($dt > time()) $upcoming[] = $a; else $past[] = $a;
}
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>My Appointments</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <script defer src="/doctor-appointment/assets/js/patient_ui.js"></script>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:1000px;margin:18px auto;padding:12px">
  <h1>My Appointments</h1>
  <?php if(function_exists('flash_render')) flash_render(); ?>

  <div class="tabs">
    <button class="tab-btn active" data-target="#upcoming">Upcoming (<?php echo count($upcoming); ?>)</button>
    <button class="tab-btn" data-target="#past">Past (<?php echo count($past); ?>)</button>
  </div>

  <div id="upcoming" class="tab-panel active">
    <?php if(empty($upcoming)): ?><div class="muted">No upcoming appointments.</div><?php endif; ?>
    <table class="wide-table">
      <thead><tr><th>Date</th><th>Time</th><th>Doctor</th><th>Specialty</th><th>Status</th><th>Actions</th></tr></thead>
      <tbody>
        <?php foreach($upcoming as $a): ?>
          <tr>
            <td><?php echo e($a['date']); ?></td>
            <td><?php echo e(substr($a['start_time'],0,5).' - '.substr($a['end_time'],0,5)); ?></td>
            <td><?php echo e($a['doctor_name']); ?></td>
            <td><?php echo e($a['specialty']); ?></td>
            <td><span class="status <?php echo e($a['status']); ?>"><?php echo e(ucfirst($a['status'])); ?></span></td>
            <td>
              <?php if($a['status']==='booked'): ?>
                <form method="post" action="/doctor-appointment/patient/cancel_appointment.php" style="display:flex;gap:6px;align-items:center">
                  <?php echo csrf_field(); ?>
                  <input type="hidden" name="appointment_id" value="<?php echo (int)$a['id']; ?>">
                  <input type="text" name="cancel_reason" placeholder="Reason (optional)" style="width:180px">
                  <button class="role-btn small-btn" type="submit">Cancel</button>
                </form>
              <?php else: ?> - <?php endif; ?>
            </td>
          </tr>
        <?php endforeach; ?>
      </tbody>
    </table>
  </div>

  <div id="past" class="tab-panel">
    <?php if(empty($past)): ?><div class="muted">No past appointments.</div><?php endif; ?>
    <table class="wide-table">
      <thead><tr><th>Date</th><th>Time</th><th>Doctor</th><th>Specialty</th><th>Status</th><th>Cancel Reason</th></tr></thead>
      <tbody>
        <?php foreach($past as $a): ?>
          <tr>
            <td><?php echo e($a['date']); ?></td>
            <td><?php echo e(substr($a['start_time'],0,5).' - '.substr($a['end_time'],0,5)); ?></td>
            <td><?php echo e($a['doctor_name']); ?></td>
            <td><?php echo e($a['specialty']); ?></td>
            <td><span class="status <?php echo e($a['status']); ?>"><?php echo e(ucfirst($a['status'])); ?></span></td>
            <td><?php echo e($a['cancel_reason'] ?? '-'); ?></td>
          </tr>
        <?php endforeach; ?>
      </tbody>
    </table>
  </div>

</div>
<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
