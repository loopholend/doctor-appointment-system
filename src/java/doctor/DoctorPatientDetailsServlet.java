package doctor;

import common.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/doctor/patient-details")
public class DoctorPatientDetailsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Add this method to handle GET requests
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Redirect to doctor dashboard if accessed directly via GET
        response.sendRedirect("dashboard");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientUsername = request.getParameter("patientUsername");
        
        if (patientUsername == null || patientUsername.trim().isEmpty()) {
            response.getWriter().println("Patient username is required");
            return;
        }

        HttpSession session = request.getSession(false);
        Integer doctorId = (session != null) ? (Integer) session.getAttribute("doctorId") : null;
        String doctorName = (session != null) ? (String) session.getAttribute("doctorName") : "Doctor";

        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Patient Medical Details</title>");
        out.println("<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Poppins:wght@400;500;600&display=swap' rel='stylesheet'>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
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
        out.println(".container{max-width:880px;margin:0 auto;background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;overflow:hidden}");
        out.println(".header{background:#2563EB;color:white;padding:28px;text-align:center}");
        out.println(".header h1{font-size:26px;margin-bottom:8px;font-weight:700}");
        out.println(".header p{font-size:14px;opacity:0.85}");
        out.println(".content{padding:32px}");
        out.println(".section{margin-bottom:28px}");
        out.println(".section-title{font-size:18px;color:#1E293B;margin-bottom:16px;padding-bottom:10px;border-bottom:2px solid #E2E8F0;font-weight:700}");
        out.println(".info-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:16px;margin-bottom:16px}");
        out.println(".info-item{background:#F8FAFC;padding:16px;border-radius:8px;border-left:3px solid #2563EB;border:1px solid #E2E8F0}");
        out.println(".info-label{font-size:11px;color:#64748B;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px;font-weight:600}");
        out.println(".info-value{font-size:16px;color:#1E293B;font-weight:600}");
        out.println(".badge{display:inline-block;padding:4px 12px;border-radius:20px;font-size:13px;font-weight:600;text-transform:uppercase}");
        out.println(".badge-yes{background:#DCFCE7;color:#166534}");
        out.println(".badge-no{background:#FEE2E2;color:#991B1B}");
        out.println(".badge-none{background:#F1F5F9;color:#64748B}");
        out.println(".emergency{background:#FEF3C7;border-left:4px solid #D97706;padding:16px;border-radius:8px;margin-top:16px;border:1px solid #FDE68A}");
        out.println(".emergency-title{color:#92400E;font-size:16px;font-weight:700;margin-bottom:8px}");
        out.println(".buttons{display:flex;gap:12px;margin-top:24px;justify-content:center;flex-wrap:wrap}");
        out.println(".btn{padding:10px 24px;border:none;border-radius:8px;font-size:14px;font-weight:600;cursor:pointer;text-decoration:none;display:inline-block;transition:all 0.2s}");
        out.println(".btn-primary{background:#1D4ED8;color:white}");
        out.println(".btn-primary:hover{background:#1E40AF}");
        out.println(".btn-secondary{background:#64748B;color:white}");
        out.println(".btn-secondary:hover{background:#475569}");
        out.println(".no-data{text-align:center;padding:60px 20px;color:#64748B}");
        out.println(".no-data h2{font-size:22px;margin-bottom:10px;color:#1E293B;font-weight:700}");
        out.println("@media print{body{background:white;padding:0}.buttons{display:none}.navbar{display:none}.sidebar{display:none}.main-content{margin-left:0;padding-top:0}}");
        out.println("</style></head><body>");

        out.println("<div class='navbar'><button class='hamburger-btn' onclick='toggleSidebar()' aria-label='Toggle menu'><span></span><span></span><span></span></button><span class='navbar-brand'>MediCare+</span><span class='navbar-title'>Patient Details</span></div>");
        out.println("<div class='sidebar'>");
        out.println("<div class='welcome'>Welcome, Dr. " + (doctorName != null ? doctorName : "Doctor") + "</div>");
        out.println("<a href='dashboard' class='menu-item'>👥 View Patients</a>");
        out.println("<a href='profile-edit' class='menu-item'>✏️ Update Info</a>");
        out.println("<a href='day-off' class='menu-item'>📅 Manage Day Off</a>");
        out.println("<a href='delete-account' class='menu-item'>🗑️ Delete Account</a>");
        out.println("<a href='logout' class='logout-btn'>🚪 Logout</a>");
        out.println("</div>");
        out.println("<div class='main-content'>");

        Connection conn = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            conn = DBConnection.getConnection();

            // Get patient basic info
            String patientSql = "SELECT full_name, contact_number, email_address FROM patients WHERE username = ?";
            pstmt1 = conn.prepareStatement(patientSql);
            pstmt1.setString(1, patientUsername);
            rs1 = pstmt1.executeQuery();

            String patientName = "Unknown";
            String patientContact = "N/A";
            String patientEmail = "N/A";

            if (rs1.next()) {
                patientName = rs1.getString("full_name");
                patientContact = rs1.getString("contact_number");
                patientEmail = rs1.getString("email_address");
            }

            // Get medical details
            String medicalSql = "SELECT * FROM patient_medical_records WHERE patient_account_username = ? ORDER BY created_at DESC LIMIT 1";
            pstmt2 = conn.prepareStatement(medicalSql);
            pstmt2.setString(1, patientUsername);
            rs2 = pstmt2.executeQuery();

            // Header
            out.println("<div class='container'>");
            out.println("<div class='header'>");
            out.println("<h1>Patient Medical Record</h1>");
            out.println("<p>Complete medical history and details</p>");
            out.println("</div>");

            if (rs2.next()) {
                // Content
                out.println("<div class='content'>");
                
                // Patient Information Section
                out.println("<div class='section'>");
                out.println("<div class='section-title'>Patient Information</div>");
                out.println("<div class='info-grid'>");
                
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Full Name</div>");
                out.println("<div class='info-value'>" + patientName + "</div>");
                out.println("</div>");
                
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Username</div>");
                out.println("<div class='info-value'>" + patientUsername + "</div>");
                out.println("</div>");
                
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Contact Number</div>");
                out.println("<div class='info-value'>" + patientContact + "</div>");
                out.println("</div>");
                
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Email</div>");
                out.println("<div class='info-value'>" + patientEmail + "</div>");
                out.println("</div>");
                
                out.println("</div>");
                out.println("</div>");

                // Basic Medical Info Section
                out.println("<div class='section'>");
                out.println("<div class='section-title'>Basic Medical Information</div>");
                out.println("<div class='info-grid'>");
                
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Blood Group</div>");
                out.println("<div class='info-value' style='color:#c62828'>" + rs2.getString("blood_type") + "</div>");
                out.println("</div>");
                
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Age</div>");
                out.println("<div class='info-value'>" + rs2.getInt("age") + " years</div>");
                out.println("</div>");
                
                out.println("</div>");
                out.println("</div>");

                // Chronic Conditions Section
                out.println("<div class='section'>");
                out.println("<div class='section-title'>Chronic Conditions</div>");
                out.println("<div class='info-grid'>");
                
                String diabetes = rs2.getString("diabetes_status");
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Diabetes</div>");
                out.println("<div class='info-value'>");
                if ("none".equalsIgnoreCase(diabetes)) {
                    out.println("<span class='badge badge-none'>None</span>");
                } else {
                    out.println("<span class='badge badge-yes'>" + diabetes + "</span>");
                }
                out.println("</div>");
                out.println("</div>");
                
                String thyroid = rs2.getString("thyroid_status");
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Thyroid</div>");
                out.println("<div class='info-value'>");
                if ("none".equalsIgnoreCase(thyroid)) {
                    out.println("<span class='badge badge-none'>None</span>");
                } else {
                    out.println("<span class='badge badge-yes'>" + thyroid + "</span>");
                }
                out.println("</div>");
                out.println("</div>");
                
                String bp = rs2.getString("blood_pressure_status");
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Blood Pressure</div>");
                out.println("<div class='info-value'>");
                if ("none".equalsIgnoreCase(bp)) {
                    out.println("<span class='badge badge-none'>None</span>");
                } else {
                    out.println("<span class='badge badge-yes'>" + bp + "</span>");
                }
                out.println("</div>");
                out.println("</div>");
                
                out.println("</div>");
                out.println("</div>");

                // Health Conditions Section
                out.println("<div class='section'>");
                out.println("<div class='section-title'>Other Health Conditions</div>");
                out.println("<div class='info-grid'>");
                
                String asthma = rs2.getString("has_asthma");
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Asthma</div>");
                out.println("<div class='info-value'>");
                if ("yes".equalsIgnoreCase(asthma)) {
                    out.println("<span class='badge badge-yes'>Yes</span>");
                } else {
                    out.println("<span class='badge badge-no'>No</span>");
                }
                out.println("</div>");
                out.println("</div>");
                
                String allergies = rs2.getString("has_allergies");
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Allergies</div>");
                out.println("<div class='info-value'>");
                if ("yes".equalsIgnoreCase(allergies)) {
                    out.println("<span class='badge badge-yes'>Yes</span>");
                } else {
                    out.println("<span class='badge badge-no'>No</span>");
                }
                out.println("</div>");
                out.println("</div>");
                
                String surgeries = rs2.getString("has_previous_surgeries");
                out.println("<div class='info-item'>");
                out.println("<div class='info-label'>Previous Surgeries</div>");
                out.println("<div class='info-value'>");
                if ("yes".equalsIgnoreCase(surgeries)) {
                    out.println("<span class='badge badge-yes'>Yes</span>");
                } else {
                    out.println("<span class='badge badge-no'>No</span>");
                }
                out.println("</div>");
                out.println("</div>");
                
                out.println("</div>");
                out.println("</div>");

                // Emergency Alert if needed
                if ("yes".equalsIgnoreCase(asthma) || "yes".equalsIgnoreCase(allergies) || 
                    !"none".equalsIgnoreCase(diabetes) || !"none".equalsIgnoreCase(bp)) {
                    out.println("<div class='emergency'>");
                    out.println("<div class='emergency-title'>Important Medical Alert</div>");
                    out.println("<p style='color:#e65100'>This patient has pre-existing conditions that require special attention during treatment.</p>");
                    out.println("</div>");
                }

                // Timestamps
                out.println("<div style='margin-top:30px;padding:20px;background:#ecf0f1;border-radius:12px'>");
                out.println("<div style='font-size:13px;color:#7f8c8d'>");
                out.println("<strong>Record Created:</strong> " + rs2.getTimestamp("created_at"));
                out.println("<br><strong>Last Updated:</strong> " + rs2.getTimestamp("updated_at"));
                out.println("</div>");
                out.println("</div>");

                // Buttons - Print only (navigation via sidebar)
                out.println("<div class='buttons'>");
                out.println("<button onclick='window.print()' class='btn btn-secondary'>Print</button>");
                out.println("</div>");

                out.println("</div>"); // content

            } else {
                // No medical data found
                out.println("<div class='content'>");
                out.println("<div class='no-data'>");
                out.println("<h2>No Medical Records Found</h2>");
                out.println("<p>This patient has not filled out their medical details form yet.</p>");
                out.println("</div>");
                out.println("</div>");
            }

            out.println("</div>"); // container

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div style='text-align:center;padding:50px;color:#1E293B'>");
            out.println("<h2>Error Loading Patient Details</h2>");
            out.println("<p>Error: " + e.getMessage() + "</p>");
            out.println("</div>");
        } finally {
            try {
                if (rs1 != null) rs1.close();
                if (rs2 != null) rs2.close();
                if (pstmt1 != null) pstmt1.close();
                if (pstmt2 != null) pstmt2.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        out.println("</div>"); // main-content
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
    }
}
