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
				System.out.println("���̵� �����մϴ�.");
				sql = "select * from member where pwd='"+search_pw+"';";
				result = stmt.executeQuery(sql);
					if (result.next()) {
						System.out.println("��й�ȣ ��ġ�մϴ�.\n"+"���� �����մϴ�.");
						con.close();
						return true;
					} else {
						System.out.println("��й�ȣ ����ġ�մϴ�.\n"+"���� �Ұ����մϴ�.");
						con.close();
						return false;
					}
			} else {
				System.out.println("��ġ�ϴ� ���̵� �����ϴ�.");
				con.close();
				return false;
			}
		} catch (Exception e) {
			System.out.println("DB���ӹ���");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
}
