package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/complete-appointment")
public class DoctorCompleteAppointmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("appointments");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (NavHelper.requireDoctorSession(request, response) == null) return;

        HttpSession session = request.getSession(false);
        int doctorId = (Integer) session.getAttribute("doctorId");

        String appointmentIdStr = request.getParameter("appointmentId");
        String action           = request.getParameter("action"); // "complete" or "noshow"

        if (appointmentIdStr == null || appointmentIdStr.trim().isEmpty()) {
            response.sendRedirect("appointments");
            return;
        }
        int appointmentId;
        try {
            appointmentId = Integer.parseInt(appointmentIdStr.trim());
        } catch (NumberFormatException nfe) {
            response.sendRedirect("appointments");
            return;
        }

        // Determine target status from action param (default to completed)
        String newStatus = "noshow".equals(action)
                ? AppointmentStatus.NO_SHOW
                : AppointmentStatus.COMPLETED;

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();

            // Verify ownership and current status before updating
            String sql = "UPDATE appointments SET appointment_status = ? " +
                         "WHERE appointment_id = ? AND doctor_id = ? AND appointment_status = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentId);
            pstmt.setInt(3, doctorId);
            pstmt.setString(4, AppointmentStatus.BOOKED);
            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        response.sendRedirect("appointments");
    }
}
