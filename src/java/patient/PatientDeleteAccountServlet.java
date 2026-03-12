package patient;

import common.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/patient/delete-account")
public class PatientDeleteAccountServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String patientName = NavHelper.requirePatientSession(request, response);
        if (patientName == null) return;

        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>MediCare+ &#8212; Delete Account</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".main-content{display:flex;align-items:flex-start;justify-content:center}");
        out.println(".card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1),0 1px 2px rgba(0,0,0,0.06);padding:40px 36px;width:100%;max-width:460px;text-align:center}");
        out.println(".danger-icon{width:68px;height:68px;background:#FEF2F2;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:30px;margin:0 auto 18px}");
        out.println(".card-title{font-family:'Poppins',sans-serif;font-size:22px;font-weight:700;color:#DC2626;margin-bottom:12px}");
        out.println(".warning-box{background:#FEF2F2;border:1px solid #FECACA;border-radius:8px;padding:14px 18px;margin-bottom:28px;text-align:left}");
        out.println(".warning-box p{font-size:14px;color:#7F1D1D;line-height:1.6}");
        out.println(".warning-box ul{margin-top:8px;padding-left:18px}");
        out.println(".warning-box ul li{font-size:13px;color:#991B1B;margin-bottom:4px}");
        out.println(".btn-danger{background:#DC2626;color:#fff;border:none;border-radius:8px;padding:13px 24px;font-size:15px;font-weight:600;cursor:pointer;width:100%;font-family:inherit;transition:background 0.2s ease}");
        out.println(".btn-danger:hover{background:#B91C1C}");
        out.println("</style></head><body>");

        NavHelper.writeNavbar(out, "Delete Account");
        NavHelper.writePatientSidebar(out, patientName, "delete");

        out.println("<div class='main-content'>");
        out.println("  <div class='card'>");
        out.println("    <div class='danger-icon'>&#9888;&#65039;</div>");
        out.println("    <h1 class='card-title'>Delete Account</h1>");
        out.println("    <div class='warning-box'>");
        out.println("      <p><strong>This action is permanent and cannot be undone.</strong></p>");
        out.println("      <ul>");
        out.println("        <li>All your personal data will be deleted</li>");
        out.println("        <li>Your appointment history will be removed</li>");
        out.println("        <li>Your medical records will be permanently erased</li>");
        out.println("      </ul>");
        out.println("    </div>");
        out.println("    <form action='delete-account' method='post' onsubmit=\"return confirm('Are you sure you want to delete your account? This action cannot be undone.');\">");
        out.println("      <button type='submit' class='btn-danger'>Yes, Delete My Account</button>");
        out.println("    </form>");
        out.println("  </div>");
        out.println("</div>");

        NavHelper.writeSidebarJS(out);
        out.println("</body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (NavHelper.requirePatientSession(request, response) == null) return;

        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("username");

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();

            // Delete patient account (this will cascade delete patient_medical_records and appointments)
            String sql = "DELETE FROM patients WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);

            int rowsDeleted = pstmt.executeUpdate();

            if (rowsDeleted > 0) {
                // Account deleted successfully - invalidate session
                session.invalidate();

                // Show success page
                out.println("<!DOCTYPE html><html><head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<title>Account Deleted</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                out.println(".success-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
                out.println(".success-icon{font-size:80px;margin-bottom:20px;color:#4caf50}");
                out.println("h2{color:#2c3e50;margin-bottom:15px;font-size:28px}");
                out.println("p{color:#666;margin-bottom:30px;font-size:16px;line-height:1.6}");
                out.println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
                out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
                out.println("</style></head><body>");
                
                out.println("<div class='success-box'>");
                out.println("<div class='success-icon'>✓</div>");
                out.println("<h2>Account Deleted Successfully</h2>");
                out.println("<p>Your account and all associated data have been permanently removed from our system.</p>");
                out.println("<p>We're sorry to see you go. Thank you for using our service.</p>");
                out.println("<a href='register.html' class='btn'>Create New Account</a>");
                out.println("</div>");
                
                out.println("</body></html>");
            } else {
                // Account not found
                out.println("<!DOCTYPE html><html><head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<title>Error</title>");
                out.println("<style>");
                out.println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
                out.println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
                out.println(".error-icon{font-size:80px;margin-bottom:20px;color:#f44336}");
                out.println("h2{color:#c62828;margin-bottom:15px}");
                out.println("p{color:#666;margin-bottom:30px}");
                out.println(".btn{display:inline-block;padding:12px 30px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:8px;font-weight:bold;transition:0.3s}");
                out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
                out.println("</style></head><body>");
                
                out.println("<div class='error-box'>");
                out.println("<div class='error-icon'>✗</div>");
                out.println("<h2>Account Not Found</h2>");
                out.println("<p>Unable to delete account. Please try again.</p>");
                out.println("<a href='dashboard' class='btn'>Back to Dashboard</a>");
                out.println("</div>");
                
                out.println("</body></html>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            
            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Error</title>");
            out.println("<style>");
            out.println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            out.println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
            out.println("h2{color:#c62828;margin-bottom:15px}");
            out.println("p{color:#666;margin-bottom:30px}");
            out.println(".btn{display:inline-block;padding:12px 30px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:8px;font-weight:bold;transition:0.3s}");
            out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
            out.println("</style></head><body>");
            
            out.println("<div class='error-box'>");
            out.println("<h2>Database Error</h2>");
            out.println("<p>Error: " + e.getMessage() + "</p>");
            out.println("<a href='dashboard' class='btn'>Back to Dashboard</a>");
            out.println("</div>");
            
            out.println("</body></html>");
            
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
