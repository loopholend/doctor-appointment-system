package patient;

import common.*;
import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/patient/book-appointment")
public class PatientBookAppointmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String selectedDate = request.getParameter("date");
        
        if (selectedDate != null) {
            showAvailableSlots(request, response, selectedDate);
        } else {
            showCalendar(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        
        if ("book".equals(action)) {
            handleBooking(request, response);
        } else if ("cancel".equals(action)) {
            handleCancellation(request, response);
        } else {
            doGet(request, response);
        }
    }

    private void showCalendar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String doctorIdParam = request.getParameter("doctorId");
        if (doctorIdParam == null) {
            response.getWriter().println("Doctor ID is required");
            return;
        }

        int doctorId = Integer.parseInt(doctorIdParam);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "SELECT full_name FROM doctor_profiles WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                response.getWriter().println("Doctor not found");
                return;
            }

            String doctorName = rs.getString("full_name");
            rs.close();
            pstmt.close();

            HttpSession session = request.getSession(false);
            String patientName = session != null ? (String) session.getAttribute("name") : null;

            String unavailSQL = "SELECT unavailable_date FROM doctor_unavailable_dates WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(unavailSQL);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();
            
            Set<LocalDate> unavailableDates = new HashSet<>();
            while (rs.next()) {
                unavailableDates.add(rs.getDate("unavailable_date").toLocalDate());
            }

            response.setContentType("text/html; charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();
            
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@400;500;600&display=swap' rel='stylesheet'>");
            out.println("<title>Select Appointment Date</title>");
            out.println("<style>");
            out.println("* { margin: 0; padding: 0; box-sizing: border-box; }");
            out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh;margin:0}");
            out.println(".navbar{position:fixed;top:0;left:0;right:0;height:64px;background:#2563EB;display:flex;align-items:center;padding:0 24px;z-index:1100;box-shadow:0 1px 3px rgba(0,0,0,0.1)}");
            out.println(".navbar-brand{color:white;font-size:20px;font-weight:700;margin-right:16px}");
            out.println(".navbar-title{color:rgba(255,255,255,0.8);font-size:14px;font-weight:500}");
            out.println(".hamburger-btn{display:flex;flex-direction:column;justify-content:space-between;width:24px;height:18px;cursor:pointer;background:none;border:none;padding:0;margin-right:16px;flex-shrink:0}");
            out.println(".hamburger-btn span{display:block;height:2px;width:100%;background:white;border-radius:2px}");
            out.println(".sidebar{position:fixed;left:0;top:64px;width:260px;height:calc(100% - 64px);background:#1E293B;padding:24px 16px;color:white;z-index:1000;overflow-y:auto;transition:transform 0.3s ease}");
            out.println(".welcome{padding:8px 12px;background:rgba(255,255,255,0.07);border-radius:8px;margin-bottom:16px;font-size:13px;color:#CBD5E1}");
            out.println(".menu-item{display:block;padding:10px 12px;margin:2px 0;color:#94A3B8;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
            out.println(".menu-item:hover{background:rgba(37,99,235,0.2);color:#CBD5E1}");
            out.println(".logout-btn{display:block;padding:10px 12px;margin:20px 0 2px;color:#FCA5A5;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
            out.println(".logout-btn:hover{background:rgba(220,38,38,0.15);color:#FCA5A5}");
            out.println(".sidebar-overlay{display:none;position:fixed;inset:0;background:rgba(0,0,0,0.4);z-index:999;top:64px}");
            out.println("body.sidebar-collapsed .sidebar{transform:translateX(-260px)}");
            out.println("body.sidebar-collapsed .main-content{margin-left:0}");
            out.println("body.sidebar-mobile-open .sidebar{transform:translateX(0)}");
            out.println("body.sidebar-mobile-open .sidebar-overlay{display:block}");
            out.println("@media(max-width:768px){.main-content{margin-left:0 !important}.sidebar{transform:translateX(-260px)}}");
            out.println(".main-content{margin-left:260px;padding:24px;min-height:100vh;background:#F8FAFC;padding-top:88px;transition:margin-left 0.3s ease}");
            out.println(".page-header{text-align:center;margin-bottom:32px}");
            out.println(".page-header h2{color:#1E293B;font-size:26px;font-weight:700;margin-bottom:6px}");
            out.println(".page-header p{color:#64748B;font-size:14px}");
            out.println(".container{max-width:1000px;margin:0 auto}");
            out.println(".calendar-container{display:flex;gap:20px;flex-wrap:wrap;justify-content:center}");
            out.println(".calendar{background:#FFFFFF;border-radius:12px;padding:24px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;min-width:320px}");
            out.println(".month-header{text-align:center;font-size:18px;font-weight:700;color:#1E293B;margin-bottom:20px;padding:10px;background:#EFF6FF;border-radius:8px}");
            out.println(".weekdays{display:grid;grid-template-columns:repeat(7, 1fr);gap:4px;margin-bottom:8px}");
            out.println(".weekday{text-align:center;font-weight:600;color:#64748B;padding:8px;font-size:12px;text-transform:uppercase;letter-spacing:0.5px}");
            out.println(".days{display:grid;grid-template-columns:repeat(7, 1fr);gap:4px}");
            out.println(".day{padding:12px;text-align:center;border-radius:8px;cursor:pointer;transition:all 0.2s;border:none;background:#F8FAFC;font-size:13px;font-weight:600;color:#1E293B}");
            out.println(".day:hover:not(.disabled):not(.unavailable){background:#2563EB;color:white}");
            out.println(".day.disabled{background:#F1F5F9;color:#94A3B8;cursor:not-allowed}");
            out.println(".day.unavailable{background:#FEE2E2;color:#991B1B;cursor:not-allowed;text-decoration:line-through}");
            out.println(".day.today{border:2px solid #2563EB;font-weight:700}");
            out.println(".day.empty{background:transparent;cursor:default}");
            out.println(".legend{background:#FFFFFF;border-radius:10px;padding:16px;margin-top:20px;text-align:center;border:1px solid #E2E8F0}");
            out.println(".legend-item{display:inline-block;margin:0 12px;font-size:13px;color:#64748B}");
            out.println(".legend-box{display:inline-block;width:16px;height:16px;margin-right:6px;border-radius:4px;vertical-align:middle}");
            out.println("</style></head><body>");

            out.println("<div class='navbar'><button class='hamburger-btn' onclick='toggleSidebar()' aria-label='Toggle menu'><span></span><span></span><span></span></button><span class='navbar-brand'>MediCare+</span><span class='navbar-title'>Book Appointment</span></div>");
            out.println("<div class='sidebar'>");
            out.println("<div class='welcome'>Welcome, " + (patientName != null ? patientName : "Patient") + "</div>");
            out.println("<a href='dashboard' class='menu-item'>🔍 View Doctors</a>");
            out.println("<a href='medical-details' class='menu-item'>🩺 Medical Info</a>");
            out.println("<a href='appointments' class='menu-item'>📅 My Appointments</a>");
            out.println("<a href='logout' class='logout-btn'>🚪 Logout</a>");
            out.println("</div>");

            out.println("<div class='main-content'>");
            out.println("<div class='container'>");
            out.println("<div class='page-header'>");
            out.println("<h2>Select Appointment Date</h2>");
            out.println("<p>Booking with Dr. " + doctorName + "</p>");
            out.println("</div>");
            
            out.println("<div class='calendar-container'>");
            
            LocalDate today = LocalDate.now();
            generateCalendar(out, today, doctorId, unavailableDates);
            
            LocalDate nextMonth = today.plusMonths(1);
            generateCalendar(out, nextMonth, doctorId, unavailableDates);
            
            out.println("</div>");
            
            out.println("<div class='legend'>");
            out.println("<div class='legend-item'><span class='legend-box' style='background:#F8FAFC;border:1px solid #E2E8F0'></span>Available</div>");
            out.println("<div class='legend-item'><span class='legend-box' style='background:#FEE2E2'></span>Doctor Unavailable</div>");
            out.println("<div class='legend-item'><span class='legend-box' style='background:#F1F5F9'></span>Past Date</div>");
            out.println("</div>");
            out.println("</div></div>");
            out.println("<div class='sidebar-overlay' onclick='toggleSidebar()'></div>");
            out.println("<script>");
            out.println("var isMobile = function(){ return window.innerWidth <= 768; };");
            out.println("function toggleSidebar(){");
            out.println("  if(isMobile()){");
            out.println("    document.body.classList.toggle('sidebar-mobile-open');");
            out.println("  } else {");
            out.println("    document.body.classList.toggle('sidebar-collapsed');");
            out.println("  }");
            out.println("}");
            out.println("window.addEventListener('resize', function(){");
            out.println("  if(!isMobile()){");
            out.println("    document.body.classList.remove('sidebar-mobile-open');");
            out.println("  } else {");
            out.println("    document.body.classList.remove('sidebar-collapsed');");
            out.println("  }");
            out.println("});");
            out.println("</script>");
            out.println("</body></html>");

        } catch (Exception ex) {
            ex.printStackTrace();
            response.getWriter().println("<h2>Error: " + ex.getMessage() + "</h2>");
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

    private void generateCalendar(java.io.PrintWriter out, LocalDate month, int doctorId, Set<LocalDate> unavailableDates) {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = month.withDayOfMonth(1);
        int daysInMonth = month.lengthOfMonth();
        int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        
        String monthName = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = month.getYear();
        
        out.println("<div class='calendar'>");
        out.println("<div class='month-header'>" + monthName + " " + year + "</div>");
        
        out.println("<div class='weekdays'>");
        String[] weekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : weekdays) {
            out.println("<div class='weekday'>" + day + "</div>");
        }
        out.println("</div>");
        
        out.println("<div class='days'>");
        
        for (int i = 0; i < startDayOfWeek; i++) {
            out.println("<div class='day empty'></div>");
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = month.withDayOfMonth(day);
            boolean isPast = currentDate.isBefore(today);
            boolean isUnavailable = unavailableDates.contains(currentDate);
            boolean isToday = currentDate.equals(today);
            
            String cssClass = "day";
            if (isToday) cssClass += " today";
            if (isPast) cssClass += " disabled";
            if (isUnavailable) cssClass += " unavailable";
            
            if (isPast || isUnavailable) {
                out.println("<div class='" + cssClass + "'>" + day + "</div>");
            } else {
                out.println("<button class='" + cssClass + "' onclick=\"location.href='book-appointment?doctorId=" + doctorId + "&date=" + currentDate + "'\">" + day + "</button>");
            }
        }
        
        out.println("</div>");
        out.println("</div>");
    }

    private LocalTime parseTime(String timeStr) {
        timeStr = timeStr.trim().toLowerCase();
        boolean isPM = timeStr.contains("pm");
        boolean isAM = timeStr.contains("am");
        timeStr = timeStr.replaceAll("[ap]m", "").trim();
        
        int hour = 0;
        int minute = 0;
        
        if (timeStr.contains(":")) {
            String[] parts = timeStr.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } else {
            hour = Integer.parseInt(timeStr);
            minute = 0;
        }
        
        if (isPM && hour != 12) {
            hour += 12;
        } else if (isAM && hour == 12) {
            hour = 0;
        }
        
        return LocalTime.of(hour, minute);
    }

    private void showAvailableSlots(HttpServletRequest request, HttpServletResponse response, String dateStr)
            throws ServletException, IOException {

        String doctorIdParam = request.getParameter("doctorId");
        int doctorId = Integer.parseInt(doctorIdParam);
        LocalDate selectedDate = LocalDate.parse(dateStr);
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.html");
            return;
        }
        String patientUsername = (String) session.getAttribute("username");
        String patientName = (String) session.getAttribute("name");

        response.setContentType("text/html; charset=UTF-8");
        Connection conn = null;
        PreparedStatement pstmt = null;
        PreparedStatement bookingCheck = null;
        ResultSet rs = null;
        ResultSet bookedSlots = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "SELECT full_name, clinic_visit_schedule FROM doctor_profiles WHERE doctor_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                response.getWriter().println("Doctor not found");
                return;
            }

            String doctorName = rs.getString("full_name");
            String slotText = rs.getString("clinic_visit_schedule");
            
            rs.close();
            pstmt.close();
            
            String[] parts = slotText.split("-");
            if (parts.length != 2) {
                response.getWriter().println("Invalid time slot format");
                return;
            }
            
            LocalTime start = parseTime(parts[0]);
            LocalTime end = parseTime(parts[1]);

            java.io.PrintWriter out = response.getWriter();
            
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@400;500;600&display=swap' rel='stylesheet'>");
            out.println("<title>Book Appointment</title>");
            out.println("<style>");
            out.println("* { margin: 0; padding: 0; box-sizing: border-box; }");
            out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh;margin:0}");
            out.println(".navbar{position:fixed;top:0;left:0;right:0;height:64px;background:#2563EB;display:flex;align-items:center;padding:0 24px;z-index:1100;box-shadow:0 1px 3px rgba(0,0,0,0.1)}");
            out.println(".navbar-brand{color:white;font-size:20px;font-weight:700;margin-right:16px}");
            out.println(".navbar-title{color:rgba(255,255,255,0.8);font-size:14px;font-weight:500}");
            out.println(".hamburger-btn{display:flex;flex-direction:column;justify-content:space-between;width:24px;height:18px;cursor:pointer;background:none;border:none;padding:0;margin-right:16px;flex-shrink:0}");
            out.println(".hamburger-btn span{display:block;height:2px;width:100%;background:white;border-radius:2px}");
            out.println(".sidebar{position:fixed;left:0;top:64px;width:260px;height:calc(100% - 64px);background:#1E293B;padding:24px 16px;color:white;z-index:1000;overflow-y:auto;transition:transform 0.3s ease}");
            out.println(".welcome{padding:8px 12px;background:rgba(255,255,255,0.07);border-radius:8px;margin-bottom:16px;font-size:13px;color:#CBD5E1}");
            out.println(".menu-item{display:block;padding:10px 12px;margin:2px 0;color:#94A3B8;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
            out.println(".menu-item:hover{background:rgba(37,99,235,0.2);color:#CBD5E1}");
            out.println(".logout-btn{display:block;padding:10px 12px;margin:20px 0 2px;color:#FCA5A5;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
            out.println(".logout-btn:hover{background:rgba(220,38,38,0.15);color:#FCA5A5}");
            out.println(".sidebar-overlay{display:none;position:fixed;inset:0;background:rgba(0,0,0,0.4);z-index:999;top:64px}");
            out.println("body.sidebar-collapsed .sidebar{transform:translateX(-260px)}");
            out.println("body.sidebar-collapsed .main-content{margin-left:0}");
            out.println("body.sidebar-mobile-open .sidebar{transform:translateX(0)}");
            out.println("body.sidebar-mobile-open .sidebar-overlay{display:block}");
            out.println("@media(max-width:768px){.main-content{margin-left:0 !important}.sidebar{transform:translateX(-260px)}}");
            out.println(".main-content{margin-left:260px;padding:24px;min-height:100vh;background:#F8FAFC;padding-top:88px;transition:margin-left 0.3s ease}");
            out.println(".page-header{text-align:center;margin-bottom:24px}");
            out.println(".page-header h2{color:#1E293B;font-size:24px;font-weight:700;margin-bottom:6px}");
            out.println(".date-badge{text-align:center;margin-bottom:24px}");
            out.println(".date-badge span{display:inline-block;background:#EFF6FF;color:#2563EB;font-size:15px;font-weight:600;padding:8px 20px;border-radius:20px;border:1px solid #BFDBFE}");
            out.println(".container{max-width:800px;margin:0 auto}");
            out.println("table{border-collapse:collapse;width:100%;background:#FFFFFF;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0}");
            out.println("th,td{padding:14px 16px;text-align:center}");
            out.println("th{background:#F1F5F9;color:#64748B;font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px}");
            out.println("tr:hover td{background:#F8FAFC}");
            out.println("td{color:#1E293B;border-bottom:1px solid #F1F5F9}");
            out.println(".slot-cell{display:flex;gap:8px;justify-content:center;align-items:center}");
            out.println("button{border:none;padding:10px 20px;border-radius:8px;cursor:pointer;font-weight:600;transition:all 0.2s;font-size:13px}");
            out.println(".book-btn{background:#1D4ED8;color:white}");
            out.println(".book-btn:hover{background:#1E40AF}");
            out.println(".booked-btn{background:#94A3B8;color:white;cursor:not-allowed;opacity:0.8}");
            out.println(".cancel-btn{background:#DC2626;color:white}");
            out.println(".cancel-btn:hover{background:#B91C1C}");
            out.println(".back-link{display:block;text-align:center;margin-top:20px}");
            out.println(".back-link a{color:#2563EB;text-decoration:none;font-size:14px;font-weight:500;padding:10px 20px;background:#EFF6FF;border-radius:8px;display:inline-block;transition:all 0.2s;border:1px solid #BFDBFE}");
            out.println(".back-link a:hover{background:#DBEAFE}");
            out.println("</style></head><body>");

            out.println("<div class='navbar'><button class='hamburger-btn' onclick='toggleSidebar()' aria-label='Toggle menu'><span></span><span></span><span></span></button><span class='navbar-brand'>MediCare+</span><span class='navbar-title'>Book Appointment</span></div>");
            out.println("<div class='sidebar'>");
            out.println("<div class='welcome'>Welcome, " + (patientName != null ? patientName : "Patient") + "</div>");
            out.println("<a href='dashboard' class='menu-item'>🔍 View Doctors</a>");
            out.println("<a href='medical-details' class='menu-item'>🩺 Medical Info</a>");
            out.println("<a href='appointments' class='menu-item'>📅 My Appointments</a>");
            out.println("<a href='logout' class='logout-btn'>🚪 Logout</a>");
            out.println("</div>");

            out.println("<div class='main-content'>");
            out.println("<div class='container'>");
            out.println("<div class='page-header'>");
            out.println("<h2>Book Appointment with Dr. " + doctorName + "</h2>");
            out.println("</div>");
            out.println("<div class='date-badge'><span>📅 " + selectedDate.toString() + "</span></div>");
            out.println("<table><tr><th>Time Slot</th><th>Status</th></tr>");

            bookingCheck = conn.prepareStatement(
                "SELECT appointment_time FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_status = 'booked'"
            );
            bookingCheck.setInt(1, doctorId);
            bookingCheck.setDate(2, java.sql.Date.valueOf(selectedDate));
            bookedSlots = bookingCheck.executeQuery();
            
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
                out.println("<td><strong>" + formatTime(currentSlot) + "</strong></td>");
                out.println("<td><div class='slot-cell'>");
                
                if (isBooked) {
                    out.println("<button class='booked-btn' disabled>Booked</button>");
                    out.println("<form action='book-appointment' method='post' style='margin:0'>");
                    out.println("<input type='hidden' name='action' value='cancel'>");
                    out.println("<input type='hidden' name='doctorId' value='" + doctorId + "'>");
                    out.println("<input type='hidden' name='date' value='" + dateStr + "'>");
                    out.println("<input type='hidden' name='time' value='" + timeStr + "'>");
                    out.println("<button type='submit' class='cancel-btn'>Cancel</button>");
                    out.println("</form>");
                } else {
                    out.println("<form action='book-appointment' method='post' style='margin:0'>");
                    out.println("<input type='hidden' name='action' value='book'>");
                    out.println("<input type='hidden' name='doctorId' value='" + doctorId + "'>");
                    out.println("<input type='hidden' name='date' value='" + dateStr + "'>");
                    out.println("<input type='hidden' name='time' value='" + timeStr + "'>");
                    out.println("<button type='submit' class='book-btn'>Book</button>");
                    out.println("</form>");
                }
                
                out.println("</div></td>");
                out.println("</tr>");
                
                currentSlot = currentSlot.plusMinutes(15);
            }
            
            out.println("</table>");
            out.println("<div class='back-link'><a href='book-appointment?doctorId=" + doctorId + "'>← Change Date</a></div>");
            out.println("</div></div>");
            out.println("<div class='sidebar-overlay' onclick='toggleSidebar()'></div>");
            out.println("<script>");
            out.println("var isMobile = function(){ return window.innerWidth <= 768; };");
            out.println("function toggleSidebar(){");
            out.println("  if(isMobile()){");
            out.println("    document.body.classList.toggle('sidebar-mobile-open');");
            out.println("  } else {");
            out.println("    document.body.classList.toggle('sidebar-collapsed');");
            out.println("  }");
            out.println("}");
            out.println("window.addEventListener('resize', function(){");
            out.println("  if(!isMobile()){");
            out.println("    document.body.classList.remove('sidebar-mobile-open');");
            out.println("  } else {");
            out.println("    document.body.classList.remove('sidebar-collapsed');");
            out.println("  }");
            out.println("});");
            out.println("</script>");
            out.println("</body></html>");

        } catch (Exception ex) {
            ex.printStackTrace();
            response.getWriter().println("<h2>Error: " + ex.getMessage() + "</h2>");
        } finally {
            try {
                if (bookedSlots != null) bookedSlots.close();
                if (bookingCheck != null) bookingCheck.close();
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String formatTime(LocalTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();
        String period = "AM";
        
        if (hour >= 12) {
            period = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        }
        if (hour == 0) {
            hour = 12;
        }
        
        return String.format("%d:%02d %s", hour, minute, period);
    }

    private void handleBooking(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.html");
            return;
        }
        String patientUsername = (String) session.getAttribute("username");

        int doctorId = Integer.parseInt(request.getParameter("doctorId"));
        String dateStr = request.getParameter("date");
        String time = request.getParameter("time");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_time = ? AND appointment_date = ? AND appointment_status = 'booked'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, doctorId);
            checkStmt.setString(2, time);
            checkStmt.setDate(3, java.sql.Date.valueOf(dateStr));
            java.sql.ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            int existingCount = checkRs.getInt(1);
            checkRs.close();
            checkStmt.close();

            if (existingCount > 0) {
                conn.rollback();
                response.sendRedirect("book-appointment?doctorId=" + doctorId + "&date=" + dateStr + "&msg=slot_taken");
                return;
            }

            String sql = "INSERT INTO appointments (doctor_id, patient_account_username, appointment_time, appointment_date, appointment_status) VALUES (?, ?, ?, ?, 'booked')";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, patientUsername);
            pstmt.setString(3, time);
            pstmt.setDate(4, java.sql.Date.valueOf(dateStr));
            pstmt.executeUpdate();

            conn.commit();
            response.sendRedirect("book-appointment?doctorId=" + doctorId + "&date=" + dateStr);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<html><body style='background:#F8FAFC;color:#1E293B;text-align:center;padding:50px;font-family:Inter,sans-serif'>");
            response.getWriter().println("<h2>Error booking appointment</h2>");
            response.getWriter().println("<p>" + ex.getMessage() + "</p>");
            response.getWriter().println("<a href='book-appointment?doctorId=" + doctorId + "' style='color:#1D4ED8;text-decoration:none;background:#EFF6FF;padding:10px 20px;border-radius:8px;display:inline-block;border:1px solid #BFDBFE;font-weight:600'>Go Back</a>");
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
        String dateStr = request.getParameter("date");
        String time = request.getParameter("time");
        
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE appointments SET appointment_status = 'cancelled' WHERE doctor_id = ? AND appointment_time = ? AND appointment_date = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, time);
            pstmt.setDate(3, java.sql.Date.valueOf(dateStr));
            pstmt.executeUpdate();
            conn.commit();
            
            response.sendRedirect("book-appointment?doctorId=" + doctorId + "&date=" + dateStr);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.setContentType("text/html");
            response.getWriter().println("<html><body style='background:#F8FAFC;color:#1E293B;text-align:center;padding:50px;font-family:Inter,sans-serif'>");
            response.getWriter().println("<h2>Error cancelling appointment</h2>");
            response.getWriter().println("<p>" + ex.getMessage() + "</p>");
            response.getWriter().println("<a href='book-appointment?doctorId=" + doctorId + "&date=" + dateStr + "' style='color:#1D4ED8;text-decoration:none;background:#EFF6FF;padding:10px 20px;border-radius:8px;display:inline-block;border:1px solid #BFDBFE;font-weight:600'>Go Back</a>");
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
}
