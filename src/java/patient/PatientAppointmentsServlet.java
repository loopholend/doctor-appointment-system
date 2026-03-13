package patient;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/patient/appointments")
public class PatientAppointmentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String patientName = NavHelper.requirePatientSession(request, response);
        if (patientName == null) return;
        HttpSession session = request.getSession(false);
        String patientUsername = (String) session.getAttribute("username");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>My Appointments</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px}");
        out.println(".appointments-container{max-width:900px}");
        out.println(".appointment-card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;padding:24px;margin-bottom:16px;transition:box-shadow 0.2s}");
        out.println(".appointment-card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.1)}");
        out.println(".card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;border-bottom:1px solid #E2E8F0;padding-bottom:16px}");
        out.println(".doctor-info{flex:1}");
        out.println(".doctor-name{font-size:18px;font-weight:700;color:#1E293B;margin-bottom:4px}");
        out.println(".specialty{color:#64748B;font-size:14px}");
        out.println(".status-badge{padding:4px 12px;border-radius:20px;font-weight:600;font-size:12px;text-transform:uppercase;letter-spacing:0.5px}");
        out.println(".status-booked{background:#DCFCE7;color:#166534}");
        out.println(".status-cancelled{background:#FEE2E2;color:#991B1B}");
        out.println(".appointment-details{display:grid;grid-template-columns:repeat(2,1fr);gap:12px;margin-bottom:16px}");
        out.println(".detail-item{background:#EFF6FF;padding:12px 16px;border-radius:8px}");
        out.println(".detail-label{font-size:11px;color:#64748B;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px}");
        out.println(".detail-value{font-size:15px;color:#1E293B;font-weight:600}");
        out.println(".cancel-btn{background:#DC2626;color:white;border:none;padding:10px 20px;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.2s}");
        out.println(".cancel-btn:hover{background:#B91C1C}");
        out.println(".no-appointments{text-align:center;padding:80px 20px}");
        out.println(".no-appointments h2{font-size:24px;color:#1E293B;margin-bottom:12px;font-weight:700}");
        out.println(".no-appointments p{font-size:15px;color:#64748B;margin-bottom:24px}");
        out.println(".btn-primary{display:inline-block;padding:10px 20px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;transition:all 0.2s;font-size:14px}");
        out.println(".btn-primary:hover{background:#1E40AF}");
        out.println("</style>");
        out.println("</head><body>");

        NavHelper.writeNavbar(out, "My Appointments");
        NavHelper.writePatientSidebar(out, patientName, "appointments");
        
        // Main content
        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>My Appointments</h1>");
        out.println("<div class='appointments-container'>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // Auto-cancel any booked appointments whose date has passed
            PreparedStatement cancelPast = conn.prepareStatement(
                "UPDATE appointments SET appointment_status = 'cancelled' " +
                "WHERE appointment_status = 'booked' AND appointment_date < CURDATE()"
            );
            cancelPast.executeUpdate();
            cancelPast.close();

            String sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.appointment_status, " +
                        "d.full_name, d.primary_specialty, d.consultation_fee, d.clinic_visit_schedule " +
                        "FROM appointments a " +
                        "JOIN doctor_profiles d ON a.doctor_id = d.doctor_id " +
                        "WHERE a.patient_account_username = ? " +
                        "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, patientUsername);
            rs = pstmt.executeQuery();

            boolean hasAppointments = false;
            while (rs.next()) {
                hasAppointments = true;
                
                out.println("<div class='appointment-card'>");
                
                // Card Header
                out.println("<div class='card-header'>");
                out.println("<div class='doctor-info'>");
                out.println("<div class='doctor-name'>Dr. " + NavHelper.esc(rs.getString("full_name")) + "</div>");
                out.println("<div class='specialty'>" + NavHelper.esc(rs.getString("primary_specialty")) + "</div>");
                out.println("</div>");
                
                String status = rs.getString("appointment_status");
                String statusClass = "booked".equals(status) ? "status-booked" : "status-cancelled";
                out.println("<div class='status-badge " + statusClass + "'>" + status + "</div>");
                out.println("</div>");
                
                // Appointment Details
                out.println("<div class='appointment-details'>");
                
                out.println("<div class='detail-item'>");
                out.println("<div class='detail-label'>Date</div>");
                out.println("<div class='detail-value'>" + rs.getDate("appointment_date") + "</div>");
                out.println("</div>");
                
                out.println("<div class='detail-item'>");
                out.println("<div class='detail-label'>Time</div>");
                out.println("<div class='detail-value'>" + NavHelper.esc(rs.getString("appointment_time")) + "</div>");
                out.println("</div>");
                
                out.println("<div class='detail-item'>");
                out.println("<div class='detail-label'>Consultation Fee</div>");
                out.println("<div class='detail-value'>Rs. " + rs.getDouble("consultation_fee") + "</div>");
                out.println("</div>");
                
                out.println("<div class='detail-item'>");
                out.println("<div class='detail-label'>Clinic Hours</div>");
                out.println("<div class='detail-value'>" + NavHelper.esc(rs.getString("clinic_visit_schedule")) + "</div>");
                out.println("</div>");
                
                out.println("</div>");
                
                // Cancel button (only for booked appointments)
                if ("booked".equals(status)) {
                    out.println("<form action='cancel-appointment' method='post' style='margin-top:15px' onsubmit='return confirm(\"Are you sure you want to cancel this appointment?\")'>");
                    out.println("<input type='hidden' name='appointmentId' value='" + rs.getInt("appointment_id") + "'>");
                    out.println("<button type='submit' class='cancel-btn'>Cancel Appointment</button>");
                    out.println("</form>");
                }
                
                out.println("</div>");
            }
            
            if (!hasAppointments) {
                out.println("<div class='no-appointments'>");
                out.println("<h2>No Appointments Yet</h2>");
                out.println("<p>You haven't booked any appointments.</p>");
                out.println("<a href='dashboard' class='btn-primary'>Browse Doctors</a>");
                out.println("</div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div style='color: #1E293B; text-align: center; padding: 20px;'>");
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

        out.println("</div>");
        out.println("</div>");
        NavHelper.writeSidebarJS(out);
        out.println("</body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
