package patient;

import common.*;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/patient/login"})
public class PatientLoginServlet extends HttpServlet {
    
    // Add doGet to prevent 405 error
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        // Get parameters from form (matching your HTML name="t1" and name="t2")
        String username = request.getParameter("t1");  // ✓ Fixed: was "username"
        String password = request.getParameter("t2");  // ✓ Fixed: was "password"
        
        System.out.println("Login attempt - Username: " + username); // Debug
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();

            // Fetch stored (hashed) password separately for secure verification
            String sql = "SELECT * FROM patients WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next() && PasswordUtil.verifyPassword(password, rs.getString("password_hash"))) {

                // Migrate legacy plaintext passwords to hashed on successful login
                if (!PasswordUtil.isHashed(rs.getString("password_hash"))) {
                    try (PreparedStatement migratePstmt = conn.prepareStatement(
                            "UPDATE patients SET password_hash = ? WHERE username = ?")) {
                        migratePstmt.setString(1, PasswordUtil.hashPassword(password));
                        migratePstmt.setString(2, username);
                        migratePstmt.executeUpdate();
                    }
                }

                // Login successful - invalidate old session first to prevent session fixation
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) oldSession.invalidate();
                HttpSession session = request.getSession(true);
                session.setAttribute("username", username);
                session.setAttribute("name", rs.getString("full_name"));
                
                rs.close();
                pstmt.close();
                
                // Check if medical details exist
                String checkMedical = "SELECT record_id FROM patient_medical_records WHERE patient_account_username = ?";
                pstmt = conn.prepareStatement(checkMedical);
                pstmt.setString(1, username);
                rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    System.out.println("Medical details found, redirecting to dashboard");
                    // Medical details already exist - go to dashboard
                    response.sendRedirect("dashboard");
                } else {
                    System.out.println("No medical details, redirecting to medical form");
                    // Medical details don't exist - go to medical form
                    response.sendRedirect("medical-form.html");
                }
                
            } else {
                
                // Login failed
                out.println("<!DOCTYPE html><html><head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<style>");
                out.println("body{font-family:Arial;background:linear-gradient(135deg,#667eea,#764ba2);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                out.println(".error-box{background:white;padding:40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:400px}");
                out.println(".error-icon{font-size:60px;margin-bottom:20px}");
                out.println("h2{color:#c62828;margin-bottom:15px}");
                out.println("p{color:#666;margin-bottom:30px;font-size:16px}");
                out.println(".btn{display:inline-block;padding:12px 30px;background:linear-gradient(135deg,#667eea,#764ba2);color:white;text-decoration:none;border-radius:8px;font-weight:bold;transition:0.3s}");
                out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(102,126,234,0.4)}");
                out.println("</style></head><body>");
                out.println("<div class='error-box'>");
                out.println("<div class='error-icon'>❌</div>");
                out.println("<h2>Login Failed</h2>");
                out.println("<p>Invalid username or password</p>");
                out.println("<a href='login.html' class='btn'>← Try Again</a>");
                out.println("</div></body></html>");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred: " + e.getMessage());
            
            out.println("<html><body style='text-align:center;padding:50px'>");
            out.println("<h2 style='color:red'>Error occurred</h2>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("<a href='login.html'>← Back to Login</a>");
            out.println("</body></html>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
