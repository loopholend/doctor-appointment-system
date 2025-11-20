<?php
// patient/medical_view.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

$viewer_role = current_user_role(); // patient or doctor or admin
$patient_id = intval($_GET['patient_id'] ?? current_user_id());
if (!$patient_id) { echo "Invalid patient id"; exit; }

$stmt = $pdo->prepare("SELECT u.username,u.full_name,u.email,u.contact, p.* FROM users u LEFT JOIN patients_profiles p ON u.id=p.user_id WHERE u.id = ? LIMIT 1");
$stmt->execute([$patient_id]);
$patient = $stmt->fetch();
if (!$patient) { echo "Patient not found"; exit; }
?>
<!doctype html>
<html><head><meta charset="utf-8"><title>Medical Profile</title><link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="center-card" style="max-width:760px">
  <h2><?php echo e($patient['full_name'] ?: $patient['username']); ?> — Medical Details</h2>
  <div><strong>Contact:</strong> <?php echo e($patient['contact']); ?></div>
  <div style="margin-top:12px">
    <table style="width:100%">
      <tr><td>Diabetes</td><td><?php echo e($patient['diabetes'] ?? ''); ?></td></tr>
      <tr><td>Thyroid</td><td><?php echo e($patient['thyroid'] ?? ''); ?></td></tr>
      <tr><td>Blood pressure</td><td><?php echo e($patient['blood_pressure'] ?? ''); ?></td></tr>
      <tr><td>Asthma</td><td><?php echo e($patient['asthma'] ?? ''); ?></td></tr>
      <tr><td>Age</td><td><?php echo e($patient['age'] ?? ''); ?></td></tr>
      <tr><td>Blood group</td><td><?php echo e($patient['blood_group'] ?? ''); ?></td></tr>
      <tr><td>Allergies</td><td><?php echo e($patient['allergies_text'] ?? ''); ?></td></tr>
      <tr><td>Past surgeries</td><td><?php echo e($patient['surgeries_text'] ?? ''); ?></td></tr>
    </table>
  </div>

  <?php if ($viewer_role === 'patient'): ?>
    <div style="margin-top:12px"><a href="profile_edit.php" class="role-btn">Edit profile</a></div>
  <?php endif; ?>
  <div style="margin-top:8px"><a href="dashboard.php" class="role-btn" style="background:#666">Back</a></div>
</div>
<?php include __DIR__ . '/../inc/footer.php'; ?></body></html>
