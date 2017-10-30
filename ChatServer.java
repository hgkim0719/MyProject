import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.annotation.Retention;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



import javax.swing.JButton;
import javax.swing.JScrollPane;

public class ChatServer implements ActionListener{

//==================GUI 자원=========================
	private JFrame frame;
	private JTextField port_tf = new JTextField();
	private JTextArea ip_ta = new JTextArea();
	private JButton start_btn = new JButton("start");
	private JTextArea msg_ta = new JTextArea();

//==================네트워크 자원=========================
	private ServerSocket server_socket;
	private Socket client_socket;
	private String ip;
	private int port;
	static Vector<UserInfo> user_vc = new Vector();//file transmission도 쓰기 때문에 default
	private Vector<RoomInfo> room_vc = new Vector();
	private IDPWCheck check_IDPW = new IDPWCheck();
	
//=============FileTransmission 자원===================
	FileTransmission filetransmission_manager;
	
	
//=====================멤버 메서드========================
	/*서버 기본 설정 메서드*/
	public ChatServer() throws UnknownHostException {//TestChatServer 생성자
		
		initialize();
		setIp();
		addActionEvent();
		
	}

	private void setIp() {//Server위치의 ip를 셋팅
		try {
			InetAddress ip = InetAddress.getLocalHost();  
			ip_ta.setEditable(false);
			this.ip_ta.setText(ip.getHostAddress());
		} catch (Exception e) {}
	}

	private void initialize() {//TestChatServer의 윈도우 모양
		frame = new JFrame();
		frame.setBounds(100, 100, 296, 342);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		ip_ta.setBounds(12, 10, 160, 24);
		frame.getContentPane().add(ip_ta);
		
		
		port_tf.setBounds(12, 44, 160, 21);
		frame.getContentPane().add(port_tf);
		port_tf.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 75, 256, 218);
		frame.getContentPane().add(scrollPane);
		msg_ta.setEditable(false);
		
		scrollPane.setViewportView(msg_ta);
		
		start_btn.setBounds(184, 10, 84, 55);
		frame.getContentPane().add(start_btn);
	}

	private void addActionEvent() {//이벤트 추가
		start_btn.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {//버튼 이벤트 메서드
		if(e.getSource()==start_btn)
		{
			System.out.println("서버 시작버튼 클릭");
			
			port=Integer.parseInt(port_tf.getText().trim());
			
			server_start();//소켓 생성 및 사용자 접속 대기
			filetransmission_manager = new FileTransmission(this);
		}	
	}
	
	/*활용 코드*/
	private void server_start() {//서버 port를 얻어서 서버 소켓 생성
		
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 사용중인 포트","알림",JOptionPane.ERROR_MESSAGE);
		}
		
		if(server_socket!=null)
		{
			Connection();
		}
		
	}
	
	private void Connection() {// accept 부분을 thread로 생성하여 클라이언트 접속을 무한 대기 한다. 
		
		Thread th = new Thread(new Runnable() {
			
			/*접속 유효성 검사는 user생성자를 먼저 만들고 생성자 자체에서 따로 처리 한다.
			이미 스트림 생성으로 쓴 socket으로 다시 스트림 생성을 하게 되면 오류가 나기 때문이다.*/
			public void run() {
				
				while(true) {
					try {
						
						msg_ta.append("사용자 접속 대기중\n");
						client_socket = server_socket.accept();//사용자 접속 무한 대기
							msg_ta.append("사용자 접속!\n");
							UserInfo user = new UserInfo(client_socket);
							user.start();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "accept 에러 발생","알림",JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
			}
		});
		
		th.start();
	}
	
	public static UserInfo getUser(String uid) {
		for (int i = 0; i < user_vc.size(); i++) {
			UserInfo u = (UserInfo)user_vc.elementAt(i);
			if (u.ID == uid) {
				System.out.println(u.ID);
				return u;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatServer window = new ChatServer();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	class UserInfo extends Thread{
//=========네트워크 자원===============
		private Socket user_socket;
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
			
//=========그 밖에 자원 ===============
		private String ID;
		private StringTokenizer st;
		
//=========멤버 메서드================
		public UserInfo(Socket socket ) {//UserInfo 생성자 socket을 받아 생성한다.
			
			this.user_socket = socket;
			userNetwork();
		}
		
		
		private void userNetwork() {//user의 접속 유무성 검사 및 네트워크 setting
			try {
//				socket 스트림을 각 스트림(in,out)으로 초기화 
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				

				String str = dis.readUTF();
				StringTokenizer chk_st = new StringTokenizer(str, "/");
				
				String chk_id = chk_st.nextToken();
				String shk_pw = chk_st.nextToken();
				
				if(check_IDPW.isMember(chk_id, shk_pw)&&!there_is_thisUser(chk_id)) {
					try {
//						접속승인이 완료되면 user의 id를 저장한다.
						dos.writeUTF("LOG_OK");
						ID = chk_id;
						set_user();
					} catch (IOException e) {
						System.out.println("스트림 에러");
					}
				}else {
					try {
						dos.writeUTF("LOG_NO");
//						접속승인을 할 수 없으므로 사용했던 네트워크 자원은 반환한다.
						os.close();is.close();dos.close();dis.close();
					} catch (IOException e) {
						System.out.println("스트림 에러");
					}
				}
			}catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream설정 에러","알림",JOptionPane.ERROR_MESSAGE);
			}
		
		}
		
		private boolean there_is_thisUser(String id) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo)user_vc.elementAt(i);
				
				if(u.ID.equals(id)) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		}
		
		private void set_user() {//접속 후 필요한 user setting
			msg_ta.append(ID+" : 사용자 접속!\n");
			
//			기존사용자들에게 새로운 본인 사용자 알림
			broadcast("LOG_NewUser/"+ID);
			
//			자신에게 기존 사용자 알림
			for(int i = 0;i<user_vc.size();i++)
			{
				UserInfo u =(UserInfo)user_vc.elementAt(i);
				
				send_message("LOG_OldUser/"+u.ID);
			}
//			기존 사용자들에게 자신을 알리고 사용자 리스트에 추가
			user_vc.add(this);
			
			broadcast("LOG_user_list_update/ ");
		}
		
		/*사용자 send_method*/
		void send_message(String str) {//file transmission도 쓰기 때문에 default
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				msg_ta.append("에러 발생 : "+this.ID+"의 접속이 끊어짐~!!\n");
				err_return_resource();
			}
		}
		
		private void broadcast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo)user_vc.elementAt(i);
				u.send_message(str);
			}
		}
		
		/*사용자 recieve_method 및 message 처리 method*/
		public void run()//Thread에서 처리할 내용, 실제 메세지를 받는 부분이다.
		{
			while(true) {
				try {
					String msg = dis.readUTF();
					msg_ta.append(ID+" : 사용자로부터 들어온 메세지 : "+msg+"\n");
					recieve_message_pro(msg);//받은 메세지 처리
				} catch (IOException e) {
					msg_ta.append(ID+" : 사용자 접속이 끊어짐!!\n");
					err_return_resource();
					break;
				}
			}
			
		}
		
		private void recieve_message_pro(String str) {//받은 메세지 처리 메서드
			st = new StringTokenizer(str, "/");
			String protocol = st.nextToken();
			
			System.out.println("프로토콜 : "+protocol);
			
			
			/*접속에 대한 prorocol*/
			if (protocol.equals("LOG_User_Out")) {
				msg_ta.append(ID+" : 사용자 접속을 종료!!\n");
				err_return_resource();
			}
			/*채팅방에 대한 prorocol*/
			else if (protocol.equals("CHATTINGROOM_INVATATION")) {
				String send_user = st.nextToken();
				String recieve_user = st.nextToken();
				String roomname = st.nextToken();
				
				//벡터에서 해당사용자를 찾아서 메세지 전송
				for(int i = 0 ;i<user_vc.size();i++)
				{
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					
					if(u.ID.equals(recieve_user)) {
						u.send_message(str);
					}
				}
				
				RoomInfo chattingroom = new RoomInfo(roomname, this);
				room_vc.add(chattingroom);
			}else if (protocol.equals("CHATTINGROOM_INVATATION_INTHEROOM")) {
				String send_user = st.nextToken();
				String recieve_user = st.nextToken();
				String roomname = st.nextToken();
				
				for(int i = 0 ;i<user_vc.size();i++)
				{
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					
					if(u.ID.equals(recieve_user)) {
						u.send_message(str);
					}
				}
			}else if (protocol.equals("CHATTINGROOM_PERMIT")) {
				String send_user = st.nextToken();
				String recieve_user = st.nextToken();
				String roomname = st.nextToken();
				
				for(int i = 0 ;i<room_vc.size();i++)
				{
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					
					if(r.room_name.equals(roomname)) {
						r.broadcast_room(str);
						r.add_user(this);
					}
				}
			}else if (protocol.equals("CHATTINGROOM_DENY")) {
				String send_user = st.nextToken();
				String recieve_user = st.nextToken();
				
				//벡터에서 해당사용자를 찾아서 메세지 전송
				for(int i = 0 ;i<user_vc.size();i++)
				{
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					
					if(u.ID.equals(recieve_user)) {
						u.send_message(str);
					}
				}
			}else if (protocol.equals("CHATTINGROOM_MESSAGE")) {
				String recieve_user = st.nextToken();
				String roomname = st.nextToken();
				for(int i = 0 ;i<room_vc.size();i++)
				{
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					
					if(r.room_name.equals(roomname)) {
						r.broadcast_room(str);
					}
				}
			}else if (protocol.equals("CHATTINGROOM_User_Out")) {
				String roomname = st.nextToken();
				String out_user = st.nextToken();
				for(int i = 0 ;i<room_vc.size();i++)
				{
					RoomInfo r = (RoomInfo)room_vc.elementAt(i);
					
					if(r.room_name.equals(roomname)) 
						r.remove_user(out_user,str);
				}
			}
			/*쪽지에 대한 prorocol*/
			else if (protocol.equals("MEMO_PASS")) {
				String pass_user = st.nextToken();
				String recieve_user = st.nextToken();
				String messga = st.nextToken();
				
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.ID.equals(recieve_user))
						u.send_message(str);
				}
			}
			/*파일보내기에 대한 prorocol*/
			else if (protocol.equals("FILETRANS_REQUEST")) {
				String pass_user = st.nextToken();
				String recieve_user = st.nextToken();
				
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.ID.equals(recieve_user))
						u.send_message(str);
				}
			}
			else if (protocol.equals("FILETRANS_PERMIT")) {
				String pass_user = st.nextToken();
				String recieve_user = st.nextToken();
				
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.ID.equals(recieve_user))
						u.send_message(str);
				}
			}else if (protocol.equals("FILETRANS_DENY")) {
				String pass_user = st.nextToken();
				String recieve_user = st.nextToken();
				
				for (int i = 0; i < user_vc.size(); i++) {
					UserInfo u = (UserInfo)user_vc.elementAt(i);
					if(u.ID.equals(recieve_user))
						u.send_message(str);
				}
			}
			

		}
		
		/*사용자 접속이 끊어진 경우 모든 자원 반환*/
		private void err_return_resource() {
			try {
				dos.close();
				dis.close();
				user_socket.close();
				user_vc.remove(this);
				broadcast("LOG_User_Out/"+ID);
				broadcast("LOG_user_list_update/ ");
			} catch (IOException e1) {}
		}
	}

	class RoomInfo{
		private String room_name;
		private Vector<UserInfo> room_user_vc = new Vector();
		
		public RoomInfo(String str, UserInfo u) {
			this.room_name = str;
			this.room_user_vc.add(u);
			u.send_message("CHATTINGROOM_alluser_list_update/"+this.room_name);
		}
		
		public void broadcast_room(String str) {//현재 방의 모든 사람들에게 알린다.
			 
			for(int i = 0;i<room_user_vc.size();i++) {
				UserInfo u = (UserInfo)room_user_vc.elementAt(i);
				
				u.send_message(str);
			}
		}
		
		private void room_newUser(String username) {//현재 접속한 user을 알린다.
			broadcast_room("CHATTINGROOM_NewUser/"+this.room_name+"/"+username);
		}
		private void room_oldUser(UserInfo u) {//현재 접속한 user에게 이미접속한 users를 알린다.
			for (UserInfo member : room_user_vc) {
				u.send_message("CHATTINGROOM_OldUser/"+this.room_name+"/"+member.ID);
			}
		}
		
		private void add_user(UserInfo u) {//user가 채팅방에 접속하면 각 user list 정보를 업데이트 및 list에 추가 한다.
			room_newUser(u.ID);
			room_oldUser(u);
			this.room_user_vc.add(u);
			u.send_message("CHATTINGROOM_alluser_list_update/"+this.room_name);
			broadcast_room("CHATTINGROOM_user_list_update/"+this.room_name);
		}
	
		private void remove_user(String user_name,String str) {//user가 채팅방을 나가면 list의 자신을 지우고 기존 users에게 그 사실을 알린다.
			for (int i = 0; i < room_user_vc.size(); i++) {
				UserInfo u = (UserInfo)room_user_vc.elementAt(i);
				
				if (u.ID.equals(user_name)) {
					room_user_vc.removeElement(u);
				}
			}
			
			broadcast_room(str);
			broadcast_room("CHATTINGROOM_user_list_update/"+this.room_name);
		}
	}
}

