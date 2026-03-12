package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/remove-day-off")
public class DoctorRemoveDayOffServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("day-off");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("doctorUsername") == null) {
            response.sendRedirect("login.html");
            return;
        }
        
        String doctorIdParam = request.getParameter("doctorId");
        String dateParam = request.getParameter("date");
        
        if (doctorIdParam == null || dateParam == null) {
            showMessage(response, "Error", "Missing required parameters.", "day-off", false);
            return;
        }
        
        int doctorId = Integer.parseInt(doctorIdParam);
        LocalDate date = LocalDate.parse(dateParam);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "DELETE FROM doctor_unavailable_dates WHERE doctor_id = ? AND unavailable_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                showMessage(response, "Success!", 
                           "Date " + date + " has been removed. Patients can now book appointments on this date.", 
                           "day-off", true);
            } else {
                showMessage(response, "Error", "Date not found in database.", "day-off", false);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage(response, "Error", "Failed to remove date: " + ex.getMessage(), "day-off", false);
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void showMessage(HttpServletResponse response, String title, String message, String backLink, boolean success) 
            throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>" + title + "</title>");
        out.println("<style>");
        out.println("body{font-family:Arial,sans-serif;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
        out.println(".message-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
        out.println(".icon{font-size:80px;margin-bottom:20px}");
        out.println("h2{color:" + (success ? "#2c3e50" : "#c62828") + ";margin-bottom:15px;font-size:28px}");
        out.println("p{color:#666;margin-bottom:30px;font-size:16px;line-height:1.6}");
        out.println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
        out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
        out.println("</style></head><body>");
        
        out.println("<div class='message-box'>");
        out.println("<div class='icon'>" + (success ? "✅" : "❌") + "</div>");
        out.println("<h2>" + title + "</h2>");
        out.println("<p>" + message + "</p>");
        out.println("<a href='" + backLink + "' class='btn'>Back</a>");
        out.println("</div></body></html>");
    }
}
