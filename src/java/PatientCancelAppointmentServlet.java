// Replaced by patient.PatientCancelAppointmentServlet in src/java/patient/
public class PatientCancelAppointmentServlet {/*

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (NavHelper.requirePatientSession(request, response) == null) return;
        String patientUsername = (String) request.getSession(false).getAttribute("username");
        String appointmentIdParam = request.getParameter("appointmentId");

        if (appointmentIdParam == null || appointmentIdParam.trim().isEmpty()) {
            response.sendRedirect("patient/appointments");
            return;
        }

        int appointmentId;
        try {
            appointmentId = Integer.parseInt(appointmentIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect("patient/appointments");
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
                response.sendRedirect("patient/appointments?msg=cancelled");
            } else {
                // Either not found or doesn't belong to this patient
                response.sendRedirect("patient/appointments?msg=error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("patient/appointments?msg=error");
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
        response.sendRedirect("patient/appointments");
    }
*/}
