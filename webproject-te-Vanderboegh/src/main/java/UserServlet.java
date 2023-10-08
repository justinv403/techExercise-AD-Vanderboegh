

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.*;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String url = "jdbc:mysql://ec2-18-220-248-248.us-east-2.compute.amazonaws.com:3306/techExerciseDB";
	static String user = "justinv";
	static String password = "!Msql403!";
	private Connection conn = null;

    public void init() throws ServletException {
    	try {
            Class.forName("com.mysql.cj.jdbc.Driver"); //old:com.mysql.jdbc.Driver
            conn = DriverManager.getConnection(url, user, password);
         } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
         } catch (SQLException e) {
        	 throw new ServletException("Unable to connect to database.", e);
         }
    }
    
    // simple script to treat any doGet request as a doPost request
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get information from HTML page
    	String action = request.getParameter("action");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        
        // this prevents null errors
        String employeeIDStr = request.getParameter("employeeID");
        int employeeID = 0;
        if(employeeIDStr != null && !employeeIDStr.isEmpty()) {
        	employeeID = Integer.parseInt(request.getParameter("employeeID"));
        }
        
        // continue getting info from HTML page
        String phoneNumber = request.getParameter("phoneNumber");
        String email = request.getParameter("email");
        email = email.toLowerCase();
        
        // get prepared statement and result set
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        // try to perform the requested action
        try {
        	
        	// tries to add a user
        	if ("add".equals(action)) {
                if(firstName.length() > 0 && lastName.length() > 0 && phoneNumber.length() > 0 && email.length() > 0) {
        		
                	stmt = conn.prepareStatement("INSERT INTO Users VALUES (?, ?, ?, ?, ?)");
                	stmt.setInt(1, employeeID);
                	stmt.setString(2, firstName);
                	stmt.setString(3, lastName);
                	stmt.setString(4, phoneNumber);
                	stmt.setString(5, email);
                	stmt.executeUpdate();
                
                	// Redirect back to User.html with a success message
                	response.sendRedirect("User.html?success=true");
                } else {
                	
                	// Redirect back to User.html with a failure message
                	response.sendRedirect("User.html?insufficientlen=true");
                }

            
            // tries to remove a user
        	} else if ("remove".equals(action)) {
                // only remove user if ID provided
        		if(employeeIDStr.length() <= 0) {
                	// return to page with error message passed as a parameter
        			response.sendRedirect("User.html?mpID=true");
                } else {
                	// delete user from ID
                	stmt = conn.prepareStatement("DELETE FROM Users WHERE EmployeeID = ?");
                	stmt.setInt(1, employeeID);
                	stmt.executeUpdate();
                
                	// Redirect back to User.html with a success message
                	response.sendRedirect("User.html?success=true");
                
                }

            
            // tries to search the user database
        	} else if ("search".equals(action)) {
        		// preparation for table creation
        		if(employeeIDStr.length() <= 0) {
        			stmt = conn.prepareStatement("SELECT * FROM Users");
        		} else {
        			stmt = conn.prepareStatement("SELECT * FROM Users WHERE EmployeeID = ?");
                    stmt.setInt(1, employeeID);
        		}
        		rs = stmt.executeQuery();
                
                // create table
                PrintWriter out = response.getWriter();
                out.println("<html><body><table>");
                out.println("<tr><th>Employee ID</th><th>First Name</th><th>LastName</th><th>Phone Number</th><th>Email</th></tr>");
                while (rs.next()) {
                    int empId = rs.getInt(1);
                    String fName = rs.getString(2);
                    String lName = rs.getString(3);
                    String phoneNum = rs.getString(4);
                    String eMail = rs.getString(5);
                    
                    out.println("<tr><td>" + empId + "</td><td>" + fName + "</td><td>" + lName + "</td><td>" + phoneNum + "</td><td>" + eMail + "</td></tr>");
                }
                out.println("</table><br>");
                out.println("<button onclick=\"location.href='User.html'\">Menu</button>");
                out.println("</body></html>");
            }

        // catch any errors
        } catch (SQLException e) {
            throw new ServletException(e);
        } finally {
        	if (rs != null) {
        		try {
        			rs.close();
        		} catch(SQLException e) {
        			// Ignore
        		}
        	}
        	if (stmt != null) {
        		try {
        			stmt.close();
        		} catch(SQLException e) {
        			// Ignore
        		}
        	}
        }
    }

    public void destroy() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
