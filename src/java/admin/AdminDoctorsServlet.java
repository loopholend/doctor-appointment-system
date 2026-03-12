package admin;

import common.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/doctors")
public class AdminDoctorsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String adminName = NavHelper.requireAdminSession(request, response);
        if (adminName == null) return;

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Manage Doctors</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeAdminLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px}");
        out.println(".doctors-container{display:grid;grid-template-columns:repeat(auto-fill,minmax(320px,1fr));gap:16px;max-width:1400px}");
        out.println(".doctor-card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;padding:20px;transition:box-shadow 0.2s}");
        out.println(".doctor-card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.1)}");
        out.println(".card-header{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px;padding-bottom:12px;border-bottom:1px solid #E2E8F0}");
        out.println(".doctor-name{font-size:17px;font-weight:700;color:#1E293B;margin-bottom:4px}");
        out.println(".username{color:#64748B;font-size:13px;margin-bottom:4px}");
        out.println(".speciality{color:#2563EB;font-size:14px;font-weight:600;margin-top:4px}");
        out.println(".status-badge{padding:4px 10px;border-radius:20px;font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;white-space:nowrap}");
        out.println(".status-approved{background:#DCFCE7;color:#166534}");
        out.println(".status-pending{background:#FEF3C7;color:#92400E}");
        out.println(".status-rejected{background:#FEE2E2;color:#991B1B}");
        out.println(".doctor-info{margin-top:12px}");
        out.println(".info-row{display:flex;justify-content:space-between;margin:6px 0;font-size:13px}");
        out.println(".info-label{color:#64748B;font-weight:600}");
        out.println(".info-value{color:#1E293B;font-weight:600}");
        out.println(".actions{margin-top:16px}");
        out.println(".btn-delete{width:100%;padding:10px 20px;background:#DC2626;color:white;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.2s}");
        out.println(".btn-delete:hover{background:#B91C1C}");
        out.println(".no-doctors{text-align:center;padding:80px 20px}");
        out.println(".no-doctors h2{font-size:22px;color:#1E293B;margin-bottom:12px;font-weight:700}");
        out.println(".no-doctors p{color:#64748B}");
        out.println(".stats{display:flex;gap:16px;margin-bottom:24px;flex-wrap:wrap}");
        out.println(".stat-card{background:#FFFFFF;padding:16px 24px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;min-width:140px}");
        out.println(".stat-number{font-size:32px;font-weight:700;color:#2563EB;margin-bottom:4px}");
        out.println(".stat-label{font-size:12px;color:#64748B;text-transform:uppercase;letter-spacing:0.5px;font-weight:600}");
        out.println("</style></head><body>");

        NavHelper.writeAdminNavbar(out, "Manage Doctors");
        NavHelper.writeAdminSidebar(out, adminName, "doctors");

        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>Manage Doctors</h1>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            int totalDoctors = 0;
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM doctor_profiles");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) totalDoctors = r.getInt(1);
            }

            out.println("<div class='stats'><div class='stat-card'>");
            out.println("<div class='stat-number'>" + totalDoctors + "</div>");
            out.println("<div class='stat-label'>Total Doctors</div>");
            out.println("</div></div>");

            pstmt = conn.prepareStatement("SELECT * FROM doctor_profiles ORDER BY created_at DESC");
            rs = pstmt.executeQuery();

            out.println("<div class='doctors-container'>");
            boolean hasDoctors = false;
            while (rs.next()) {
                hasDoctors = true;
                int doctorId         = rs.getInt("doctor_id");
                String username      = rs.getString("username");
                String fullName      = rs.getString("full_name");
                String specialty     = rs.getString("primary_specialty");
                String status        = rs.getString("approval_status");
                String experience    = rs.getString("years_of_experience");
                double fee           = rs.getDouble("consultation_fee");

                String statusClass = "pending".equals(status) ? "status-pending"
                                   : "rejected".equals(status) ? "status-rejected" : "status-approved";

                out.println("<div class='doctor-card'>");
                out.println("  <div class='card-header'><div>");
                out.println("    <div class='doctor-name'>Dr. " + NavHelper.esc(fullName) + "</div>");
                out.println("    <div class='username'>" + NavHelper.esc(username) + "</div>");
                out.println("    <div class='speciality'>&#129658; " + NavHelper.esc(specialty) + "</div>");
                out.println("  </div><span class='status-badge " + statusClass + "'>" + status + "</span></div>");
                out.println("  <div class='doctor-info'>");
                out.println("    <div class='info-row'><span class='info-label'>Doctor ID:</span><span class='info-value'>" + doctorId + "</span></div>");
                out.println("    <div class='info-row'><span class='info-label'>Experience:</span><span class='info-value'>" + NavHelper.esc(experience) + "</span></div>");
                out.println("    <div class='info-row'><span class='info-label'>Consultation Fee:</span><span class='info-value'>Rs. " + fee + "</span></div>");
                out.println("  </div>");
                out.println("  <div class='actions'>");
                out.println("    <form action='delete-doctor' method='post' style='margin:0'>");
                out.println("      <input type='hidden' name='doctorId' value='" + doctorId + "'>");
                out.println("      <button type='submit' class='btn-delete' onclick='return confirm(\"Delete Dr. " + NavHelper.esc(fullName) + "?\")'>&#128465; Delete Doctor</button>");
                out.println("    </form></div>");
                out.println("</div>");
            }

            if (!hasDoctors) {
                out.println("<div class='no-doctors'><h2>No Doctors Found</h2><p>There are no doctors in the system yet.</p></div>");
            }
            out.println("</div>");

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p style='color:red'>Error: " + NavHelper.esc(e.getMessage()) + "</p>");
        } finally {
            try { if (rs != null) rs.close(); if (pstmt != null) pstmt.close(); if (conn != null) conn.close(); } catch (Exception ex) { ex.printStackTrace(); }
        }

        out.println("</div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
