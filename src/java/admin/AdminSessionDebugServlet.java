package admin;

import common.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Session debug tool — restricted to admin sessions only.
 * Remove or disable this servlet before deploying to production.
 */
@WebServlet("/admin/session-debug")
public class AdminSessionDebugServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Restrict access to admin sessions only
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Session Debug Info (Admin Only)</h2>");
        response.getWriter().println("<p style='color:green'>Session exists!</p>");
        response.getWriter().println("<p>Session ID: " + session.getId() + "</p>");
        response.getWriter().println("<hr>");
        response.getWriter().println("<p><strong>adminId:</strong> " + session.getAttribute("adminId") + "</p>");
        response.getWriter().println("<p><strong>doctorId:</strong> " + session.getAttribute("doctorId") + "</p>");
        response.getWriter().println("<p><strong>doctorUsername:</strong> " + session.getAttribute("doctorUsername") + "</p>");
        response.getWriter().println("<p><strong>username (patient):</strong> " + session.getAttribute("username") + "</p>");
        response.getWriter().println("</body></html>");
    }
}
