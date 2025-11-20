<?php
// /doctor-appointment/doctor/profile_edit.php
// Robust doctor profile editor:
// - auto-detects an existing doctor table by searching for common columns
// - if none found, offers to create a safe `doctors` table (on explicit POST)
// - creates placeholder row for user_id if missing
// - updates only existing columns
// - debug-friendly (prints meaningful messages instead of silent redirects)

ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

if (session_status() === PHP_SESSION_NONE) session_start();

require_once __DIR__ . '/../inc/auth_checks.php';
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';

// basic login+role guard — will show message if not satisfied
$user_id = (int)($_SESSION['user_id'] ?? 0);
$role = $_SESSION['role'] ?? null;
if ($user_id <= 0) {
    echo "<h2>Not logged in</h2><p>Please login as a doctor.</p>";
    exit;
}
if ($role !== 'doctor') {
    echo "<h2>Access denied</h2><p>Your session role is not 'doctor'. Current role: ".htmlspecialchars($role)."</p>";
    exit;
}

// helper: does column exist
function column_exists(PDO $pdo, string $table, string $column): bool {
    $sql = "SELECT 1 FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
    $st = $pdo->prepare($sql);
    $st->execute([$table, $column]);
    return (bool)$st->fetchColumn();
}

// 1) Try to auto-detect table that likely contains doctor info.
// Look for tables that contain any of these "doctor" columns.
$doctor_column_candidates = [
    'license_no','license','experience','specialty','speciality','fee','bio','image','status','user_id'
];

$found_table = null;

// search information_schema for any table containing at least one of those columns
$placeholders = implode(',', array_fill(0, count($doctor_column_candidates), '?'));
$sql = "SELECT TABLE_NAME, COLUMN_NAME
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND COLUMN_NAME IN ({$placeholders})
        ORDER BY TABLE_NAME";

$st = $pdo->prepare($sql);
$st->execute($doctor_column_candidates);
$rows = $st->fetchAll(PDO::FETCH_ASSOC);

// group by table name and count matching doctor-ish columns
$counts = [];
foreach ($rows as $r) {
    $counts[$r['TABLE_NAME']][] = $r['COLUMN_NAME'];
}

// Choose the table that matches the most candidate columns (heuristic)
$bestTable = null;
$bestCount = 0;
foreach ($counts as $tbl => $cols) {
    $c = count($cols);
    if ($c > $bestCount) {
        $bestCount = $c;
        $bestTable = $tbl;
    }
}

if ($bestTable) {
    $found_table = $bestTable;
}

// If nothing found, $found_table remains null — offer to create a new 'doctors' table.
if (!$found_table) {
    // If user clicked "create_table", create safe doctors table
    if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['create_table']) && $_POST['create_table'] == '1') {
        try {
            $create_sql = <<<SQL
CREATE TABLE IF NOT EXISTS `doctors` (
  `user_id` INT(11) NOT NULL PRIMARY KEY,
  `license_no` VARCHAR(64) NOT NULL,
  `experience` INT(11) DEFAULT 0,
  `specialty` VARCHAR(128) DEFAULT NULL,
  `fee` DECIMAL(10,2) DEFAULT 0.00,
  `bio` TEXT DEFAULT NULL,
  `image` VARCHAR(255) DEFAULT NULL,
  `status` ENUM('approved','pending','disabled') DEFAULT 'pending',
  `created_by` INT(11) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
SQL;
            $pdo->exec($create_sql);
            $found_table = 'doctors';
        } catch (Exception $e) {
            echo "<h2>Failed to create table</h2><pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
            exit;
        }
    } else {
        // show friendly UI explaining table not found and offering to create one
        ?>
        <!doctype html>
        <html>
        <head><meta charset="utf-8"><title>Doctor profile table not found</title></head>
        <body>
        <h2>Doctor profile storage not found</h2>
        <p>The system couldn't find a table in the <strong><?=htmlspecialchars($pdo->query('SELECT DATABASE()')->fetchColumn())?></strong> database that looks like a doctors table.</p>
        <p>Detected columns matched in tables: <strong><?= htmlspecialchars(json_encode($counts ?: [])) ?></strong></p>
        <p>You have two options:</p>
        <ol>
          <li>Tell me the correct table name that stores doctor data (so I can use it), or</li>
          <li>Create a new <code>doctors</code> table now (safe default schema). The page will create it for you.</li>
        </ol>

        <form method="post" style="margin-top:1rem">
          <input type="hidden" name="create_table" value="1">
          <button type="submit">Create `doctors` table (safe default)</button>
        </form>

        <p>If you prefer not to create a new table, paste the output of <code>SHOW TABLES;</code> and the result of this query in your DB console and send it to me:<br>
        <code>SELECT TABLE_NAME, COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND COLUMN_NAME IN ('user_id','license_no','license','experience','specialty','speciality','fee','bio','image','status','created_by') ORDER BY TABLE_NAME, COLUMN_NAME;</code></p>
        </body>
        </html>
        <?php
        exit;
    }
}

// by here, $found_table is set to a usable table name
$doctor_table = $found_table;

// Ensure the table has user_id column. If not, try to find suitable id column.
if (!column_exists($pdo, $doctor_table, 'user_id')) {
    // attempt to find an id column (id or user_id)
    $id_col = column_exists($pdo, $doctor_table, 'id') ? 'id' : null;
    if (!$id_col) {
        echo "<h2>Table found but no user identifier column.</h2>";
        echo "<p>Found table: " . htmlspecialchars($doctor_table) . " but it doesn't contain 'user_id' or 'id'.</p>";
        exit;
    } else {
        // fallback: we'll use 'id' (but this means linking by direct id)
        $user_key_col = 'id';
    }
} else {
    $user_key_col = 'user_id';
}

// fetch doctor row
try {
    $st = $pdo->prepare("SELECT * FROM `{$doctor_table}` WHERE {$user_key_col} = ? LIMIT 1");
    $st->execute([$user_id]);
    $doctor = $st->fetch(PDO::FETCH_ASSOC);
} catch (Exception $e) {
    echo "<h2>DB error fetching doctor row</h2><pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
    exit;
}

// if no row, create placeholder row (so form loads)
if (!$doctor) {
    try {
        // build insert that matches available columns
        $cols = [];
        $vals = [];
        $placeholders = [];
        // required minimal columns
        if (column_exists($pdo, $doctor_table, 'user_id')) {
            $cols[] = 'user_id';
            $placeholders[] = '?';
            $vals[] = $user_id;
        }
        if (column_exists($pdo, $doctor_table, 'license_no')) {
            $cols[] = 'license_no'; $placeholders[] = '?'; $vals[] = '';
        }
        if (column_exists($pdo, $doctor_table, 'experience')) {
            $cols[] = 'experience'; $placeholders[] = '?'; $vals[] = 0;
        }
        if (column_exists($pdo, $doctor_table, 'specialty') || column_exists($pdo, $doctor_table, 'speciality')) {
            $colname = column_exists($pdo, $doctor_table, 'specialty') ? 'specialty' : 'speciality';
            $cols[] = "`{$colname}`"; $placeholders[] = '?'; $vals[] = null;
        }
        if (column_exists($pdo, $doctor_table, 'fee')) {
            $cols[] = 'fee'; $placeholders[] = '?'; $vals[] = 0.00;
        }
        if (column_exists($pdo, $doctor_table, 'status')) {
            $cols[] = 'status'; $placeholders[] = '?'; $vals[] = 'pending';
        }

        if (!empty($cols)) {
            $sql = "INSERT INTO `{$doctor_table}` (" . implode(',', $cols) . ") VALUES (" . implode(',', $placeholders) . ")";
            $ins = $pdo->prepare($sql);
            $ins->execute($vals);
            // re-fetch
            $st->execute([$user_id]);
            $doctor = $st->fetch(PDO::FETCH_ASSOC);
        } else {
            echo "<h2>Cannot create placeholder row</h2><p>No writable columns detected in table " . htmlspecialchars($doctor_table) . ".</p>";
            exit;
        }
    } catch (Exception $e) {
        echo "<h2>Failed to create placeholder row</h2><pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
        exit;
    }
}

// prepare form defaults using available columns
$form = [];
$doctor_cols = [];
$colStmt = $pdo->prepare("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?");
$colStmt->execute([$doctor_table]);
foreach ($colStmt->fetchAll(PDO::FETCH_COLUMN) as $c) {
    $doctor_cols[$c] = true;
    $form[$c] = $doctor[$c] ?? '';
}

// handle POST update (update only existing columns)
$errors = [];
if ($_SERVER['REQUEST_METHOD'] === 'POST' && !isset($_POST['create_table'])) {
    // collect only fields that exist in table
    $updates = [];
    $params = [];
    $allowed_cols = ['license_no','experience','specialty','speciality','fee','bio','image','status'];
    foreach ($allowed_cols as $c) {
        if (isset($doctor_cols[$c]) && array_key_exists($c, $_POST)) {
            $val = trim($_POST[$c]);
            // type coercions
            if ($c === 'experience') {
                if ($val !== '' && !ctype_digit($val)) $errors[] = 'Experience must be integer years.';
                $val = $val === '' ? 0 : (int)$val;
            } elseif ($c === 'fee') {
                if ($val !== '' && !is_numeric($val)) $errors[] = 'Fee must be numeric.';
                $val = $val === '' ? 0.00 : $val;
            } else {
                // leave as string (allow null)
                $val = $val === '' ? null : $val;
            }
            $updates[] = "`{$c}` = ?";
            $params[] = $val;
            $form[$c] = $val;
        }
    }

    if (empty($errors)) {
        if (!empty($updates)) {
            $params[] = $user_id;
            $sql = "UPDATE `{$doctor_table}` SET " . implode(', ', $updates) . " WHERE {$user_key_col} = ?";
            try {
                $upd = $pdo->prepare($sql);
                $upd->execute($params);
                flash_set('success', 'Profile updated successfully.');
                header('Location: /doctor-appointment/doctor/profile_edit.php');
                exit;
            } catch (Exception $e) {
                $errors[] = 'DB error: ' . $e->getMessage();
            }
        } else {
            $errors[] = 'No editable fields found.';
        }
    }
}

// Render the form using only columns present.
?>
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Edit Doctor Profile</title>
  <link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
  <style>
    .container { max-width:900px; margin:36px auto; padding:20px; background:#fff; border-radius:8px; box-shadow:0 8px 30px rgba(0,0,0,0.04); }
    label{display:block;margin:12px 0 6px;font-weight:700}
    input[type="text"], textarea { width:100%; padding:10px; border:1px solid #e5e7eb; border-radius:6px; }
    .muted{color:#6b7280;font-size:14px}
    .btn{display:inline-block;padding:10px 14px;border-radius:8px;text-decoration:none;font-weight:700}
    .btn-primary{background:#2563eb;color:#fff;border:none}
    .errors{background:#fff5f5;border:1px solid #fecaca;padding:12px;border-radius:6px;margin-bottom:12px}
  </style>
</head>
<body>
<?php include __DIR__ . '/../inc/header.php'; ?>

<div class="container">
  <h2>Edit Doctor Profile</h2>

  <?php if (function_exists('flash_render')) flash_render(); ?>

  <?php if (!empty($errors)): ?>
    <div class="errors"><ul><?php foreach ($errors as $e) echo '<li>' . htmlspecialchars($e) . '</li>'; ?></ul></div>
  <?php endif; ?>

  <form method="post" action="">
    <?php if (isset($doctor_cols['license_no'])): ?>
      <label for="license_no">License number</label>
      <input id="license_no" name="license_no" type="text" value="<?= htmlspecialchars($form['license_no'] ?? '') ?>">
    <?php endif; ?>

    <?php if (isset($doctor_cols['experience'])): ?>
      <label for="experience">Experience (years)</label>
      <input id="experience" name="experience" type="text" value="<?= htmlspecialchars($form['experience'] ?? '') ?>">
    <?php endif; ?>

    <?php if (isset($doctor_cols['specialty']) || isset($doctor_cols['speciality'])): 
        $special_col = isset($doctor_cols['specialty']) ? 'specialty' : 'speciality';
    ?>
      <label for="<?= htmlspecialchars($special_col) ?>"><?= ucfirst(htmlspecialchars($special_col)) ?></label>
      <input id="<?= htmlspecialchars($special_col) ?>" name="<?= htmlspecialchars($special_col) ?>" type="text" value="<?= htmlspecialchars($form[$special_col] ?? '') ?>">
    <?php endif; ?>

    <?php if (isset($doctor_cols['fee'])): ?>
      <label for="fee">Fee (e.g. 500.00)</label>
      <input id="fee" name="fee" type="text" value="<?= htmlspecialchars($form['fee'] ?? '') ?>">
    <?php endif; ?>

    <?php if (isset($doctor_cols['bio'])): ?>
      <label for="bio">Bio / About</label>
      <textarea id="bio" name="bio" rows="6"><?= htmlspecialchars($form['bio'] ?? '') ?></textarea>
    <?php endif; ?>

    <?php if (isset($doctor_cols['image'])): ?>
      <label for="image">Image filename/path</label>
      <input id="image" name="image" type="text" value="<?= htmlspecialchars($form['image'] ?? '') ?>">
      <p class="muted">(This field stores filename/path only. Use your upload flow to manage images.)</p>
    <?php endif; ?>

    <?php if (isset($doctor_cols['status'])): ?>
      <label for="status">Status (admin controlled)</label>
      <input id="status" name="status" type="text" value="<?= htmlspecialchars($form['status'] ?? '') ?>" readonly>
    <?php endif; ?>

    <div style="margin-top:16px">
      <button class="btn btn-primary" type="submit">Save profile</button>
      <a class="btn" href="/doctor-appointment/doctor/dashboard.php">Cancel</a>
    </div>
  </form>
</div>

<?php include __DIR__ . '/../inc/footer.php'; ?>
</body>
</html>
