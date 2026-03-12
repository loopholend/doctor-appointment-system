package admin;

import common.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/report")
public class AdminReportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // CHECK SESSION - MUST MATCH AdminDashboardServlet.java
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {  // ✅ CHANGED TO adminId
            response.sendRedirect("login.html");
            return;
        }
        
        generateReport(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void generateReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Connection conn = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Collect all statistics
            int totalPatients = getCount(conn, "SELECT COUNT(*) FROM patients");
            int totalDoctors = getCount(conn, "SELECT COUNT(*) FROM doctor_profiles");
            int approvedDoctors = getCount(conn, "SELECT COUNT(*) FROM doctor_profiles WHERE approval_status='approved'");
            int pendingDoctors = getCount(conn, "SELECT COUNT(*) FROM doctor_profiles WHERE approval_status='pending'");
            int rejectedDoctors = getCount(conn, "SELECT COUNT(*) FROM doctor_profiles WHERE approval_status='rejected'");
            
            int totalAppointments = getCount(conn, "SELECT COUNT(*) FROM appointments");
            int bookedAppointments = getCount(conn, "SELECT COUNT(*) FROM appointments WHERE appointment_status='booked'");
            int completedAppointments = getCount(conn, "SELECT COUNT(*) FROM appointments WHERE appointment_status='completed'");
            int cancelledAppointments = getCount(conn, "SELECT COUNT(*) FROM appointments WHERE appointment_status='cancelled'");
            
            int todayAppointments = getCount(conn, "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURDATE()");
            int thisMonthAppointments = getCount(conn, "SELECT COUNT(*) FROM appointments WHERE MONTH(appointment_date) = MONTH(CURDATE()) AND YEAR(appointment_date) = YEAR(CURDATE())");
            
            double totalRevenue = getTotalRevenue(conn);
            
            response.setContentType("text/html; charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();
            
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            String reportDate = today.format(formatter);
            
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Healthcare System Report</title>");
            out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@400;500;600&display=swap' rel='stylesheet'>");
            out.println("<style>");
            out.println("@media print{.no-print{display:none !important}}");
            out.println("body{font-family:'Inter','Poppins',sans-serif;margin:0;padding:24px;background:#F8FAFC}");
            out.println(".report-container{max-width:1200px;margin:0 auto;background:#FFFFFF;padding:40px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;border-radius:12px}");
            out.println(".header{text-align:center;border-bottom:3px solid #2563EB;padding-bottom:20px;margin-bottom:30px}");
            out.println(".header h1{color:#1E293B;font-size:32px;margin:0 0 8px 0;font-weight:700}");
            out.println(".header h2{color:#64748B;font-size:16px;margin:0;font-weight:400}");
            out.println(".report-date{text-align:right;color:#64748B;font-size:13px;margin-bottom:24px}");
            out.println(".section{margin:28px 0}");
            out.println(".section-title{color:#1E293B;font-size:20px;font-weight:700;border-bottom:2px solid #E2E8F0;padding-bottom:10px;margin-bottom:16px}");
            out.println(".stats-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:16px;margin:16px 0}");
            out.println(".stat-card{background:#2563EB;color:white;padding:20px 24px;border-radius:12px}");
            out.println(".stat-card h3{margin:0 0 8px 0;font-size:13px;font-weight:600;opacity:0.9;text-transform:uppercase;letter-spacing:0.5px}");
            out.println(".stat-card .value{font-size:32px;font-weight:700;margin:0}");
            out.println(".stat-card.success{background:#059669}");
            out.println(".stat-card.warning{background:#D97706}");
            out.println(".stat-card.danger{background:#DC2626}");
            out.println(".stat-card.purple{background:#7C3AED}");
            out.println("table{width:100%;border-collapse:collapse;margin:16px 0}");
            out.println("table th{background:#F1F5F9;color:#64748B;padding:12px 16px;text-align:left;font-weight:600;font-size:12px;text-transform:uppercase;letter-spacing:0.5px}");
            out.println("table td{padding:12px 16px;border-bottom:1px solid #F1F5F9;color:#1E293B;font-size:14px}");
            out.println("table tr:hover td{background:#F8FAFC}");
            out.println(".badge{display:inline-block;padding:4px 10px;border-radius:20px;font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px}");
            out.println(".badge.approved{background:#DCFCE7;color:#166534}");
            out.println(".badge.pending{background:#FEF3C7;color:#92400E}");
            out.println(".badge.rejected{background:#FEE2E2;color:#991B1B}");
            out.println(".print-btn{background:#1D4ED8;color:white;border:none;padding:10px 28px;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;margin:16px 0;transition:all 0.2s}");
            out.println(".print-btn:hover{background:#1E40AF}");
            out.println(".back-btn{background:#64748B;color:white;text-decoration:none;padding:10px 28px;border-radius:8px;font-size:14px;font-weight:600;display:inline-block;margin-left:10px;transition:all 0.2s}");
            out.println(".back-btn:hover{background:#475569}");
            out.println(".summary{background:#EFF6FF;padding:20px;border-radius:10px;border-left:4px solid #2563EB;margin:20px 0}");
            out.println(".summary h3{color:#1D4ED8;margin:0 0 12px 0;font-size:16px;font-weight:700}");
            out.println(".summary p{margin:6px 0;color:#1E293B;line-height:1.8;font-size:14px}");
            out.println("</style>");
            out.println("<script>");
            out.println("function printReport(){window.print();}");
            out.println("</script>");
            out.println("</head><body>");
            
            out.println("<div class='report-container'>");
            
            // Header
            out.println("<div class='header'>");
            out.println("<h1>&#127973; MediCare+ Healthcare System</h1>");
            out.println("<h2>Comprehensive Analytics Report</h2>");
            out.println("</div>");
            
            out.println("<div class='report-date'>Report Generated: " + reportDate + "</div>");
            
            // Print/Back buttons
            out.println("<div class='no-print' style='text-align:center;margin:20px 0'>");
            out.println("<button class='print-btn' onclick='printReport()'>&#128424;&#65039; Print / Save as PDF</button>");
            out.println("<a href='dashboard' class='back-btn'>← Back to Dashboard</a>");
            out.println("</div>");
            
            // Executive Summary
            out.println("<div class='summary'>");
            out.println("<h3>&#128202; Executive Summary</h3>");
            out.println("<p><strong>Total System Users:</strong> " + (totalPatients + totalDoctors) + " (Patients: " + totalPatients + ", Doctors: " + totalDoctors + ")</p>");
            out.println("<p><strong>Total Appointments:</strong> " + totalAppointments + " (Booked: " + bookedAppointments + ", Completed: " + completedAppointments + ", Cancelled: " + cancelledAppointments + ")</p>");
            out.println("<p><strong>Total Revenue:</strong> Rs. " + String.format("%.2f", totalRevenue) + "</p>");
            out.println("<p><strong>Doctor Status:</strong> " + approvedDoctors + " Approved, " + pendingDoctors + " Pending, " + rejectedDoctors + " Rejected</p>");
            out.println("</div>");
            
            // Section 1: Overview Statistics
            out.println("<div class='section'>");
            out.println("<h2 class='section-title'>1. Overview Statistics</h2>");
            out.println("<div class='stats-grid'>");
            
            out.println("<div class='stat-card'>");
            out.println("<h3>Total Patients</h3>");
            out.println("<p class='value'>" + totalPatients + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card success'>");
            out.println("<h3>Approved Doctors</h3>");
            out.println("<p class='value'>" + approvedDoctors + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card warning'>");
            out.println("<h3>Pending Doctors</h3>");
            out.println("<p class='value'>" + pendingDoctors + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card danger'>");
            out.println("<h3>Today's Appointments</h3>");
            out.println("<p class='value'>" + todayAppointments + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card purple'>");
            out.println("<h3>Total Appointments</h3>");
            out.println("<p class='value'>" + totalAppointments + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card success'>");
            out.println("<h3>Total Revenue</h3>");
            out.println("<p class='value'>₹" + String.format("%.0f", totalRevenue) + "</p>");
            out.println("</div>");
            
            out.println("</div>");
            out.println("</div>");
            
            // Section 2: Doctor Details
            out.println("<div class='section'>");
            out.println("<h2 class='section-title'>2. Doctor Statistics</h2>");
            out.println("<table>");
            out.println("<tr><th>Doctor Name</th><th>Speciality</th><th>Experience</th><th>Fee (Rs.)</th><th>Status</th><th>Total Appointments</th></tr>");
            
             String doctorSQL = "SELECT d.full_name, d.primary_specialty, d.years_of_experience, d.consultation_fee, d.approval_status, " +
                               "COUNT(a.appointment_id) as appointment_count " +
                               "FROM doctor_profiles d LEFT JOIN appointments a ON d.doctor_id = a.doctor_id " +
                               "GROUP BY d.doctor_id ORDER BY appointment_count DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(doctorSQL);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String status = rs.getString("approval_status");
                String badgeClass = status.equals("approved") ? "approved" : (status.equals("pending") ? "pending" : "rejected");
                
                out.println("<tr>");
                out.println("<td>" + rs.getString("full_name") + "</td>");
                out.println("<td>" + rs.getString("primary_specialty") + "</td>");
                out.println("<td>" + rs.getString("years_of_experience") + "</td>");
                out.println("<td>" + rs.getDouble("consultation_fee") + "</td>");
                out.println("<td><span class='badge " + badgeClass + "'>" + status.toUpperCase() + "</span></td>");
                out.println("<td>" + rs.getInt("appointment_count") + "</td>");
                out.println("</tr>");
            }
            rs.close();
            pstmt.close();
            
            out.println("</table>");
            out.println("</div>");
            
            // Section 3: Patient Statistics
            out.println("<div class='section'>");
            out.println("<h2 class='section-title'>3. Top 10 Patients (Most Active)</h2>");
            out.println("<table>");
            out.println("<tr><th>Patient Name</th><th>Username</th><th>Total Appointments</th></tr>");
            
             String patientSQL = "SELECT p.full_name, p.username, COUNT(a.appointment_id) as appointment_count " +
                                "FROM patients p LEFT JOIN appointments a ON p.username = a.patient_account_username " +
                                "GROUP BY p.username ORDER BY appointment_count DESC LIMIT 10";
            
            pstmt = conn.prepareStatement(patientSQL);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getString("full_name") + "</td>");
                out.println("<td>" + rs.getString("username") + "</td>");
                out.println("<td>" + rs.getInt("appointment_count") + "</td>");
                out.println("</tr>");
            }
            rs.close();
            pstmt.close();
            
            out.println("</table>");
            out.println("</div>");
            
            // Section 4: Appointment Statistics
            out.println("<div class='section'>");
            out.println("<h2 class='section-title'>4. Appointment Breakdown</h2>");
            out.println("<div class='stats-grid'>");
            
            out.println("<div class='stat-card success'>");
            out.println("<h3>Booked</h3>");
            out.println("<p class='value'>" + bookedAppointments + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card'>");
            out.println("<h3>Completed</h3>");
            out.println("<p class='value'>" + completedAppointments + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card danger'>");
            out.println("<h3>Cancelled</h3>");
            out.println("<p class='value'>" + cancelledAppointments + "</p>");
            out.println("</div>");
            
            out.println("<div class='stat-card purple'>");
            out.println("<h3>This Month</h3>");
            out.println("<p class='value'>" + thisMonthAppointments + "</p>");
            out.println("</div>");
            
            out.println("</div>");
            out.println("</div>");
            
            // Footer
            out.println("<div style='text-align:center;margin-top:50px;padding-top:20px;border-top:2px solid #e0e0e0;color:#999'>");
            out.println("<p>© 2025 Healthcare Management System | Generated on " + reportDate + "</p>");
            out.println("<p>This report is confidential and for internal use only.</p>");
            out.println("</div>");
            
            out.println("</div>");
            out.println("</body></html>");

        } catch (Exception ex) {
            ex.printStackTrace();
            response.getWriter().println("<h2>Error generating report: " + ex.getMessage() + "</h2>");
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private int getCount(Connection conn, String sql) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                int count = rs.getInt(1);
                rs.close();
                stmt.close();
                return count;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private double getTotalRevenue(Connection conn) {
        try {
            String sql = "SELECT SUM(d.consultation_fee) as total FROM appointments a " +
                        "JOIN doctor_profiles d ON a.doctor_id = d.doctor_id " +
                        "WHERE a.appointment_status IN ('booked', 'completed')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double revenue = rs.getDouble("total");
                rs.close();
                stmt.close();
                return revenue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
