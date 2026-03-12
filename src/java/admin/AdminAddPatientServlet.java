package admin;

import common.*;
import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/admin/add-patient")
public class AdminAddPatientServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String adminName = NavHelper.requireAdminSession(request, response);
        if (adminName == null) return;
        response.setContentType("text/html; charset=UTF-8");
        renderForm(response.getWriter(), adminName, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String adminName = NavHelper.requireAdminSession(request, response);
        if (adminName == null) return;

        String username      = request.getParameter("username");
        String fullName      = request.getParameter("fullName");
        String contactNumber = request.getParameter("contactNumber");
        String email         = request.getParameter("email");
        String password      = request.getParameter("password");

        if (isBlank(username) || isBlank(fullName) || isBlank(contactNumber) ||
            isBlank(email)    || isBlank(password)) {
            response.setContentType("text/html; charset=UTF-8");
            renderForm(response.getWriter(), adminName, "All fields are required.");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(
                "INSERT INTO patients (username, full_name, contact_number, email_address, password_hash) VALUES (?,?,?,?,?)");
            pstmt.setString(1, username.trim());
            pstmt.setString(2, fullName.trim());
            pstmt.setString(3, contactNumber.trim());
            pstmt.setString(4, email.trim());
            pstmt.setString(5, PasswordUtil.hashPassword(password));
            pstmt.executeUpdate();
            response.sendRedirect("patients?added=1");

        } catch (SQLIntegrityConstraintViolationException e) {
            response.setContentType("text/html; charset=UTF-8");
            renderForm(response.getWriter(), adminName, "Username or email already exists.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
            renderForm(response.getWriter(), adminName, "An error occurred: " + NavHelper.esc(e.getMessage()));
        } finally {
            if (pstmt != null) try { pstmt.close(); } catch (Exception ex) {}
            if (conn != null) try { conn.close(); } catch (Exception ex) {}
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private void renderForm(PrintWriter out, String adminName, String errorMsg) {
        out.println("<!DOCTYPE html><html lang='en'><head>");
        out.println("<meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Add Patient — MediCare+</title>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeAdminLayoutCSS(out);
        out.println(".form-card{background:#FFFFFF;padding:32px 40px;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;max-width:480px;margin:0 auto}");
        out.println("h1{text-align:center;color:#1E293B;margin-bottom:4px;font-size:22px;font-weight:700}");
        out.println(".subtitle{text-align:center;color:#64748B;font-size:13px;margin-bottom:24px}");
        out.println("label{display:block;margin-top:14px;font-weight:600;color:#1E293B;font-size:13px}");
        out.println(".req{color:#EF4444;margin-left:2px}");
        out.println("input{width:100%;padding:10px 12px;margin-top:4px;border-radius:8px;border:1px solid #E2E8F0;font-size:14px;outline:none;transition:all 0.2s;box-sizing:border-box;color:#1E293B;background:#FFFFFF;font-family:'Inter','Poppins',sans-serif}");
        out.println("input:focus{border-color:#2563EB;box-shadow:0 0 0 3px rgba(37,99,235,0.1)}");
        out.println(".error-box{background:#FEF2F2;border:1px solid #FECACA;color:#B91C1C;padding:12px 16px;border-radius:8px;margin-bottom:16px;font-size:13px;font-weight:500}");
        out.println(".btn-submit{display:block;width:100%;margin-top:28px;background:#1D4ED8;color:white;border:none;padding:13px;font-size:15px;font-weight:600;border-radius:8px;cursor:pointer;transition:all 0.2s;font-family:'Inter','Poppins',sans-serif}");
        out.println(".btn-submit:hover{background:#1E40AF}");
        out.println("</style></head><body>");

        NavHelper.writeAdminNavbar(out, "Add Patient");
        NavHelper.writeAdminSidebar(out, adminName, "add-patient");

        out.println("<div class='main-content'><div class='form-card'>");
        out.println("<h1>&#128101; Add New Patient</h1>");
        out.println("<p class='subtitle'>Create a patient account on their behalf</p>");

        if (errorMsg != null) out.println("<div class='error-box'>&#9888;&#65039; " + errorMsg + "</div>");

        out.println("<form action='add-patient' method='post'>");
        out.println("<label>Username <span class='req'>*</span></label>");
        out.println("<input type='text' name='username' placeholder='e.g. john_doe' required>");
        out.println("<label>Full Name <span class='req'>*</span></label>");
        out.println("<input type='text' name='fullName' placeholder='John Doe' required>");
        out.println("<label>Contact Number <span class='req'>*</span></label>");
        out.println("<input type='text' name='contactNumber' placeholder='e.g. 9876543210' required>");
        out.println("<label>Email Address <span class='req'>*</span></label>");
        out.println("<input type='email' name='email' placeholder='patient@email.com' required>");
        out.println("<label>Password <span class='req'>*</span></label>");
        out.println("<input type='password' name='password' id='pwd' placeholder='Set a password' required>");
        out.println("<label>Confirm Password <span class='req'>*</span></label>");
        out.println("<input type='password' name='confirmPassword' id='cpwd' placeholder='Repeat password' required>");
        out.println("<button type='submit' class='btn-submit'>&#10003; Add Patient</button>");
        out.println("</form></div></div>");

        out.println("<script>");
        out.println("document.querySelector('form').addEventListener('submit',function(e){");
        out.println("  if(document.getElementById('pwd').value!==document.getElementById('cpwd').value){");
        out.println("    e.preventDefault();alert('Passwords do not match!');");
        out.println("  }");
        out.println("});");
        out.println("</script></body></html>");
    }
}
