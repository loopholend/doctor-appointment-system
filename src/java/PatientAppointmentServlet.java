// Replaced by patient.PatientAppointmentServlet in src/java/patient/
// This file is kept as an empty stub to avoid build errors from missing references.
// Do not add logic here.
public class PatientAppointmentServlet {}

/*  --- REPLACED: see patient/PatientAppointmentServlet.java ---
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        
        if ("book".equals(action)) {
            handleBooking(request, response);
        } else if ("cancel".equals(action)) {
            handleCancellation(request, response);
        } else {
            showAvailableSlots(request, response);
        }
    }

    private void showAvailableSlots(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String doctorIdParam = request.getParameter("doctorId");
        if (doctorIdParam == null) {
            response.setContentType("text/html");
            response.getWriter().println("<h2 style='color:white'>Doctor ID is required</h2>");
            return;
        }

        // Require patient session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("patient/login.html");
            return;
        }

        int doctorId = Integer.parseInt(doctorIdParam);

        response.setContentType("text/html; charset=UTF-8");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "SELECT full_name, clinic_visit_schedule FROM doctor_profiles WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                response.getWriter().println("<h2 style='color:white;text-align:center'>Doctor not found</h2>");
                return;
            }

            String doctorName = rs.getString("full_name");
            String slotText = rs.getString("clinic_visit_schedule");
            
            String[] parts = slotText.split("-");
            int startHour = Integer.parseInt(parts[0].trim());
            int endHour = Integer.parseInt(parts[1].trim());
            if (endHour < startHour) endHour += 12;
            
            LocalTime start = LocalTime.of(startHour, 0);
            LocalTime end = LocalTime.of(endHour, 0);

            java.io.PrintWriter out = response.getWriter();
            
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Book Appointment</title>");
            out.println("<style>");
            out.println("*{margin:0;padding:0;box-sizing:border-box}");
            out.println("body{font-family:'Arial',sans-serif;background:linear-gradient(135deg, #0077b6, #0096c7);padding:20px;min-height:100vh}");
            out.println("h2{color:white;text-align:center;margin-bottom:30px;font-size:32px;text-shadow:2px 2px 4px rgba(0,0,0,0.3)}");
            out.println(".container{max-width:900px;margin:0 auto}");
            out.println("table{border-collapse:collapse;width:100%;background:white;border-radius:15px;overflow:hidden;box-shadow:0 15px 35px rgba(0,0,0,0.3)}");
            out.println("th,td{padding:20px;text-align:center}");
            out.println("th{background:#2c3e50;color:white;font-size:18px;font-weight:600;text-transform:uppercase;letter-spacing:1px}");
            out.println("tr:nth-child(even){background:#f8f9fa}");
            out.println("tr:hover{background:#e3f2fd;transition:0.3s}");
            out.println(".slot-cell{display:flex;gap:12px;justify-content:center;align-items:center}");
            out.println("button{border:none;padding:12px 28px;border-radius:8px;cursor:pointer;font-weight:bold;transition:all 0.3s;font-size:15px;text-transform:uppercase;letter-spacing:0.5px}");
            out.println(".book-btn{background:linear-gradient(135deg, #0077b6, #0096c7);color:white;box-shadow:0 4px 15px rgba(0,119,182,0.4)}");
            out.println(".book-btn:hover{transform:translateY(-2px);box-shadow:0 6px 20px rgba(0,119,182,0.6)}");
            out.println(".booked-btn{background:linear-gradient(135deg,#dc3545,#c82333);color:white;cursor:not-allowed;opacity:0.8}");
            out.println(".cancel-btn{background:linear-gradient(135deg,#ffc107,#ff9800);color:#000;font-weight:bold;box-shadow:0 4px 15px rgba(255,193,7,0.4)}");
            out.println(".cancel-btn:hover{transform:translateY(-2px);box-shadow:0 6px 20px rgba(255,193,7,0.6)}");
            out.println(".time-slot{font-size:18px;font-weight:700;color:#2c3e50;letter-spacing:1px}");
            out.println(".back-link{display:block;text-align:center;margin-top:30px;color:white;text-decoration:none;font-size:16px;padding:12px 24px;background:rgba(255,255,255,0.2);border-radius:8px;width:200px;margin:30px auto 0}");
            out.println(".back-link:hover{background:rgba(255,255,255,0.3)}");
            out.println("</style></head><body>");
            
            out.println("<div class='container'>");
            out.println("<h2>Book Appointment with Dr. " + doctorName + "</h2>");
            out.println("<table><tr><th>Time Slot</th><th>Action</th></tr>");

            LocalDate today = LocalDate.now();
            
            // Get all appointments for today (regardless of status)
            PreparedStatement bookingCheck = conn.prepareStatement(
                "SELECT appointment_time FROM appointments WHERE doctor_id = ? AND appointment_date = ?"
            );
            bookingCheck.setInt(1, doctorId);
            bookingCheck.setDate(2, Date.valueOf(today));
            ResultSet bookedSlots = bookingCheck.executeQuery();
            
            Set<String> bookedTimes = new HashSet<String>();
            while (bookedSlots.next()) {
                bookedTimes.add(bookedSlots.getString("appointment_time"));
            }
            bookedSlots.close();
            bookingCheck.close();

            LocalTime currentSlot = start;
            while (currentSlot.isBefore(end)) {
                String timeStr = currentSlot.toString();
                boolean isBooked = bookedTimes.contains(timeStr);
                
                out.println("<tr>");
                out.println("<td><span class='time-slot'>" + timeStr + "</span></td>");
                out.println("<td><div class='slot-cell'>");
                
                if (isBooked) {
                    out.println("<button class='booked-btn' disabled>Booked</button>");
                    out.println("<form action='patient/book-appointment' method='post' style='margin:0'>");
                    out.println("<input type='hidden' name='action' value='cancel'>");
                    out.println("<input type='hidden' name='doctorId' value='" + doctorId + "'>");
                    out.println("<input type='hidden' name='time' value='" + timeStr + "'>");
                    out.println("<button type='submit' class='cancel-btn'>Cancel</button>");
                    out.println("</form>");
                } else {
                    out.println("<form action='patient/book-appointment' method='post' style='margin:0'>");
                    out.println("<input type='hidden' name='action' value='book'>");
                    out.println("<input type='hidden' name='doctorId' value='" + doctorId + "'>");
                    out.println("<input type='hidden' name='time' value='" + timeStr + "'>");
                    out.println("<button type='submit' class='book-btn'>Book</button>");
                    out.println("</form>");
                }
                
                out.println("</div></td>");
                out.println("</tr>");
                
                currentSlot = currentSlot.plusMinutes(15);
            }
            
            out.println("</table>");
            out.println("<a href='patient/dashboard' class='back-link'>Back to Dashboard</a>");
            out.println("</div>");
            out.println("</body></html>");

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<h2 style='color:white;text-align:center;padding:20px'>Error: " + ex.getMessage() + "</h2>");
            response.getWriter().println("<a href='patient/book-appointment?doctorId=" + doctorIdParam + "' style='color:white;display:block;text-align:center'>Try Again</a>");
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

    private void handleBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int doctorId = Integer.parseInt(request.getParameter("doctorId"));
        String time = request.getParameter("time");
        // Get patient username from session only (never from hidden form field)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("patient/login.html");
            return;
        }
        String patientUsername = (String) session.getAttribute("username");
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Lock the slot: check it's still available within the transaction
            String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_time = ? AND appointment_date = ? AND appointment_status = 'booked'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, doctorId);
            checkStmt.setString(2, time);
            checkStmt.setDate(3, Date.valueOf(LocalDate.now()));
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            int existingCount = checkRs.getInt(1);
            checkRs.close();
            checkStmt.close();

            if (existingCount > 0) {
                conn.rollback();
                response.sendRedirect("patient/book-appointment?doctorId=" + doctorId + "&msg=slot_taken");
                return;
            }

            // Insert the new appointment
            String insertSql = "INSERT INTO appointments (doctor_id, patient_account_username, appointment_time, appointment_date, appointment_status) VALUES (?, ?, ?, ?, 'booked')";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, patientUsername);
            pstmt.setString(3, time);
            pstmt.setDate(4, Date.valueOf(LocalDate.now()));
            pstmt.executeUpdate();

            conn.commit();
            response.sendRedirect("patient/book-appointment?doctorId=" + doctorId);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<html><body style='background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-align:center;padding:50px;font-family:Arial'>");
            response.getWriter().println("<h2>Booking Failed</h2>");
            response.getWriter().println("<p style='font-size:18px'>" + ex.getMessage() + "</p>");
            response.getWriter().println("<br><a href='patient/book-appointment?doctorId=" + doctorId + "' style='color:white;text-decoration:none;background:rgba(255,255,255,0.2);padding:12px 24px;border-radius:8px;display:inline-block'>Go Back</a>");
            response.getWriter().println("</body></html>");
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleCancellation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        int doctorId = Integer.parseInt(request.getParameter("doctorId"));
        String time = request.getParameter("time");
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Only cancel the slot owned by the current patient
            String sql = "DELETE FROM appointments WHERE doctor_id = ? AND appointment_time = ? AND appointment_date = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, time);
            pstmt.setDate(3, Date.valueOf(LocalDate.now()));

            int rowsDeleted = pstmt.executeUpdate();
            conn.commit();
            
            if (rowsDeleted > 0) {
                response.sendRedirect("patient/book-appointment?doctorId=" + doctorId);
            } else {
                response.setContentType("text/html");
                response.getWriter().println("<html><body style='background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-align:center;padding:50px;font-family:Arial'>");
                response.getWriter().println("<h2>Appointment Not Found</h2>");
                response.getWriter().println("<br><a href='patient/book-appointment?doctorId=" + doctorId + "' style='color:white;text-decoration:none;background:rgba(255,255,255,0.2);padding:12px 24px;border-radius:8px;display:inline-block'>Go Back</a>");
                response.getWriter().println("</body></html>");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<html><body style='background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-align:center;padding:50px;font-family:Arial'>");
            response.getWriter().println("<h2>Cancellation Failed</h2>");
            response.getWriter().println("<p style='font-size:18px'>" + ex.getMessage() + "</p>");
            response.getWriter().println("<br><a href='patient/book-appointment?doctorId=" + doctorId + "' style='color:white;text-decoration:none;background:rgba(255,255,255,0.2);padding:12px 24px;border-radius:8px;display:inline-block'>Go Back</a>");
            response.getWriter().println("</body></html>");
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
*/
