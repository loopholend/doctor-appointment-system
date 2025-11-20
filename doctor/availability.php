<?php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('doctor');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$doctor_id = current_user_id();

// fetch existing availability
$stmt = $pdo->prepare("
    SELECT id, day_of_week, start_time, end_time
    FROM doctor_available_times
    WHERE doctor_id=? AND repeat_weekly=1
    ORDER BY FIELD(day_of_week,'mon','tue','wed','thu','fri','sat','sun'), start_time
");
$stmt->execute([$doctor_id]);
$rows = $stmt->fetchAll();

// group by day
$days = ['mon'=>[], 'tue'=>[], 'wed'=>[], 'thu'=>[], 'fri'=>[], 'sat'=>[], 'sun'=>[]];
foreach($rows as $r){
    $days[$r['day_of_week']][] = $r;
}

$labels = [
    'mon'=>'Monday','tue'=>'Tuesday','wed'=>'Wednesday','thu'=>'Thursday',
    'fri'=>'Friday','sat'=>'Saturday','sun'=>'Sunday'
];
?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>Manage Weekly Availability</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="page-wrap" style="max-width:900px;margin:20px auto;padding:12px;">
<h1>Weekly Availability</h1>
<p class="muted">Set your available time ranges for each weekday.</p>

<?php flash_render(); ?>

<form method="post" action="availability_save.php">
    <input type="hidden" name="csrf_token" value="<?php echo csrf_token(); ?>">

    <?php foreach($labels as $k=>$label): ?>
        <div style="margin-bottom:25px;border-bottom:1px solid #e5e7eb;padding-bottom:15px;">
            <h2><?php echo $label; ?></h2>

            <div id="block-<?php echo $k; ?>">
                <?php if(empty($days[$k])): ?>
                    <div class="muted small">No time ranges added.</div>
                <?php else: ?>
                    <?php foreach($days[$k] as $i=>$block): ?>
                        <div class="time-block" style="margin-bottom:8px;">
                            <input type="time" name="start_<?php echo $k; ?>[]" value="<?php echo $block['start_time']; ?>">
                            <input type="time" name="end_<?php echo $k; ?>[]" value="<?php echo $block['end_time']; ?>">
                        </div>
                    <?php endforeach; ?>
                <?php endif; ?>
            </div>

            <button class="role-btn small-btn"
                    type="button"
                    onclick="addBlock('<?php echo $k; ?>')"
                    style="margin-top:8px;">
                + Add Time Range
            </button>
        </div>
    <?php endforeach; ?>

    <button class="role-btn" type="submit">Save Availability</button>
</form>

</div>

<script>
function addBlock(day){
    var container = document.getElementById("block-"+day);
    var div = document.createElement("div");
    div.className = "time-block";
    div.style.marginBottom = "8px";
    div.innerHTML = `
        <input type="time" name="start_${day}[]" required>
        <input type="time" name="end_${day}[]" required>
    `;
    container.appendChild(div);
}
</script>

<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
