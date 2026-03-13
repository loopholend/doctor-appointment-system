package admin;

import common.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String password = request.getParameter("password");

        // Load admin password from db.properties (not hardcoded in source)
        String adminPassword = DBConnection.getAdminPassword();

        if (password != null && java.security.MessageDigest.isEqual(
                adminPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                password.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            // Successful login - CREATE SESSION (invalidate old session first to prevent session fixation)
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute("adminId", 1);
            session.setAttribute("adminName", "Administrator");
            session.setAttribute("isAdmin", true);

            // REDIRECT TO ADMIN DASHBOARD
            response.sendRedirect("dashboard");
        } else {
            // Invalid password
            response.setContentType("text/html; charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();

            out.println("<!DOCTYPE html><html><head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<title>Access Denied</title>");
            out.println("<style>");
            out.println("body{font-family:Arial;background:linear-gradient(135deg, #0077b6, #0096c7);min-height:100vh;display:flex;align-items:center;justify-content:center;margin:0}");
            out.println(".error-box{background:white;padding:50px 40px;border-radius:20px;box-shadow:0 20px 60px rgba(0,0,0,0.3);text-align:center;max-width:500px}");
            out.println(".error-icon{font-size:80px;margin-bottom:20px;color:#f44336}");
            out.println("h2{color:#c62828;margin-bottom:15px;font-size:28px}");
            out.println("p{color:#666;margin-bottom:30px;font-size:16px}");
            out.println(".btn{display:inline-block;padding:14px 35px;background:linear-gradient(135deg, #0077b6, #0096c7);color:white;text-decoration:none;border-radius:10px;font-weight:bold;transition:0.3s;font-size:16px}");
            out.println(".btn:hover{transform:translateY(-2px);box-shadow:0 10px 30px rgba(0,119,182,0.4)}");
            out.println("</style></head><body>");
            out.println("<div class='error-box'>");
            out.println("<div class='error-icon'>🔒</div>");
            out.println("<h2>Access Denied</h2>");
            out.println("<p>Invalid admin password. Please try again.</p>");
            out.println("<a href='login.html' class='btn'>Back to Login</a>");
            out.println("</div></body></html>");
        }
    }
}
