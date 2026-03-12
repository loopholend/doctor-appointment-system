package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/doctor/day-off")
public class DoctorDayOffServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("doctorUsername") == null) {
            response.sendRedirect("login.html");
            return;
        }
        
        String doctorName = (String) session.getAttribute("doctorName");
        String doctorUsername = (String) session.getAttribute("doctorUsername");
        int doctorId = getDoctorIdFromUsername(doctorUsername);
        
        if (doctorId == 0) {
            response.getWriter().println("<h2>Error: Doctor profile not found</h2>");
            return;
        }
        
        showDayOffCalendar(request, response, doctorId, doctorName);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private int getDoctorIdFromUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String sql = "SELECT doctor_id FROM doctor_profiles WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return 0;
    }

    private void showDayOffCalendar(HttpServletRequest request, HttpServletResponse response, int doctorId, String doctorName)
            throws ServletException, IOException {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "SELECT unavailable_date, reason FROM doctor_unavailable_dates WHERE doctor_id = ? ORDER BY unavailable_date";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, doctorId);
            rs = pstmt.executeQuery();
            
            Set<LocalDate> markedDates = new HashSet<>();
            Map<LocalDate, String> dateReasons = new HashMap<>();
            
            while (rs.next()) {
                LocalDate date = rs.getDate("unavailable_date").toLocalDate();
                String reason = rs.getString("reason");
                markedDates.add(date);
                if (reason != null) {
                    dateReasons.put(date, reason);
                }
            }

            response.setContentType("text/html; charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();
            
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Manage Day Off</title>");
            NavHelper.writeFontsLink(out);
            out.println("<style>");
            NavHelper.writeLayoutCSS(out);
            out.println("h1{color:#1E293B;text-align:center;margin-bottom:24px;font-size:24px;font-weight:700}");
            out.println(".container{max-width:900px;margin:0 auto}");
            out.println(".info-box{background:#FFFFFF;border-radius:12px;padding:20px;margin-bottom:16px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0}");
            out.println(".info-box h3{color:#1E293B;margin-bottom:12px;font-size:16px;font-weight:700}");
            out.println(".info-box p{color:#64748B;line-height:1.6;font-size:14px}");
            out.println(".calendar-container{display:flex;gap:20px;flex-wrap:wrap;justify-content:center}");
            out.println(".calendar{background:#FFFFFF;border-radius:12px;padding:24px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;min-width:320px}");
            out.println(".month-header{text-align:center;font-size:18px;font-weight:700;color:#1E293B;margin-bottom:16px;padding:10px;background:#EFF6FF;border-radius:8px}");
            out.println(".weekdays{display:grid;grid-template-columns:repeat(7, 1fr);gap:4px;margin-bottom:8px}");
            out.println(".weekday{text-align:center;font-weight:600;color:#64748B;padding:8px;font-size:12px;text-transform:uppercase;letter-spacing:0.5px}");
            out.println(".days{display:grid;grid-template-columns:repeat(7, 1fr);gap:4px}");
            out.println(".day{padding:12px;text-align:center;border-radius:8px;cursor:pointer;transition:all 0.2s;border:2px solid transparent;background:#F8FAFC;font-size:13px;font-weight:600;color:#1E293B}");
            out.println(".day:hover:not(.disabled):not(.empty){background:#2563EB;color:white}");
            out.println(".day.disabled{background:#F1F5F9;color:#94A3B8;cursor:not-allowed}");
            out.println(".day.selected{background:#FEE2E2;border-color:#DC2626;color:#991B1B}");
            out.println(".day.today{border:2px solid #2563EB}");
            out.println(".day.empty{background:transparent;cursor:default}");
            out.println(".submit-section{text-align:center;margin-top:24px}");
            out.println(".btn-submit{padding:12px 32px;background:#1D4ED8;color:white;border:none;border-radius:8px;font-size:15px;font-weight:600;cursor:pointer;transition:all 0.2s}");
            out.println(".btn-submit:hover{background:#1E40AF}");
            out.println(".marked-dates{background:#FFFFFF;border-radius:12px;padding:20px;margin-bottom:16px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0}");
            out.println(".marked-dates h3{color:#1E293B;margin-bottom:12px;font-size:16px;font-weight:700}");
            out.println(".marked-date-item{padding:10px 14px;margin:6px 0;background:#FEE2E2;border-left:4px solid #DC2626;border-radius:6px;display:flex;justify-content:space-between;align-items:center;font-size:14px;color:#1E293B}");
            out.println(".btn-remove{padding:6px 14px;background:#DC2626;color:white;border:none;border-radius:6px;cursor:pointer;font-size:12px;font-weight:600;transition:all 0.2s}");
            out.println(".btn-remove:hover{background:#B91C1C}");
            out.println("</style>");
            
            out.println("<script>");
            out.println("let selectedDates = new Set();");
            out.println("function toggleDate(dateStr, element) {");
            out.println("  if (selectedDates.has(dateStr)) {");
            out.println("    selectedDates.delete(dateStr);");
            out.println("    element.classList.remove('selected');");
            out.println("  } else {");
            out.println("    selectedDates.add(dateStr);");
            out.println("    element.classList.add('selected');");
            out.println("  }");
            out.println("  document.getElementById('selectedDatesInput').value = Array.from(selectedDates).join(',');");
            out.println("}");
            out.println("</script>");
            
            out.println("</head><body>");
            NavHelper.writeNavbar(out, "Manage Day Off");
            NavHelper.writeDoctorSidebar(out, doctorName, "dayoff");
            
            out.println("<div class='main-content'>");
            out.println("<div class='container'>");
            out.println("<h1>Manage Unavailable Dates</h1>");
            
            // Info box
            out.println("<div class='info-box'>");
            out.println("<h3>📅 How to use:</h3>");
            out.println("<p>1. Click on dates you will NOT be available<br>");
            out.println("2. Selected dates will turn red<br>");
            out.println("3. Click 'Submit' to save your unavailable dates<br>");
            out.println("4. Patients won't be able to book appointments on these dates</p>");
            out.println("</div>");
            
            // Show already marked dates
            if (!markedDates.isEmpty()) {
                out.println("<div class='marked-dates'>");
                out.println("<h3>Currently Marked Dates (" + markedDates.size() + "):</h3>");
                for (LocalDate date : markedDates) {
                    String reason = dateReasons.get(date);
                    out.println("<div class='marked-date-item'>");
                    out.println("<span>" + date.toString() + (reason != null ? " - " + reason : "") + "</span>");
                    out.println("<form action='remove-day-off' method='post' style='margin:0'>");
                    out.println("<input type='hidden' name='doctorId' value='" + doctorId + "'>");
                    out.println("<input type='hidden' name='date' value='" + date + "'>");
                    out.println("<button type='submit' class='btn-remove' onclick='return confirm(\"Remove this date?\")'>Remove</button>");
                    out.println("</form>");
                    out.println("</div>");
                }
                out.println("</div>");
            }
            
            // Calendar form
            out.println("<form action='save-day-off' method='post'>");
            out.println("<input type='hidden' name='doctorId' value='" + doctorId + "'>");
            out.println("<input type='hidden' id='selectedDatesInput' name='selectedDates' value=''>");
            
            out.println("<div class='calendar-container'>");
            
            LocalDate today = LocalDate.now();
            generateCalendar(out, today, markedDates);
            
            LocalDate nextMonth = today.plusMonths(1);
            generateCalendar(out, nextMonth, markedDates);
            
            out.println("</div>");
            
            out.println("<div class='submit-section'>");
            out.println("<button type='submit' class='btn-submit'>✓ Submit Selected Dates</button>");
            out.println("</div>");
            
            out.println("</form>");
            out.println("</div>");
            out.println("</div>");
            NavHelper.writeSidebarJS(out);
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

    private void generateCalendar(java.io.PrintWriter out, LocalDate month, Set<LocalDate> markedDates) {
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
            boolean isMarked = markedDates.contains(currentDate);
            boolean isToday = currentDate.equals(today);
            
            String cssClass = "day";
            if (isToday) cssClass += " today";
            if (isPast) cssClass += " disabled";
            if (isMarked) cssClass += " selected";
            
            if (isPast) {
                out.println("<div class='" + cssClass + "'>" + day + "</div>");
            } else {
                out.println("<div class='" + cssClass + "' onclick=\"toggleDate('" + currentDate + "', this)\">" + day + "</div>");
            }
        }
        
        out.println("</div>");
        out.println("</div>");
    }
}
