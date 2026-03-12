package patient;

import common.*;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/patient/medical-details")
public class PatientMedicalDetailsServlet extends HttpServlet {

    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String patientName = NavHelper.requirePatientSession(request, response);
        if (patientName == null) return;
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("username");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String bloodGroup = "";
        String age = "";
        String diabetes = "none";
        String thyroid = "none";
        String bp = "none";
        String asthma = "no";
        String allergies = "no";
        String surgeries = "no";
        boolean hasExistingData = false;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT * FROM patient_medical_records WHERE patient_account_username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                hasExistingData = true;
                bloodGroup = rs.getString("blood_type");
                age = String.valueOf(rs.getInt("age"));
                diabetes = rs.getString("diabetes_status");
                thyroid = rs.getString("thyroid_status");
                bp = rs.getString("blood_pressure_status");
                asthma = rs.getString("has_asthma");
                allergies = rs.getString("has_allergies");
                surgeries = rs.getString("has_previous_surgeries");
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
        
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Medical Details — MediCare+</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".main-content{display:flex;justify-content:center;align-items:flex-start}");
        out.println(".form-card{background:#FFFFFF;padding:32px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;width:100%;max-width:520px}");
        out.println("h1{margin-bottom:24px;color:#1E293B;font-size:22px;font-weight:700}");
        out.println("form label{display:block;margin-top:14px;font-weight:600;color:#1E293B;font-size:13px}");
        out.println("input[type='text'],input[type='number'],select{width:100%;padding:10px 12px;margin-top:5px;border-radius:8px;border:1px solid #E2E8F0;font-size:14px;outline:none;transition:all 0.2s;box-sizing:border-box;color:#1E293B;font-family:'Inter','Poppins',sans-serif}");
        out.println("input:focus,select:focus{border-color:#2563EB;box-shadow:0 0 0 3px rgba(37,99,235,0.1)}");
        out.println(".yes-no{margin-top:8px;display:flex;gap:20px}");
        out.println(".yes-no label{margin:0;font-weight:500;display:flex;align-items:center;gap:6px;cursor:pointer;color:#1E293B;font-size:14px}");
        out.println("input[type='radio']{cursor:pointer;accent-color:#2563EB}");
        out.println("button[type='submit']{display:block;width:100%;margin-top:24px;background:#1D4ED8;color:white;border:none;padding:12px;font-size:15px;font-weight:600;border-radius:8px;cursor:pointer;transition:all 0.2s;font-family:'Inter','Poppins',sans-serif}");
        out.println("button[type='submit']:hover{background:#1E40AF}");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");

        NavHelper.writeNavbar(out, "Medical Info");
        NavHelper.writePatientSidebar(out, patientName, "medical");

        // Main content
        out.println("<div class='main-content'>");
        out.println("<div class='form-card'>");
        out.println("<h1>" + (hasExistingData ? "Update Medical Details" : "Medical Details") + "</h1>");
        out.println("<form action='medical-info' method='post'>");
        
        out.println("<label for='bloodGroup'>Blood Group:</label>");
        out.println("<input type='text' id='bloodGroup' name='bloodGroup' value='" + bloodGroup + "' placeholder='e.g., A+, O-, AB+' required>");
        
        out.println("<label for='age'>Age:</label>");
        out.println("<input type='number' id='age' name='age' value='" + age + "' min='0' max='110' placeholder='Enter your age' required>");
        
        out.println("<label for='diabetes'>Do you have diabetes?</label>");
        out.println("<select id='diabetes' name='diabetes'>");
        out.println("<option value='none' " + ("none".equals(diabetes) ? "selected" : "") + ">None</option>");
        out.println("<option value='type1' " + ("type1".equals(diabetes) ? "selected" : "") + ">Type 1</option>");
        out.println("<option value='type2' " + ("type2".equals(diabetes) ? "selected" : "") + ">Type 2</option>");
        out.println("<option value='pre-diabetic' " + ("pre-diabetic".equals(diabetes) ? "selected" : "") + ">Pre-diabetic</option>");
        out.println("<option value='gestational' " + ("gestational".equals(diabetes) ? "selected" : "") + ">Gestational</option>");
        out.println("</select>");
        
        out.println("<label for='thyroid'>Do you have thyroid?</label>");
        out.println("<select id='thyroid' name='thyroid'>");
        out.println("<option value='none' " + ("none".equals(thyroid) ? "selected" : "") + ">None</option>");
        out.println("<option value='hypothyroidism' " + ("hypothyroidism".equals(thyroid) ? "selected" : "") + ">Hypothyroidism</option>");
        out.println("<option value='hyperthyroidism' " + ("hyperthyroidism".equals(thyroid) ? "selected" : "") + ">Hyperthyroidism</option>");
        out.println("<option value='parathyroidism' " + ("parathyroidism".equals(thyroid) ? "selected" : "") + ">Parathyroidism</option>");
        out.println("</select>");
        
        out.println("<label for='bp'>Do you have blood pressure?</label>");
        out.println("<select id='bp' name='bp'>");
        out.println("<option value='none' " + ("none".equals(bp) ? "selected" : "") + ">None</option>");
        out.println("<option value='hypertension' " + ("hypertension".equals(bp) ? "selected" : "") + ">Hypertension (High BP)</option>");
        out.println("<option value='hypotension' " + ("hypotension".equals(bp) ? "selected" : "") + ">Hypotension (Low BP)</option>");
        out.println("</select>");
        
        out.println("<label>Do you have asthma?</label>");
        out.println("<div class='yes-no'>");
        out.println("<label><input type='radio' name='asthma' value='yes' " + ("yes".equals(asthma) ? "checked" : "") + " required> Yes</label>");
        out.println("<label><input type='radio' name='asthma' value='no' " + ("no".equals(asthma) ? "checked" : "") + "> No</label>");
        out.println("</div>");
        
        out.println("<label>Do you have any allergies?</label>");
        out.println("<div class='yes-no'>");
        out.println("<label><input type='radio' name='allergies' value='yes' " + ("yes".equals(allergies) ? "checked" : "") + " required> Yes</label>");
        out.println("<label><input type='radio' name='allergies' value='no' " + ("no".equals(allergies) ? "checked" : "") + "> No</label>");
        out.println("</div>");
        
        out.println("<label>Have you had any surgeries before?</label>");
        out.println("<div class='yes-no'>");
        out.println("<label><input type='radio' name='surgeries' value='yes' " + ("yes".equals(surgeries) ? "checked" : "") + " required> Yes</label>");
        out.println("<label><input type='radio' name='surgeries' value='no' " + ("no".equals(surgeries) ? "checked" : "") + "> No</label>");
        out.println("</div>");
        
        out.println("<button type='submit'>" + (hasExistingData ? "Update" : "Submit") + "</button>");
        out.println("</form>");
        out.println("</div>");
        out.println("</div>");

        // Sidebar toggle JS
        NavHelper.writeSidebarJS(out);
        
        out.println("</body>");
        out.println("</html>");
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
