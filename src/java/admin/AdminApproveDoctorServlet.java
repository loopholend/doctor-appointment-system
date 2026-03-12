package admin;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/approve-doctor")
public class AdminApproveDoctorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // CHECK ADMIN SESSION
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        int doctorId = Integer.parseInt(request.getParameter("doctorId"));
        int adminId = (Integer) session.getAttribute("adminId");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "UPDATE doctor_profiles SET approval_status = 'approved', approved_by_admin = ?, approval_date = NOW() WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            pstmt.setInt(2, doctorId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Success - redirect back to requests page
                response.sendRedirect("doctor-requests");
            } else {
                response.setContentType("text/html");
                response.getWriter().println("<!DOCTYPE html><html><head>");
                response.getWriter().println("<meta charset='UTF-8'>");
                response.getWriter().println("<title>Error</title>");
                response.getWriter().println("<style>");
                response.getWriter().println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                response.getWriter().println(".error-box{background:#FFFFFF;padding:48px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;max-width:500px}");
                response.getWriter().println("h2{color:#DC2626;margin-bottom:12px;font-size:22px;font-weight:700}");
                response.getWriter().println("p{color:#64748B;margin-bottom:24px;font-size:15px}");
                response.getWriter().println(".btn{display:inline-block;padding:10px 24px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;transition:all 0.2s;font-size:14px}");
                response.getWriter().println("</style></head><body>");
                response.getWriter().println("<div class='error-box'>");
                response.getWriter().println("<h2>Error</h2>");
                response.getWriter().println("<p>Failed to approve doctor. Doctor may not exist.</p>");
                response.getWriter().println("<a href='doctor-requests' class='btn'>Back to Requests</a>");
                response.getWriter().println("</div></body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<!DOCTYPE html><html><head>");
            response.getWriter().println("<meta charset='UTF-8'>");
            response.getWriter().println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' rel='stylesheet'>");
            response.getWriter().println("<title>Error</title>");
            response.getWriter().println("<style>");
            response.getWriter().println("body{font-family:'Inter',sans-serif;background:#F8FAFC;min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            response.getWriter().println(".error-box{background:#FFFFFF;padding:48px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;text-align:center;max-width:500px}");
            response.getWriter().println("h2{color:#DC2626;margin-bottom:12px;font-size:22px;font-weight:700}");
            response.getWriter().println("p{color:#64748B;margin-bottom:24px;font-size:15px}");
            response.getWriter().println(".btn{display:inline-block;padding:10px 24px;background:#1D4ED8;color:white;text-decoration:none;border-radius:8px;font-weight:600;transition:all 0.2s;font-size:14px}");
            response.getWriter().println("</style></head><body>");
            response.getWriter().println("<div class='error-box'>");
            response.getWriter().println("<h2>Database Error</h2>");
            response.getWriter().println("<p>Error: " + e.getMessage() + "</p>");
            response.getWriter().println("<a href='doctor-requests' class='btn'>Back to Requests</a>");
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
