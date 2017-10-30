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
//============GUI �ڿ�==============
	TestLogin login_GUI;
	TestMain main_GUI;
	TestChattingRoom chattingroom_GUI;
	TestMemo memo_GUI;
	File_Send send_file_GUI;
	File_Recieve recieve_file_GUI;
	
//============User �ڿ�===============
	String myID;
	private String myPW;
	Vector<String> User_list = new Vector<>();
	Vector<TestChattingRoom> Room_list = new Vector<>();
	
//==========��Ʈ��ũ �ڿ�==============
	private Socket socket;
	String server_ip;//���ϼ����� �� �ּҸ� ��� �Ѵ�.
	private int server_port;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	
//============�׿� �ڿ�================
	StringTokenizer st;
	
//============��� �޼���==============
	public TestClient() {//init method ����
		init();
	}
	private void init() {//ó�� �����ϴ� method
		login_GUI = new TestLogin(this);
	}
	
	void setBase( String server_ip , int server_port, String myID ,String myPW) {//�⺻ ���� setting �ܺ� class ������ ���� default
		this.server_ip = server_ip;
		this.server_port = server_port;
		network(myID,myPW);		
	}
	
	private void network(String id,String pw) {//������ �����ϰ� �� �⺻ ������ ����
		try {
			socket = new Socket(server_ip, server_port);
//			�α��� ������ �޾ƾ� �Ѵ�.
			if (approval_login(id, pw, socket)) {
				this.myID = id;
				this.myPW = pw;
				System.out.printf("id = %s , pw = %s , ip = %s , port = %d socket connecting is %b%n",
						myID,myPW,server_ip,server_port,socket.isConnected());
//				Login_GUI�� �ݴ´�.
				login_GUI.dispose();
				
//				���������� ������ ����Ǿ��� ���
				if(socket!=null)
					connection();
				
			} else {
//				��� �ڿ��� ��ȯ �ϰ� �ʱ�ȭ ��Ų��.
				os.close();is.close();dos.close();dis.close();socket.close();
				this.server_ip = "";
				this.server_port = 0;
				
//				�˸� �޼����� ���� �α��� �ٽ� ����
				JOptionPane.showMessageDialog(null, "��ϵ� ���̵�,��й�ȣ�� �ƴմϴ�!\n�ٽ� �Է����ּ���~","�˸�",JOptionPane.ERROR_MESSAGE);
			}
		}catch (UnknownHostException e) {
			JOptionPane.showMessageDialog(null, "���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(null, "���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	

	private void connection() {//��Ʈ�� �����ϰ� main_GUI �����Ѵ�.recieve message�� ���� �κ��� �̰��� �ִ�!
		
//		main_GUI�� ����
		main_GUI = new TestMain(this);
		main_GUI.thisUser_ta.append(myID);
		
//		user_list�� ����� �߰� but �ڱ��ڽ��̱� ������ �߰� ���� �ʴ´�.
//		User_list.add(myID);
		
		/*��� �޼����� �̰����� �޴´�*/
		Thread recieve_message = new Thread(new Runnable() {
			public void run() {
				
				
				while(true)
				{
					try {
						String msg = dis.readUTF();
						
						System.out.println("�����κ��� ���ŵ� �޼���"+msg);
						
						rec_msg_pro(msg);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "�������� ������ ������","�˸�",JOptionPane.ERROR_MESSAGE);
						err_return_resource();
						err_data_init();
						main_GUI=null;//main_GUI��ü ��ȯ
						login_GUI.setVisible(true);
						break;
					} 
				}
			}
		});
		recieve_message.start();
	}
	
	private void rec_msg_pro(String str) {//���� �޼����� ó���ϴ� �κ�
		st = new StringTokenizer(str, "/");
		String protocol = st.nextToken();
		String message = st.nextToken();
		
		System.out.println("�������� : "+protocol);
		System.out.println("���� : "+message); 
		
		/*���ӿ� ���� prorocol*/
		if(protocol.equals("LOG_NewUser")){//���ο� ������ 
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
		/*ä�ù濡 ���� prorocol*/
		else if(protocol.equals("CHATTINGROOM_INVATATION")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			
			if ((JOptionPane.showConfirmDialog(null, message+"���Լ� \n"+roomname+"��ȭ������ �ʴ�", "��ȭ�� �ʴ�", JOptionPane.YES_NO_OPTION))==0) {
				make_chattingroom(roomname);
				send_message("CHATTINGROOM_PERMIT/"+myID+"/"+message+"/"+roomname);
			} else {
				send_message("CHATTINGROOM_DENY/"+myID+"/"+message+"/"+roomname);
			}
		}else if (protocol.equals("CHATTINGROOM_INVATATION_INTHEROOM")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			
			if ((JOptionPane.showConfirmDialog(null, message+"���Լ� \n"+roomname+"��ȭ������ �ʴ�", "��ȭ�� �ʴ�", JOptionPane.YES_NO_OPTION))==0) {
				make_chattingroom(roomname);
				send_message("CHATTINGROOM_PERMIT/"+myID+"/"+message+"/"+roomname);
			} else {
				send_message("CHATTINGROOM_DENY/"+myID+"/"+message+"/"+roomname);
				System.out.println("CHATTINGROOM_DENY/"+myID+"/"+message+"/"+roomname);
			}
		}else if (protocol.equals("CHATTINGROOM_PERMIT")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			JOptionPane.showMessageDialog(null, message+"�� "+roomname+"��\n�����Ͽ����ϴ�" );
		}else if (protocol.equals("CHATTINGROOM_DENY")) {
			String recieve_user = st.nextToken();
			String roomname = st.nextToken();
			JOptionPane.showMessageDialog(null, message+"�� "+roomname+"��\n�����Ͽ����ϴ�" );
		}else if(protocol.equals("CHATTINGROOM_NewUser")){//���ο� ������ 
			String add_user = st.nextToken();
			String talk_message ="           < "+add_user+" ���� �����Ͽ����ϴ�! >\n";

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
					room.setTalk_ta("           < "+delete_user+" ���� �����Ͽ����ϴ�! >\n");
				}
			}
		}else if (protocol.equals("CHATTINGROOM_MESSAGE")) {
			String roomname = st.nextToken();
			String recieve_message = st.nextToken();
			String talk_message = message+" ��:\n"+recieve_message+"\n";
			
			for (int i = 0; i < Room_list.size(); i++) {
				
				TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
				if(r.roomname.equals(roomname))
					r.setTalk_ta(talk_message);
			}
		}
		/*������ ���� prorocol*/
		else if (protocol.equals("MEMO_PASS")) {
			String recieve_user =st.nextToken();
			String pass_message = st.nextToken();
			memo_GUI = new TestMemo(this,myID,message,pass_message);
		}
		/*�׸�ä�ÿ� ���� prorocol*/
		
		/*���Ϻ����⿡ ���� prorocol*/
		else if (protocol.equals("FILETRANS_REQUEST")) {//file ���ۿ�û�� ������ File_recieveâ�� �����Ͽ� ���� ���� �غ� �Ѵ�.
			String recieve_user = st.nextToken();
			String file_name = st.nextToken();
			String all_size = st.nextToken();
			JOptionPane.showMessageDialog(null, message+"�� "+"���� ������\n��û�Ͽ����ϴ�" );
			recieve_file_GUI = new File_Recieve(this, file_name,Integer.valueOf(all_size));
		}else if (protocol.equals("FILETRANS_PERMIT")) {
			String recieve_user = st.nextToken();
			
			JOptionPane.showMessageDialog(null, message+"�� "+"���� ������\n�����Ͽ����ϴ�" );
			
			
		}else if (protocol.equals("FILETRANS_DENY")) {
			String recieve_user = st.nextToken();
			
			JOptionPane.showMessageDialog(null, message+"�� "+"���� ������\n�����Ͽ����ϴ�" );
		}
		
	}
	
	
	

	void send_message(String str) {//������ �޼����� ������. �ٸ� Ŭ�������� ���� ������ defalut�� ������ ����
		try {
			dos.writeUTF(str);
		} catch (IOException e) {
//			���������� �������� �޼����� ���� �� ����.
//			��� �ڿ��� ��ȯ�ϰ� �α��� â���� �Ѿ�� �Ѵ�.
			JOptionPane.showMessageDialog(null, "can't send message","�˸�",JOptionPane.ERROR_MESSAGE);
		}
	}


	
	/*main_GUI �̹�Ʈ ó�� �޼���*/
	/*chattingroom_btn �̹�Ʈ ó��*/
 	String make_chattingroom_name() {//���̸��� ����� user�� �̸��� 1000���� ������ ���ļ� ����� ���̸� �ߺ� �˻縦 ���� ä��
 		Random r = new Random();
		String roomname = null;
		do {
			int roomNum=r.nextInt(1000);
			roomname = myID + String.valueOf(roomNum);
		} while (checkname(roomname));
		return roomname;
	}
	
	private boolean checkname(String name) {//���̸� äũ
		
		for (int i = 0; i < Room_list.size(); i++) {
			
			TestChattingRoom r = (TestChattingRoom)Room_list.elementAt(i);
			if(r.roomname.equals(name)&&name=="")
				return true;			
		}
		return false;
	}
	
	void make_chattingroom(String name) {//���̸��� �޾� ChattingRoom ��ü ����
		chattingroom_GUI = new TestChattingRoom(this, name);
		Room_list.add(chattingroom_GUI);
	}
	
	/*memo �̹�Ʈ ó��*/
	void make_memo(String user,String message) {
		memo_GUI = new TestMemo(this,myID,user,message);
	}
	
	/*�������� �̺�Ʈ ó��*/
	void transfile_set(String target_user) {
		send_file_GUI = new File_Send(this, target_user);
	}
	
	
	void close_main() {//user�� �޽��� ����� �ߴ��� �� 
		
		if (JOptionPane.showConfirmDialog(null, "���� �ϰڽ��ϱ�?", "main ����", JOptionPane.YES_NO_OPTION)==0) {
			/*�ִ� ���� �� ������ �Ѵ�*/
			for (int i = Room_list.size()-1; i >= 0 ; i--) {				
				TestChattingRoom room = (TestChattingRoom)Room_list.elementAt(i);
				close_chattingroom(room);
			}
			
			/*�޸�����*/
			
			/*�׸�ä������*/
			
			/*���Ϲޱ�����*/
			
			/*���� ���α׷� ����*/
			send_message("LOG_User_Out/"+myID);
			
			
			System.exit(0);
			
		} 
	}
	
	void close_chattingroom(TestChattingRoom room) {//user�� ä�ù��� ���� ��
		
		if (JOptionPane.showConfirmDialog(null, "ä�ù��� �����ڽ��ϱ�?", room.roomname+"�� ����", JOptionPane.YES_NO_OPTION)==0) {
			room.dispose();
			Room_list.remove(room);
			String str = "CHATTINGROOM_User_Out/"+room.roomname+"/"+myID;
			this.send_message(str);
			room=null;
		}
		
	}
	
	void close_memo(TestMemo memo) {//user�� ������ ���� ��
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
	
	private void err_data_init() {//������ �ش� Ŭ���̾�Ʈ�� ��� ���� ������ ����
		myID="";
		myPW="";
		if (!(User_list.isEmpty())) {
			User_list.removeAllElements();
		}
		if(!(Room_list.isEmpty())) {
			Room_list.removeAllElements();
		}
	}
	
	private void err_return_resource() {//������ �ش� Ŭ���̾�Ʈ�� ��� ��Ʈ��ũ �ڿ� ��ȯ
		try {
			os.close();is.close();dos.close();dis.close();socket.close();
		} catch (IOException e) {}
	}
	
	public static void main(String[] args) {
		new TestClient();
	}
}
