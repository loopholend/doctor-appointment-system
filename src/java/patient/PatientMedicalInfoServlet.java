package patient;

import common.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/patient/medical-info")
public class PatientMedicalInfoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("medical-details");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        String patientUsername = null;
        
        if (session != null) {
            patientUsername = (String) session.getAttribute("username");
        }
        
        if (patientUsername == null || patientUsername.isEmpty()) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<!DOCTYPE html><html><head>");
            response.getWriter().println("<meta charset='UTF-8'>");
            response.getWriter().println("<title>Login Required</title>");
            response.getWriter().println("<style>");
            response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            response.getWriter().println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
            response.getWriter().println("h2{color:#c62828;margin-bottom:15px;font-size:28px}");
            response.getWriter().println("p{color:#666;margin-bottom:30px;font-size:16px}");
            response.getWriter().println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
            response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<div class='error-box'>");
            response.getWriter().println("<h2>Please Login First!</h2>");
            response.getWriter().println("<p>You need to be logged in to submit medical details.</p>");
            response.getWriter().println("<a href='login.html' class='btn'>Go to Login</a>");
            response.getWriter().println("</div></body></html>");
            return;
        }

        String bloodGroup = request.getParameter("bloodGroup");
        String age = request.getParameter("age");
        String diabetes = request.getParameter("diabetes");
        String thyroid = request.getParameter("thyroid");
        String bp = request.getParameter("bp");
        String asthma = request.getParameter("asthma");
        String allergies = request.getParameter("allergies");
        String surgeries = request.getParameter("surgeries");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();

            String checkSql = "SELECT record_id FROM patient_medical_records WHERE patient_account_username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, patientUsername);
            java.sql.ResultSet rs = checkStmt.executeQuery();
            
            String sql;
            boolean isUpdate = false;
            if (rs.next()) {
                isUpdate = true;
                sql = "UPDATE patient_medical_records SET blood_type=?, age=?, diabetes_status=?, thyroid_status=?, blood_pressure_status=?, has_asthma=?, has_allergies=?, has_previous_surgeries=? WHERE patient_account_username=?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bloodGroup);
                pstmt.setInt(2, Integer.parseInt(age));
                pstmt.setString(3, diabetes);
                pstmt.setString(4, thyroid);
                pstmt.setString(5, bp);
                pstmt.setString(6, asthma);
                pstmt.setString(7, allergies);
                pstmt.setString(8, surgeries);
                pstmt.setString(9, patientUsername);
            } else {
                sql = "INSERT INTO patient_medical_records (patient_account_username, blood_type, age, diabetes_status, thyroid_status, blood_pressure_status, has_asthma, has_allergies, has_previous_surgeries) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, patientUsername);
                pstmt.setString(2, bloodGroup);
                pstmt.setInt(3, Integer.parseInt(age));
                pstmt.setString(4, diabetes);
                pstmt.setString(5, thyroid);
                pstmt.setString(6, bp);
                pstmt.setString(7, asthma);
                pstmt.setString(8, allergies);
                pstmt.setString(9, surgeries);
            }
            
            rs.close();
            checkStmt.close();

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().println("<!DOCTYPE html><html><head>");
                response.getWriter().println("<meta charset='UTF-8'>");
                response.getWriter().println("<title>Success</title>");
                response.getWriter().println("<style>");
                response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                response.getWriter().println(".success-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
                response.getWriter().println(".success-icon{font-size:80px;margin-bottom:20px;color:#4caf50}");
                response.getWriter().println("h2{color:#2c3e50;margin-bottom:15px;font-size:28px}");
                response.getWriter().println("p{color:#666;margin-bottom:30px;font-size:16px;line-height:1.6}");
                response.getWriter().println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
                response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
                response.getWriter().println("</style></head><body>");
                response.getWriter().println("<div class='success-box'>");
                response.getWriter().println("<div class='success-icon'>✓</div>");
                response.getWriter().println("<h2>Medical Details " + (isUpdate ? "Updated" : "Saved") + " Successfully!</h2>");
                response.getWriter().println("<p>Your medical information has been recorded for <strong>" + patientUsername + "</strong></p>");
                response.getWriter().println("<a href='dashboard' class='btn'>Go to Dashboard</a>");
                response.getWriter().println("</div>");
                response.getWriter().println("</body></html>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
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
            response.getWriter().println("<h2>Database Error</h2>");
            response.getWriter().println("<p>" + e.getMessage() + "</p>");
            response.getWriter().println("<a href='medical-details' class='btn'>Go Back</a>");
            response.getWriter().println("</div></body></html>");
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<!DOCTYPE html><html><head>");
            response.getWriter().println("<meta charset='UTF-8'>");
            response.getWriter().println("<title>Invalid Input</title>");
            response.getWriter().println("<style>");
            response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            response.getWriter().println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
            response.getWriter().println("h2{color:#c62828;margin-bottom:15px}");
            response.getWriter().println("p{color:#666;margin-bottom:30px}");
            response.getWriter().println(".btn{display:inline-block;padding:12px 30px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:8px;font-weight:bold;transition:0.3s}");
            response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<div class='error-box'>");
            response.getWriter().println("<h2>Invalid Age</h2>");
            response.getWriter().println("<p>Please enter a valid age.</p>");
            response.getWriter().println("<a href='medical-details' class='btn'>Go Back</a>");
            response.getWriter().println("</div></body></html>");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
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
            response.getWriter().println("<a href='medical-details' class='btn'>Go Back</a>");
            response.getWriter().println("</div></body></html>");
            
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
