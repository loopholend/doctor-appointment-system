package admin;

import common.*;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet("/admin/add-doctor")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize       = 1024 * 1024 * 10,
    maxRequestSize    = 1024 * 1024 * 50
)
public class AdminAddDoctorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String adminName = NavHelper.requireAdminSession(request, response);
        if (adminName == null) return;
        response.setContentType("text/html; charset=UTF-8");
        renderForm(response.getWriter(), adminName, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String adminName = NavHelper.requireAdminSession(request, response);
        if (adminName == null) return;

        String username      = request.getParameter("username");
        String email         = request.getParameter("email");
        String password      = request.getParameter("password");
        String fullName      = request.getParameter("fullName");
        String gender        = request.getParameter("gender");
        String dob           = request.getParameter("dob");
        String license       = request.getParameter("license");
        String experience    = request.getParameter("experience");
        String primarySpec   = request.getParameter("primarySpeciality");
        String secondarySpec = request.getParameter("secondarySpeciality");
        String fee           = request.getParameter("fee");
        String timeSlot      = request.getParameter("timeSlot");
        String bio           = request.getParameter("bio");

        if (isBlank(username) || isBlank(email) || isBlank(password) ||
            isBlank(fullName) || isBlank(gender) || isBlank(dob) ||
            isBlank(license)  || isBlank(experience) || isBlank(primarySpec) ||
            isBlank(fee)      || isBlank(timeSlot)   || isBlank(bio)) {
            response.setContentType("text/html; charset=UTF-8");
            renderForm(response.getWriter(), adminName, "All required fields must be filled.");
            return;
        }

        Part filePart = request.getPart("profileImage");
        InputStream imageStream = null;
        String imageType = null;
        if (filePart != null && filePart.getSize() > 0) {
            String ct = filePart.getContentType();
            if (ct != null && (ct.startsWith("image/jpeg") || ct.startsWith("image/png") ||
                               ct.startsWith("image/gif")  || ct.startsWith("image/webp"))) {
                imageStream = filePart.getInputStream();
                imageType = ct;
            }
        }

        String secondaryVal = isBlank(secondarySpec) ? "none" : secondarySpec.trim();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert account credentials
            try (PreparedStatement acctPs = conn.prepareStatement(
                    "INSERT INTO doctor_accounts (username, email_address, password_hash) VALUES (?,?,?)")) {
                acctPs.setString(1, username.trim());
                acctPs.setString(2, email.trim());
                acctPs.setString(3, PasswordUtil.hashPassword(password));
                acctPs.executeUpdate();
            }

            // 2. Insert profile — admin-added doctors are pre-approved
            if (imageStream != null) {
                ps = conn.prepareStatement(
                    "INSERT INTO doctor_profiles (username,email_address,full_name,gender,date_of_birth," +
                    "medical_license_number,years_of_experience,primary_specialty,secondary_specialty," +
                    "consultation_fee,clinic_visit_schedule,professional_bio,profile_image,image_type,approval_status)" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'approved')");
                ps.setString(1, username.trim());
                ps.setString(2, email.trim());
                ps.setString(3, fullName.trim());
                ps.setString(4, gender);
                ps.setString(5, dob);
                ps.setString(6, license.trim());
                ps.setString(7, experience.trim());
                ps.setString(8, primarySpec.trim());
                ps.setString(9, secondaryVal);
                ps.setDouble(10, Double.parseDouble(fee));
                ps.setString(11, timeSlot.trim());
                ps.setString(12, bio.trim());
                ps.setBlob(13, imageStream);
                ps.setString(14, imageType);
            } else {
                ps = conn.prepareStatement(
                    "INSERT INTO doctor_profiles (username,email_address,full_name,gender,date_of_birth," +
                    "medical_license_number,years_of_experience,primary_specialty,secondary_specialty," +
                    "consultation_fee,clinic_visit_schedule,professional_bio,approval_status)" +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,'approved')");
                ps.setString(1, username.trim());
                ps.setString(2, email.trim());
                ps.setString(3, fullName.trim());
                ps.setString(4, gender);
                ps.setString(5, dob);
                ps.setString(6, license.trim());
                ps.setString(7, experience.trim());
                ps.setString(8, primarySpec.trim());
                ps.setString(9, secondaryVal);
                ps.setDouble(10, Double.parseDouble(fee));
                ps.setString(11, timeSlot.trim());
                ps.setString(12, bio.trim());
            }
            ps.executeUpdate();
            conn.commit();
            response.sendRedirect("doctors?added=1");

        } catch (SQLIntegrityConstraintViolationException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            response.setContentType("text/html; charset=UTF-8");
            renderForm(response.getWriter(), adminName, "Username, email, or Medical License Number already exists.");
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
            renderForm(response.getWriter(), adminName, "An error occurred: " + NavHelper.esc(e.getMessage()));
        } finally {
            if (imageStream != null) try { imageStream.close(); } catch (Exception ex) {}
            if (ps != null) try { ps.close(); } catch (Exception ex) {}
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ex) {}
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private void renderForm(PrintWriter out, String adminName, String errorMsg) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Add Doctor — MediCare+</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeAdminLayoutCSS(out);
        out.println(".form-card{background:#FFFFFF;padding:32px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;max-width:600px;margin:0 auto}");
        out.println("h1{text-align:center;color:#1E293B;margin-bottom:4px;font-size:22px;font-weight:700}");
        out.println(".subtitle{text-align:center;color:#64748B;font-size:13px;margin-bottom:24px}");
        out.println(".section-title{font-size:11px;font-weight:700;color:#2563EB;text-transform:uppercase;letter-spacing:0.6px;margin:22px 0 10px;padding-bottom:6px;border-bottom:2px solid #EFF6FF}");
        out.println("label{display:block;margin-top:12px;font-weight:600;color:#1E293B;font-size:13px}");
        out.println(".req{color:#EF4444;margin-left:2px}");
        out.println("input[type='text'],input[type='email'],input[type='password'],input[type='date'],input[type='number'],input[type='file'],select,textarea{width:100%;padding:10px 12px;margin-top:4px;border-radius:8px;border:1px solid #E2E8F0;font-size:14px;outline:none;transition:all 0.2s;box-sizing:border-box;color:#1E293B;background:#FFFFFF;font-family:'Inter','Poppins',sans-serif}");
        out.println("input:focus,select:focus,textarea:focus{border-color:#2563EB;box-shadow:0 0 0 3px rgba(37,99,235,0.1)}");
        out.println("textarea{resize:vertical;min-height:80px}");
        out.println(".row2{display:grid;grid-template-columns:1fr 1fr;gap:14px}");
        out.println(".error-box{background:#FEF2F2;border:1px solid #FECACA;color:#B91C1C;padding:12px 16px;border-radius:8px;margin-bottom:16px;font-size:13px;font-weight:500}");
        out.println(".approved-badge{display:inline-block;background:#DCFCE7;color:#16A34A;padding:4px 12px;border-radius:20px;font-size:12px;font-weight:600;margin-bottom:20px}");
        out.println(".btn-submit{display:block;width:100%;margin-top:28px;background:#1D4ED8;color:white;border:none;padding:13px;font-size:15px;font-weight:600;border-radius:8px;cursor:pointer;transition:all 0.2s;font-family:'Inter','Poppins',sans-serif}");
        out.println(".btn-submit:hover{background:#1E40AF}");
        out.println(".img-note{font-size:12px;color:#64748B;margin-top:4px;font-style:italic}");
        out.println("#imgPreview{display:none;margin:10px auto 0;width:80px;height:80px;border-radius:50%;object-fit:cover;border:2px solid #E2E8F0;display:none}");
        out.println("</style></head><body>");

        NavHelper.writeAdminNavbar(out, "Add Doctor");
        NavHelper.writeAdminSidebar(out, adminName, "add-doctor");

        out.println("<div class='main-content'><div class='form-card'>");
        out.println("<h1>&#128104;&#8205;&#9877; Add New Doctor</h1>");
        out.println("<p class='subtitle'>Doctor will be <span class='approved-badge'>&#9989; Immediately Approved</span></p>");

        if (errorMsg != null) out.println("<div class='error-box'>&#9888;&#65039; " + errorMsg + "</div>");

        out.println("<form action='add-doctor' method='post' enctype='multipart/form-data'>");

        out.println("<div class='section-title'>Account Credentials</div>");
        out.println("<div class='row2'>");
        out.println("<div><label>Username <span class='req'>*</span></label><input type='text' name='username' placeholder='e.g. dr_john' required></div>");
        out.println("<div><label>Email <span class='req'>*</span></label><input type='email' name='email' placeholder='doctor@email.com' required></div>");
        out.println("</div>");
        out.println("<div class='row2'>");
        out.println("<div><label>Password <span class='req'>*</span></label><input type='password' name='password' id='pwd' placeholder='Set a strong password' required></div>");
        out.println("<div><label>Confirm Password <span class='req'>*</span></label><input type='password' name='confirmPassword' id='cpwd' placeholder='Repeat password' required></div>");
        out.println("</div>");

        out.println("<div class='section-title'>Personal Information</div>");
        out.println("<label>Full Name <span class='req'>*</span></label>");
        out.println("<input type='text' name='fullName' placeholder='Dr. First Last' required>");
        out.println("<div class='row2'>");
        out.println("<div><label>Gender <span class='req'>*</span></label><select name='gender' required><option value=''>Select gender</option><option value='male'>Male</option><option value='female'>Female</option><option value='other'>Other</option></select></div>");
        out.println("<div><label>Date of Birth <span class='req'>*</span></label><input type='date' name='dob' required></div>");
        out.println("</div>");

        out.println("<div class='section-title'>Professional Details</div>");
        out.println("<div class='row2'>");
        out.println("<div><label>Medical License No. <span class='req'>*</span></label><input type='text' name='license' placeholder='License number' required></div>");
        out.println("<div><label>Years of Experience <span class='req'>*</span></label><input type='text' name='experience' placeholder='e.g. 8 years' required></div>");
        out.println("</div>");
        out.println("<div class='row2'>");
        out.println("<div><label>Primary Specialty <span class='req'>*</span></label><input type='text' name='primarySpeciality' placeholder='e.g. Cardiologist' required></div>");
        out.println("<div><label>Secondary Specialty</label><input type='text' name='secondarySpeciality' placeholder='Optional'></div>");
        out.println("</div>");
        out.println("<div class='row2'>");
        out.println("<div><label>Consultation Fee (&#8377;) <span class='req'>*</span></label><input type='number' name='fee' min='0' step='0.01' placeholder='Amount' required></div>");
        out.println("<div><label>Clinic Time Slot <span class='req'>*</span></label><input type='text' name='timeSlot' placeholder='e.g. 10am–2pm' required></div>");
        out.println("</div>");
        out.println("<label>Short Bio <span class='req'>*</span></label>");
        out.println("<textarea name='bio' placeholder='Brief professional background...' required></textarea>");
        out.println("<label>Profile Image</label>");
        out.println("<input type='file' name='profileImage' accept='image/*' id='imgFile'>");
        out.println("<div class='img-note'>Optional — doctor can upload later from their profile page</div>");
        out.println("<img id='imgPreview' alt='Preview'>");

        out.println("<button type='submit' class='btn-submit'>&#10003; Add Doctor</button>");
        out.println("</form></div></div>");

        out.println("<script>");
        out.println("document.getElementById('imgFile').addEventListener('change',function(){");
        out.println("  var f=this.files[0];if(!f)return;");
        out.println("  var r=new FileReader();r.onload=function(e){var i=document.getElementById('imgPreview');i.src=e.target.result;i.style.display='block';};");
        out.println("  r.readAsDataURL(f);");
        out.println("});");
        out.println("document.querySelector('form').addEventListener('submit',function(e){");
        out.println("  if(document.getElementById('pwd').value!==document.getElementById('cpwd').value){");
        out.println("    e.preventDefault();alert('Passwords do not match!');");
        out.println("  }");
        out.println("});");
        out.println("</script></body></html>");
    }
}
