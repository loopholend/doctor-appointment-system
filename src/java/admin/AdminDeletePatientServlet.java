package admin;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/delete-patient")
public class AdminDeletePatientServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CHECK ADMIN SESSION
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String username = request.getParameter("username");

        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;

        try {
            conn = DBConnection.getConnection();
            
            // Start transaction
            conn.setAutoCommit(false);

            // Delete from appointments table first (foreign key constraint)
            String sql1 = "DELETE FROM appointments WHERE patient_account_username = ?";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, username);
            pstmt1.executeUpdate();
            pstmt1.close();

            // Delete from patient table
            String sql2 = "DELETE FROM patients WHERE username = ?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, username);
            int rowsAffected = pstmt2.executeUpdate();
            pstmt2.close();

            // Commit transaction
            conn.commit();

            if (rowsAffected > 0) {
                // Success
                response.setContentType("text/html");
                response.getWriter().println("<!DOCTYPE html><html><head>");
                response.getWriter().println("<meta charset='UTF-8'>");
                response.getWriter().println("<title>Patient Deleted</title>");
                response.getWriter().println("<style>");
                response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                response.getWriter().println(".success-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
                response.getWriter().println(".success-icon{font-size:80px;margin-bottom:20px;color:#4caf50}");
                response.getWriter().println("h2{color:#2c3e50;margin-bottom:15px;font-size:28px}");
                response.getWriter().println("p{color:#666;margin-bottom:30px;font-size:16px}");
                response.getWriter().println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
                response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
                response.getWriter().println("</style></head><body>");
                response.getWriter().println("<div class='success-box'>");
                response.getWriter().println("<div class='success-icon'>✓</div>");
                response.getWriter().println("<h2>Patient Deleted Successfully</h2>");
                response.getWriter().println("<p>Patient @" + username + " and all associated appointments have been removed from the system.</p>");
                response.getWriter().println("<a href='patients' class='btn'>Back to Manage Patients</a>");
                response.getWriter().println("</div></body></html>");
            } else {
                conn.rollback();
                showError(response, "Failed to delete patient. Patient may not exist.", "patients");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showError(response, "Database Error: " + e.getMessage(), "patients");
        } finally {
            try {
                if (pstmt1 != null) pstmt1.close();
                if (pstmt2 != null) pstmt2.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void showError(HttpServletResponse response, String message, String backLink) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().println("<!DOCTYPE html><html><head>");
        response.getWriter().println("<meta charset='UTF-8'>");
        response.getWriter().println("<title>Error</title>");
        response.getWriter().println("<style>");
        response.getWriter().println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
        response.getWriter().println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
        response.getWriter().println(".error-icon{font-size:80px;margin-bottom:20px;color:#f44336}");
        response.getWriter().println("h2{color:#c62828;margin-bottom:15px;font-size:28px}");
        response.getWriter().println("p{color:#666;margin-bottom:30px;font-size:16px}");
        response.getWriter().println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
        response.getWriter().println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
        response.getWriter().println("</style></head><body>");
        response.getWriter().println("<div class='error-box'>");
        response.getWriter().println("<div class='error-icon'>✗</div>");
        response.getWriter().println("<h2>Error</h2>");
        response.getWriter().println("<p>" + message + "</p>");
        response.getWriter().println("<a href='" + backLink + "' class='btn'>Go Back</a>");
        response.getWriter().println("</div></body></html>");
    }
}
