package admin;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/delete-doctor")
public class AdminDeleteDoctorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("doctors");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CHECK ADMIN SESSION
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        String doctorIdStr = request.getParameter("doctorId");
        if (doctorIdStr == null || doctorIdStr.trim().isEmpty()) {
            response.sendRedirect("doctors");
            return;
        }
        int doctorId;
        try {
            doctorId = Integer.parseInt(doctorIdStr.trim());
        } catch (NumberFormatException nfe) {
            response.sendRedirect("doctors");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        PreparedStatement pstmt3 = null;
        PreparedStatement pstmt4 = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            
            // Start transaction
            conn.setAutoCommit(false);

            // STEP 1: Get username from doctor_profiles using doctor_id
            String getUsername = "SELECT username FROM doctor_profiles WHERE doctor_id = ?";
            pstmt1 = conn.prepareStatement(getUsername);
            pstmt1.setInt(1, doctorId);
            rs = pstmt1.executeQuery();
            
            String username = null;
            if (rs.next()) {
                username = rs.getString("username");
            }
            rs.close();
            pstmt1.close();
            
            if (username == null) {
                throw new Exception("Doctor not found");
            }

            // STEP 2: Delete from appointments table (using doctor_id)
            String sql2 = "DELETE FROM appointments WHERE doctor_id = ?";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setInt(1, doctorId);
            pstmt2.executeUpdate();
            pstmt2.close();

            // STEP 3: Delete from doctor_profiles table (using doctor_id)
            String sql3 = "DELETE FROM doctor_profiles WHERE doctor_id = ?";
            pstmt3 = conn.prepareStatement(sql3);
            pstmt3.setInt(1, doctorId);
            pstmt3.executeUpdate();
            pstmt3.close();

            // STEP 4: Delete from doctor_accounts table (using username)
            String sql4 = "DELETE FROM doctor_accounts WHERE username = ?";
            pstmt4 = conn.prepareStatement(sql4);
            pstmt4.setString(1, username);
            int rowsAffected = pstmt4.executeUpdate();
            pstmt4.close();

            // Commit transaction
            conn.commit();

            if (rowsAffected > 0) {
                // Success
                response.setContentType("text/html");
                response.getWriter().println("<!DOCTYPE html><html><head>");
                response.getWriter().println("<meta charset='UTF-8'>");
                response.getWriter().println("<title>Doctor Deleted</title>");
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
                response.getWriter().println("<h2>Doctor Deleted Successfully</h2>");
                response.getWriter().println("<p>Dr. " + username + " and all associated data have been removed from the system.</p>");
                response.getWriter().println("<a href='doctors' class='btn'>Back to Manage Doctors</a>");
                response.getWriter().println("</div></body></html>");
            } else {
                throw new Exception("Failed to delete doctor from doctor_accounts table");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showError(response, "Database Error: " + e.getMessage(), "doctors");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt1 != null) pstmt1.close();
                if (pstmt2 != null) pstmt2.close();
                if (pstmt3 != null) pstmt3.close();
                if (pstmt4 != null) pstmt4.close();
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
