<?php
// admin/reports_export_csv.php
require_once __DIR__ . '/../inc/auth_checks.php';
require_role('admin');
require_once __DIR__ . '/../inc/db.php';

// create CSV for appointments (example). Adjust columns as needed.
header('Content-Type: text/csv');
header('Content-Disposition: attachment; filename="doctor_appointment_export_'.date('Y-m-d').'.csv"');

$out = fopen('php://output','w');
fputcsv($out, ['appt_id','date','start_time','end_time','status','doctor_id','doctor_name','patient_id','patient_name','cancel_reason']);

$stmt = $pdo->query("SELECT a.id,a.date,a.start_time,a.end_time,a.status,a.cancel_reason, d.id AS doctor_id, d.full_name AS doctor_name, p.id AS patient_id, p.full_name AS patient_name
                      FROM appointments a
                      JOIN users d ON d.id=a.doctor_id
                      JOIN users p ON p.id=a.patient_id
                      ORDER BY a.date DESC");
while($r = $stmt->fetch()){
    fputcsv($out, [$r['id'],$r['date'],$r['start_time'],$r['end_time'],$r['status'],$r['doctor_id'],$r['doctor_name'],$r['patient_id'],$r['patient_name'],$r['cancel_reason']]);
}
fclose($out);
exit;
