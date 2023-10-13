

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
        // try to convert to int
        try {
        	if(employeeIDStr != null && !employeeIDStr.isEmpty()) {
            	employeeID = Integer.parseInt(request.getParameter("employeeID"));
            }
        } catch(NumberFormatException e) {
        	// it will be ignored for now - catch later
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
                try {
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
                } catch(NumberFormatException e) {
                	response.sendRedirect("User.html?typeFailure=true");
                }
            
            // tries to remove a user
        	} else if ("remove".equals(action)) {
                // only remove user if ID provided
        		if(employeeIDStr.length() <= 0) {
                	// return to page with error message passed as a parameter
        			response.sendRedirect("User.html?mpID=true");
                } else {
                	// catch integer error for user id
                	try {
                		// parse the integer -- again
                		employeeID = Integer.parseInt(employeeIDStr);
                		
                		// delete user from ID
                    	stmt = conn.prepareStatement("DELETE FROM Users WHERE EmployeeID = ?");
                    	stmt.setInt(1, employeeID);
                    	stmt.executeUpdate();
                    
                    	// Redirect back to User.html with a success message
                    	response.sendRedirect("User.html?success=true");
                	} catch(NumberFormatException e) {
                		response.sendRedirect("User.html?typeFailure=true");
                	}
                	
                
                }

            
            // tries to search the user database
        	} else if ("search".equals(action)) {
        		// preparation for table creation
        		boolean searchSuccess = false;
        		boolean typeFailure = false;
        		
        		
        		// search database
        		// check if any argument provided
        		if(employeeIDStr.length() <= 0 && firstName.length() <= 0 && lastName.length() <= 0
        				&& phoneNumber.length() <= 0 && email.length() <= 0) {
        			stmt = conn.prepareStatement("SELECT * FROM Users");
        			rs = stmt.executeQuery();
        			searchSuccess = true;
        		
        		// search by ID
        		} else if( employeeIDStr.length() > 0 && firstName.length() <= 0 && lastName.length() <= 0
        		        && phoneNumber.length() <= 0 && email.length() <= 0) {
        		    // catch error if user inputs string instead of integer
        			try {
        		        employeeID = Integer.parseInt(employeeIDStr);
        		        stmt = conn.prepareStatement("SELECT * FROM Users WHERE EmployeeID = ?");
        		        stmt.setInt(1, employeeID);
        		        rs = stmt.executeQuery();
        		        searchSuccess = true;
        		    } catch (NumberFormatException e) {
        		        searchSuccess = false;
        		        typeFailure = true;
        		    }
        		// search by first name
        		} else if (firstName.length() > 0 && employeeIDStr.length() <= 0 && lastName.length() <= 0
        				&& phoneNumber.length() <= 0 && email.length() <= 0) {
        			stmt = conn.prepareStatement("SELECT * FROM Users WHERE LOWER(FirstName) LIKE ?");
                    stmt.setString(1, "%" + firstName + "%");
                    rs = stmt.executeQuery();
                    searchSuccess = true;
        		
        		// search by last name
        		} else if (lastName.length() > 0 && employeeIDStr.length() <= 0 && firstName.length() <= 0 
        				&& phoneNumber.length() <= 0 && email.length() <= 0) {
        			stmt = conn.prepareStatement("SELECT * FROM Users WHERE LOWER(LastName) LIKE ?");
                    stmt.setString(1, "%" + lastName + "%");
                    rs = stmt.executeQuery();
                    searchSuccess = true;
        		
        		// search by phone number
    			} else if (phoneNumber.length() > 0 && employeeIDStr.length() <= 0 && firstName.length() <= 0 && lastName.length() <= 0
        				&& email.length() <= 0) {
    				stmt = conn.prepareStatement("SELECT * FROM Users WHERE PhoneNumber LIKE ?");
                    stmt.setString(1, "%" + phoneNumber + "%");
                    rs = stmt.executeQuery();
                    searchSuccess = true;
        	
        		// search by email
				} else if (email.length() > 0 && employeeIDStr.length() <= 0 && firstName.length() <= 0 && lastName.length() <= 0
        				&& phoneNumber.length() <= 0) {
					stmt = conn.prepareStatement("SELECT * FROM Users WHERE LOWER(Email) LIKE ?");
                    stmt.setString(1, "%" + email + "%");
                    rs = stmt.executeQuery();
                    searchSuccess = true;
				}
        		
                
                // create table if search conditions met
        		if(searchSuccess) {
        			PrintWriter out = response.getWriter();
                    out.println("<html><head><style>"
                            + "table {width: 100%;border-collapse: collapse;margin: 15px 0;}"
                            + "th, td {border: 1px solid #999;padding: 0.5rem;text-align: left;}"
                            + "th {background-color: #f3f3f3;}"
                            + "</style></head><body><table>");
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
                    out.println("<button style=\"font-size:1em;padding:10px;background-color:#4CAF50;color:white;border:none;border-radius:5px;cursor:pointer;\" onclick=\"location.href='User.html'\">Menu</button>");
                    out.println("</body></html>");
        		} else {
        			if(!typeFailure) {
        				response.sendRedirect("User.html?tmArgs=true");
        			} else {
        				response.sendRedirect("User.html?typeFailure=true");
        			}
        		}
                
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
