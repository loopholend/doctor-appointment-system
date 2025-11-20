<?php
// patient/book.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('patient');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/csrf.php';
require_once __DIR__ . '/../inc/functions.php';

$doctor_id = intval($_GET['doctor_id'] ?? 0);
if ($doctor_id <= 0) { echo "Invalid doctor id"; exit; }

$stmt = $pdo->prepare("SELECT u.id,u.full_name,dp.specialty,dp.fee,dp.image FROM users u JOIN doctors_profiles dp ON u.id=dp.user_id WHERE u.id=? LIMIT 1");
$stmt->execute([$doctor_id]);
$doc = $stmt->fetch();
if (!$doc) { echo "Doctor not found"; exit; }

?>
<!doctype html><html><head><meta charset="utf-8"><title>Book with <?php echo htmlspecialchars($doc['full_name']); ?></title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
<script>
function fetchSlots(){
  var date = document.getElementById('date').value;
  if(!date) return;
  fetch('/doctor-appointment/patient/slots_ajax.php?doctor_id=<?php echo $doctor_id; ?>&date='+encodeURIComponent(date))
    .then(r=>r.json()).then(d=>{
      var wrap = document.getElementById('slots');
      wrap.innerHTML = '';
      if(d.error){ wrap.innerText = d.error; return; }
      d.slots.forEach(function(s){
        var b = document.createElement('button');
        b.className='role-btn small-btn';
        b.innerText = s.start+' - '+s.end;
        b.onclick = function(){ bookSlot(date,s.start,s.end); };
        wrap.appendChild(b);
      });
    });
}

function bookSlot(date,start,end){
  if(!confirm('Book '+date+' '+start+'?')) return;
  var fd = new FormData();
  fd.append('csrf_token', document.querySelector('input[name=csrf_token]').value);
  fd.append('doctor_id', '<?php echo $doctor_id; ?>');
  fd.append('date', date);
  fd.append('start_time', start);
  fd.append('end_time', end);

  fetch('/doctor-appointment/patient/book_action.php', { method:'POST', body: fd })
    .then(r=>r.json()).then(d=>{
      if(d.error) alert('Error: '+d.error);
      else { alert('Booked'); window.location='/doctor-appointment/patient/appointments.php'; }
    });
}
</script>
</head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div style="max-width:900px;margin:18px auto;padding:12px">
  <h2>Book with <?php echo htmlspecialchars($doc['full_name']); ?></h2>
  <div style="display:flex;gap:18px;">
    <img src="<?php echo htmlspecialchars($doc['image'] ?: '/doctor-appointment/assets/images/placeholder.png'); ?>" style="width:160px;height:160px;object-fit:cover;border-radius:10px">
    <div>
      <div class="muted"><?php echo htmlspecialchars($doc['specialty']); ?></div>
      <div style="margin-top:6px">Fee: ₹ <?php echo number_format($doc['fee'],2); ?></div>
      <div style="margin-top:12px">
        <label>Select date <input type="date" id="date" onchange="fetchSlots()"></label>
        <div id="slots" style="margin-top:10px"></div>
      </div>
    </div>
  </div>

  <form style="display:none">
    <?php echo csrf_field(); ?>
  </form>
</div>
<?php include __DIR__ . '/../inc/footer.php'; ?>
</body></html>
