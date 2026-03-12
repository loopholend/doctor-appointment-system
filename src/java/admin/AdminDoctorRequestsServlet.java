package admin;

import common.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/doctor-requests")
public class AdminDoctorRequestsServlet extends HttpServlet {
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
        out.println("<title>Doctor Requests</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeAdminLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px}");
        out.println(".requests-container{max-width:1100px}");
        out.println(".request-card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;padding:24px;margin-bottom:16px;transition:box-shadow 0.2s}");
        out.println(".request-card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.1)}");
        out.println(".card-header{display:flex;justify-content:space-between;align-items:center;margin-bottom:20px;padding-bottom:16px;border-bottom:1px solid #E2E8F0}");
        out.println(".doctor-info{flex:1}");
        out.println(".doctor-name{font-size:20px;font-weight:700;color:#1E293B;margin-bottom:4px}");
        out.println(".doctor-specialty{color:#64748B;font-size:14px}");
        out.println(".pending-badge{background:#FEF3C7;color:#92400E;padding:4px 12px;border-radius:20px;font-weight:600;font-size:12px;border:1px solid #FDE68A}");
        out.println(".details-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:16px}");
        out.println(".detail-item{background:#F8FAFC;padding:12px 14px;border-radius:8px;border:1px solid #E2E8F0}");
        out.println(".detail-label{font-size:11px;color:#64748B;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:4px}");
        out.println(".detail-value{font-size:14px;color:#1E293B;font-weight:600}");
        out.println(".bio-section{background:#EFF6FF;padding:14px;border-radius:8px;margin-bottom:16px;border:1px solid #BFDBFE}");
        out.println(".bio-label{font-size:11px;color:#64748B;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px}");
        out.println(".bio-text{font-size:13px;color:#1E293B;line-height:1.6}");
        out.println(".profile-image{width:80px;height:80px;border-radius:50%;object-fit:cover;border:2px solid #E2E8F0;margin-right:16px}");
        out.println(".actions{display:flex;gap:12px;justify-content:flex-end}");
        out.println(".btn{padding:10px 20px;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;transition:all 0.2s;text-decoration:none;display:inline-block}");
        out.println(".btn-approve{background:#059669;color:white}.btn-approve:hover{background:#047857}");
        out.println(".btn-reject{background:#DC2626;color:white}.btn-reject:hover{background:#B91C1C}");
        out.println(".no-requests{text-align:center;padding:80px 20px}");
        out.println(".no-requests h2{font-size:22px;color:#1E293B;margin-bottom:12px;font-weight:700}");
        out.println(".no-requests p{color:#64748B}");
        out.println("</style></head><body>");

        NavHelper.writeAdminNavbar(out, "Doctor Requests");
        NavHelper.writeAdminSidebar(out, adminName, "requests");

        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>Pending Doctor Requests</h1>");
        out.println("<div class='requests-container'>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(
                "SELECT * FROM doctor_profiles WHERE approval_status = 'pending' ORDER BY created_at DESC");
            rs = pstmt.executeQuery();

            boolean hasRequests = false;
            while (rs.next()) {
                hasRequests = true;
                int doctorId             = rs.getInt("doctor_id");
                String username          = rs.getString("username");
                String email             = rs.getString("email_address");
                String fullName          = rs.getString("full_name");
                String gender            = rs.getString("gender");
                String dob               = rs.getString("date_of_birth");
                String license           = rs.getString("medical_license_number");
                String experience        = rs.getString("years_of_experience");
                String primarySpeciality = rs.getString("primary_specialty");
                String secondarySpec     = rs.getString("secondary_specialty");
                double fee               = rs.getDouble("consultation_fee");
                String timeSlot          = rs.getString("clinic_visit_schedule");
                String bio               = rs.getString("professional_bio");

                byte[] imageBytes = rs.getBytes("profile_image");
                String imageSrc = "";
                if (imageBytes != null && imageBytes.length > 0) {
                    String imageType = rs.getString("image_type");
                    if (imageType == null) imageType = "image/jpeg";
                    imageSrc = "data:" + imageType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
                }

                out.println("<div class='request-card'>");
                out.println("  <div class='card-header'>");
                if (!imageSrc.isEmpty())
                    out.println("  <img src='" + imageSrc + "' alt='Profile' class='profile-image'>");
                out.println("  <div class='doctor-info'>");
                out.println("    <div class='doctor-name'>Dr. " + NavHelper.esc(fullName) + "</div>");
                out.println("    <div class='doctor-specialty'>" + NavHelper.esc(primarySpeciality) + "</div>");
                out.println("  </div><div class='pending-badge'>PENDING</div></div>");

                out.println("  <div class='details-grid'>");
                detail(out, "Username",            username);
                detail(out, "Email",               email);
                detail(out, "Gender",              gender);
                detail(out, "Date of Birth",       dob);
                detail(out, "License Number",      license);
                detail(out, "Experience",          experience);
                detail(out, "Primary Speciality",  primarySpeciality);
                detail(out, "Secondary Speciality", secondarySpec != null ? secondarySpec : "None");
                detail(out, "Consultation Fee",    "Rs. " + fee);
                detail(out, "Clinic Hours",        timeSlot);
                out.println("  </div>");

                if (bio != null && !bio.trim().isEmpty()) {
                    out.println("  <div class='bio-section'><div class='bio-label'>Bio</div>");
                    out.println("  <div class='bio-text'>" + NavHelper.esc(bio) + "</div></div>");
                }

                out.println("  <div class='actions'>");
                out.println("    <form action='approve-doctor' method='post' style='display:inline'>");
                out.println("      <input type='hidden' name='doctorId' value='" + doctorId + "'>");
                out.println("      <button type='submit' class='btn btn-approve' onclick='return confirm(\"Approve this doctor?\")'>&#10003; Approve</button>");
                out.println("    </form>");
                out.println("    <form action='rejectdoctor' method='post' style='display:inline'>");
                out.println("      <input type='hidden' name='doctorId' value='" + doctorId + "'>");
                out.println("      <button type='submit' class='btn btn-reject' onclick='return confirm(\"Reject this doctor?\")'>&#10007; Reject</button>");
                out.println("    </form>");
                out.println("  </div></div>");
            }

            if (!hasRequests) {
                out.println("<div class='no-requests'><h2>No Pending Requests</h2>");
                out.println("<p>All doctor requests have been processed.</p></div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<p style='color:red'>Error: " + NavHelper.esc(e.getMessage()) + "</p>");
        } finally {
            try { if (rs != null) rs.close(); if (pstmt != null) pstmt.close(); if (conn != null) conn.close(); } catch (Exception ex) { ex.printStackTrace(); }
        }

        out.println("</div></div></body></html>");
    }

    private void detail(PrintWriter out, String label, String value) {
        out.println("<div class='detail-item'><div class='detail-label'>" + label + "</div>");
        out.println("<div class='detail-value'>" + NavHelper.esc(value) + "</div></div>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
