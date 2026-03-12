package patient;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Handles appointment cancellation from the patient's "My Appointments" page.
 * Verifies the appointment belongs to the logged-in patient before cancelling.
 */
@WebServlet("/patient/cancel-appointment")
public class PatientCancelAppointmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (NavHelper.requirePatientSession(request, response) == null) return;
        String patientUsername = (String) request.getSession(false).getAttribute("username");
        String appointmentIdParam = request.getParameter("appointmentId");

        if (appointmentIdParam == null || appointmentIdParam.trim().isEmpty()) {
            response.sendRedirect("appointments");
            return;
        }

        int appointmentId;
        try {
            appointmentId = Integer.parseInt(appointmentIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect("appointments");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();

            // Only cancel if the appointment belongs to this patient and is still booked
            String sql = "UPDATE appointments SET appointment_status = 'cancelled' " +
                         "WHERE appointment_id = ? AND patient_account_username = ? AND appointment_status = 'booked'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, appointmentId);
            pstmt.setString(2, patientUsername);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                response.sendRedirect("appointments?msg=cancelled");
            } else {
                response.sendRedirect("appointments?msg=error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("appointments?msg=error");
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("appointments");
    }
}
