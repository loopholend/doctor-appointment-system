package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/register")
public class DoctorRegistrationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("t1");
        String name = request.getParameter("t2");
        String cn = request.getParameter("t3");
        String email = request.getParameter("t4");
        String password = request.getParameter("t5");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnection.getConnection();

            // Hash password before storing
            String hashedPassword = PasswordUtil.hashPassword(password);
            String sql = "INSERT INTO doctor_accounts (username, full_name, contact_number, email_address, password_hash) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setString(3, cn);
            pstmt.setString(4, email);
            pstmt.setString(5, hashedPassword);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                // Get the generated doctor_id
                generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int doctorId = generatedKeys.getInt(1);
                    
                    // CREATE SESSION AND STORE DATA
                    HttpSession session = request.getSession();
                    session.setAttribute("doctorId", doctorId);
                    session.setAttribute("doctorUsername", username);
                    session.setAttribute("doctorName", name);
                    session.setAttribute("doctorEmail", email);
                    
                    // Show success page
                    response.setContentType("text/html; charset=UTF-8");
                    response.getWriter().println("<!DOCTYPE html><html><head>");
                    response.getWriter().println("<meta charset='UTF-8'>");
                    response.getWriter().println("<title>Registration Successful</title>");
                    response.getWriter().println("<style>");
                    response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                    response.getWriter().println(".success-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
                    response.getWriter().println(".success-icon{font-size:80px;margin-bottom:20px;color:#4caf50}");
                    response.getWriter().println("h2{color:#2c3e50;margin-bottom:15px;font-size:28px}");
                    response.getWriter().println("p{color:#666;margin-bottom:20px;font-size:16px}");
                    response.getWriter().println(".info{background:#f8f9fa;padding:15px;border-radius:8px;margin:20px 0;text-align:left}");
                    response.getWriter().println(".info-item{margin:8px 0;font-size:14px}");
                    response.getWriter().println(".label{color:#7f8c8d;font-weight:600}");
                    response.getWriter().println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px;margin-top:20px}");
                    response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
                    response.getWriter().println("</style></head><body>");
                    response.getWriter().println("<div class='success-box'>");
                    response.getWriter().println("<div class='success-icon'>✓</div>");
                    response.getWriter().println("<h2>Registration Successful!</h2>");
                    response.getWriter().println("<p>Welcome to our platform, Dr. " + name + "!</p>");
                    response.getWriter().println("<div class='info'>");
                    response.getWriter().println("<div class='info-item'><span class='label'>Username:</span> " + username + "</div>");
                    response.getWriter().println("<div class='info-item'><span class='label'>Email:</span> " + email + "</div>");
                    response.getWriter().println("<div class='info-item'><span class='label'>Doctor ID:</span> " + doctorId + "</div>");
                    response.getWriter().println("</div>");
                    response.getWriter().println("<p>Please complete your profile to start using the system.</p>");
                    response.getWriter().println("<a href='profile-edit' class='btn'>Complete Profile</a>");
                    response.getWriter().println("</div></body></html>");
                }
            } else {
                showError(response, "Failed to register. Please try again.", "register.html");
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            showError(response, "Username or email already exists. Please try using different credentials.", "register.html");
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError(response, "Database Error: " + e.getMessage(), "register.html");
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            showError(response, "MySQL JDBC Driver not found!", "register.html");
            
        } catch (Exception e) {
            e.printStackTrace();
            showError(response, "Error: " + e.getMessage(), "register.html");
            
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
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
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Registration Failed</title>");
        out.println("<style>");
        out.println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
        out.println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
        out.println(".error-icon{font-size:80px;margin-bottom:20px;color:#f44336}");
        out.println("h2{color:#c62828;margin-bottom:15px;font-size:28px}");
        out.println("p{color:#666;margin-bottom:30px;font-size:16px}");
        out.println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
        out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
        out.println("</style></head><body>");
        out.println("<div class='error-box'>");
        out.println("<div class='error-icon'>✗</div>");
        out.println("<h2>Registration Failed</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<a href='" + backLink + "' class='btn'>Back to Registration</a>");
        out.println("</div></body></html>");
    }
}
