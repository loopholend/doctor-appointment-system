package common;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Shared navigation utility — call these methods from every servlet
 * instead of duplicating navbar/sidebar/CSS/session code in each file.
 */
public class NavHelper {

    // ─── PATIENT / DOCTOR LAYOUT CSS ─────────────────────────────────────────

    /** Write the base layout CSS (call inside &lt;style&gt; in &lt;head&gt;). */
    public static void writeLayoutCSS(PrintWriter out) {
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh}");
        // Navbar
        out.println(".navbar{position:fixed;top:0;left:0;right:0;height:64px;background:linear-gradient(to right,#2563EB,#1D4ED8);display:flex;align-items:center;padding:0 24px;z-index:1100;box-shadow:0 2px 4px rgba(0,0,0,0.15)}");
        out.println(".hamburger-btn{display:flex;flex-direction:column;justify-content:space-between;width:24px;height:18px;cursor:pointer;background:none;border:none;padding:0;margin-right:16px;flex-shrink:0}");
        out.println(".hamburger-btn span{display:block;height:2px;width:100%;background:white;border-radius:2px}");
        out.println(".navbar-brand{color:white;font-size:20px;font-weight:700;font-family:'Poppins',sans-serif;margin-right:12px}");
        out.println(".navbar-title{color:rgba(255,255,255,0.85);font-size:14px;font-weight:500}");
        // Sidebar
        out.println(".sidebar{position:fixed;left:0;top:64px;width:260px;height:calc(100% - 64px);background:#1E293B;padding:24px 16px;color:white;z-index:1000;overflow-y:auto;transition:transform 0.3s ease}");
        out.println(".welcome{padding:8px 12px;background:rgba(255,255,255,0.07);border-radius:8px;margin-bottom:16px;font-size:13px;color:#CBD5E1}");
        out.println(".menu-item{display:block;padding:10px 12px;margin:2px 0;color:#94A3B8;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
        out.println(".menu-item:hover{background:rgba(37,99,235,0.25);color:#E2E8F0}");
        out.println(".menu-item.active{background:#2563EB;color:white}");
        out.println(".logout-btn{display:block;padding:10px 12px;margin:20px 0 2px;color:#FCA5A5;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
        out.println(".logout-btn:hover{background:rgba(220,38,38,0.15)}");
        // Sidebar overlay + collapse behaviour
        out.println(".sidebar-overlay{display:none;position:fixed;inset:0;top:64px;background:rgba(0,0,0,0.4);z-index:999}");
        out.println("body.sidebar-collapsed .sidebar{transform:translateX(-260px)}");
        out.println("body.sidebar-collapsed .main-content{margin-left:0}");
        out.println("body.sidebar-mobile-open .sidebar{transform:translateX(0)}");
        out.println("body.sidebar-mobile-open .sidebar-overlay{display:block}");
        // Main content area
        out.println(".main-content{margin-left:260px;padding:24px;min-height:100vh;background:#F8FAFC;padding-top:88px;transition:margin-left 0.3s ease}");
        // Mobile: sidebar hidden by default, main content full-width
        out.println("@media(max-width:768px){.main-content{margin-left:0!important}.sidebar{transform:translateX(-260px)}}");
    }

    // ─── ADMIN LAYOUT CSS ────────────────────────────────────────────────────

    /** Write the admin layout CSS (call inside &lt;style&gt; in &lt;head&gt;). */
    public static void writeAdminLayoutCSS(PrintWriter out) {
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter','Poppins',sans-serif;background:#F8FAFC;min-height:100vh}");
        out.println(".navbar{position:fixed;top:0;left:0;right:0;height:64px;background:#2563EB;display:flex;align-items:center;padding:0 24px;z-index:1100;box-shadow:0 1px 3px rgba(0,0,0,0.1)}");
        out.println(".navbar-brand{color:white;font-size:20px;font-weight:700;margin-right:16px}");
        out.println(".navbar-title{color:rgba(255,255,255,0.8);font-size:14px;font-weight:500}");
        out.println(".navbar-actions{display:flex;align-items:center;gap:8px;margin-left:auto}");
        out.println(".navbar-quick-btn{display:inline-flex;align-items:center;gap:6px;padding:7px 14px;background:rgba(255,255,255,0.18);color:white;text-decoration:none;border-radius:20px;font-size:13px;font-weight:600;transition:all 0.2s;border:1px solid rgba(255,255,255,0.3)}");
        out.println(".navbar-quick-btn:hover{background:rgba(255,255,255,0.32);border-color:rgba(255,255,255,0.6)}");
        out.println(".sidebar{position:fixed;left:0;top:64px;width:260px;height:calc(100% - 64px);background:#1E293B;padding:24px 16px;color:white;z-index:1000;overflow-y:auto}");
        out.println(".admin-badge{background:#2563EB;padding:8px 12px;border-radius:8px;margin-bottom:16px;font-size:12px;font-weight:600;text-align:center;letter-spacing:0.5px}");
        out.println(".welcome{padding:8px 12px;background:rgba(255,255,255,0.07);border-radius:8px;margin-bottom:16px;font-size:13px;color:#CBD5E1}");
        out.println(".menu-item{display:block;padding:10px 12px;margin:2px 0;color:#94A3B8;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
        out.println(".menu-item:hover{background:rgba(37,99,235,0.2);color:#CBD5E1}");
        out.println(".menu-item.active{background:#2563EB;color:white}");
        out.println(".menu-icon{margin-right:8px}");
        out.println(".menu-sub{font-size:11px;font-weight:700;color:#475569;text-transform:uppercase;letter-spacing:0.5px;padding:12px 12px 4px;margin-top:6px}");
        out.println(".logout-btn{display:block;padding:10px 12px;margin:20px 0 2px;color:#FCA5A5;text-decoration:none;border-radius:8px;transition:all 0.2s;font-size:14px;font-weight:500}");
        out.println(".logout-btn:hover{background:rgba(220,38,38,0.15);color:#FCA5A5}");
        out.println(".main-content{margin-left:260px;padding:24px;min-height:100vh;background:#F8FAFC;padding-top:88px}");
    }

    /** Write Google Fonts link tag (call in &lt;head&gt; before &lt;style&gt;). */
    public static void writeFontsLink(PrintWriter out) {
        out.println("<link rel='preconnect' href='https://fonts.googleapis.com'>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@400;500;600;700&display=swap' rel='stylesheet'>");
    }

    // ─── PATIENT / DOCTOR HTML ───────────────────────────────────────────────

    /**
     * Write the fixed navbar with hamburger button.
     * Call immediately after &lt;body&gt;.
     */
    public static void writeNavbar(PrintWriter out, String pageTitle) {
        out.println("<div class='navbar'>");
        out.println("  <button class='hamburger-btn' onclick='toggleSidebar()' aria-label='Toggle menu'><span></span><span></span><span></span></button>");
        out.println("  <span class='navbar-brand'>MediCare+</span>");
        out.println("  <span class='navbar-title'>" + pageTitle + "</span>");
        out.println("</div>");
        out.println("<div class='sidebar-overlay' onclick='toggleSidebar()'></div>");
    }

    /**
     * Write the patient sidebar.
     * @param activeItem  one of: "doctors" | "medical" | "appointments" | "delete"
     */
    public static void writePatientSidebar(PrintWriter out, String patientName, String activeItem) {
        String a = activeItem == null ? "" : activeItem;
        out.println("<div class='sidebar'>");
        out.println("  <div class='welcome'>&#128100; " + esc(patientName != null ? patientName : "Patient") + "</div>");
        out.println("  <a href='dashboard' class='menu-item" + ("doctors".equals(a) ? " active" : "") + "'>&#128269; View Doctors</a>");
        out.println("  <a href='medical-details'    class='menu-item" + ("medical".equals(a)  ? " active" : "") + "'>&#129658; Medical Info</a>");
        out.println("  <a href='appointments'    class='menu-item" + ("appointments".equals(a) ? " active" : "") + "'>&#128197; My Appointments</a>");
        out.println("  <a href='delete-account' class='menu-item" + ("delete".equals(a) ? " active" : "") + "'>&#128465; Delete Account</a>");
        out.println("  <a href='logout' class='logout-btn'>&#128682; Logout</a>");
        out.println("</div>");
    }

    /**
     * Write the doctor sidebar.
     * @param activeItem  one of: "patients" | "profile" | "dayoff" | "delete"
     */
    public static void writeDoctorSidebar(PrintWriter out, String doctorName, String activeItem) {
        String a = activeItem == null ? "" : activeItem;
        out.println("<div class='sidebar'>");
        out.println("  <div class='welcome'>&#128104;&#8205;&#9877; Dr. " + esc(doctorName != null ? doctorName : "Doctor") + "</div>");
        out.println("  <a href='dashboard' class='menu-item" + ("patients".equals(a) ? " active" : "") + "'>&#128101; View Patients</a>");
        out.println("  <a href='profile-edit'       class='menu-item" + ("profile".equals(a)  ? " active" : "") + "'>&#9999; Update Profile</a>");
        out.println("  <a href='day-off'           class='menu-item" + ("dayoff".equals(a)   ? " active" : "") + "'>&#128197; Manage Day Off</a>");
        out.println("  <a href='delete-account' class='menu-item" + ("delete".equals(a) ? " active" : "") + "'>&#128465; Delete Account</a>");
        out.println("  <a href='logout' class='logout-btn'>&#128682; Logout</a>");
        out.println("</div>");
    }

    // ─── ADMIN HTML ──────────────────────────────────────────────────────────

    /**
     * Write the admin navbar (no hamburger — admin sidebar is always visible).
     * Includes quick-action buttons on the right side.
     * Call immediately after &lt;body&gt;.
     */
    public static void writeAdminNavbar(PrintWriter out, String pageTitle) {
        out.println("<div class='navbar'>");
        out.println("  <span class='navbar-brand'>MediCare+</span>");
        out.println("  <span class='navbar-title'>" + pageTitle + "</span>");
        out.println("  <div class='navbar-actions'>");
        out.println("    <a href='add-doctor'  class='navbar-quick-btn'>&#10133; Add Doctor</a>");
        out.println("    <a href='add-patient' class='navbar-quick-btn'>&#10133; Add Patient</a>");
        out.println("  </div>");
        out.println("</div>");
    }

    /**
     * Write the admin sidebar.
     * @param activeItem  one of: "dashboard" | "requests" | "doctors" | "add-doctor" | "patients" | "add-patient" | "report"
     */
    public static void writeAdminSidebar(PrintWriter out, String adminName, String activeItem) {
        String a = activeItem == null ? "" : activeItem;
        out.println("<div class='sidebar'>");
        out.println("  <div class='admin-badge'>ADMINISTRATOR</div>");
        out.println("  <div class='welcome'>Welcome, " + esc(adminName != null ? adminName : "Admin") + "</div>");
        out.println("  <a href='dashboard'       class='menu-item" + ("dashboard".equals(a) ? " active" : "") + "'><span class='menu-icon'>&#127968;</span>Dashboard</a>");
        out.println("  <a href='doctor-requests' class='menu-item" + ("requests".equals(a)  ? " active" : "") + "'><span class='menu-icon'>&#128203;</span>Doctor Requests</a>");
        out.println("  <div class='menu-sub'>Doctors</div>");
        out.println("  <a href='doctors'         class='menu-item" + ("doctors".equals(a)     ? " active" : "") + "'><span class='menu-icon'>&#128104;&#8205;&#9877;</span>Manage Doctors</a>");
        out.println("  <a href='add-doctor'      class='menu-item" + ("add-doctor".equals(a)  ? " active" : "") + "'><span class='menu-icon'>&#10133;</span>Add Doctor</a>");
        out.println("  <div class='menu-sub'>Patients</div>");
        out.println("  <a href='patients'        class='menu-item" + ("patients".equals(a)    ? " active" : "") + "'><span class='menu-icon'>&#128101;</span>Manage Patients</a>");
        out.println("  <a href='add-patient'     class='menu-item" + ("add-patient".equals(a) ? " active" : "") + "'><span class='menu-icon'>&#10133;</span>Add Patient</a>");
        out.println("  <div class='menu-sub'>Reports</div>");
        out.println("  <a href='report'          class='menu-item" + ("report".equals(a)      ? " active" : "") + "'><span class='menu-icon'>&#128202;</span>Generate Report</a>");
        out.println("  <a href='logout' class='logout-btn'>&#128682; Logout</a>");
        out.println("</div>");
    }

    // ─── SHARED LOGOUT PAGE ──────────────────────────────────────────────────

    /**
     * Write the logout success page.
     * @param name      user's display name (may be null)
     * @param prefix    prefix before the name, e.g. "" or "Dr. "
     * @param loginUrl  URL to redirect to, e.g. "pateintlogin.html"
     */
    public static void writeLogoutPage(PrintWriter out, String name, String prefix, String loginUrl) {
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<meta http-equiv='refresh' content='3;url=" + loginUrl + "'>");
        out.println("<title>Logged Out</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Inter',sans-serif;background:linear-gradient(135deg,#2563EB,#1D4ED8);min-height:100vh;display:flex;align-items:center;justify-content:center}");
        out.println(".box{background:white;padding:60px 50px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:480px;width:90%}");
        out.println(".icon{font-size:80px;margin-bottom:24px}");
        out.println("h1{color:#1E293B;margin-bottom:16px;font-size:28px;font-weight:700}");
        out.println("p{color:#64748B;font-size:15px;line-height:1.8;margin:12px 0}");
        out.println(".redirect-info{background:#EFF6FF;color:#1D4ED8;padding:12px 16px;border-radius:8px;margin:20px 0;font-size:13px;font-weight:600}");
        out.println(".spinner{border:3px solid #E2E8F0;border-top:3px solid #2563EB;border-radius:50%;width:36px;height:36px;animation:spin 1s linear infinite;margin:16px auto}");
        out.println("@keyframes spin{to{transform:rotate(360deg)}}");
        out.println(".btn{display:inline-block;padding:12px 32px;background:#2563EB;color:white;text-decoration:none;border-radius:8px;font-weight:600;font-size:15px;margin-top:16px;transition:background 0.2s}");
        out.println(".btn:hover{background:#1D4ED8}");
        out.println("</style></head><body>");
        out.println("<div class='box'>");
        out.println("  <div class='icon'>&#128075;</div>");
        out.println("  <h1>Logged Out Successfully</h1>");
        if (name != null && !name.isEmpty()) {
            out.println("  <p>Goodbye, " + prefix + esc(name) + "!</p>");
        }
        out.println("  <p>Thank you for using MediCare+.</p>");
        out.println("  <div class='redirect-info'>Redirecting to login page in 3 seconds...</div>");
        out.println("  <div class='spinner'></div>");
        out.println("  <a href='" + loginUrl + "' class='btn'>Login Again</a>");
        out.println("</div></body></html>");
    }

    // ─── SESSION HELPERS ─────────────────────────────────────────────────────

    /**
     * Check admin session. Returns the admin's name if valid; sends redirect
     * to admin/login.html and returns null if not authenticated.
     */
    public static String requireAdminSession(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("login.html");
            return null;
        }
        String name = (String) session.getAttribute("adminName");
        return name != null ? name : "Administrator";
    }

    /**
     * Check doctor session. Returns the doctor's name if valid; sends redirect
     * to doctor/login.html and returns null if not authenticated.
     */
    public static String requireDoctorSession(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("doctorId") == null) {
            response.sendRedirect("login.html");
            return null;
        }
        String name = (String) session.getAttribute("doctorName");
        return name != null ? name : "Doctor";
    }

    /**
     * Check patient session. Returns the patient's name if valid; sends redirect
     * to patient/login.html and returns null if not authenticated.
     */
    public static String requirePatientSession(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("login.html");
            return null;
        }
        String name = (String) session.getAttribute("name");
        return name != null ? name : "Patient";
    }

    // ─── JS ──────────────────────────────────────────────────────────────────

    /** Write sidebar toggle script (patient/doctor pages). Call just before &lt;/body&gt;. */
    public static void writeSidebarJS(PrintWriter out) {
        out.println("<script>");
        out.println("var isMobile=function(){return window.innerWidth<=768};");
        out.println("function toggleSidebar(){");
        out.println("  if(isMobile()){document.body.classList.toggle('sidebar-mobile-open');}");
        out.println("  else{document.body.classList.toggle('sidebar-collapsed');}");
        out.println("}");
        out.println("window.addEventListener('resize',function(){");
        out.println("  if(!isMobile()){document.body.classList.remove('sidebar-mobile-open');}");
        out.println("  else{document.body.classList.remove('sidebar-collapsed');}");
        out.println("});");
        out.println("</script>");
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    /** Escape HTML special chars to avoid XSS in names. */
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }
}
