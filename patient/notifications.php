<?php
// patient/notifications.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('patient');
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';

$uid = current_user_id();

// mark single read
if ($_SERVER['REQUEST_METHOD']==='POST' && isset($_POST['mark_id'])) {
    if (!validate_csrf($_POST['csrf_token'] ?? '')) { flash_set('error','CSRF'); header('Location: notifications.php'); exit; }
    $id = intval($_POST['mark_id']);
    $pdo->prepare("UPDATE notifications SET is_read=1 WHERE id=? AND user_id=?")->execute([$id,$uid]);
    header('Location: notifications.php'); exit;
}

// fetch
$stmt = $pdo->prepare("SELECT * FROM notifications WHERE user_id=? ORDER BY id DESC LIMIT 200");
$stmt->execute([$uid]);
$notes = $stmt->fetchAll();
?>
<!doctype html><html><head><meta charset="utf-8"><title>Notifications</title><link rel="stylesheet" href="/doctor-appointment/assets/css/style.css"></head><body>
<?php include __DIR__ . '/../inc/header.php'; ?>
<div class="page-wrap" style="max-width:900px;margin:18px auto;padding:12px">
  <h1>Notifications</h1>
  <?php if(function_exists('flash_render')) flash_render(); ?>

  <?php if(empty($notes)): ?><div class="muted">No notifications.</div><?php endif; ?>

  <ul class="notif-list">
    <?php foreach($notes as $n): ?>
      <li class="<?php echo $n['is_read'] ? 'read' : 'unread'; ?>">
        <div class="notif-title"><?php echo e($n['title']); ?> <span class="muted" style="font-size:12px"><?php echo e($n['created_at']); ?></span></div>
        <div class="notif-msg"><?php echo e($n['message']); ?></div>
        <?php if(!$n['is_read']): ?>
          <form method="post" style="margin-top:6px;">
            <?php echo csrf_field(); ?>
            <input type="hidden" name="mark_id" value="<?php echo (int)$n['id']; ?>">
            <button class="role-btn small-btn" type="submit">Mark as read</button>
          </form>
        <?php endif; ?>
      </li>
    <?php endforeach; ?>
  </ul>
</div>
<?php include __DIR__ . '/../inc/footer.php'; ?>
</body></html>
