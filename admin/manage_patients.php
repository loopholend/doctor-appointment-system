<?php
// admin/manage_patients.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$q = trim($_GET['q'] ?? '');
$where = "WHERE u.role='patient'";
$params = [];
if ($q !== '') {
    $where .= " AND (u.username LIKE ? OR u.full_name LIKE ? OR u.email LIKE ?)";
    $like = "%$q%"; $params = [$like,$like,$like];
}

$stmt = $pdo->prepare("SELECT u.id,u.username,u.full_name,u.email,u.contact, u.status FROM users u $where ORDER BY u.id DESC");
$stmt->execute($params);
$rows = $stmt->fetchAll();
?>
<!doctype html><html><head><meta charset="utf-8"><title>Manage Patients</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:1100px;margin:18px auto;padding:12px">
  <h1>Manage Patients</h1>
  <?php flash_render(); ?>
  <div style="margin-bottom:12px">
    <a class="role-btn" href="/doctor-appointment/admin/add_patient.php">Add Patient</a>
  </div>
  <form method="get" style="margin-bottom:12px">
    <input name="q" placeholder="search patient" value="<?php echo e($q); ?>">
    <button class="role-btn" type="submit">Search</button>
  </form>

  <?php if(empty($rows)): ?><div class="muted">No patients found.</div><?php else: ?>
  <table class="wide-table">
    <thead><tr><th>ID</th><th>Username</th><th>Name</th><th>Email</th><th>Contact</th><th>Status</th><th>Actions</th></tr></thead>
    <tbody>
    <?php foreach($rows as $r): ?>
      <tr>
        <td><?php echo e($r['id']); ?></td>
        <td><?php echo e($r['username']); ?></td>
        <td><?php echo e($r['full_name']); ?></td>
        <td><?php echo e($r['email']); ?></td>
        <td><?php echo e($r['contact']); ?></td>
        <td><?php echo e($r['status']); ?></td>
        <td>
          <a class="role-btn small-btn" href="/doctor-appointment/admin/edit_patient.php?id=<?php echo (int)$r['id']; ?>">Edit</a>
          <form method="post" action="/doctor-appointment/admin/delete_patient.php" style="display:inline">
            <?php echo csrf_field(); ?>
            <input type="hidden" name="id" value="<?php echo (int)$r['id']; ?>">
            <button class="role-btn small-btn" style="background:#d93838">Delete</button>
          </form>
          <a class="role-btn small-btn" href="/doctor-appointment/admin/appointments.php?patient_id=<?php echo (int)$r['id']; ?>" style="background:#6b7280">View Appts</a>
        </td>
      </tr>
    <?php endforeach; ?>
    </tbody>
  </table>
  <?php endif; ?>
</div>
</body></html>
