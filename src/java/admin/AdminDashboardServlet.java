package admin;

import common.*;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String adminName = NavHelper.requireAdminSession(request, response);
        if (adminName == null) return;

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Admin Dashboard</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeAdminLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px;text-align:center}");
        out.println(".cards-container{display:flex;flex-wrap:wrap;gap:24px;justify-content:center;margin-top:24px}");
        out.println(".option-card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;width:280px;padding:32px 24px;text-align:center;transition:box-shadow 0.2s}");
        out.println(".option-card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.1)}");
        out.println(".card-icon{font-size:48px;margin-bottom:16px}");
        out.println(".card-title{font-size:18px;font-weight:700;color:#1E293B;margin-bottom:8px}");
        out.println(".card-description{font-size:13px;color:#64748B;line-height:1.6}");
        out.println(".card-link{display:block;text-decoration:none;color:inherit}");
        out.println("</style></head><body>");

        NavHelper.writeAdminNavbar(out, "Admin Dashboard");
        NavHelper.writeAdminSidebar(out, adminName, "dashboard");

        out.println("<div class='main-content'>");
        out.println("<h1 class='page-title'>Admin Dashboard</h1>");
        out.println("<div class='cards-container'>");

        printCard(out, "doctor-requests", "&#128203;",              "Doctor Requests", "Review and approve pending doctor registration requests");
        printCard(out, "doctors",         "&#128104;&#8205;&#9877;","Manage Doctors",  "View, edit, and delete doctor accounts and details");
        printCard(out, "add-doctor",      "&#128104;&#8205;&#9877;&#65039;&#8205;&#10133;","Add Doctor",     "Create and immediately approve a new doctor account");
        printCard(out, "patients",        "&#128101;",              "Manage Patients", "View and manage patient accounts and information");
        printCard(out, "add-patient",     "&#10133;&#128101;",      "Add Patient",     "Register a new patient account on their behalf");
        printCard(out, "report",          "&#128202;",              "Generate Report", "Download comprehensive analytics report with all statistics");

        out.println("</div></div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void printCard(PrintWriter out, String href, String icon, String title, String desc) {
        out.println("<a href='" + href + "' class='card-link'>");
        out.println("  <div class='option-card'>");
        out.println("    <div class='card-icon'>" + icon + "</div>");
        out.println("    <div class='card-title'>" + title + "</div>");
        out.println("    <div class='card-description'>" + desc + "</div>");
        out.println("  </div></a>");
    }
}
