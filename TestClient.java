import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class TestClient extends JFrame{
//============GUI 자원==============
	TestLogin login_GUI;
	TestMain main_GUI;
	TestChattingRoom chattingroom_GUI;
	TestMemo memo_GUI;
	File_Send send_file_GUI;
	File_Recieve recieve_file_GUI;
	
//============User 자원===============
	String myID;
	private String myPW;
	Vector<String> User_list = new Vector<>();
	Vector<TestChattingRoom> Room_list = new Vector<>();
	
//==========네트워크 자원==============
	private Socket socket;
	String server_ip;//파일서버도 이 주소를 써야 한다.
	private int server_port;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	
//============그외 자원================
	StringTokenizer st;
	
//============멤버 메서드==============
	public TestClient() {//init method 실행
		init();
	}
	private void init() {//처음 실행하는 method
		login_GUI = new TestLogin(this);
	}
	
	void setBase( String server_ip , int server_port, String myID ,String myPW) {//기본 정보 setting 외부 class 접근을 위해 default
		this.server_ip = server_ip;
		this.server_port = server_port;
		network(myID,myPW);		
	}
	
	private void network(String id,String pw) {//소켓을 연결하고 각 기본 데이터 저장
		try {
			socket = new Socket(server_ip, server_port);
//			로그인 승인을 받아야 한다.
			if (approval_login(id, pw, socket)) {
				this.myID = id;
				this.myPW = pw;
				System.out.printf("id = %s , pw = %s , ip = %s , port = %d socket connecting is %b%n",
						myID,myPW,server_ip,server_port,socket.isConnected());
//				Login_GUI을 닫는다.
				login_GUI.dispose();
				
//				정상적으로 소켓이 연결되었을 경우
				if(socket!=null)
					connection();
				
			} else {
//				모든 자원을 반환 하고 초기화 시킨다.
				os.close();is.close();dos.close();dis.close();socket.close();
				this.server_ip = "";
				this.server_port = 0;
				
//				알림 메세지를 열고 로그인 다시 유도
				JOptionPane.showMessageDialog(null, "등록된 아이디,비밀번호가 아닙니다!\n다시 입력해주세요~","알림",JOptionPane.ERROR_MESSAGE);
			}
		}catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "연결 실패","알림",JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "연결 실패","알림",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private boolean approval_login(String id,String pw,Socket socket) {
		try {
			is = socket.getInputStream();
			dis = new DataInputStream(is);			
			os = socket.getOutputStream();
			dos = new DataOutputStream(os);
			
			dos.writeUTF(id+"/"+pw);
			String check_ticket = dis.readUTF();
			
			if(check_ticket.equals("LOG_OK")) {
				return true;
			} else if (check_ticket.equals("LOG_NO")) {
				return false;
			}
		} catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "연결 실패","알림",JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "연결 실패","알림",JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	

	private void connection() {//스트림 생성하고 main_GUI 생성한다.recieve message에 대한 부분이 이곳에 있다!
		
//		main_GUI를 연다
		main_GUI = new TestMain(this);
		main_GUI.thisUser_ta.append(myID);
		
//		user_list에 사용자 추가 but 자기자신이기 때문에 추가 하지 않는다.
//		User_list.add(myID);
		
		/*모든 메세지는 이곳에서 받는다*/
		Thread recieve_message = new Thread(new Runnable() {
			public void run() {
				
				
				while(true)
				{
					try {
						String msg = dis.readUTF();
						
						System.out.println("서버로부터 수신된 메세지"+msg);
						
						rec_msg_pro(msg);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "서버와의 접속이 끊어짐","알림",JOptionPane.ERROR_MESSAGE);
						err_return_resource();
						err_data_init();
						main_GUI=null;//main_GUI객체 반환
						login_GUI.setVisible(true);
						break;
					} 
				}
			}
		});
		recieve_message.start();
	}
	
	private void rec_msg_pro(String str) {//받은 메세지를 처리하는 부분
		st = new StringTokenizer(str, "/");
		String protocol = st.nextToken();
		String message = st.nextToken();
		
		System.out.println("프로토콜 : "+protocol);
		System.out.println("내용 : "+message); 
		
		/*접속에 대한 prorocol*/
		if(protocol.equals("LOG_NewUser")){//새로운 접속자 
			User_list.add(message);
		}else if(protocol.equals("LOG_OldUser")) {
			User_list.add(message);
		}else if(protocol.equals("LOG_user_list_update")) {
			main_GUI.users_list.setListData(User_list);
			for (TestChattingRoom room : Room_list) {
				room.setAllUser_list(User_list);
			}
		}else if(protocol.equals("LOG_User_Out")) {
			User_list.remove(message);
		}
		/*채팅방에 대한 prorocol*/
		else if(protocol.equals("CHATTINGROOM_INVATATION")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			
			if ((JOptionPane.showConfirmDialog(null, message+"에게서 \n"+roomname+"대화방으로 초대", "대화방 초대", JOptionPane.YES_NO_OPTION))==0) {
				make_chattingroom(roomname);
				send_message("CHATTINGROOM_PERMIT/"+myID+"/"+message+"/"+roomname);
			} else {
				send_message("CHATTINGROOM_DENY/"+myID+"/"+message+"/"+roomname);
			}
		}else if (protocol.equals("CHATTINGROOM_INVATATION_INTHEROOM")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			
			if ((JOptionPane.showConfirmDialog(null, message+"에게서 \n"+roomname+"대화방으로 초대", "대화방 초대", JOptionPane.YES_NO_OPTION))==0) {
				make_chattingroom(roomname);
				send_message("CHATTINGROOM_PERMIT/"+myID+"/"+message+"/"+roomname);
			} else {
				send_message("CHATTINGROOM_DENY/"+myID+"/"+message+"/"+roomname);
				System.out.println("CHATTINGROOM_DENY/"+myID+"/"+message+"/"+roomname);
			}
		}else if (protocol.equals("CHATTINGROOM_PERMIT")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			JOptionPane.showMessageDialog(null, message+"가 "+roomname+"을\n수락하였습니다" );
		}else if (protocol.equals("CHATTINGROOM_DENY")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			JOptionPane.showMessageDialog(null, message+"가 "+roomname+"을\n거절하였습니다" );
		}else if(protocol.equals("CHATTINGROOM_NewUser")){//새로운 접속자 
			String add_user = st.nextToken();
			String talk_message ="           < "+add_user+" 님이 입장하였습니다! >\n";

			for (int i = 0; i < Room_list.size(); i++) {
				
				TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
				if(r.roomname.equals(message)) {
					r.this_room_users.add(add_user);	
					r.setTalk_ta(talk_message);
				}
			}
		}else if(protocol.equals("CHATTINGROOM_OldUser")) {
			String add_user = st.nextToken();
			for (int i = 0; i < Room_list.size(); i++) {
				
				TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
				if(r.roomname.equals(message))
					r.this_room_users.add(add_user);	
			}
		}else if(protocol.equals("CHATTINGROOM_user_list_update")) {
			for (int i = 0; i < Room_list.size(); i++) {
				
				TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
				if(r.roomname.equals(message))
					r.setChattingRoomMember_list();
			}
		}else if (protocol.equals("CHATTINGROOM_alluser_list_update")) {
			for (int i = 0; i < Room_list.size(); i++) {
				
				TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
				if(r.roomname.equals(message))
					r.setAllUser_list(User_list);
			}
		}else if(protocol.equals("CHATTINGROOM_User_Out")) {
			String delete_user = st.nextToken();
			for (TestChattingRoom room : Room_list) {
				if(room.roomname.equals(message)) {
					room.this_room_users.remove(delete_user);
					room.setTalk_ta("           < "+delete_user+" 님이 퇴장하였습니다! >\n");
				}
			}
		}else if (protocol.equals("CHATTINGROOM_MESSAGE")) {
			String roomname = st.nextToken();
			String recieve_message = st.nextToken();
			String talk_message = message+" 님:\n"+recieve_message+"\n";
			
			for (int i = 0; i < Room_list.size(); i++) {
				
				TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
				if(r.roomname.equals(roomname))
					r.setTalk_ta(talk_message);
			}
		}
		/*쪽지에 대한 prorocol*/
		else if (protocol.equals("MEMO_PASS")) {
			String recieve_user =st.nextToken();
			String pass_message = st.nextToken();
			memo_GUI = new TestMemo(this,myID,message,pass_message);
		}
		/*그림채팅에 대한 prorocol*/
		
		/*파일보내기에 대한 prorocol*/
		else if (protocol.equals("FILETRANS_REQUEST")) {//file 전송요청이 들어오면 File_recieve창을 생성하여 파일 받을 준비를 한다.
			String recieve_user = st.nextToken();
			String file_name = st.nextToken();
			String all_size = st.nextToken();
			JOptionPane.showMessageDialog(null, message+"가 "+"파일 전송을\n신청하였습니다" );
			recieve_file_GUI = new File_Recieve(this, file_name,Integer.valueOf(all_size));
		}else if (protocol.equals("FILETRANS_PERMIT")) {
			String recieve_user = st.nextToken();
			
			JOptionPane.showMessageDialog(null, message+"가 "+"파일 전송을\n수럭하였습니다" );
			
			
		}else if (protocol.equals("FILETRANS_DENY")) {
			String recieve_user = st.nextToken();
			
			JOptionPane.showMessageDialog(null, message+"가 "+"파일 전송을\n거절하였습니다" );
		}
		
	}
	
	
	

	void send_message(String str) {//서버에 메세지를 보낸다. 다른 클래스에도 쓰기 때문에 defalut로 접근자 지정
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
//			서버접속이 끊어지면 메세지가 보낼 수 없다.
//			모든 자원을 반환하고 로그인 창으로 넘어가야 한다.
			JOptionPane.showMessageDialog(null, "can't send message","알림",JOptionPane.ERROR_MESSAGE);
		}
	}


	
	/*main_GUI 이밴트 처리 메서드*/
	/*chattingroom_btn 이밴트 처리*/
 	String make_chattingroom_name() {//방이름은 만드는 user의 이름과 1000안의 난수를 합쳐서 만들고 방이름 중복 검사를 한후 채택
 		Random r = new Random();
		String roomname = null;
		do {
			int roomNum=r.nextInt(1000);
			roomname = myID + String.valueOf(roomNum);
		} while (checkname(roomname));
		return roomname;
	}
	
	private boolean checkname(String name) {//방이름 채크
		
		for (int i = 0; i < Room_list.size(); i++) {
			
			TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
			if(r.roomname.equals(name)&&name=="")
				return true;			
		}
		return false;
	}
	
	void make_chattingroom(String name) {//방이름을 받아 ChattingRoom 객체 생성
		chattingroom_GUI = new TestChattingRoom(this, name);
		Room_list.add(chattingroom_GUI);
	}
	
	/*memo 이밴트 처리*/
	void make_memo(String user,String message) {
		memo_GUI = new TestMemo(this,myID,user,message);
	}
	
	/*파일전송 이벤트 처리*/
	void transfile_set(String target_user) {
		send_file_GUI = new File_Send(this, target_user);
	}
	
	
	void close_main() {//user가 메신저 사용을 중단할 때 
		
		if (JOptionPane.showConfirmDialog(null, "종료 하겠습니까?", "main 종료", JOptionPane.YES_NO_OPTION)==0) {
			/*있는 방은 다 나가야 한다*/
			for (int i = Room_list.size()-1; i >= 0 ; i--) {				
				TestChattingRoom room = (TestChattingRoom)Room_list.elementAt(i);
				close_chattingroom(room);
			}
			
			/*메모종료*/
			
			/*그림채팅종료*/
			
			/*파일받기종료*/
			
			/*실제 프로그램 종료*/
			send_message("LOG_User_Out/"+myID);
			
			
			System.exit(0);
			
		} 
	}
	
	void close_chattingroom(TestChattingRoom room) {//user가 채팅방을 나갈 때
		
		if (JOptionPane.showConfirmDialog(null, "채팅방을 나가겠습니까?", room.roomname+"방 종료", JOptionPane.YES_NO_OPTION)==0) {
			room.dispose();
			Room_list.remove(room);
			String str = "CHATTINGROOM_User_Out/"+room.roomname+"/"+myID;
			this.send_message(str);
			room=null;
		}
		
	}
	
	void close_memo(TestMemo memo) {//user가 쪽지를 닫을 때
		memo_GUI.dispose();
		memo=null;
	}
	void close_file_send(File_Send filesend) {
		filesend.dispose();
		filesend=null;
	}
	
	void close_file_recieve(File_Recieve filerecieve) {
		filerecieve.dispose();
		filerecieve=null;
	}
	
	private void err_data_init() {//오류시 해당 클라이언트의 모든 내부 데이터 삭제
		myID="";
		myPW="";
		if (!(User_list.isEmpty())) {
			User_list.removeAllElements();
		}
		if(!(Room_list.isEmpty())) {
			Room_list.removeAllElements();
		}
	}
	
	private void err_return_resource() {//오류시 해당 클라이언트의 모든 네트워크 자원 반환
		try {
			os.close();is.close();dos.close();dis.close();socket.close();
		} catch (IOException e) {}
	}
	
	public static void main(String[] args) {
		new TestClient();
	}
}
