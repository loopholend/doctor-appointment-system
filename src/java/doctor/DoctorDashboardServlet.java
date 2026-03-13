package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/dashboard")
public class DoctorDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        if (NavHelper.requireDoctorSession(request, response) == null) return;
        
        // CHECK IF PROFILE EXISTS - NOW USING USERNAME
        String profileStatus = checkProfileStatus(request);
        
        if ("not_exists".equals(profileStatus)) {
            showProfileIncomplete(request, response);
        } else if ("pending".equals(profileStatus)) {
            showPendingPage(request, response);
        } else if ("approved".equals(profileStatus)) {
            showDashboard(request, response);
        } else if ("rejected".equals(profileStatus)) {
            showRejectedPage(request, response);
        } else {
            response.sendRedirect("profile-edit");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private String checkProfileStatus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("DEBUG: Session is null");
            return "not_exists";
        }
        
        String doctorUsername = (String) session.getAttribute("doctorUsername");
        System.out.println("DEBUG: Session doctorUsername = " + doctorUsername);
        
        if (doctorUsername == null) {
            System.out.println("DEBUG: doctorUsername is null in session");
            return "not_exists";
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT approval_status FROM doctor_profiles WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, doctorUsername);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("approval_status");
                System.out.println("DEBUG: Found profile with status = " + status);
                return status;
            } else {
                System.out.println("DEBUG: No profile found for username: " + doctorUsername);
                return "not_exists";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("DEBUG: Exception occurred: " + e.getMessage());
            return "not_exists";
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private int getDoctorIdFromUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT doctor_id FROM doctor_profiles WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }
    
    private void showProfileIncomplete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String doctorName = (String) session.getAttribute("doctorName");
        
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Profile Incomplete</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh;display:flex;align-items:center;justify-content:center}");
        out.println(".container{background:#FFFFFF;padding:60px 50px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;max-width:600px}");
        out.println(".warning-icon{font-size:80px;margin-bottom:24px;color:#D97706}");
        out.println("h1{color:#1E293B;margin-bottom:16px;font-size:28px;font-weight:700}");
        out.println("p{color:#64748B;font-size:15px;line-height:1.8;margin:16px 0}");
        out.println(".highlight{background:#FEF3C7;color:#92400E;padding:14px;border-radius:8px;margin:16px 0;font-weight:600;border:1px solid #FDE68A}");
        out.println(".btn{display:inline-block;padding:10px 24px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;transition:all 0.2s;font-size:14px;margin:8px}");
        out.println(".btn:hover{background:#1E40AF}");
        out.println(".btn-secondary{background:#64748B}");
        out.println(".btn-secondary:hover{background:#475569}");
        out.println("</style></head><body>");
        
        out.println("<div class='container'>");
        out.println("<div class='warning-icon'>⚠️</div>");
        out.println("<h1>Profile Incomplete</h1>");
        out.println("<p>Welcome, Dr. " + doctorName + "!</p>");
        out.println("<div class='highlight'>You need to complete your profile before accessing the dashboard.</div>");
        out.println("<p>Please fill in your professional details, specialization, experience, and other required information.</p>");
        out.println("<a href='profile-edit' class='btn'>Complete Profile Now</a>");
        out.println("<a href='logout' class='btn btn-secondary'>Logout</a>");
        out.println("</div></body></html>");
    }
    
    private void showPendingPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String doctorName = (String) session.getAttribute("doctorName");
        
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<meta http-equiv='refresh' content='10'>");
        out.println("<title>Pending Approval</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh;display:flex;align-items:center;justify-content:center}");
        out.println(".pending-container{background:#FFFFFF;padding:60px 50px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;max-width:600px}");
        out.println(".loading-icon{font-size:80px;margin-bottom:24px;animation:spin 2s linear infinite}");
        out.println("@keyframes spin{0%{transform:rotate(0deg)}100%{transform:rotate(360deg)}}");
        out.println("h1{color:#1E293B;margin-bottom:16px;font-size:28px;font-weight:700}");
        out.println(".pending-badge{background:#FEF3C7;color:#92400E;padding:8px 20px;border-radius:20px;display:inline-block;margin:16px 0;font-weight:600;font-size:14px;border:1px solid #FDE68A}");
        out.println("p{color:#64748B;font-size:15px;line-height:1.8;margin:16px 0}");
        out.println(".info-box{background:#EFF6FF;padding:20px;border-radius:8px;margin:24px 0;border-left:4px solid #2563EB}");
        out.println(".info-box ul{text-align:left;color:#64748B;line-height:2;padding-left:20px}");
        out.println(".btn{display:inline-block;padding:10px 24px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;transition:all 0.2s;font-size:14px;margin:8px}");
        out.println(".btn:hover{background:#1E40AF}");
        out.println(".btn-secondary{background:#64748B}");
        out.println(".btn-secondary:hover{background:#475569}");
        out.println("</style></head><body>");
        
        out.println("<div class='pending-container'>");
        out.println("<div class='loading-icon'>⏳</div>");
        out.println("<h1>Approval Pending</h1>");
        out.println("<div class='pending-badge'>AWAITING ADMIN APPROVAL</div>");
        out.println("<p>Welcome, <strong>Dr. " + doctorName + "</strong></p>");
        out.println("<p>Your profile has been submitted and is currently under review by our admin team.</p>");
        
        out.println("<div class='info-box'>");
        out.println("<p><strong>What happens next?</strong></p>");
        out.println("<ul>");
        out.println("<li>Admin will review your credentials</li>");
        out.println("<li>You will receive access once approved</li>");
        out.println("<li>This page refreshes automatically every 10 seconds</li>");
        out.println("</ul>");
        out.println("</div>");
        
        out.println("<a href='dashboard' class='btn'>Refresh Status</a>");
        out.println("<a href='logout' class='btn btn-secondary'>Logout</a>");
        out.println("</div></body></html>");
    }
    
    private void showRejectedPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String doctorName = (String) session.getAttribute("doctorName");
        
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Application Rejected</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh;display:flex;align-items:center;justify-content:center}");
        out.println(".rejected-container{background:#FFFFFF;padding:60px 50px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;max-width:600px}");
        out.println(".rejected-icon{font-size:80px;margin-bottom:24px;color:#DC2626}");
        out.println("h1{color:#DC2626;margin-bottom:16px;font-size:28px;font-weight:700}");
        out.println(".rejected-badge{background:#FEE2E2;color:#991B1B;padding:8px 20px;border-radius:20px;display:inline-block;margin:16px 0;font-weight:600;font-size:14px;border:1px solid #FECACA}");
        out.println("p{color:#64748B;font-size:15px;line-height:1.8;margin:16px 0}");
        out.println(".btn{display:inline-block;padding:10px 24px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;transition:all 0.2s;font-size:14px;margin:8px}");
        out.println(".btn:hover{background:#1E40AF}");
        out.println("</style></head><body>");
        
        out.println("<div class='rejected-container'>");
        out.println("<div class='rejected-icon'>✗</div>");
        out.println("<h1>Application Rejected</h1>");
        out.println("<div class='rejected-badge'>NOT APPROVED</div>");
        out.println("<p>Dear Dr. " + doctorName + ",</p>");
        out.println("<p>Unfortunately, your profile has not been approved by the admin. Please contact support for more information.</p>");
        out.println("<a href='logout' class='btn'>Logout</a>");
        out.println("</div></body></html>");
    }
    
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        String doctorName = (String) session.getAttribute("doctorName");
        String doctorUsername = (String) session.getAttribute("doctorUsername");
        
        int doctorId = getDoctorIdFromUsername(doctorUsername);

        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Doctor Dashboard</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px;text-align:center}");
        out.println(".cards-container{display:flex;flex-wrap:wrap;gap:20px;justify-content:center}");
        out.println(".card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;width:320px;padding:24px;transition:box-shadow 0.2s}");
        out.println(".card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.1)}");
        out.println(".patient-name{font-size:18px;font-weight:700;color:#1E293B;margin-bottom:8px}");
        out.println(".appointment-time{font-size:14px;color:#64748B;margin-bottom:6px}");
        out.println(".appointment-date{font-size:15px;color:#2563EB;font-weight:600;margin-bottom:16px}");
        out.println(".view-btn{width:100%;padding:10px 20px;background:#1D4ED8;color:white;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.2s;margin-bottom:8px}");
        out.println(".view-btn:hover{background:#1E40AF}");
        out.println(".card-actions{display:flex;gap:8px;margin-top:4px}");
        out.println(".done-btn{flex:1;padding:8px;background:#16A34A;color:white;border:none;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;transition:all 0.2s}");
        out.println(".done-btn:hover{background:#15803D}");
        out.println(".noshow-btn{flex:1;padding:8px;background:#DC2626;color:white;border:none;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;transition:all 0.2s}");
        out.println(".noshow-btn:hover{background:#B91C1C}");
        out.println(".no-appointments{text-align:center;padding:80px 20px}");
        out.println(".no-appointments h2{font-size:22px;color:#1E293B;margin-bottom:12px;font-weight:700}");
        out.println(".no-appointments p{font-size:15px;color:#64748B}");
        out.println("</style></head><body>");

        NavHelper.writeNavbar(out, "Doctor Dashboard");
        NavHelper.writeDoctorSidebar(out, doctorName, "patients");
        
        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>Upcoming Appointments</h1>");
        out.println("<div class='cards-container'>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // ✅ FIXED: Shows ALL future appointments, not just today
            String sql = "SELECT a.appointment_id, a.patient_account_username, a.appointment_time, a.appointment_date, p.full_name " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_account_username = p.username " +
                        "WHERE a.doctor_id = ? AND a.appointment_date >= CURDATE() AND a.appointment_status = 'booked' " +
                        "ORDER BY a.appointment_date, a.appointment_time";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();

            boolean hasAppointments = false;
            while (rs.next()) {
                hasAppointments = true;
                String patientName = rs.getString("full_name");
                String appointmentTime = rs.getString("appointment_time");
                String appointmentDate = rs.getString("appointment_date");
                String patientUsername = rs.getString("patient_account_username");
                
                out.println("<div class='card'>");
                out.println("<div class='patient-name'>" + NavHelper.esc(patientName) + "</div>");
                out.println("<div class='appointment-date'>📅 " + NavHelper.esc(appointmentDate) + "</div>");
                out.println("<div class='appointment-time'>⏰ " + NavHelper.esc(appointmentTime) + "</div>");
                out.println("<form action='patient-details' method='post'>");
                out.println("<input type='hidden' name='patientUsername' value='" + NavHelper.esc(patientUsername) + "'>");
                out.println("<button type='submit' class='view-btn'>View Details</button>");
                out.println("</form>");
                out.println("<div class='card-actions'>");
                out.println("<form action='complete-appointment' method='post' onsubmit='return confirm(\"Mark as completed?\")'>");
                out.println("<input type='hidden' name='appointmentId' value='" + rs.getInt("appointment_id") + "'>");
                out.println("<input type='hidden' name='action' value='complete'>");
                out.println("<button type='submit' class='done-btn'>&#10003; Completed</button>");
                out.println("</form>");
                out.println("<form action='complete-appointment' method='post' onsubmit='return confirm(\"Mark as no-show?\")'>");
                out.println("<input type='hidden' name='appointmentId' value='" + rs.getInt("appointment_id") + "'>");
                out.println("<input type='hidden' name='action' value='noshow'>");
                out.println("<button type='submit' class='noshow-btn'>&#10007; No-Show</button>");
                out.println("</form>");
                out.println("</div>");
                out.println("</div>");
            }
            
            if (!hasAppointments) {
                out.println("<div class='no-appointments'>");
                out.println("<h2>No Upcoming Appointments</h2>");
                out.println("<p>You don't have any booked appointments at this time.</p>");
                out.println("</div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div style='color:#1E293B;text-align:center;padding:20px'>");
            out.println("<h2>Error Loading Appointments</h2>");
            out.println("<p>Error: " + e.getMessage() + "</p>");
            out.println("</div>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        out.println("</div></div>");
        NavHelper.writeSidebarJS(out);
        out.println("</body></html>");
    }
}
