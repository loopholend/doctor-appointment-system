<?php
// admin/appointments_create.php
// Purpose: Admin form to create an appointment (GET) + POST handler to create appointment with concurrency checks.
// Path: C:\xampp\htdocs\doctor-appointment\admin\appointments_create.php

require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

// POST handler: create appointment
if ($_SERVER['REQUEST_METHOD'] === 'POST' && (($_POST['action'] ?? '') === 'create')) {
    if (!validate_csrf($_POST['csrf_token'] ?? '')) {
        flash_set('error','Invalid CSRF token.');
        header('Location: appointments_create.php');
        exit;
    }

    $patient_id = intval($_POST['patient_id'] ?? 0);
    $doctor_id  = intval($_POST['doctor_id'] ?? 0);
    $date       = trim($_POST['date'] ?? '');
    $slot       = trim($_POST['slot'] ?? '');

    if ($patient_id <= 0 || $doctor_id <= 0 || !preg_match('/^\d{4}-\d{2}-\d{2}$/', $date) || $slot === '') {
        flash_set('error','Missing or invalid input.');
        header('Location: appointments_create.php');
        exit;
    }

    // slot format start||end
    if (strpos($slot,'||') === false) {
        flash_set('error','Invalid slot chosen.');
        header('Location: appointments_create.php');
        exit;
    }
    list($start, $end) = explode('||', $slot, 2);
    $start = trim($start); $end = trim($end);

    try {
        $pdo->beginTransaction();

        // Lock any appointment with same doctor,date,start_time (prevents race conditions)
        $lock = $pdo->prepare("SELECT id FROM appointments WHERE doctor_id=? AND date=? AND start_time=? FOR UPDATE");
        $lock->execute([$doctor_id, $date, $start]);
        if ($lock->fetch()) {
            throw new Exception('Slot already taken. Choose another.');
        }

        // Final double-check: ensure doctor isn't off on that date
        $doff = $pdo->prepare("SELECT id FROM doctor_dayoffs WHERE doctor_id=? AND date=? LIMIT 1");
        $doff->execute([$doctor_id, $date]);
        if ($doff->fetch()) {
            throw new Exception('Doctor has a day-off on that date.');
        }

        // Insert appointment
        $ins = $pdo->prepare("INSERT INTO appointments (patient_id, doctor_id, date, start_time, end_time, status, created_at, created_by) VALUES (?,?,?,?,?,'booked',NOW(),?)");
        $ins->execute([$patient_id, $doctor_id, $date, $start, $end, current_user_id()]);
        $appt_id = $pdo->lastInsertId();

        // Create notifications
        $note = $pdo->prepare("INSERT INTO notifications (user_id, title, message, created_at) VALUES (?, ?, ?, NOW())");
        $note->execute([$patient_id, 'Appointment booked', "Admin booked your appointment on $date at $start."]);
        $note->execute([$doctor_id, 'New appointment', "Admin booked appointment with patient ID $patient_id on $date at $start."]);

        // Log action
        $log = $pdo->prepare("INSERT INTO logs (actor_id, actor_role, event_type, detail, created_at) VALUES (?, ?, ?, ?, NOW())");
        $detail = "appt_id={$appt_id};doctor_id={$doctor_id};patient_id={$patient_id};date={$date};start={$start}";
        $log->execute([current_user_id(), 'admin', 'create_appointment', $detail]);

        $pdo->commit();
        flash_set('success','Appointment created successfully.');
        header('Location: appointments.php');
        exit;
    } catch (Exception $e) {
        if ($pdo->inTransaction()) $pdo->rollBack();
        flash_set('error','Could not create appointment: '.$e->getMessage());
        header('Location: appointments_create.php');
        exit;
    }
}

// GET: prepare lists for the form
$doctors = $pdo->prepare("SELECT u.id, u.full_name, dp.fee FROM users u LEFT JOIN doctors_profiles dp ON dp.user_id=u.id WHERE u.role='doctor' AND (dp.status='approved' OR dp.status IS NULL) ORDER BY u.full_name");
$doctors->execute();
$doctors = $doctors->fetchAll();

$patients = $pdo->prepare("SELECT id, full_name, username FROM users WHERE role='patient' ORDER BY full_name");
$patients->execute();
$patients = $patients->fetchAll();
?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>Create Appointment (Admin)</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
<script>
function loadSlots(){
  var doctorId = document.getElementById('doctor_id').value;
  var date = document.getElementById('date').value;
  var el = document.getElementById('slots');
  el.innerHTML = '<div class="muted">Loading slots…</div>';
  if(!doctorId || !date) { el.innerHTML = '<div class="muted">Select doctor and date to load slots.</div>'; return; }
  fetch('appointments_create_ajax_slots.php?doctor_id='+encodeURIComponent(doctorId)+'&date='+encodeURIComponent(date))
    .then(function(res){ return res.json(); })
    .then(function(data){
      if(data.error){ el.innerHTML = '<div class="muted">'+data.error+'</div>'; return; }
      if(!data.slots || data.slots.length===0){ el.innerHTML = '<div class="muted">No available slots for this date.</div>'; return; }
      var html = '<div style="display:flex;flex-wrap:wrap;gap:8px">';
      data.slots.forEach(function(s){
        var value = s.start + '||' + s.end;
        html += '<label style="padding:6px;border:1px solid #e6eaf2;border-radius:8px;display:inline-flex;align-items:center;gap:8px;"><input type="radio" name="slot" value="'+value+'"> '+s.start+' - '+s.end+'</label>';
      });
      html += '</div>';
      el.innerHTML = html;
    })
    .catch(function(err){
      el.innerHTML = '<div class="muted">Failed to load slots: '+err.message+'</div>';
    });
}
</script>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="page-wrap" style="max-width:900px;margin:20px auto;padding:12px;">
  <h1>Create Appointment (Admin)</h1>
  <?php flash_render(); ?>

  <form method="post" action="appointments_create.php">
    <?php echo csrf_field(); ?>

    <label>Patient
      <select id="patient_id" name="patient_id" required>
        <option value="">-- Select patient --</option>
        <?php foreach($patients as $p): ?>
          <option value="<?php echo (int)$p['id']; ?>"><?php echo htmlspecialchars($p['full_name'].' ('.$p['username'].')'); ?></option>
        <?php endforeach; ?>
      </select>
      <a class="role-btn small-btn" href="add_patient.php" style="margin-left:8px;background:#10b981">Add new patient</a>
    </label>

    <label>Doctor
      <select id="doctor_id" name="doctor_id" onchange="loadSlots()" required>
        <option value="">-- Select doctor --</option>
        <?php foreach($doctors as $d): ?>
          <option value="<?php echo (int)$d['id']; ?>"><?php echo htmlspecialchars($d['full_name'].' - ₹'.($d['fee'] ?? '0')); ?></option>
        <?php endforeach; ?>
      </select>
    </label>

    <label>Date
      <input type="date" id="date" name="date" onchange="loadSlots()" required>
    </label>

    <div id="slots" style="margin-top:12px;">
      <div class="muted">Select doctor & date to load available slots.</div>
    </div>

    <div style="margin-top:12px;">
      <input type="hidden" name="action" value="create">
      <button class="role-btn" type="submit">Create Appointment</button>
      <a class="role-btn" href="appointments.php" style="background:#6b7280">Back</a>
    </div>
  </form>
</div>

</body>
</html>
