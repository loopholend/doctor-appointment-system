package doctor;

import common.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/appointments")
public class DoctorAppointmentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String doctorName = NavHelper.requireDoctorSession(request, response);
        if (doctorName == null) return;

        HttpSession session = request.getSession(false);
        int doctorId = (Integer) session.getAttribute("doctorId");

        String filterStatus = request.getParameter("status");
        if (filterStatus == null || filterStatus.trim().isEmpty()) filterStatus = "all";

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>My Appointments</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px}");
        out.println(".filter-bar{background:#FFFFFF;border:1px solid #E2E8F0;border-radius:12px;padding:16px 20px;margin-bottom:24px;display:flex;gap:10px;flex-wrap:wrap;align-items:center}");
        out.println(".filter-label{font-size:13px;font-weight:600;color:#64748B;margin-right:4px}");
        out.println(".filter-btn{padding:7px 18px;border-radius:20px;border:1px solid #E2E8F0;font-size:13px;font-weight:600;cursor:pointer;text-decoration:none;transition:all 0.2s}");
        out.println(".filter-btn.active{background:#2563EB;color:white;border-color:#2563EB}");
        out.println(".filter-btn:not(.active){background:#F8FAFC;color:#64748B}");
        out.println(".filter-btn:not(.active):hover{background:#E2E8F0;color:#1E293B}");
        out.println(".appointment-card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.08);border:1px solid #E2E8F0;padding:20px 24px;margin-bottom:14px;display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px}");
        out.println(".patient-info .name{font-size:17px;font-weight:700;color:#1E293B}");
        out.println(".patient-info .sub{font-size:13px;color:#64748B;margin-top:3px}");
        out.println(".appt-meta{display:flex;gap:20px;align-items:center;flex-wrap:wrap}");
        out.println(".meta-item{font-size:14px;color:#1E293B}");
        out.println(".meta-item span{font-weight:600}");
        out.println(".status-badge{padding:4px 12px;border-radius:20px;font-weight:600;font-size:12px;text-transform:uppercase;letter-spacing:0.5px;white-space:nowrap}");
        out.println(".status-booked{background:#DCFCE7;color:#166534}");
        out.println(".status-cancelled{background:#FEE2E2;color:#991B1B}");
        out.println(".status-completed{background:#DBEAFE;color:#1D4ED8}");
        out.println(".status-no_show{background:#FEF3C7;color:#92400E}");
        out.println(".complete-btn{padding:8px 18px;background:#16A34A;color:white;border:none;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;transition:all 0.2s;white-space:nowrap}");
        out.println(".complete-btn:hover{background:#15803D}");
        out.println(".noshow-btn{padding:8px 18px;background:#DC2626;color:white;border:none;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;transition:all 0.2s;white-space:nowrap}");
        out.println(".noshow-btn:hover{background:#B91C1C}");
        out.println(".action-btns{display:flex;gap:8px;flex-wrap:wrap}");
        out.println(".no-appts{text-align:center;padding:80px 20px}");
        out.println(".no-appts h2{font-size:22px;color:#1E293B;margin-bottom:10px;font-weight:700}");
        out.println(".no-appts p{font-size:15px;color:#64748B}");
        out.println("</style></head><body>");

        NavHelper.writeNavbar(out, "My Appointments");
        NavHelper.writeDoctorSidebar(out, doctorName, "appointments");

        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>My Appointments</h1>");

        // Status filter tabs
        String base = "appointments?status=";
        out.println("<div class='filter-bar'>");
        out.println("<span class='filter-label'>Filter:</span>");
        out.println("<a href='" + base + "all'       class='filter-btn" + ("all".equals(filterStatus)       ? " active" : "") + "'>All</a>");
        out.println("<a href='" + base + "booked'    class='filter-btn" + ("booked".equals(filterStatus)    ? " active" : "") + "'>Booked</a>");
        out.println("<a href='" + base + "completed' class='filter-btn" + ("completed".equals(filterStatus) ? " active" : "") + "'>Completed</a>");
        out.println("<a href='" + base + "no_show'   class='filter-btn" + ("no_show".equals(filterStatus)   ? " active" : "") + "'>No-Show</a>");
        out.println("<a href='" + base + "cancelled' class='filter-btn" + ("cancelled".equals(filterStatus) ? " active" : "") + "'>Cancelled</a>");
        out.println("</div>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String sql;
            if ("all".equals(filterStatus)) {
                sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.appointment_status, " +
                      "p.full_name AS patient_name, p.username AS patient_username " +
                      "FROM appointments a " +
                      "JOIN patients p ON a.patient_account_username = p.username " +
                      "WHERE a.doctor_id = ? " +
                      "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, doctorId);
            } else {
                sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.appointment_status, " +
                      "p.full_name AS patient_name, p.username AS patient_username " +
                      "FROM appointments a " +
                      "JOIN patients p ON a.patient_account_username = p.username " +
                      "WHERE a.doctor_id = ? AND a.appointment_status = ? " +
                      "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, doctorId);
                pstmt.setString(2, filterStatus);
            }

            rs = pstmt.executeQuery();
            boolean hasAny = false;

            while (rs.next()) {
                hasAny = true;
                String status = rs.getString("appointment_status");
                String statusClass = "status-" + status;
                String patientDisplayName = rs.getString("patient_name");
                String patientUsername    = rs.getString("patient_username");

                out.println("<div class='appointment-card'>");

                out.println("<div class='patient-info'>");
                out.println("  <div class='name'>" + NavHelper.esc(patientDisplayName) + "</div>");
                out.println("  <div class='sub'>@" + NavHelper.esc(patientUsername) + "</div>");
                out.println("</div>");

                out.println("<div class='appt-meta'>");
                out.println("  <div class='meta-item'>&#128197; <span>" + rs.getDate("appointment_date") + "</span></div>");
                out.println("  <div class='meta-item'>&#9200; <span>" + NavHelper.esc(rs.getString("appointment_time")) + "</span></div>");
                out.println("  <div class='status-badge " + statusClass + "'>" + status.toUpperCase() + "</div>");
                out.println("</div>");

                if (AppointmentStatus.BOOKED.equals(status)) {
                    out.println("<div class='action-btns'>");
                    out.println("<form action='complete-appointment' method='post' onsubmit='return confirm(\"Mark as completed? This means the patient showed up.\")'>");
                    out.println("  <input type='hidden' name='appointmentId' value='" + rs.getInt("appointment_id") + "'>");
                    out.println("  <input type='hidden' name='action' value='complete'>");
                    out.println("  <button type='submit' class='complete-btn'>&#10003; Completed</button>");
                    out.println("</form>");
                    out.println("<form action='complete-appointment' method='post' onsubmit='return confirm(\"Mark as no-show? This means the patient did not show up.\")'>");
                    out.println("  <input type='hidden' name='appointmentId' value='" + rs.getInt("appointment_id") + "'>");
                    out.println("  <input type='hidden' name='action' value='noshow'>");
                    out.println("  <button type='submit' class='noshow-btn'>&#10007; No-Show</button>");
                    out.println("</form>");
                    out.println("</div>");
                }

                out.println("</div>");
            }

            if (!hasAny) {
                out.println("<div class='no-appts'>");
                out.println("<h2>No Appointments Found</h2>");
                out.println("<p>There are no appointments matching this filter.</p>");
                out.println("</div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p style='color:#DC2626;padding:20px'>Error loading appointments: " + NavHelper.esc(e.getMessage()) + "</p>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        out.println("</div>");
        NavHelper.writeSidebarJS(out);
        out.println("</body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
