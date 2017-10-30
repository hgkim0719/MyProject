import java.sql.*;

public class IDPWCheck {
	String driverName = "com.mysql.jdbc.Driver";
	String DBName = "member";
	String dbURL = "jdbc:mysql://localhost:3306/"+DBName;
	String search_id=null;
	String search_pw=null;
	
	public boolean isMember(String id,String pw) {
		try {
			search_id = id;
			search_pw = pw;
			
			Class.forName(driverName);
			Connection con = DriverManager.getConnection(dbURL, "root", "apmsetup");
			
			Statement stmt = con.createStatement();
			String sql = "select * from member where id='"+search_id+"';";
			ResultSet result = stmt.executeQuery(sql);
			
			if (result.next()) {
				System.out.println("아이디가 존재합니다.");
				sql = "select * from member where pwd='"+search_pw+"';";
				result = stmt.executeQuery(sql);
					if (result.next()) {
						System.out.println("비밀번호 일치합니다.\n"+"접속 가능합니다.");
						con.close();
						return true;
					} else {
						System.out.println("비밀번호 불일치합니다.\n"+"접속 불가능합니다.");
						con.close();
						return false;
					}
			} else {
				System.out.println("일치하는 아이디가 없습니다.");
				con.close();
				return false;
			}
		} catch (Exception e) {
			System.out.println("DB접속문제");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
}
