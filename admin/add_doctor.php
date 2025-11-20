<?php
// admin/add_doctor.php
if (session_status() === PHP_SESSION_NONE && !headers_sent()) session_start();
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/csrf.php';
require_once __DIR__ . '/../inc/functions.php';
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Add Doctor</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <style>
    .day-row { display:flex; gap:8px; align-items:center; margin-bottom:8px; }
    .small{width:90px}
    .day-checkbox { width:18px; height:18px; }
    .time-input { padding:6px; border-radius:6px; border:1px solid #ddd; }
    .avail-box { background: rgba(30,60,90,0.03); padding:10px; border-radius:8px; margin-top:8px; }
  </style>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:900px;margin:22px auto;">
  <h1>Add Doctor (admin)</h1>
  <?php flash_render(); ?>
  <form method="post" action="add_doctor_save.php" enctype="multipart/form-data">
    <?php echo csrf_field(); ?>
    <label>Username<input name="username" required></label>
    <label>Full name<input name="full_name" required></label>
    <label>Email<input name="email" type="email" required></label>
    <label>Contact<input name="contact" required></label>
    <label>Password<input name="password" type="password" required></label>
    <label>Medical license number<input name="license_no" required></label>
    <label>Years of experience<input name="experience" type="number" min="0"></label>
    <label>Specialty<input name="specialty" required></label>
    <label>Consultation fee<input name="fee" type="number" min="0" required></label>
    <label>Short bio<textarea name="bio"></textarea></label>
    <label>Profile image (jpg/png, max 2MB)<input type="file" name="image"></label>

    <div class="avail-box">
      <h3>Weekly Availability (check day, then set start/end)</h3>
      <?php
        $days = ['0'=>'Sunday','1'=>'Monday','2'=>'Tuesday','3'=>'Wednesday','4'=>'Thursday','5'=>'Friday','6'=>'Saturday'];
        foreach($days as $k=>$d):
      ?>
        <div class="day-row">
          <label style="width:120px"><input type="checkbox" class="day-toggle" data-day="<?php echo $k?>" name="day_<?php echo $k?>_enabled"> <?php echo $d?></label>
          <label class="small">Start <input class="time-input" type="time" name="day_<?php echo $k?>_start" disabled></label>
          <label class="small">End <input class="time-input" type="time" name="day_<?php echo $k?>_end" disabled></label>
        </div>
      <?php endforeach; ?>
      <p style="font-size:13px;color:#555">You may add multiple ranges later via doctor profile (this is a single default range per selected day).</p>
    </div>

    <div style="margin-top:12px">
      <button class="role-btn" type="submit">Create Doctor</button>
      <a class="role-btn" href="manage_doctors.php" style="background:#6b7280">Cancel</a>
    </div>
  </form>
</div>

<script>
document.querySelectorAll('.day-toggle').forEach(function(ch){
  ch.addEventListener('change', function(){
    var day = ch.getAttribute('data-day');
    var start = document.querySelector('input[name="day_'+day+'_start"]');
    var end = document.querySelector('input[name="day_'+day+'_end"]');
    if (ch.checked) { start.disabled = false; end.disabled = false; }
    else { start.disabled = true; end.disabled = true; start.value=''; end.value=''; }
  });
});
</script>
</body>
</html>
