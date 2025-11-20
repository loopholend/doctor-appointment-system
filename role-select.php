<?php
// role-select.php - choose role to register/login
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Choose Role</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <style>
    .roles { display:flex; gap:18px; max-width:900px;margin:48px auto; }
    .role-card{flex:1;background:#fff;padding:18px;border-radius:12px;box-shadow:0 8px 30px rgba(10,30,80,0.04);text-align:center}
    .role-card h3{margin-top:6px}
    .role-actions{margin-top:12px}
    .btn{display:inline-block;padding:8px 12px;border-radius:8px;text-decoration:none;font-weight:700}
    .btn-primary{background:#2563eb;color:#fff}
    .btn-ghost{background:#eef2ff;color:#2563eb}
  </style>
</head>
<body>
<?php include __DIR__ . '/inc/header.php'; ?>

<div style="padding:20px;">
  <h2 style="text-align:center">Register or Login</h2>
  <div class="roles">
    <div class="role-card">
      <img src="/doctor-appointment/assets/images/placeholder.png" style="width:84px">
      <h3>Patient</h3>
      <p class="muted">Find doctors, view profiles, and book appointments.</p>
      <div class="role-actions">
        <a class="btn btn-primary" href="/doctor-appointment/auth/register_patient.php">Register</a>
        <a class="btn btn-ghost" href="/doctor-appointment/auth/login.php?role=patient">Login</a>
      </div>
    </div>

    <div class="role-card">
      <img src="/doctor-appointment/assets/images/placeholder.png" style="width:84px">
      <h3>Doctor</h3>
      <p class="muted">Create profile, set availability. Admin approval required.</p>
      <div class="role-actions">
        <a class="btn btn-primary" href="/doctor-appointment/auth/register_doctor.php">Register</a>
        <a class="btn btn-ghost" href="/doctor-appointment/auth/login.php?role=doctor">Login</a>
      </div>
    </div>

    <div class="role-card">
      <img src="/doctor-appointment/assets/images/placeholder.png" style="width:84px">
      <h3>Admin / Operator</h3>
      <p class="muted">Platform controls and reporting.</p>
      <div class="role-actions">
        <a class="btn btn-primary" href="/doctor-appointment/auth/login.php?role=admin">Login</a>
      </div>
    </div>
  </div>
</div>

<?php include __DIR__ . '/inc/footer.php'; ?>
</body>
</html>
