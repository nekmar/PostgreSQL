package SocialNetworkingService;

import java.sql.*;
import java.util.Scanner;

public class MyClass {
	
	Connection conn = null;
	Statement st = null;
	ResultSet rs = null;
	ResultSet rst = null;
	
	//constructor
	public MyClass() {
		//read input from keyboard
		Scanner input = new Scanner(System.in);
		Scanner inputString = new Scanner(System.in);
		boolean exit = false;
		String email = null;
        do {
            printMenu();
            System.out.println("Choose menu item: ");
            int menuItem = input.nextInt();
			switch (menuItem) {
            	case 0://"Connect to database"
            		System.out.println("0. Connect to PostgreSQL database");
            		String url = "jdbc:postgresql://localhost:";
            		String IP = "5433";
            		String database = "snsDB";
					String login = "postgres";
					String passwd = "1024";
            		try {
    					Class.forName("org.postgresql.Driver");
    					conn = DriverManager.getConnection(url+IP+"/"+database, login, passwd);
						st = conn.createStatement();
            		} catch(Exception e) {
    					e.printStackTrace();
            		} 
            		break;
                case 1://"Begin transaction"
                	System.out.println("1. Begin transaction");
                	System.out.println("Insert user email: "); //'mteplica@teplica.co.uk'
                	email= inputString.nextLine();
            		if (email.isEmpty()){
            			System.out.println("You did not give the user email.");
            		}
            		try {
            			beginTransaction(email);
            		} catch (SQLException e1) {
            			// TODO Auto-generated catch block
            			e1.printStackTrace();
            		}
            		break;
            	case 2://"Rollback transaction"
            		System.out.println("2. Rollback transaction");
            		try {
            			conn.rollback();
            		} catch (SQLException e1) {
            			// TODO Auto-generated catch block
            			e1.printStackTrace();
            		}
                   	break;
                case 3://"Commit Transaction"
                	System.out.println("3. Commit Transaction");
                	try {
                		conn.commit();
                		//conn.setAutoCommit(true);
                	} catch (SQLException e1) {
                		// TODO Auto-generated catch block
                		e1.printStackTrace();
                	} 
            	    break;
                case 4://"Find the professional network"
                	System.out.println("4. Find the professional network");
                	System.out.println("Insert degree: ");
                	int n = input.nextInt();
                	try {
                		showProfessionalNetwork(n,email);
                	} catch (SQLException e) {
                		// TODO Auto-generated catch block
                		e.printStackTrace();
                	}
                   	break;
                case 5://"Insert comment"
                	System.out.println("5. Insert comment");
                	System.out.println("Insert articleID: ");
                	int articleId = input.nextInt();
                	System.out.println("Insert theComment: ");
                	String comment = inputString.nextLine();
                	try {
                		insertTheComment(email,articleId,comment);
                	} catch (SQLException e) {
                		// TODO Auto-generated catch block
                		e.printStackTrace();
                	}
                    break;
                case 6://"Send message"
                	System.out.println("6. Send message");
                	System.out.println("Insert theSubject: ");
                	String subject = inputString.nextLine();
                	System.out.println("Insert theText: ");
                	String text = inputString.nextLine();
                	System.out.println("Insert receiver_email: ");
                	String receiver = inputString.nextLine();
                	try {
                		sendTheMessage(subject,text,email,receiver);
                	} catch (SQLException e) {
                		// TODO Auto-generated catch block
                		e.printStackTrace();
                	}
                    break;
                case 7:
                    System.out.println("7. Exit");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (!exit);
        try {
			rs.close();
			rst.close();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("\nThe end");;
	}


	//functions

	//menu
	public void printMenu() {
        System.out.println("\n==============================================");
        System.out.println("|               THIS IS THE MENU               |");
        System.out.println("==============================================");
        System.out.println("|  Options:                                   |");
        System.out.println("0. Connect to PostgreSQL database");
        System.out.println("1. Begin transaction");
        System.out.println("2. Rollback transaction");
        System.out.println("3. Commit Transaction");
        System.out.println("4. Find the professional network");
        System.out.println("5. Insert comment");
        System.out.println("6. Send message");
        System.out.println("7. Exit");
        System.out.println("==============================================");
    }
	
	//begin transaction
	public void beginTransaction(String email) throws SQLException{
		/*checkEmail function
		 * input: email
		 * output: integer
		 * 
		 * Check if the given email exists in the database. If yes, return 1 else return null.
		*/
		String checkSql="select \"checkEmail\"("+email+")";
		rs = st.executeQuery(checkSql);
		while (rs.next()){
			String foundEmail = rs.getString(1);
			if(foundEmail!=null){
				System.out.println("\nEmail found!");
				//turn off auto-commit
				conn.setAutoCommit(false);
				//specifies that statements cannot read data that has been modified but not committed by other transactions
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			}
			else{
				System.out.println("\nError! Email does not exist!");
				break;
			}
		}
	}
	
	//nth degree professional network
	/*
	 * professionalNetworkByDegree function
	 * input: email, n
	 * output: table member info
	 * 
	 * Initially we find the "parents" of the node we are looking for and in our case we find the first degree of the connection.
	 * Then we make a union with all the other ancestors by calling anc() and thus we find the connections 
	 * which degree is greather than 1.
	 */
	public void showProfessionalNetwork(int n, String email) throws SQLException {
		String sql="select \"professionalNetworkByDegree\"("+email+","+n+")"; 
		rs = st.executeQuery(sql);
		while (rs.next()){
			System.out.println(rs.getString(1));
		}
	}
	
	//insert comment for specific article
	public void insertTheComment(String email, int articleId, String comment) throws SQLException{
		// Create a sequence for the right column "commentID" numeration when inserting data.
		String setSql = "select setval('commentID_seq', (select max(\"commentID\") from article_comment))";
		String sql="select \"insertCommentSpecificArticle\"("+email+","+articleId+","+comment+")"; 
		rst = st.executeQuery(setSql);
		rs = st.executeQuery(sql);
		while (rs.next()){
			System.out.println(rs.getString(1));
		}
	}
	
	//send message to another user
	public void sendTheMessage(String subject, String text,String email, String receiver) throws SQLException {
		String checkSql="select \"checkEmail\"("+receiver+")";
		rs = st.executeQuery(checkSql);
		while (rs.next()){
			String foundREmail = rs.getString(1);
			if((foundREmail!=null) && (!receiver.equals(email))){
				System.out.println("\nReceiver email found!");
				// Create a sequence for the right column "msgID" numeration when inserting data.
				String setSql = "select setval('msgid_seq', (select max(\"msgID\") from msg))";
				String sql="select \"sendMsgToUser\"("+email+","+receiver+","+subject+","+text+")";
				rst = st.executeQuery(setSql);
				rs = st.executeQuery(sql);
				while (rs.next()){
					System.out.println(rs.getString(1));
				}
			}
			else{
				System.out.println("\nError! Receiver email does not exist or receiver email equals sender_email!");
				break;
			}
		}
		
	}

	
	//main
	public static void main(String[] args) {
		new MyClass();
	}

}