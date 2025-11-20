<?php
// patient/dashboard.php - upgraded UI with notifications bell and cleaner cards
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('patient');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

$per_page = 8;
$page = max(1,intval($_GET['page'] ?? 1));
$q = trim($_GET['q'] ?? '');
$min_fee = $_GET['min_fee'] ?? '';
$max_fee = $_GET['max_fee'] ?? '';

// fetch unread notification count
$notif_count = $pdo->prepare("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0");
$notif_count->execute([current_user_id()]);
$n_count = (int)$notif_count->fetchColumn();

$where = "WHERE u.role='doctor' AND dp.status='approved'";
$params = [];
if ($q !== '') {
  $where .= " AND (u.full_name LIKE ? OR dp.specialty LIKE ?)";
  $like = "%$q%"; $params[] = $like; $params[] = $like;
}
if ($min_fee !== '') { $where .= " AND dp.fee >= ?"; $params[] = floatval($min_fee); }
if ($max_fee !== '') { $where .= " AND dp.fee <= ?"; $params[] = floatval($max_fee); }

$totalQ = $pdo->prepare("SELECT COUNT(*) FROM users u JOIN doctors_profiles dp ON u.id=dp.user_id $where");
$totalQ->execute($params);
$total = (int)$totalQ->fetchColumn();
$offset = ($page-1)*$per_page;

$stmt = $pdo->prepare("SELECT u.id AS doc_id, u.full_name, dp.specialty, dp.fee, dp.bio, dp.image FROM users u JOIN doctors_profiles dp ON u.id=dp.user_id $where ORDER BY u.id DESC LIMIT $per_page OFFSET $offset");
$stmt->execute($params);
$doctors = $stmt->fetchAll();
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Find Doctors — Dashboard</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <script defer src="/doctor-appointment/assets/js/patient_ui.js"></script>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="page-wrap" style="max-width:1100px;margin:18px auto;padding:12px;">
  <div class="top-row" style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
    <div>
      <h1 style="margin:0">Find doctors</h1>
      <div class="muted">Search, filter and book appointments easily.</div>
    </div>

    <div style="display:flex;gap:10px;align-items:center">
      <a class="role-btn" href="/doctor-appointment/patient/profile_edit.php">Update Medical Details</a>
      <a class="role-btn" href="/doctor-appointment/patient/appointments.php">My Appointments</a>
      <a class="icon-btn" href="/doctor-appointment/patient/notifications.php" title="Notifications">
        🔔 <?php if($n_count>0) echo "<span class='badge'>{$n_count}</span>"; ?>
      </a>
    </div>
  </div>

  <form method="get" class="filter-bar" style="display:flex;gap:8px;align-items:center;margin-bottom:14px;">
    <input name="q" placeholder="Search doctor name or specialty" value="<?php echo e($q); ?>">
    <input name="min_fee" placeholder="min fee" style="width:110px" value="<?php echo e($min_fee); ?>">
    <input name="max_fee" placeholder="max fee" style="width:110px" value="<?php echo e($max_fee); ?>">
    <button class="role-btn" type="submit">Filter</button>
  </form>

  <div class="grid-cards">
    <?php if(empty($doctors)): ?>
      <div class="muted">No doctors found for chosen filters.</div>
    <?php endif; ?>
    <?php foreach($doctors as $d): ?>
      <div class="doc-card">
        <div class="doc-image"><img src="<?php echo e($d['image'] ?: '/doctor-appointment/assets/images/placeholder.png'); ?>" alt=""></div>
        <div class="doc-body">
          <h3><?php echo e($d['full_name']); ?></h3>
          <div class="muted"><?php echo e($d['specialty']); ?></div>
          <div class="fee">₹ <?php echo number_format($d['fee'],2); ?></div>
          <p><?php echo e(substr($d['bio'],0,140)); ?></p>
          <div style="display:flex;gap:8px;margin-top:8px">
            <a class="role-btn small-btn" href="/doctor-appointment/doctor/profile.php?doctor_id=<?php echo (int)$d['doc_id']; ?>">View</a>
            <a class="role-btn small-btn" href="/doctor-appointment/patient/book.php?doctor_id=<?php echo (int)$d['doc_id']; ?>">Book</a>
          </div>
        </div>
      </div>
    <?php endforeach; ?>
  </div>

  <div class="pagination" style="margin-top:14px;">
    <?php for($p=1;$p<=max(1,ceil($total/$per_page));$p++): ?>
      <a class="page-link<?php echo $p==$page?' active':'';?>" href="?<?php echo http_build_query(array_merge($_GET,['page'=>$p])); ?>"><?php echo $p; ?></a>
    <?php endfor; ?>
  </div>
</div>

<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
