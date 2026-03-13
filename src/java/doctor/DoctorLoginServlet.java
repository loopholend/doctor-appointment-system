package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/login")
public class DoctorLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("t1");
        String password = request.getParameter("t2");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            // Fetch stored (hashed) password for secure verification
            String sql = "SELECT * FROM doctor_accounts WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next() && PasswordUtil.verifyPassword(password, rs.getString("password_hash"))) {

                // Migrate legacy plaintext passwords to hashed on successful login
                if (!PasswordUtil.isHashed(rs.getString("password_hash"))) {
                    try (PreparedStatement migratePstmt = conn.prepareStatement(
                            "UPDATE doctor_accounts SET password_hash = ? WHERE username = ?")) {
                        migratePstmt.setString(1, PasswordUtil.hashPassword(password));
                        migratePstmt.setString(2, username);
                        migratePstmt.executeUpdate();
                    }
                }

                // Successful login - invalidate old session first to prevent session fixation
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) oldSession.invalidate();
                HttpSession session = request.getSession(true);
                session.setAttribute("doctorId", rs.getInt("id"));
                session.setAttribute("doctorName", rs.getString("full_name"));
                session.setAttribute("doctorEmail", rs.getString("email_address"));
                session.setAttribute("doctorUsername", username);
                session.setAttribute("doctorContact", rs.getString("contact_number"));

                // REDIRECT TO DASHBOARD
                response.sendRedirect("dashboard");
            } else {
                // Invalid credentials
                showError(response, "Invalid username or password. Please try again.", "login.html", "Login Failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError(response, "Database Error: " + e.getMessage(), "login.html", "Error");
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
    
    private void showError(HttpServletResponse response, String message, String backLink, String title) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>" + title + "</title>");
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
        out.println("<h2>" + title + "</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<a href='" + backLink + "' class='btn'>Back to Login</a>");
        out.println("</div></body></html>");
    }
}
