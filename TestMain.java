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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.List;
import javax.swing.ListSelectionModel;

public class TestMain extends JFrame implements ActionListener{

//=======class client 자원========
	TestClient client;
	
//=======GUI자원=============
	private JPanel contentPane;
	JTextArea thisUser_ta = new JTextArea();
	JScrollPane scrollPane = new JScrollPane();
	JList users_list = new JList();
	JButton chattingRoom_btn = new JButton("채팅");
	JButton memo_btn = new JButton("쪽지");
	JButton file_btn = new JButton("파일보내기");
	
	/*윈도우 폼에 대한 정의*/	
	public TestMain() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 248, 445);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		thisUser_ta.setEditable(false);
		
		
		thisUser_ta.setBounds(67, 10, 153, 24);
		contentPane.add(thisUser_ta);
		
		
		scrollPane.setBounds(12, 49, 208, 278);
		contentPane.add(scrollPane);
		users_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		scrollPane.setViewportView(users_list);
		chattingRoom_btn.setFont(new Font("굴림", Font.PLAIN, 11));
		
		chattingRoom_btn.setBounds(12, 337, 97, 23);
		contentPane.add(chattingRoom_btn);
		memo_btn.setFont(new Font("굴림", Font.PLAIN, 11));
		
		
		memo_btn.setBounds(123, 337, 97, 23);
		contentPane.add(memo_btn);
		file_btn.setFont(new Font("굴림", Font.PLAIN, 11));
		
		
		file_btn.setBounds(12, 370, 208, 23);
		contentPane.add(file_btn);
		
		JLabel thisUser_lb = new JLabel("사용자");
		thisUser_lb.setBounds(12, 14, 43, 15);
		contentPane.add(thisUser_lb);
		
		this.setVisible(true);
		addActionListener();
		addWindowListener (new WindowAdapter() { 
			public void windowClosing(WindowEvent e) { 
				System.out.println("종료버튼");
				client.close_main();
			}
		});
	}
	
	private void init() {		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 248, 445);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		thisUser_ta.setBounds(67, 10, 153, 24);
		contentPane.add(thisUser_ta);
		thisUser_ta.setEditable(false);
		
		scrollPane.setBounds(12, 49, 208, 278);
		contentPane.add(scrollPane);
		
		scrollPane.setViewportView(users_list);
		
		chattingRoom_btn.setBounds(12, 337, 97, 23);
		contentPane.add(chattingRoom_btn);
		chattingRoom_btn.setFont(new Font("굴림", Font.PLAIN, 11));
		
		
		memo_btn.setBounds(123, 337, 97, 23);
		contentPane.add(memo_btn);
		memo_btn.setFont(new Font("굴림", Font.PLAIN, 11));
		
		
		file_btn.setBounds(12, 370, 208, 23);
		contentPane.add(file_btn);
		file_btn.setFont(new Font("굴림", Font.PLAIN, 11));

		JLabel thisUser_lb = new JLabel("사용자");
		thisUser_lb.setBounds(12, 14, 43, 15);
		contentPane.add(thisUser_lb);
		
		this.setVisible(true);
	}

	/*활용코드*/
	public TestMain(TestClient client){
		this.client = client;
		init();
		addActionListener();
		addWindowListener (new WindowAdapter() { 
			public void windowClosing(WindowEvent e) { 
				client.close_main();
			}
		});
		
	}
	
	private void addActionListener() {
		chattingRoom_btn.addActionListener(this);
		memo_btn.addActionListener(this);
		file_btn.addActionListener(this);		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == chattingRoom_btn ) {
			System.out.println("채팅방만들기 버튼 클릭");
			
			if (users_list.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(null, "client를 선택해 주세요~!");
			}else {
				String user = (String) users_list.getSelectedValue();
				String roomname = client.make_chattingroom_name();
				client.make_chattingroom(roomname);
				client.send_message("CHATTINGROOM_INVATATION/"+client.myID+"/"+user+"/"+roomname);
			}
			
		}else if (e.getSource() == memo_btn) {
			System.out.println("쪽지 보내기 버튼 클릭");
			
			if (users_list.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(null, "client를 선택해 주세요~!");
			}else {
				String user = (String) users_list.getSelectedValue();
				client.make_memo(user,"");	
			}
		}else if (e.getSource() == file_btn) {
			System.out.println("파일보내기 버튼 클릭");
			
			if (users_list.isSelectionEmpty()) {
				JOptionPane.showMessageDialog(null, "client를 선택해 주세요~!");
			}else {
				String user = (String) users_list.getSelectedValue();
				client.transfile_set(user);	
			}
		}
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestMain frame = new TestMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
