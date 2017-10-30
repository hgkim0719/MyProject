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

//==================GUI �ڿ�=========================
	private JFrame frame;
	private JTextField port_tf = new JTextField();
	private JTextArea ip_ta = new JTextArea();
	private JButton start_btn = new JButton("start");
	private JTextArea msg_ta = new JTextArea();

//==================��Ʈ��ũ �ڿ�=========================
	private ServerSocket server_socket;
	private Socket client_socket;
	private String ip;
	private int port;
	static Vector<UserInfo> user_vc = new Vector();//file transmission�� ���� ������ default
	private Vector<RoomInfo> room_vc = new Vector();
	private IDPWCheck check_IDPW = new IDPWCheck();
	
//=============FileTransmission �ڿ�===================
	FileTransmission filetransmission_manager;
	
	
//=====================��� �޼���========================
	/*���� �⺻ ���� �޼���*/
	public ChatServer() throws UnknownHostException {//TestChatServer ������
		
		initialize();
		setIp();
		addActionEvent();
		
	}

	private void setIp() {//Server��ġ�� ip�� ����
		try {
			InetAddress ip = InetAddress.getLocalHost();  
			ip_ta.setEditable(false);
			this.ip_ta.setText(ip.getHostAddress());
		} catch (Exception e) {}
	}

	private void initialize() {//TestChatServer�� ������ ���
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

	private void addActionEvent() {//�̺�Ʈ �߰�
		start_btn.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {//��ư �̺�Ʈ �޼���
		if(e.getSource()==start_btn)
		{
			System.out.println("���� ���۹�ư Ŭ��");
			
			port=Integer.parseInt(port_tf.getText().trim());
			
			server_start();//���� ���� �� ����� ���� ���
			filetransmission_manager = new FileTransmission(this);
		}	
	}
	
	/*Ȱ�� �ڵ�*/
	private void server_start() {//���� port�� �� ���� ���� ����
		
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "�̹� ������� ��Ʈ","�˸�",JOptionPane.ERROR_MESSAGE);
		}
		
		if(server_socket!=null)
		{
			Connection();
		}
		
	}
	
	private void Connection() {// accept �κ��� thread�� �����Ͽ� Ŭ���̾�Ʈ ������ ���� ��� �Ѵ�. 
		
		Thread th = new Thread(new Runnable() {
			
			/*���� ��ȿ�� �˻�� user�����ڸ� ���� ����� ������ ��ü���� ���� ó�� �Ѵ�.
			�̹� ��Ʈ�� �������� �� socket���� �ٽ� ��Ʈ�� ������ �ϰ� �Ǹ� ������ ���� �����̴�.*/
			public void run() {
				
				while(true) {
					try {
						
						msg_ta.append("����� ���� �����\n");
						client_socket = server_socket.accept();//����� ���� ���� ���
							msg_ta.append("����� ����!\n");
							UserInfo user = new UserInfo(client_socket);
							user.start();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null, "accept ���� �߻�","�˸�",JOptionPane.ERROR_MESSAGE);
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
//=========��Ʈ��ũ �ڿ�===============
		private Socket user_socket;
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
			
//=========�� �ۿ� �ڿ� ===============
		private String ID;
		private StringTokenizer st;
		
//=========��� �޼���================
		public UserInfo(Socket socket ) {//UserInfo ������ socket�� �޾� �����Ѵ�.
			
			this.user_socket = socket;
			userNetwork();
		}
		
		
		private void userNetwork() {//user�� ���� ������ �˻� �� ��Ʈ��ũ setting
			try {
//				socket ��Ʈ���� �� ��Ʈ��(in,out)���� �ʱ�ȭ 
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
//						���ӽ����� �Ϸ�Ǹ� user�� id�� �����Ѵ�.
						dos.writeUTF("LOG_OK");
						ID = chk_id;
						set_user();
					} catch (IOException e) {
						System.out.println("��Ʈ�� ����");
					}
				}else {
					try {
						dos.writeUTF("LOG_NO");
//						���ӽ����� �� �� �����Ƿ� ����ߴ� ��Ʈ��ũ �ڿ��� ��ȯ�Ѵ�.
						os.close();is.close();dos.close();dis.close();
					} catch (IOException e) {
						System.out.println("��Ʈ�� ����");
					}
				}
			}catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
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
		
		private void set_user() {//���� �� �ʿ��� user setting
			msg_ta.append(ID+" : ����� ����!\n");
			
//			��������ڵ鿡�� ���ο� ���� ����� �˸�
			broadcast("LOG_NewUser/"+ID);
			
//			�ڽſ��� ���� ����� �˸�
			for(int i = 0;i<user_vc.size();i++)
			{
				UserInfo u =(UserInfo)user_vc.elementAt(i);
				
				send_message("LOG_OldUser/"+u.ID);
			}
//			���� ����ڵ鿡�� �ڽ��� �˸��� ����� ����Ʈ�� �߰�
			user_vc.add(this);
			
			broadcast("LOG_user_list_update/ ");
		}
		
		/*����� send_method*/
		void send_message(String str) {//file transmission�� ���� ������ default
			try {
				dos.writeUTF(str);
			} catch (IOException e) {
				msg_ta.append("���� �߻� : "+this.ID+"�� ������ ������~!!\n");
				err_return_resource();
			}
		}
		
		private void broadcast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo u = (UserInfo)user_vc.elementAt(i);
				u.send_message(str);
			}
		}
		
		/*����� recieve_method �� message ó�� method*/
		public void run()//Thread���� ó���� ����, ���� �޼����� �޴� �κ��̴�.
		{
			while(true) {
				try {
					String msg = dis.readUTF();
					msg_ta.append(ID+" : ����ڷκ��� ���� �޼��� : "+msg+"\n");
					recieve_message_pro(msg);//���� �޼��� ó��
				} catch (IOException e) {
					msg_ta.append(ID+" : ����� ������ ������!!\n");
					err_return_resource();
					break;
				}
			}
			
		}
		
		private void recieve_message_pro(String str) {//���� �޼��� ó�� �޼���
			st = new StringTokenizer(str, "/");
			String protocol = st.nextToken();
			
			System.out.println("�������� : "+protocol);
			
			
			/*���ӿ� ���� prorocol*/
			if (protocol.equals("LOG_User_Out")) {
				msg_ta.append(ID+" : ����� ������ ����!!\n");
				err_return_resource();
			}
			/*ä�ù濡 ���� prorocol*/
			else if (protocol.equals("CHATTINGROOM_INVATATION")) {
				String send_user = st.nextToken();
				String recieve_user = st.nextToken();
				String roomname = st.nextToken();
				
				//���Ϳ��� �ش����ڸ� ã�Ƽ� �޼��� ����
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
				
				//���Ϳ��� �ش����ڸ� ã�Ƽ� �޼��� ����
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
			/*������ ���� prorocol*/
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
			/*���Ϻ����⿡ ���� prorocol*/
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
		
		/*����� ������ ������ ��� ��� �ڿ� ��ȯ*/
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
		
		public void broadcast_room(String str) {//���� ���� ��� ����鿡�� �˸���.
			 
			for(int i = 0;i<room_user_vc.size();i++) {
				UserInfo u = (UserInfo)room_user_vc.elementAt(i);
				
				u.send_message(str);
			}
		}
		
		private void room_newUser(String username) {//���� ������ user�� �˸���.
			broadcast_room("CHATTINGROOM_NewUser/"+this.room_name+"/"+username);
		}
		private void room_oldUser(UserInfo u) {//���� ������ user���� �̹������� users�� �˸���.
			for (UserInfo member : room_user_vc) {
				u.send_message("CHATTINGROOM_OldUser/"+this.room_name+"/"+member.ID);
			}
		}
		
		private void add_user(UserInfo u) {//user�� ä�ù濡 �����ϸ� �� user list ������ ������Ʈ �� list�� �߰� �Ѵ�.
			room_newUser(u.ID);
			room_oldUser(u);
			this.room_user_vc.add(u);
			u.send_message("CHATTINGROOM_alluser_list_update/"+this.room_name);
			broadcast_room("CHATTINGROOM_user_list_update/"+this.room_name);
		}
	
		private void remove_user(String user_name,String str) {//user�� ä�ù��� ������ list�� �ڽ��� ����� ���� users���� �� ����� �˸���.
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

