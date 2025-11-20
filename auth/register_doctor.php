<?php
// auth/register_doctor.php
require_once __DIR__ . '/../inc/db.php';
require_once __DIR__ . '/../inc/functions.php';
require_once __DIR__ . '/../inc/csrf.php';
session_start();

if (!empty($_SESSION['role'])) {
    header("Location: /doctor-appointment/role-select.php");
    exit;
}

$errors = [];
$success = "";

$upload_dir = __DIR__ . '/../uploads';
if (!is_dir($upload_dir)) mkdir($upload_dir, 0755, true);

if ($_SERVER["REQUEST_METHOD"] === "POST") {

    if (!validate_csrf($_POST["csrf_token"] ?? "")) {
        $errors[] = "Invalid CSRF token.";
    }

    $username = trim($_POST["username"]);
    $full_name = trim($_POST["full_name"]);
    $email = trim($_POST["email"]);
    $contact = trim($_POST["contact"]);
    $password = $_POST["password"];
    $license_no = trim($_POST["license_no"]);
    $specialty = trim($_POST["specialty"]);
    $experience = intval($_POST["experience"] ?? 0);
    $fee = floatval($_POST["fee"] ?? 0);
    $bio = trim($_POST["bio"]);

    if ($username === "" || $full_name === "" || $email === "" || 
        $contact === "" || $password === "" || $license_no === "" ||
        $specialty === "" ) {
        $errors[] = "All fields except image are required.";
    }

    // UNIQUE username
    $stmt = $pdo->prepare("SELECT id FROM users WHERE username=?");
    $stmt->execute([$username]);
    if ($stmt->fetch()) $errors[] = "Username already exists.";

    // Upload image
    $image_path = "/doctor-appointment/assets/images/placeholder.png";
    if (!empty($_FILES["image"]["name"])) {
        $img = $_FILES["image"];

        if ($img["error"] === UPLOAD_ERR_OK) {
            $allowed = ["image/jpeg", "image/png"];
            if (!in_array($img["type"], $allowed)) {
                $errors[] = "Image must be JPG or PNG.";
            } elseif ($img["size"] > 2 * 1024 * 1024) {
                $errors[] = "Image too large.";
            } else {
                $ext = pathinfo($img["name"], PATHINFO_EXTENSION);
                $filename = "doc-" . uniqid() . "." . $ext;
                $target = $upload_dir . "/" . $filename;
                move_uploaded_file($img["tmp_name"], $target);
                $image_path = "/doctor-appointment/uploads/" . $filename;
            }
        }
    }

    if (empty($errors)) {
        $hash = password_hash($password, PASSWORD_DEFAULT);

        $pdo->beginTransaction();

        $stmt = $pdo->prepare("
            INSERT INTO users (role, username, full_name, email, contact, password_hash, status, created_at)
            VALUES ('doctor', ?, ?, ?, ?, ?, 'pending', NOW())
        ");
        $stmt->execute([$username, $full_name, $email, $contact, $hash]);

        $doctor_id = $pdo->lastInsertId();

        $stmt = $pdo->prepare("
            INSERT INTO doctors_profiles
            (user_id, license_no, experience, specialty, fee, bio, image, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'pending')
        ");
        $stmt->execute([$doctor_id, $license_no, $experience, $specialty, $fee, $bio, $image_path]);

        $pdo->commit();

        // after commit, set flash and redirect to role-select
        $success = "Doctor registration submitted! Admin approval required...";

    }
}
?>
<!doctype html>
<html>
<head>
<title>Doctor Registration</title>
<link rel="stylesheet" href="/doctor-appointment/assets/css/style.css">
</head>
<body>

<div class="center-card" style="max-width:650px;">
<h2>Register as Doctor</h2>

<?php if ($errors): ?>
<div class="error"><?php echo implode("<br>", $errors); ?></div>
<?php endif; ?>

<?php if ($success): ?>
<div class="success"><?php echo $success; ?></div>
<?php endif; ?>

<form method="post" enctype="multipart/form-data">
    <?php echo csrf_field(); ?>

    <label>Username<br><input name="username" required></label><br>
    <label>Full Name<br><input name="full_name" required></label><br>
    <label>Email<br><input type="email" name="email" required></label><br>
    <label>Contact<br><input name="contact" required></label><br>
    <label>Password<br><input type="password" name="password" required></label><br>
    <label>Medical License No.<br><input name="license_no" required></label><br>
    <label>Specialty<br><input name="specialty" required></label><br>
    <label>Experience (Years)<br><input type="number" name="experience"></label><br>
    <label>Consultation Fee<br><input type="number" name="fee"></label><br>
    <label>Short Bio<br><textarea name="bio"></textarea></label><br>

    <label>Profile Image (optional)<br><input type="file" name="image" accept="image/*"></label><br>

    <button class="role-btn" type="submit">Register</button>
</form>

<a href="login.php" class="muted">Already have an account? Login</a>
</div>

</body>
</html>
