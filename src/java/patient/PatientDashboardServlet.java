package patient;

import common.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/patient/dashboard")
public class PatientDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (NavHelper.requirePatientSession(request, response) == null) return;
        
        // Show dashboard
        showDashboard(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private void showDashboard(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String patientName = (String) session.getAttribute("name");
        
        response.setContentType("text/html; charset=UTF-8");
        java.io.PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Patient Dashboard</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        NavHelper.writeFontsLink(out);
        out.println("<style>");
        NavHelper.writeLayoutCSS(out);
        out.println(".page-title{font-size:24px;font-weight:700;color:#1E293B;margin-bottom:24px}");
        out.println(".cards-container{display:flex;flex-wrap:wrap;gap:20px}");
        out.println(".card{background:#FFFFFF;border-radius:12px;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #E2E8F0;width:320px;overflow:hidden;transition:box-shadow 0.2s}");
        out.println(".card:hover{box-shadow:0 4px 12px rgba(0,0,0,0.12)}");
        out.println(".card-image{width:100%;height:220px;object-fit:cover;object-position:top;background:#EFF6FF}");
        out.println(".card-content{padding:20px}");
        out.println(".doctor-name{font-size:18px;font-weight:700;color:#1E293B;margin-bottom:12px}");
        out.println(".info-row{margin:8px 0;display:flex;align-items:center;font-size:14px}");
        out.println(".info-label{font-weight:600;color:#64748B;margin-right:6px}");
        out.println(".info-value{color:#1E293B}");
        out.println(".bio{margin:12px 0;color:#64748B;font-size:13px;line-height:1.6;max-height:72px;overflow:hidden}");
        out.println(".book-btn{width:100%;padding:10px 20px;background:#1D4ED8;color:white;border:none;border-radius:8px;font-size:15px;font-weight:600;cursor:pointer;transition:all 0.2s;margin-top:12px}");
        out.println(".book-btn:hover{background:#1E40AF}");
        out.println(".no-doctors{text-align:center;color:#64748B;padding:60px 20px;font-size:16px}");
        out.println(".filter-bar{background:#FFFFFF;border:1px solid #E2E8F0;border-radius:12px;padding:20px 24px;margin-bottom:24px;box-shadow:0 1px 3px rgba(0,0,0,0.06)}");
        out.println(".filter-row{display:flex;flex-wrap:wrap;gap:14px;align-items:flex-end}");
        out.println(".filter-group{display:flex;flex-direction:column;gap:5px;min-width:140px;flex:1}");
        out.println(".filter-label{font-size:12px;font-weight:600;color:#64748B;text-transform:uppercase;letter-spacing:0.4px}");
        out.println(".filter-input{padding:9px 12px;border:1px solid #E2E8F0;border-radius:8px;font-size:14px;color:#1E293B;font-family:'Inter','Poppins',sans-serif;outline:none;transition:border-color 0.2s}");
        out.println(".filter-input:focus{border-color:#2563EB;box-shadow:0 0 0 3px rgba(37,99,235,0.1)}");
        out.println(".filter-reset{padding:9px 18px;background:#F1F5F9;color:#64748B;border:1px solid #E2E8F0;border-radius:8px;font-size:13px;font-weight:600;cursor:pointer;align-self:flex-end;white-space:nowrap;transition:all 0.2s}");
        out.println(".filter-reset:hover{background:#E2E8F0;color:#1E293B}");
        out.println("</style>");
        out.println("</head><body>");

        NavHelper.writeNavbar(out, "Find Doctors");
        NavHelper.writePatientSidebar(out, patientName, "doctors");
        
        // Main content
        out.println("<div class='main-content'>");
        out.println("<div class='filter-bar'>");
        out.println("<div class='filter-row'>");
        out.println("<div class='filter-group'>");
        out.println("<label class='filter-label'>Search by Name</label>");
        out.println("<input class='filter-input' id='f-name' type='text' placeholder='Doctor name...' oninput='filterDoctors()'>");
        out.println("</div>");
        out.println("<div class='filter-group'>");
        out.println("<label class='filter-label'>Speciality</label>");
        out.println("<input class='filter-input' id='f-spec' type='text' placeholder='e.g. Cardiologist' oninput='filterDoctors()'>");
        out.println("</div>");
        out.println("<div class='filter-group'>");
        out.println("<label class='filter-label'>Max Fee (Rs.)</label>");
        out.println("<input class='filter-input' id='f-fee' type='number' placeholder='e.g. 500' oninput='filterDoctors()'>");
        out.println("</div>");
        out.println("<div class='filter-group'>");
        out.println("<label class='filter-label'>Min Experience (yrs)</label>");
        out.println("<input class='filter-input' id='f-exp' type='number' placeholder='e.g. 5' oninput='filterDoctors()'>");
        out.println("</div>");
        out.println("<div class='filter-group'>");
        out.println("<label class='filter-label'>Location</label>");
        out.println("<input class='filter-input' id='f-loc' type='text' placeholder='City / clinic area...' oninput='filterDoctors()'>");
        out.println("</div>");
        out.println("<button class='filter-reset' onclick='resetFilters()'>Clear</button>");
        out.println("</div>");
        out.println("</div>");
        out.println("<div id='no-results' style='display:none;text-align:center;padding:40px;color:#64748B;font-size:15px;'>No doctors match your filters.</div>");
        out.println("<h1 class='page-title'>Available Doctors</h1>");
        out.println("<div class='cards-container'>");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();

            String sql = "SELECT * FROM doctor_profiles WHERE approval_status = 'approved' ORDER BY doctor_id";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            boolean hasDoctors = false;
            while (rs.next()) {
                hasDoctors = true;
                String cardName = rs.getString("full_name");
                String cardSpec = rs.getString("primary_specialty");
                double cardFee = rs.getDouble("consultation_fee");
                String cardExp = rs.getString("years_of_experience");
                if (cardExp == null) cardExp = "0";
                String cardExpNum = cardExp.replaceAll("[^0-9]", "");
                if (cardExpNum.isEmpty()) cardExpNum = "0";
                out.println("<div class='card' data-name='" + cardName.toLowerCase().replace("'","") + "' data-spec='" + (cardSpec != null ? cardSpec.toLowerCase().replace("'","") : "") + "' data-fee='" + cardFee + "' data-exp='" + cardExpNum + "' data-location=''>");
                
                // Display profile image
                byte[] imageBytes = rs.getBytes("profile_image");
                if (imageBytes != null && imageBytes.length > 0) {
                    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    String imageType = rs.getString("image_type");
                    if (imageType == null) imageType = "image/jpeg";
                    out.println("<img class='card-image' src='data:" + imageType + ";base64," + base64Image + "' alt='Doctor Photo'>");
                } else {
                    out.println("<img class='card-image' src='https://via.placeholder.com/350x250?text=Doctor' alt='Doctor'>");
                }
                
                out.println("<div class='card-content'>");
                out.println("<div class='doctor-name'>" + NavHelper.esc(rs.getString("full_name")) + "</div>");
                
                out.println("<div class='info-row'>");
                out.println("<span class='info-label'>Specialty:</span>");
                out.println("<span class='info-value'>" + NavHelper.esc(rs.getString("primary_specialty")) + "</span>");
                out.println("</div>");
                
                out.println("<div class='info-row'>");
                out.println("<span class='info-label'>Fee: Rs.</span>");
                out.println("<span class='info-value'>" + rs.getDouble("consultation_fee") + "</span>");
                out.println("</div>");
                
                out.println("<div class='info-row'>");
                out.println("<span class='info-label'>Time:</span>");
                out.println("<span class='info-value'>" + NavHelper.esc(rs.getString("clinic_visit_schedule")) + "</span>");
                out.println("</div>");
                
                out.println("<div class='info-row'>");
                out.println("<span class='info-label'>Experience:</span>");
                out.println("<span class='info-value'>" + NavHelper.esc(rs.getString("years_of_experience")) + " years</span>");
                out.println("</div>");
                
                String bio = rs.getString("professional_bio");
                if (bio != null && !bio.isEmpty()) {
                    out.println("<div class='bio'>" + NavHelper.esc(bio) + "</div>");
                }
                
                out.println("<form action='book-appointment' method='GET'>");
                out.println("<input type='hidden' name='doctorId' value='" + rs.getInt("doctor_id") + "'>");
                out.println("<button type='submit' class='book-btn'>Book Appointment</button>");
                out.println("</form>");
                
                out.println("</div>");
                out.println("</div>");
            }
            
            if (!hasDoctors) {
                out.println("<div class='no-doctors'>");
                out.println("<h2>No Doctors Available</h2>");
                out.println("<p>Please check back later.</p>");
                out.println("</div>");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div style='color: #1E293B; text-align: center; padding: 20px;'>");
            out.println("<h2>Error Loading Doctors</h2>");
            out.println("<p>Error: " + e.getMessage() + "</p>");
            out.println("</div>");
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        out.println("</div>");
        out.println("</div>");
        NavHelper.writeSidebarJS(out);
        out.println("<script>");
        out.println("function filterDoctors(){");
        out.println("  var name=(document.getElementById('f-name').value||'').toLowerCase().trim();");
        out.println("  var spec=(document.getElementById('f-spec').value||'').toLowerCase().trim();");
        out.println("  var maxFee=parseFloat(document.getElementById('f-fee').value)||Infinity;");
        out.println("  var minExp=parseInt(document.getElementById('f-exp').value)||0;");
        out.println("  var loc=(document.getElementById('f-loc').value||'').toLowerCase().trim();");
        out.println("  var cards=document.querySelectorAll('.card');");
        out.println("  var visible=0;");
        out.println("  cards.forEach(function(c){");
        out.println("    var n=c.dataset.name||'';");
        out.println("    var s=c.dataset.spec||'';");
        out.println("    var f=parseFloat(c.dataset.fee)||0;");
        out.println("    var e=parseInt(c.dataset.exp)||0;");
        out.println("    var l=(c.dataset.location||'')+(c.textContent||'');");
        out.println("    var show=(!name||n.includes(name))&&(!spec||s.includes(spec))&&(f<=maxFee)&&(e>=minExp)&&(!loc||l.toLowerCase().includes(loc));");
        out.println("    c.style.display=show?'':'none';");
        out.println("    if(show)visible++;");
        out.println("  });");
        out.println("  document.getElementById('no-results').style.display=visible===0?'block':'none';");
        out.println("}");
        out.println("function resetFilters(){");
        out.println("  ['f-name','f-spec','f-fee','f-exp','f-loc'].forEach(function(id){document.getElementById(id).value='';});");
        out.println("  filterDoctors();");
        out.println("}");
        out.println("</script>");
        out.println("</body></html>");
    }
}
