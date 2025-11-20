<?php
// admin/manage_doctors.php - list and manage doctors
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$q = trim($_GET['q'] ?? '');
$where = "WHERE u.role='doctor'";
$params = [];
if ($q !== '') {
    $where .= " AND (u.username LIKE ? OR u.full_name LIKE ? OR dp.specialty LIKE ?)";
    $like = "%$q%"; $params = [$like,$like,$like];
}

$stmt = $pdo->prepare("SELECT u.id,u.username,u.full_name,u.email,dp.specialty,dp.fee,dp.status FROM users u LEFT JOIN doctors_profiles dp ON dp.user_id=u.id $where ORDER BY u.id DESC");
$stmt->execute($params);
$rows = $stmt->fetchAll();
?>
<!doctype html><html><head><meta charset="utf-8"><title>Manage Doctors</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:1100px;margin:18px auto;padding:12px">
  <h1>Manage Doctors</h1>
  <?php flash_render(); ?>
  <div style="margin-bottom:12px">
    <a class="role-btn" href="/doctor-appointment/admin/add_doctor.php">Add Doctor</a>
  </div>
  <form method="get" style="margin-bottom:12px">
    <input name="q" placeholder="search doctor" value="<?php echo e($q); ?>">
    <button class="role-btn" type="submit">Search</button>
  </form>

  <?php if(empty($rows)): ?><div class="muted">No doctors found.</div><?php else: ?>
  <table class="wide-table">
    <thead><tr><th>ID</th><th>Username</th><th>Name</th><th>Specialty</th><th>Fee</th><th>Status</th><th>Actions</th></tr></thead>
    <tbody>
    <?php foreach($rows as $r): ?>
      <tr>
        <td><?php echo e($r['id']); ?></td>
        <td><?php echo e($r['username']); ?></td>
        <td><?php echo e($r['full_name']); ?></td>
        <td><?php echo e($r['specialty'] ?? '-'); ?></td>
        <td><?php echo e($r['fee'] ?? '-'); ?></td>
        <td><?php echo e($r['status'] ?? 'pending'); ?></td>
        <td>
          <a class="role-btn small-btn" href="/doctor-appointment/admin/edit_doctor.php?id=<?php echo (int)$r['id']; ?>">Edit</a>
          <form method="post" action="/doctor-appointment/admin/delete_doctor.php" style="display:inline">
            <?php echo csrf_field(); ?>
            <input type="hidden" name="id" value="<?php echo (int)$r['id']; ?>">
            <button class="role-btn small-btn" style="background:#d93838">Delete</button>
          </form>
        </td>
      </tr>
    <?php endforeach; ?>
    </tbody>
  </table>
  <?php endif; ?>
</div>
</body></html>
