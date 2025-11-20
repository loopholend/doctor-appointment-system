<?php
// doctor/appointments.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('doctor');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$doctor_id = current_user_id();

// filters
$filter_date = trim($_GET['date'] ?? '');
$filter_status = trim($_GET['status'] ?? ''); // booked, cancelled, completed, all
$q_patient = trim($_GET['patient_q'] ?? '');

// base query
$sql = "SELECT a.*, u.full_name AS patient_name, u.contact AS patient_contact, dp.blood_group
        FROM appointments a
        JOIN users u ON u.id = a.patient_id
        LEFT JOIN patients_profiles dp ON dp.user_id = u.id
        WHERE a.doctor_id = :doc";
$params = [':doc' => $doctor_id];

// apply filters
if ($filter_date !== '') {
    $sql .= " AND a.date = :date";
    $params[':date'] = $filter_date;
}
if ($filter_status !== '' && in_array($filter_status,['booked','cancelled','completed'])) {
    $sql .= " AND a.status = :status";
    $params[':status'] = $filter_status;
}
if ($q_patient !== '') {
    $sql .= " AND (u.full_name LIKE :pq OR u.username LIKE :pq)";
    $params[':pq'] = "%$q_patient%";
}

$sql .= " ORDER BY a.date DESC, a.start_time DESC LIMIT 500"; // limit for safety

$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$rows = $stmt->fetchAll();
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>All Appointments</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="page-wrap" style="max-width:1100px;margin:20px auto;padding:12px;">
  <h1>Appointments</h1>
  <?php if(function_exists('flash_render')) flash_render(); ?>

  <form method="get" style="display:flex;gap:8px;align-items:center;margin-bottom:12px;flex-wrap:wrap">
    <label>Date <input type="date" name="date" value="<?php echo e($filter_date); ?>"></label>
    <label>Status
      <select name="status">
        <option value="">All</option>
        <option value="booked" <?php echo $filter_status==='booked'?'selected':''; ?>>Booked</option>
        <option value="cancelled" <?php echo $filter_status==='cancelled'?'selected':''; ?>>Cancelled</option>
        <option value="completed" <?php echo $filter_status==='completed'?'selected':''; ?>>Completed</option>
      </select>
    </label>
    <input name="patient_q" placeholder="patient name or username" value="<?php echo e($q_patient); ?>">
    <button class="role-btn" type="submit">Filter</button>
    <a class="role-btn" href="/doctor-appointment/doctor/appointments.php" style="background:#6b7280">Reset</a>
  </form>

  <?php if(empty($rows)): ?>
    <div class="muted">No appointments found for the current filters.</div>
  <?php else: ?>
    <table class="wide-table">
      <thead>
        <tr>
          <th>Date</th>
          <th>Time</th>
          <th>Patient</th>
          <th>Contact</th>
          <th>Blood Group</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <?php foreach($rows as $r): ?>
          <tr>
            <td><?php echo e($r['date']); ?></td>
            <td><?php echo e(substr($r['start_time'],0,5).' - '.substr($r['end_time'],0,5)); ?></td>
            <td><?php echo e($r['patient_name']); ?></td>
            <td><?php echo e($r['patient_contact']); ?></td>
            <td><?php echo e($r['blood_group'] ?? '-'); ?></td>
            <td><span class="status <?php echo e($r['status']); ?>"><?php echo e(ucfirst($r['status'])); ?></span></td>
            <td>
              <a class="role-btn small-btn" href="/doctor-appointment/doctor/patient_view.php?patient_id=<?php echo (int)$r['patient_id']; ?>&appointment_id=<?php echo (int)$r['id']; ?>">View medical</a>
              <!-- No cancel button for doctors per business rules -->
            </td>
          </tr>
        <?php endforeach; ?>
      </tbody>
    </table>
  <?php endif; ?>

  <div style="margin-top:12px">
    <a class="role-btn" href="/doctor-appointment/doctor/dashboard.php">Back to Dashboard</a>
  </div>
</div>

</body>
</html>
