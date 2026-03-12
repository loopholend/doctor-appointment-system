import common.NavHelper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.*;

/**
 * Unified logout handler for all roles (admin / doctor / patient).
 * Replaces the three separate adminlogout, doctorlogout, patientlogout servlets.
 */
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path     = request.getServletPath();   // e.g. "/adminlogout"
        String name     = null;
        String prefix   = "";
        String loginUrl;

        HttpSession session = request.getSession(false);
        if (session != null) {
            if (path.contains("admin")) {
                name     = (String) session.getAttribute("adminName");
                loginUrl = "login.html";
            } else if (path.contains("doctor")) {
                name     = (String) session.getAttribute("doctorName");
                prefix   = "Dr. ";
                loginUrl = "login.html";
            } else {
                name     = (String) session.getAttribute("name");
                loginUrl = "login.html";
            }
            session.invalidate();
        } else {
            // No session — redirect to the role-specific login page
            response.sendRedirect("login.html");
            return;
        }

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        NavHelper.writeLogoutPage(out, name, prefix, loginUrl);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
