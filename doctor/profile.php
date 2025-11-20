<?php
// C:\xampp\htdocs\doctor-appointment\doctor\profile.php
// Rewritten to fix "Undefined variable $pdo" and fatal on ->prepare()
// - Safe PDO fallback if $pdo not provided by project bootstrap
// - Validates doctor_id input
// - Fetches doctor profile and availability safely
// - Outputs minimal HTML for embedding in your existing layout

session_start();

/**
 * Bootstrap DB connection:
 * - If your project already defines $pdo in an include file, keep that include above this file.
 * - This code will attempt to include ../inc/db.php (common pattern). If $pdo is still undefined,
 *   it will create a local PDO connection using typical XAMPP defaults (adjust if your DB differs).
 */
$incDbPath = __DIR__ . '/../inc/db.php';
if (file_exists($incDbPath)) {
    require_once $incDbPath;
}

if (!isset($pdo) || !$pdo instanceof PDO) {
    // Fallback PDO connection; update credentials if your environment differs.
    $dbHost = '127.0.0.1';
    $dbName = 'doctor_appointment';
    $dbUser = 'root';
    $dbPass = ''; // XAMPP default empty password for root — change if necessary
    $dsn = "mysql:host={$dbHost};dbname={$dbName};charset=utf8mb4";
    try {
        $pdo = new PDO($dsn, $dbUser, $dbPass, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]);
    } catch (PDOException $e) {
        http_response_code(500);
        echo "<h2>Database connection failed</h2>";
        echo "<pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
        exit;
    }
}

// Validate and sanitize doctor_id
$doctor_id = null;
if (isset($_GET['doctor_id']) && is_numeric($_GET['doctor_id'])) {
    $doctor_id = (int) $_GET['doctor_id'];
} else {
    // If doctor_id is missing or invalid, show friendly message and stop
    http_response_code(400);
    echo "<h2>Invalid doctor specified</h2>";
    echo "<p>Missing or invalid <code>doctor_id</code> in the request.</p>";
    exit;
}

// Fetch doctor profile (join users + doctors_profiles)
// Adjust column list if your schema differs
try {
    $sql = "
        SELECT
            u.id AS user_id,
            u.full_name,
            u.email,
            u.contact,
            u.status AS user_status,
            dp.license_no,
            dp.experience,
            dp.specialty,
            dp.fee,
            dp.bio,
            dp.image,
            dp.status AS doctor_status
        FROM users u
        LEFT JOIN doctors_profiles dp ON dp.user_id = u.id
        WHERE u.id = :id AND u.role = 'doctor'
        LIMIT 1
    ";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([':id' => $doctor_id]);
    $doctor = $stmt->fetch();
} catch (PDOException $e) {
    http_response_code(500);
    echo "<h2>Query error</h2>";
    echo "<pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
    exit;
}

if (!$doctor) {
    http_response_code(404);
    echo "<h2>Doctor not found</h2>";
    exit;
}

// Fetch availability from doctor_available_times
// Assumes table has columns: id, doctor_id, day_of_week, start_time, end_time, date_specific, repeat_weekly
// If your column names differ, change the SELECT list accordingly.
try {
    $sqlTimes = "
        SELECT id, doctor_id, day_of_week, start_time, end_time, date_specific, repeat_weekly
        FROM doctor_available_times
        WHERE doctor_id = :id
        ORDER BY
            -- put date-specific entries first (if present), then by day_of_week and start_time
            (date_specific IS NOT NULL) DESC,
            day_of_week ASC,
            start_time ASC
    ";
    $stmt2 = $pdo->prepare($sqlTimes);
    $stmt2->execute([':id' => $doctor_id]);
    $availability = $stmt2->fetchAll();
} catch (PDOException $e) {
    // If the table or columns are missing, show a clear message to debug schema mismatch
    http_response_code(500);
    echo "<h2>Availability query error</h2>";
    echo "<pre>" . htmlspecialchars($e->getMessage()) . "</pre>";
    exit;
}

// Helper: readable day_of_week if stored as number (0-6 or 1-7). Adjust mapping if you store text.
$dayMap = [
    '0' => 'Sunday', '1' => 'Monday', '2' => 'Tuesday', '3' => 'Wednesday',
    '4' => 'Thursday', '5' => 'Friday', '6' => 'Saturday',
    // also allow 1-7 mapping
    '1' => 'Monday', '2' => 'Tuesday', '3' => 'Wednesday',
    '4' => 'Thursday', '5' => 'Friday', '6' => 'Saturday', '7' => 'Sunday',
];

// Output minimal HTML — adapt classes / layout to your site's CSS
?>
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Doctor profile — <?php echo htmlspecialchars($doctor['full_name']); ?></title>
<style>
  table { border-collapse: collapse; width: 100%; max-width: 800px; }
  th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
  th { background: #f4f4f4; }
  .meta { margin-bottom: 1rem; }
</style>
</head>
<body>
  <h1><?php echo htmlspecialchars($doctor['full_name']); ?></h1>

  <div class="meta">
    <strong>Specialty:</strong> <?php echo htmlspecialchars($doctor['specialty'] ?? 'Not specified'); ?><br>
    <strong>Experience:</strong> <?php echo isset($doctor['experience']) ? ((int)$doctor['experience'] . ' years') : 'N/A'; ?><br>
    <strong>Fee:</strong> <?php echo isset($doctor['fee']) ? htmlspecialchars($doctor['fee']) : 'N/A'; ?><br>
    <strong>Contact:</strong> <?php echo htmlspecialchars($doctor['contact'] ?? 'N/A'); ?><br>
    <strong>Email:</strong> <?php echo htmlspecialchars($doctor['email'] ?? 'N/A'); ?><br>
    <strong>Profile status:</strong> <?php echo htmlspecialchars($doctor['doctor_status'] ?? $doctor['user_status']); ?><br>
  </div>

  <h2>Availability</h2>

  <?php if (empty($availability)): ?>
    <p>No availability set for this doctor.</p>
  <?php else: ?>
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Day / Date</th>
          <th>Time</th>
          <th>Repeats weekly</th>
        </tr>
      </thead>
      <tbody>
        <?php foreach ($availability as $i => $slot): ?>
          <?php
            $displayDay = '';
            if (!empty($slot['date_specific'])) {
                $displayDay = date('M j, Y', strtotime($slot['date_specific']));
            } elseif ($slot['day_of_week'] !== null && $slot['day_of_week'] !== '') {
                $key = (string)$slot['day_of_week'];
                $displayDay = $dayMap[$key] ?? ("Day " . htmlspecialchars($slot['day_of_week']));
            } else {
                $displayDay = 'Not specified';
            }

            $start = $slot['start_time'] ?? '';
            $end = $slot['end_time'] ?? '';
            // normalize times if present
            if ($start) $start = date('g:i A', strtotime($start));
            if ($end) $end = date('g:i A', strtotime($end));
            $timeRange = trim("$start - $end", " -");
            $repeats = (isset($slot['repeat_weekly']) && (int)$slot['repeat_weekly']) ? 'Yes' : 'No';
          ?>
          <tr>
            <td><?php echo $i + 1; ?></td>
            <td><?php echo htmlspecialchars($displayDay); ?></td>
            <td><?php echo htmlspecialchars($timeRange ?: 'N/A'); ?></td>
            <td><?php echo htmlspecialchars($repeats); ?></td>
          </tr>
        <?php endforeach; ?>
      </tbody>
    </table>
  <?php endif; ?>

  <?php if (!empty($doctor['bio'])): ?>
    <h3>About</h3>
    <p><?php echo nl2br(htmlspecialchars($doctor['bio'])); ?></p>
  <?php endif; ?>

</body>
</html>
