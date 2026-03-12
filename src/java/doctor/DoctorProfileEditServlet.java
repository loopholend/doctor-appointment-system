package doctor;

import common.*;
import java.io.*;
import java.sql.*;
import java.util.Base64;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/profile-edit")
public class DoctorProfileEditServlet extends HttpServlet {

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // CHECK SESSION
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("doctorUsername") == null) {
            response.sendRedirect("login.html");
            return;
        }
        
        // Get doctor username from session
        String doctorUsername = (String) session.getAttribute("doctorUsername");
        String doctorName = (String) session.getAttribute("doctorName");
        
        // Get doctor_id from doctor_profiles using username
        int doctorId = getDoctorIdFromUsername(doctorUsername);
        
        if (doctorId == 0) {
            response.setContentType("text/html");
            response.getWriter().println("<h2>Error: Doctor profile not found. Please log in again.</h2>");
            return;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        // Variables to store doctor info
        String fullName = "";
        String gender = "";
        String dob = "";
        String license = "";
        String experience = "";
        String primarySpeciality = "";
        String secondarySpeciality = "";
        double fee = 0;
        String timeSlot = "";
        String bio = "";
        String existingImage = "";
        boolean hasData = false;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT * FROM doctor_profiles WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                hasData = true;
                fullName = rs.getString("full_name");
                gender = rs.getString("gender");
                dob = rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toString() : "";
                license = rs.getString("medical_license_number");
                experience = rs.getString("years_of_experience");
                primarySpeciality = rs.getString("primary_specialty");
                secondarySpeciality = rs.getString("secondary_specialty");
                if (secondarySpeciality == null || "none".equals(secondarySpeciality)) {
                    secondarySpeciality = "";
                }
                fee = rs.getDouble("consultation_fee");
                timeSlot = rs.getString("clinic_visit_schedule");
                bio = rs.getString("professional_bio");
                
                // Get existing image
                byte[] imageBytes = rs.getBytes("profile_image");
                if (imageBytes != null && imageBytes.length > 0) {
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    String imageType = rs.getString("image_type");
                    if (imageType == null) imageType = "image/jpeg";
                    existingImage = "data:" + imageType + ";base64," + base64Image;
                }
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
        
        // Display form
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Update Doctor Details</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".main-content{margin-left:260px;padding:24px;min-height:100vh;background:#F8FAFC;padding-top:88px;display:flex;justify-content:center;align-items:flex-start;transition:margin-left 0.3s ease}");
        out.println(".container{background:#FFFFFF;padding:32px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;width:100%;max-width:480px}");
        out.println("h1{text-align:center;color:#1E293B;margin-bottom:24px;font-size:24px;font-weight:700}");
        out.println("label{display:block;margin-top:14px;font-weight:600;color:#1E293B;font-size:13px}");
        out.println("input[type='text'],input[type='date'],input[type='number'],input[type='file'],select,textarea{width:100%;padding:10px 12px;margin-top:5px;border-radius:8px;border:1px solid #E2E8F0;font-size:14px;outline:none;transition:all 0.2s;box-sizing:border-box;color:#1E293B;background:#FFFFFF;font-family:'Inter','Poppins',sans-serif}");
        out.println("input:focus,select:focus,textarea:focus{border-color:#2563EB;box-shadow:0 0 0 3px rgba(37,99,235,0.1)}");
        out.println("input[readonly]{background:#F1F5F9;color:#64748B;cursor:not-allowed;border-color:#E2E8F0!important;box-shadow:none!important}");
        out.println(".readonly-note{font-size:12px;color:#94A3B8;margin-top:4px;font-style:italic}");
        out.println("textarea{resize:vertical;min-height:80px}");
        out.println("button{display:block;width:100%;margin-top:24px;background:#1D4ED8;color:white;border:none;padding:12px;font-size:15px;font-weight:600;border-radius:8px;cursor:pointer;transition:all 0.2s;font-family:'Inter','Poppins',sans-serif}");
        out.println("button:hover{background:#1E40AF}");
        out.println("#preview{display:block;margin:12px auto 0;width:100px;height:100px;border-radius:50%;object-fit:cover;border:2px solid #E2E8F0;box-shadow:0 1px 3px rgba(0,0,0,0.1)}");
        out.println(".image-note{font-size:12px;color:#64748B;margin-top:4px;font-style:italic}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        NavHelper.writeNavbar(out, "Update Profile");
        NavHelper.writeDoctorSidebar(out, doctorName, "profile");
        
        out.println("<div class='main-content'>");
        out.println("<div class='container'>");
        out.println("<h1>" + (hasData ? "Update Doctor Details" : "Doctor Details") + "</h1>");
        out.println("<form action='profile' method='post' enctype='multipart/form-data'>");
        
        out.println("<label for='fullName'>Full Name:</label>");
        out.println("<input type='text' id='fullName' name='fullName' value='" + fullName + "' placeholder='Enter full name' required>");
        
        out.println("<label for='gender'>Gender:</label>");
        out.println("<select id='gender' name='gender' required>");
        out.println("<option value=''>Select gender</option>");
        out.println("<option value='male' " + ("male".equals(gender) ? "selected" : "") + ">Male</option>");
        out.println("<option value='female' " + ("female".equals(gender) ? "selected" : "") + ">Female</option>");
        out.println("<option value='other' " + ("other".equals(gender) ? "selected" : "") + ">Other</option>");
        out.println("</select>");
        
        out.println("<label for='dob'>Date of Birth:</label>");
        out.println("<input type='date' id='dob' name='dob' value='" + dob + "' required>");
        
        out.println("<label for='license'>Medical License Number:</label>");
        if (hasData) {
            out.println("<input type='text' id='license' name='license' value='" + license + "' readonly>");
            out.println("<div class='readonly-note'>🔒 License number cannot be changed after initial setup</div>");
        } else {
            out.println("<input type='text' id='license' name='license' value='" + license + "' placeholder='Enter license number' required>");
        }
        
        out.println("<label for='experience'>Years of Experience:</label>");
        out.println("<input type='text' id='experience' name='experience' value='" + experience + "' placeholder='e.g., 10 years' required>");
        
        out.println("<label for='primarySpeciality'>Primary Speciality:</label>");
        out.println("<input type='text' id='primarySpeciality' name='primarySpeciality' value='" + primarySpeciality + "' placeholder='e.g., Cardiologist' required>");
        
        out.println("<label for='secondarySpeciality'>Secondary Speciality:</label>");
        out.println("<input type='text' id='secondarySpeciality' name='secondarySpeciality' value='" + secondarySpeciality + "' placeholder='Enter if applicable'>");
        
        out.println("<label for='fee'>Consultation Fee (Rs.):</label>");
        out.println("<input type='number' id='fee' name='fee' value='" + (fee > 0 ? fee : "") + "' placeholder='Enter fee amount' min='0' required>");
        
        out.println("<label for='timeSlot'>Clinic Visit Time Slot:</label>");
        out.println("<input type='text' id='timeSlot' name='timeSlot' value='" + timeSlot + "' placeholder='e.g., 10am-2pm' required>");
        
        out.println("<label for='bio'>Short Bio:</label>");
        out.println("<textarea id='bio' name='bio' placeholder='Write a short bio...' required>" + bio + "</textarea>");
        
        out.println("<label for='profileImage'>Profile Image:</label>");
        out.println("<input type='file' id='profileImage' name='profileImage' accept='image/*' " + (hasData ? "" : "required") + ">");
        if (hasData) {
            out.println("<div class='image-note'>Leave empty to keep current image</div>");
        }
        
        if (!existingImage.isEmpty()) {
            out.println("<img id='preview' src='" + existingImage + "' alt='Current Profile'>");
        } else {
            out.println("<img id='preview' style='display:none' alt='Profile Preview'>");
        }
        
        out.println("<button type='submit'>" + (hasData ? "Update Details" : "Submit Details") + "</button>");
        out.println("</form>");
        out.println("</div>");
        out.println("</div>");
        
        out.println("<script>");
        out.println("const profileImage = document.getElementById('profileImage');");
        out.println("const preview = document.getElementById('preview');");
        out.println("profileImage.addEventListener('change', (event) => {");
        out.println("  const file = event.target.files[0];");
        out.println("  if (file) {");
        out.println("    const reader = new FileReader();");
        out.println("    reader.onload = (e) => {");
        out.println("      preview.src = e.target.result;");
        out.println("      preview.style.display = 'block';");
        out.println("    };");
        out.println("    reader.readAsDataURL(file);");
        out.println("  }");
        out.println("});");
        out.println("</script>");
        NavHelper.writeSidebarJS(out);
        out.println("</body>");
        out.println("</html>");
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
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
