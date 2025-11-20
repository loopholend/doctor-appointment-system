<?php
// admin/appointments.php - view all appointments and cancel
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$filter_doc = intval($_GET['doctor_id'] ?? 0);
$filter_patient = intval($_GET['patient_id'] ?? 0);
$filter_date = trim($_GET['date'] ?? '');
$filter_status = trim($_GET['status'] ?? '');

$sql = "SELECT a.*, d.full_name AS doctor_name, p.full_name AS patient_name FROM appointments a JOIN users d ON d.id=a.doctor_id JOIN users p ON p.id=a.patient_id WHERE 1=1";
$params = [];
if ($filter_doc) { $sql .= " AND a.doctor_id=?"; $params[] = $filter_doc; }
if ($filter_patient) { $sql .= " AND a.patient_id=?"; $params[] = $filter_patient; }
if ($filter_date) { $sql .= " AND a.date=?"; $params[] = $filter_date; }
if (in_array($filter_status,['booked','cancelled','completed'])) { $sql .= " AND a.status=?"; $params[] = $filter_status; }
$sql .= " ORDER BY a.date DESC, a.start_time DESC LIMIT 1000";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$rows = $stmt->fetchAll();

// get doctors & patients for filters
$docs = $pdo->query("SELECT id,full_name FROM users WHERE role='doctor' ORDER BY full_name")->fetchAll();
$patients = $pdo->query("SELECT id,full_name FROM users WHERE role='patient' ORDER BY full_name")->fetchAll();
?>
<!doctype html><html><head><meta charset="utf-8"><title>Admin Appointments</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:1200px;margin:18px auto;padding:12px">
  <h1>Appointments (Admin)</h1>
  <?php flash_render(); ?>
  <form method="get" style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px">
    <label>Doctor
      <select name="doctor_id"><option value="">All</option>
        <?php foreach($docs as $d): ?><option value="<?php echo $d['id'];?>" <?php echo $filter_doc==$d['id']?'selected':'';?>><?php echo e($d['full_name']);?></option><?php endforeach;?>
      </select>
    </label>
    <label>Patient
      <select name="patient_id"><option value="">All</option>
        <?php foreach($patients as $p): ?><option value="<?php echo $p['id'];?>" <?php echo $filter_patient==$p['id']?'selected':'';?>><?php echo e($p['full_name']);?></option><?php endforeach;?>
      </select>
    </label>
    <label>Date<input type="date" name="date" value="<?php echo e($filter_date);?>"></label>
    <label>Status
      <select name="status"><option value="">All</option>
        <option value="booked" <?php echo $filter_status=='booked'?'selected':'';?>>Booked</option>
        <option value="cancelled" <?php echo $filter_status=='cancelled'?'selected':'';?>>Cancelled</option>
        <option value="completed" <?php echo $filter_status=='completed'?'selected':'';?>>Completed</option>
      </select>
    </label>
    <button class="role-btn" type="submit">Filter</button>
    <a class="role-btn" href="appointments.php" style="background:#6b7280">Reset</a>
    <a class="role-btn" href="appointments_create.php" style="background:#10b981">Create Appointment</a>
  </form>

  <?php if(empty($rows)): ?><div class="muted">No appointments.</div><?php else: ?>
  <table class="wide-table">
    <thead><tr><th>Date</th><th>Time</th><th>Doctor</th><th>Patient</th><th>Status</th><th>Cancel Reason</th><th>Actions</th></tr></thead>
    <tbody>
    <?php foreach($rows as $r): ?>
      <tr>
        <td><?php echo e($r['date']);?></td>
        <td><?php echo e(substr($r['start_time'],0,5).' - '.substr($r['end_time'],0,5));?></td>
        <td><?php echo e($r['doctor_name']);?></td>
        <td><?php echo e($r['patient_name']);?></td>
        <td><span class="status <?php echo e($r['status']);?>"><?php echo e(ucfirst($r['status']));?></span></td>
        <td><?php echo e($r['cancel_reason'] ?? '-'); ?></td>
        <td>
          <?php if($r['status']=='booked'): ?>
            <form method="post" action="appointments_cancel.php" style="display:inline">
              <?php echo csrf_field(); ?>
              <input type="hidden" name="id" value="<?php echo (int)$r['id']; ?>">
              <input type="text" name="cancel_reason" placeholder="Cancel reason" required>
              <button class="role-btn small-btn" type="submit" style="background:#ef4444">Cancel</button>
            </form>
          <?php endif; ?>
          <a class="role-btn small-btn" href="/doctor-appointment/admin/appointments.php?patient_id=<?php echo (int)$r['patient_id']; ?>">View Patient</a>
        </td>
      </tr>
    <?php endforeach;?>
    </tbody>
  </table>
  <?php endif; ?>
</div>
</body></html>
