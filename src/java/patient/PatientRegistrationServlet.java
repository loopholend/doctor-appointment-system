package patient;

import common.*;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = {"/patient/register"})
public class PatientRegistrationServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("t1");
        String name     = request.getParameter("t2");
        String cn       = request.getParameter("t3");
        String email    = request.getParameter("t4");
        String password = request.getParameter("t5");

        // Basic server-side validation
        if (username == null || username.trim().isEmpty() ||
            name     == null || name.trim().isEmpty() ||
            cn       == null || cn.trim().isEmpty() ||
            email    == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            response.sendRedirect("register.html?error=missing_fields");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();

            // Hash password before storing
            String hashedPassword = PasswordUtil.hashPassword(password);

            // Use PreparedStatement to prevent SQL injection
            String sql = "INSERT INTO patients (username, full_name, contact_number, email_address, password_hash) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            pstmt.setString(2, name.trim());
            pstmt.setString(3, cn.trim());
            pstmt.setString(4, email.trim());
            pstmt.setString(5, hashedPassword);
            pstmt.executeUpdate();

            response.sendRedirect("login.html");

        } catch (SQLIntegrityConstraintViolationException e) {
            response.sendRedirect("register.html?error=duplicate");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("register.html?error=server");
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
