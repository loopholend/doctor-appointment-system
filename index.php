
<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Doctor Appointment — Home</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <style>
    .hero { max-width:980px;margin:40px auto;padding:36px;border-radius:14px;background:linear-gradient(180deg,#ffffff,#f7fbff);box-shadow:0 12px 40px rgba(10,30,80,0.06);display:flex;gap:24px;align-items:center; }
    .hero-left{flex:1}
    .hero-right{text-align:center;width:320px}
    .big-btn{display:inline-block;padding:10px 16px;border-radius:10px;background:#2563eb;color:#fff;text-decoration:none;font-weight:700}
    .muted{color:#6b7280}
  </style>
</head>
<body>
  <div style="padding:18px;">
    <header style="display:flex;justify-content:space-between;align-items:center;max-width:1100px;margin:0 auto;">
      <h1 style="margin:0">Doctor Appointment System</h1>
      <nav>
        <a href="/doctor-appointment/role-select.php" class="big-btn" style="background:#0ea5a4">Get Started</a>
      </nav>
    </header>

    <section class="hero">
      <div class="hero-left">
        <h2>Book appointments instantly. Manage availability. Admin controls.</h2>
        <p class="muted">Register as patient to book doctors, or register as doctor (admin approval required) to receive patients.</p>
        <p style="margin-top:18px">
          <a class="big-btn" href="/doctor-appointment/role-select.php">Register / Login</a>
        </p>
      </div>
      <div class="hero-right">
        <img src="/doctor-appointment/assets/images/placeholder.png" alt="app" style="max-width:100%;border-radius:10px;box-shadow:0 8px 24px rgba(10,30,80,0.06)">
        <p class="muted" style="font-size:13px;margin-top:10px">Test users available in seed data.</p>
      </div>
    </section>

    <div style="max-width:1100px;margin:20px auto;">
      <h3>Quick links</h3>
      <p><a href="/doctor-appointment/auth/register_patient.php">Register as Patient</a> — <a href="/doctor-appointment/auth/login.php">Patient Login</a></p>
      <p><a href="/doctor-appointment/auth/register_doctor.php">Register as Doctor</a> — <a href="/doctor-appointment/auth/login.php">Doctor Login</a></p>
      <p><a href="/doctor-appointment/admin/dashboard.php">Admin Dashboard (admin1)</a></p>
    </div>
  </div>
</body>
</html>
