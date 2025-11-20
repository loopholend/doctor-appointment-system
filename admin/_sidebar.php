<?php
// admin/_sidebar.php - include from admin pages to show consistent sidebar
// expects session with admin user logged in (require_role('admin') should be called by parent)
?>
<aside class="admin-sidebar">
  <div class="admin-brand"><a href="/doctor-appointment/admin/dashboard.php">Admin</a></div>
  <nav class="admin-nav">
    <a href="/doctor-appointment/admin/dashboard.php" class="nav-item">Dashboard</a>
    <a href="/doctor-appointment/admin/requests.php" class="nav-item">Doctor Requests</a>
    <a href="/doctor-appointment/admin/manage_doctors.php" class="nav-item">Manage Doctors</a>
    <a href="/doctor-appointment/admin/manage_patients.php" class="nav-item">Manage Patients</a>
    <a href="/doctor-appointment/admin/appointments.php" class="nav-item">Appointments</a>
    <a href="/doctor-appointment/admin/reports.php" class="nav-item">Reports</a>
    <a href="/doctor-appointment/notifications.php" class="nav-item">Notifications</a>
    <a href="/doctor-appointment/role-select.php" class="nav-item">Back to Role Select / Logout</a>
  </nav>
  <div style="padding:12px;color:#7a7a7a;font-size:13px">Signed in as: <strong><?php echo htmlspecialchars($_SESSION['username'] ?? ''); ?></strong></div>
</aside>
