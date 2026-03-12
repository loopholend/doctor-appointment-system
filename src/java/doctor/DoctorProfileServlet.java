package doctor;

import common.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/profile")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class DoctorProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get doctor info from session
        HttpSession session = request.getSession(false);
        String doctorUsername = "";
        String doctorEmail = "";
        
        if (session != null) {
            doctorUsername = (String) session.getAttribute("doctorUsername");
            doctorEmail = (String) session.getAttribute("doctorEmail");
        }
        
        if (doctorUsername == null || doctorUsername.isEmpty()) {
            response.sendRedirect("login.html");
            return;
        }

        String fullName = request.getParameter("fullName");
        String gender = request.getParameter("gender");
        String dob = request.getParameter("dob");
        String license = request.getParameter("license");
        String experience = request.getParameter("experience");
        String primarySpeciality = request.getParameter("primarySpeciality");
        String secondarySpeciality = request.getParameter("secondarySpeciality");
        String customSpeciality = request.getParameter("customSpeciality");
        String fee = request.getParameter("fee");
        String timeSlot = request.getParameter("timeSlot");
        String bio = request.getParameter("bio");

        if ("custom".equals(secondarySpeciality) && customSpeciality != null && !customSpeciality.trim().isEmpty()) {
            secondarySpeciality = customSpeciality;
        }

        Part filePart = request.getPart("profileImage");
        InputStream imageInputStream = null;
        String imageType = null;

        if (filePart != null && filePart.getSize() > 0) {
            String contentType = filePart.getContentType();
            // Only allow image MIME types
            if (contentType == null || (!contentType.startsWith("image/jpeg") &&
                    !contentType.startsWith("image/png") &&
                    !contentType.startsWith("image/gif") &&
                    !contentType.startsWith("image/webp"))) {
                showError(response, "Invalid file type. Only JPEG, PNG, GIF, and WebP images are allowed.", "profile-edit");
                return;
            }
            imageInputStream = filePart.getInputStream();
            imageType = contentType;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            String checkSql = "SELECT doctor_id FROM doctor_profiles WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, doctorUsername);
            ResultSet rs = checkStmt.executeQuery();
            boolean isUpdate = rs.next();
            rs.close();
            checkStmt.close();

            if (isUpdate && imageInputStream == null) {
                // Update without changing image - license number is immutable
                String sql = "UPDATE doctor_profiles SET email_address=?, full_name=?, gender=?, date_of_birth=?, " +
                            "years_of_experience=?, primary_specialty=?, secondary_specialty=?, " +
                            "consultation_fee=?, clinic_visit_schedule=?, professional_bio=?, approval_status='pending' WHERE username=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, doctorEmail);
                pstmt.setString(2, fullName);
                pstmt.setString(3, gender);
                pstmt.setString(4, dob);
                pstmt.setString(5, experience);
                pstmt.setString(6, primarySpeciality);
                pstmt.setString(7, secondarySpeciality);
                pstmt.setDouble(8, Double.parseDouble(fee));
                pstmt.setString(9, timeSlot);
                pstmt.setString(10, bio);
                pstmt.setString(11, doctorUsername);
                
            } else if (isUpdate && imageInputStream != null) {
                // Update with new image - license number is immutable
                String sql = "UPDATE doctor_profiles SET email_address=?, full_name=?, gender=?, date_of_birth=?, " +
                            "years_of_experience=?, primary_specialty=?, secondary_specialty=?, " +
                            "consultation_fee=?, clinic_visit_schedule=?, professional_bio=?, profile_image=?, image_type=?, approval_status='pending' WHERE username=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, doctorEmail);
                pstmt.setString(2, fullName);
                pstmt.setString(3, gender);
                pstmt.setString(4, dob);
                pstmt.setString(5, experience);
                pstmt.setString(6, primarySpeciality);
                pstmt.setString(7, secondarySpeciality);
                pstmt.setDouble(8, Double.parseDouble(fee));
                pstmt.setString(9, timeSlot);
                pstmt.setString(10, bio);
                pstmt.setBlob(11, imageInputStream);
                pstmt.setString(12, imageType);
                pstmt.setString(13, doctorUsername);
                
            } else {
                // Insert new doctor - NO doctor_id needed (auto-generated)
                String sql = "INSERT INTO doctor_profiles (username, email_address, full_name, gender, date_of_birth, medical_license_number, " +
                            "years_of_experience, primary_specialty, secondary_specialty, " +
                            "consultation_fee, clinic_visit_schedule, professional_bio, profile_image, image_type, approval_status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending')";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, doctorUsername);
                pstmt.setString(2, doctorEmail);
                pstmt.setString(3, fullName);
                pstmt.setString(4, gender);
                pstmt.setString(5, dob);
                pstmt.setString(6, license);
                pstmt.setString(7, experience);
                pstmt.setString(8, primarySpeciality);
                pstmt.setString(9, secondarySpeciality);
                pstmt.setDouble(10, Double.parseDouble(fee));
                pstmt.setString(11, timeSlot);
                pstmt.setString(12, bio);
                pstmt.setBlob(13, imageInputStream);
                pstmt.setString(14, imageType);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                response.setContentType("text/html");
                response.getWriter().println("<!DOCTYPE html><html><head>");
                response.getWriter().println("<meta charset='UTF-8'>");
                response.getWriter().println("<title>Submitted</title>");
                response.getWriter().println("<style>");
                response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                response.getWriter().println(".success-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
                response.getWriter().println(".success-icon{font-size:80px;margin-bottom:20px;color:#ff9800}");
                response.getWriter().println("h2{color:#2c3e50;margin-bottom:15px;font-size:28px}");
                response.getWriter().println("p{color:#666;margin-bottom:20px;font-size:16px;line-height:1.6}");
                response.getWriter().println(".pending-badge{background:#fff3cd;color:#856404;padding:10px 20px;border-radius:20px;display:inline-block;margin:20px 0;font-weight:bold}");
                response.getWriter().println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px;margin:5px}");
                response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
                response.getWriter().println("</style></head><body>");
                response.getWriter().println("<div class='success-box'>");
                response.getWriter().println("<div class='success-icon'>⏳</div>");
                response.getWriter().println("<h2>" + (isUpdate ? "Profile Updated!" : "Registration Submitted!") + "</h2>");
                response.getWriter().println("<div class='pending-badge'>PENDING APPROVAL</div>");
                response.getWriter().println("<p><strong>Doctor Name:</strong> " + fullName + "</p>");
                response.getWriter().println("<p>Your profile has been submitted to the admin for approval. You will be able to access your dashboard once approved.</p>");
                response.getWriter().println("<a href='dashboard' class='btn'>Check Status</a>");
                response.getWriter().println("</div></body></html>");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<!DOCTYPE html><html><head>");
            response.getWriter().println("<meta charset='UTF-8'>");
            response.getWriter().println("<title>Error</title>");
            response.getWriter().println("<style>");
            response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            response.getWriter().println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
            response.getWriter().println("h2{color:#c62828;margin-bottom:15px}");
            response.getWriter().println("p{color:#666;margin-bottom:30px}");
            response.getWriter().println(".btn{display:inline-block;padding:12px 30px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:8px;font-weight:bold;transition:0.3s}");
            response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<div class='error-box'>");
            response.getWriter().println("<h2>Registration Failed</h2>");
            response.getWriter().println("<p>Medical License Number: <strong>" + license + "</strong> already exists in the system.</p>");
            response.getWriter().println("<a href='profile-edit' class='btn'>Go Back</a>");
            response.getWriter().println("</div></body></html>");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<!DOCTYPE html><html><head>");
            response.getWriter().println("<meta charset='UTF-8'>");
            response.getWriter().println("<title>Error</title>");
            response.getWriter().println("<style>");
            response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            response.getWriter().println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
            response.getWriter().println("h2{color:#c62828;margin-bottom:15px}");
            response.getWriter().println("p{color:#666;margin-bottom:30px}");
            response.getWriter().println(".btn{display:inline-block;padding:12px 30px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:8px;font-weight:bold;transition:0.3s}");
            response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<div class='error-box'>");
            response.getWriter().println("<h2>Error</h2>");
            response.getWriter().println("<p>" + e.getMessage() + "</p>");
            response.getWriter().println("<a href='profile-edit' class='btn'>Go Back</a>");
            response.getWriter().println("</div></body></html>");
            
        } finally {
            try {
                if (imageInputStream != null) imageInputStream.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showError(HttpServletResponse response, String message, String backLink) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Error</title>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap' rel='stylesheet'>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter',sans-serif;background:#F8FAFC;min-height:100vh;display:flex;align-items:center;justify-content:center}");
        out.println(".card{background:#FFFFFF;padding:48px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;max-width:460px;width:100%}");
        out.println(".icon{font-size:48px;margin-bottom:16px}");
        out.println("h2{color:#DC2626;font-size:22px;font-weight:700;margin-bottom:12px}");
        out.println("p{color:#64748B;font-size:14px;line-height:1.6;margin-bottom:28px}");
        out.println(".btn{display:inline-block;padding:11px 28px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;font-size:14px;transition:background 0.2s}");
        out.println(".btn:hover{background:#1E40AF}");
        out.println("</style></head><body>");
        out.println("<div class='card'><div class='icon'>⚠️</div>");
        out.println("<h2>Error</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<a href='" + backLink + "' class='btn'>Go Back</a>");
        out.println("</div></body></html>");
    }
}
