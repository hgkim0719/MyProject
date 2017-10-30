import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

class JFrameWindowClosingEventHandler extends WindowAdapter { 
	public void windowClosing(WindowEvent e) { 
		JFrame frame = (JFrame)e.getWindow(); 
		frame.dispose(); 
		System.out.println("windowClosing()"); 
	} 
}


public class TestChattingRoom extends JFrame implements ActionListener {
//=======class Client 자원========
	TestClient client;

//=======채팅방 자원================
	String roomname;
	Vector<String> this_room_users = new Vector<>();
		
//===========GUI 자원==================
	private JPanel contentPane;
	private JTextArea talk_ta = new JTextArea();
	private JList chattingRoomMember_list = new JList();
	private JList allUser_list = new JList();
	private JTextArea thisUser_ta = new JTextArea();
	private JTextField sendTalk_tf = new JTextField();
	private JButton addMember_btn = new JButton("추가");
	private JButton send_btn = new JButton("전송");

	/*윈도우 폼에 대한 정의*/
	public TestChattingRoom() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 526, 345);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 382, 253);
		contentPane.add(scrollPane);
		
		
		scrollPane.setViewportView(talk_ta);
		
		JLabel chattingRoomMember_lb = new JLabel("참여자");
		chattingRoomMember_lb.setBounds(409, 10, 89, 15);
		contentPane.add(chattingRoomMember_lb);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(406, 35, 91, 80);
		contentPane.add(scrollPane_1);
		
		
		scrollPane_1.setViewportView(chattingRoomMember_list);
		
		JLabel allUsers_lb = new JLabel("접속리스트");
		allUsers_lb.setBounds(409, 125, 89, 15);
		contentPane.add(allUsers_lb);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(406, 150, 91, 80);
		contentPane.add(scrollPane_2);
		allUser_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		scrollPane_2.setViewportView(allUser_list);
		
		
		addMember_btn.setBounds(406, 240, 92, 23);
		contentPane.add(addMember_btn);
		thisUser_ta.setEditable(false);
		
		
		thisUser_ta.setBounds(12, 273, 68, 23);
		contentPane.add(thisUser_ta);
		
		
		sendTalk_tf.setBounds(92, 274, 302, 21);
		contentPane.add(sendTalk_tf);
		sendTalk_tf.setColumns(10);
		
		
		send_btn.setBounds(406, 273, 92, 23);
		contentPane.add(send_btn);
		
		this.setVisible(true);
		addActionListener();
	}
	
	private void init() {
		this.setTitle(this.roomname);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 526, 345);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 382, 253);
		contentPane.add(scrollPane);
		
		
		scrollPane.setViewportView(talk_ta);
		
		JLabel chattingRoomMember_lb = new JLabel("참여자");
		chattingRoomMember_lb.setBounds(409, 10, 89, 15);
		contentPane.add(chattingRoomMember_lb);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(406, 35, 91, 80);
		contentPane.add(scrollPane_1);
		
		
		scrollPane_1.setViewportView(chattingRoomMember_list);
		
		JLabel allUsers_lb = new JLabel("접속리스트");
		allUsers_lb.setBounds(409, 125, 89, 15);
		contentPane.add(allUsers_lb);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(406, 150, 91, 80);
		contentPane.add(scrollPane_2);
		
		
		scrollPane_2.setViewportView(allUser_list);
		
		
		addMember_btn.setBounds(406, 240, 92, 23);
		contentPane.add(addMember_btn);
		
		
		thisUser_ta.setBounds(12, 273, 68, 23);
		contentPane.add(thisUser_ta);
		thisUser_ta.setText(client.myID);
		thisUser_ta.setEditable(false);

		
		sendTalk_tf.setBounds(92, 274, 302, 21);
		contentPane.add(sendTalk_tf);
		sendTalk_tf.setColumns(10);
		
		
		send_btn.setBounds(406, 273, 92, 23);
		contentPane.add(send_btn);
		
		this.setVisible(true);
	}
	
	/*활용코드*/
	public TestChattingRoom(TestClient client , String roomname) {
		this.client = client;
		this.roomname = roomname;
		init();
		addActionListener();
		addWindowListener (new WindowAdapter() { 
			public void windowClosing(WindowEvent e) { 
				TestChattingRoom r = (TestChattingRoom)e.getWindow();
				r.client.close_chattingroom(r);
			} });
	

	}
	private void addActionListener() {
		addMember_btn.addActionListener(this);
		send_btn.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==addMember_btn) {
			String user = (String) allUser_list.getSelectedValue();
			for (int i = 0; i < this_room_users.size(); i++) {
				String chk_user = (String)this_room_users.elementAt(i);
				System.out.println(chk_user+" 접속중");
				if (chk_user.equals(user)) {
					JOptionPane.showMessageDialog(null, "이미 접속한 사용자입니다.");
				} else {
					JOptionPane.showMessageDialog(null, user);
					client.send_message("CHATTINGROOM_INVATATION_INTHEROOM/"+client.myID+"/"+user+"/"+roomname);
				}				
			}			
		}else if (e.getSource()== send_btn) {
			String message = sendTalk_tf.getText();
			if(message!="")
				client.send_message("CHATTINGROOM_MESSAGE/"+client.myID+"/"+roomname+"/"+message);
			sendTalk_tf.setText("");
		}
	}
	public void setChattingRoomMember_list() {
		chattingRoomMember_list.setListData(this_room_users);
	}
	public void setAllUser_list(Vector<String> user_list) {
		this.allUser_list.setListData(user_list);
	}
	public void setTalk_ta(String str) {
		this.talk_ta.append(str);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestChattingRoom frame = new TestChattingRoom();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
