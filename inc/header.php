<?php
// inc/header.php — common header + sidebars for patient/admin/doctor
if (session_status() === PHP_SESSION_NONE) session_start();
require_once __DIR__ . '/db.php';

// unread notifications
$unread = 0;
if (!empty($_SESSION['user_id'])) {
    $uid = $_SESSION['user_id'];
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0");
    $stmt->execute([$uid]);
    $unread = (int)$stmt->fetchColumn();
}

$role = $_SESSION['role'] ?? '';
?>
<header style="background:#fff;box-shadow:0 3px 10px rgba(20,50,90,0.06);padding:12px 20px;
                display:flex;justify-content:space-between;align-items:center;position:sticky;top:0;z-index:50;">
  <div>
    <a href="/doctor-appointment/index.php"
       style="font-weight:700;font-size:20px;text-decoration:none;color:#222;">
      Doctor Appointment System
    </a>
  </div>

  <?php if($role): ?>
  <div style="display:flex;align-items:center;gap:16px;">

    <!-- Notification Bell -->
    <div style="position:relative;cursor:pointer;" id="notif-bell">
      <img src="/doctor-appointment/assets/images/bell.png" style="width:24px;">
      <?php if($unread > 0): ?>
        <span id="notif-count"
              style="position:absolute;top:-6px;right:-6px;background:#ff3b3b;color:#fff;
                     padding:2px 6px;font-size:11px;border-radius:50%;font-weight:700;">
          <?php echo $unread; ?>
        </span>
      <?php endif; ?>
    </div>

    <a href="/doctor-appointment/auth/logout_confirm.php"
       class="role-btn small-btn"
       style="background:#444;color:#fff;text-decoration:none;">
       Logout
    </a>
  </div>
  <?php endif; ?>
</header>


<!-- SIDEBAR MENUS -->
<?php if($role === 'patient'): ?>
    <aside class="sidebar-menu">
        <a href="/doctor-appointment/patient/dashboard.php">Dashboard</a>
        <a href="/doctor-appointment/patient/appointments.php">My Appointments</a>
        <a href="/doctor-appointment/patient/profile_edit.php">Update Medical Details</a>
        <a href="/doctor-appointment/auth/logout_confirm.php">Logout</a>
    </aside>
<?php endif; ?>

<?php if($role === 'admin'): ?>
    <aside class="sidebar-menu">
        <a href="/doctor-appointment/admin/dashboard.php">Dashboard</a>
        <a href="/doctor-appointment/admin/requests.php">Doctor Requests</a>
        <a href="/doctor-appointment/admin/manage_doctors.php">Manage Doctors</a>
        <a href="/doctor-appointment/admin/manage_patients.php">Manage Patients</a>
        <a href="/doctor-appointment/admin/appointments.php">Appointments</a>
        <a href="/doctor-appointment/admin/reports.php">Reports</a>
        <a href="/doctor-appointment/auth/logout_confirm.php">Logout</a>

    </aside>
<?php endif; ?>


<style>
/* vertical sidebar (same as admin style) */
.sidebar-menu {
    width: 200px;
    position: fixed;
    top: 70px;
    left: 0;
    background:#fff;
    border-right:1px solid #e5e7eb;
    height: calc(100vh - 70px);
    display:flex;
    flex-direction:column;
    padding:12px 0;
    z-index:30;
}
.sidebar-menu a {
    padding:12px 18px;
    text-decoration:none;
    color:#111827;
    font-size:15px;
    border-radius:4px;
}
.sidebar-menu a:hover {
    background:#f3f4f6;
}
.page-wrap {
    margin-left:220px !important;
}
</style>

<script>
document.addEventListener("DOMContentLoaded",function(){
  var bell = document.getElementById("notif-bell");
  if(bell){
    bell.addEventListener("click",function(){
      window.location.href="/doctor-appointment/notifications.php";
    });
  }
});
</script>
