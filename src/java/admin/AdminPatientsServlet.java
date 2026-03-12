package admin;

import common.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/patients")
public class AdminPatientsServlet extends HttpServlet {
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
        out.println("<title>Manage Patients</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeAdminLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px}");
        out.println(".patients-container{display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:16px;max-width:1400px}");
        out.println(".patient-card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;padding:20px;transition:box-shadow 0.2s}");
        out.println(".patient-card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.1)}");
        out.println(".card-header{display:flex;align-items:center;gap:12px;margin-bottom:16px;padding-bottom:12px;border-bottom:1px solid #E2E8F0}");
        out.println(".patient-icon{width:48px;height:48px;background:#2563EB;color:white;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:700;flex-shrink:0}");
        out.println(".patient-name{font-size:16px;font-weight:700;color:#1E293B;margin-bottom:3px}");
        out.println(".username{color:#64748B;font-size:13px}");
        out.println(".patient-details{margin-top:12px}");
        out.println(".detail-row{display:flex;justify-content:space-between;margin:8px 0;font-size:13px}");
        out.println(".detail-label{color:#64748B;font-weight:600}");
        out.println(".detail-value{color:#1E293B;font-weight:600}");
        out.println(".actions{margin-top:16px}");
        out.println(".btn-delete{width:100%;padding:10px 20px;background:#DC2626;color:white;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.2s}");
        out.println(".btn-delete:hover{background:#B91C1C}");
        out.println(".no-patients{text-align:center;padding:80px 20px}");
        out.println(".no-patients h2{font-size:22px;color:#1E293B;margin-bottom:12px;font-weight:700}");
        out.println(".no-patients p{color:#64748B}");
        out.println(".stats{display:flex;gap:16px;margin-bottom:24px;flex-wrap:wrap}");
        out.println(".stat-card{background:#FFFFFF;padding:16px 24px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;min-width:140px}");
        out.println(".stat-number{font-size:32px;font-weight:700;color:#2563EB;margin-bottom:4px}");
        out.println(".stat-label{font-size:12px;color:#64748B;text-transform:uppercase;letter-spacing:0.5px;font-weight:600}");
        out.println("</style></head><body>");

        NavHelper.writeAdminNavbar(out, "Manage Patients");
        NavHelper.writeAdminSidebar(out, adminName, "patients");

        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>Manage Patients</h1>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            int totalPatients = 0;
            try (PreparedStatement s = conn.prepareStatement("SELECT COUNT(*) FROM patients");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) totalPatients = r.getInt(1);
            }

            out.println("<div class='stats'><div class='stat-card'>");
            out.println("<div class='stat-number'>" + totalPatients + "</div>");
            out.println("<div class='stat-label'>Total Patients</div>");
            out.println("</div></div>");

            pstmt = conn.prepareStatement("SELECT * FROM patients ORDER BY username");
            rs = pstmt.executeQuery();

            out.println("<div class='patients-container'>");
            boolean hasPatients = false;
            while (rs.next()) {
                hasPatients = true;
                String username    = rs.getString("username");
                String name        = rs.getString("full_name");
                String email       = rs.getString("email_address");
                String contact     = rs.getString("contact_number");
                String firstLetter = name.substring(0, 1).toUpperCase();

                out.println("<div class='patient-card'>");
                out.println("  <div class='card-header'>");
                out.println("    <div class='patient-icon'>" + NavHelper.esc(firstLetter) + "</div>");
                out.println("    <div><div class='patient-name'>" + NavHelper.esc(name) + "</div>");
                out.println("    <div class='username'>" + NavHelper.esc(username) + "</div></div>");
                out.println("  </div>");
                out.println("  <div class='patient-details'>");
                out.println("    <div class='detail-row'><span class='detail-label'>Email:</span><span class='detail-value'>" + NavHelper.esc(email) + "</span></div>");
                out.println("    <div class='detail-row'><span class='detail-label'>Contact:</span><span class='detail-value'>" + NavHelper.esc(contact) + "</span></div>");
                out.println("  </div>");
                out.println("  <div class='actions'>");
                out.println("    <form action='delete-patient' method='post' style='margin:0'>");
                out.println("      <input type='hidden' name='username' value='" + NavHelper.esc(username) + "'>");
                out.println("      <button type='submit' class='btn-delete' onclick='return confirm(\"Delete patient " + NavHelper.esc(name) + "?\")'>&#128465; Delete Patient</button>");
                out.println("    </form></div>");
                out.println("</div>");
            }

            if (!hasPatients) {
                out.println("<div class='no-patients'><h2>No Patients Found</h2><p>There are no patients registered yet.</p></div>");
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
