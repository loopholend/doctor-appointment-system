<?php
// patient/profile_edit.php - medical details editor
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('patient');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$uid = current_user_id();
$stmt = $pdo->prepare("SELECT * FROM patients_profiles WHERE user_id = ?");
$stmt->execute([$uid]);
$profile = $stmt->fetch();

$errors = [];
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!validate_csrf($_POST['csrf_token'] ?? '')) { $errors[] = 'Invalid CSRF'; }
    $diabetes = $_POST['diabetes'] ?? 'non';
    $thyroid = $_POST['thyroid'] ?? 'none';
    $blood_pressure = $_POST['blood_pressure'] ?? 'normal';
    $asthma = $_POST['asthma'] ?? 'no';
    $age = intval($_POST['age'] ?? 0) ?: null;
    $blood_group = trim($_POST['blood_group'] ?? '') ?: null;
    $allergies = $_POST['allergies'] ?? 'no';
    $allergies_text = trim($_POST['allergies_text'] ?? '') ?: null;
    $past_surgeries = $_POST['past_surgeries'] ?? 'no';
    $surgeries_text = trim($_POST['surgeries_text'] ?? '') ?: null;

    try {
        $up = $pdo->prepare("UPDATE patients_profiles SET diabetes=?, thyroid=?, blood_pressure=?, asthma=?, age=?, blood_group=?, allergies=?, allergies_text=?, past_surgeries=?, surgeries_text=? WHERE user_id=?");
        $up->execute([$diabetes,$thyroid,$blood_pressure,$asthma,$age,$blood_group,$allergies,$allergies_text,$past_surgeries,$surgeries_text,$uid]);
        flash_set('success','Profile saved — you can now book doctors.');
        header('Location: /doctor-appointment/patient/dashboard.php');
        exit;
    } catch (Exception $e) {
        $errors[] = 'Save failed: '.$e->getMessage();
    }
}
?>
<!doctype html><html><head><meta charset="utf-8"><title>Edit Medical Details</title><link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:760px;margin:18px auto;padding:12px">
  <h1>Medical Details</h1>
  <?php if($errors) echo '<div class="error">'.e(implode("<br>",$errors)).'</div>'; if(function_exists('flash_render')) flash_render(); ?>

  <form method="post" style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
    <?php echo csrf_field(); ?>
    <label>Diabetes
      <select name="diabetes">
        <?php $opts=['type1','type2','gestational','prediabetic','non']; foreach($opts as $o): ?>
          <option value="<?php echo $o; ?>" <?php echo ($profile['diabetes']==$o)?'selected':''; ?>><?php echo ucfirst($o); ?></option>
        <?php endforeach; ?>
      </select>
    </label>

    <label>Thyroid
      <select name="thyroid">
        <?php foreach(['hypo','hyper','none'] as $o): ?>
          <option value="<?php echo $o; ?>" <?php echo ($profile['thyroid']==$o)?'selected':''; ?>><?php echo ucfirst($o); ?></option>
        <?php endforeach; ?>
      </select>
    </label>

    <label>Blood Pressure
      <select name="blood_pressure">
        <?php foreach(['hypertension','hypotension','normal'] as $o): ?>
          <option value="<?php echo $o; ?>" <?php echo ($profile['blood_pressure']==$o)?'selected':''; ?>><?php echo ucfirst($o); ?></option>
        <?php endforeach; ?>
      </select>
    </label>

    <label>Asthma
      <select name="asthma"><option value="no" <?php echo ($profile['asthma']=='no')?'selected':''; ?>>No</option><option value="yes" <?php echo ($profile['asthma']=='yes')?'selected':''; ?>>Yes</option></select>
    </label>

    <label>Age<input type="number" name="age" value="<?php echo e($profile['age']); ?>"></label>

    <label>Blood Group<input name="blood_group" value="<?php echo e($profile['blood_group']); ?>"></label>

    <label>Allergies
      <select name="allergies"><option value="no" <?php echo ($profile['allergies']=='no')?'selected':''; ?>>No</option><option value="yes" <?php echo ($profile['allergies']=='yes')?'selected':''; ?>>Yes</option></select>
    </label>

    <label>Allergies details<textarea name="allergies_text"><?php echo e($profile['allergies_text']); ?></textarea></label>

    <label>Past surgeries
      <select name="past_surgeries"><option value="no" <?php echo ($profile['past_surgeries']=='no')?'selected':''; ?>>No</option><option value="yes" <?php echo ($profile['past_surgeries']=='yes')?'selected':''; ?>>Yes</option></select>
    </label>

    <label>Surgeries details<textarea name="surgeries_text"><?php echo e($profile['surgeries_text']); ?></textarea></label>

    <div style="grid-column:1/3;text-align:right">
      <button class="role-btn" type="submit">Save Profile</button>
    </div>
  </form>
</div>
<?php include __DIR__ . '/../inc/footer.php'; ?>
</body></html>
