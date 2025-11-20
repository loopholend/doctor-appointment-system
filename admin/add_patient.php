<?php
// admin/add_patient.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';
?>
<!doctype html><html><head><meta charset="utf-8"><title>Add Patient</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:800px;margin:18px auto;padding:12px">
  <h1>Add Patient</h1>
  <?php flash_render(); ?>
  <form method="post" action="add_patient_save.php">
    <?php echo csrf_field(); ?>
    <label>Username<input name="username" required></label>
    <label>Full name<input name="full_name" required></label>
    <label>Email<input name="email" type="email" required></label>
    <label>Contact<input name="contact" required></label>
    <label>Password<input name="password" type="password" required></label>

    <!-- optional medical details -->
    <h3>Optional medical details</h3>
    <label>Age<input name="age" type="number"></label>
    <label>Blood group<input name="blood_group"></label>

    <div style="margin-top:12px">
      <button class="role-btn" type="submit">Create Patient</button>
      <a class="role-btn" href="manage_patients.php" style="background:#6b7280">Cancel</a>
    </div>
  </form>
</div>
</body></html>
