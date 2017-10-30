import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Font;

public class TestMemo extends JDialog implements ActionListener{
//=======class Client 자원========
	TestClient client;

//==========쪽지 자원===================
	String this_user;
	String to_user;
	String message;
		
//===========GUI 자원==================
	private final JPanel contentPanel = new JPanel();
	private JTextArea memo_ta = new JTextArea();
	private JTextField sendMsg_tf = new JTextField();
	private JButton send_btn = new JButton("전송");
	private JButton check_btn = new JButton("확인");
	
	/*윈도우 폼에 대한 정의*/
	public TestMemo() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 10, 410, 208);
			contentPanel.add(scrollPane);
			{
				memo_ta.setEditable(false);
				scrollPane.setViewportView(memo_ta);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(0, 226, 434, 35);
			contentPanel.add(buttonPane);
			buttonPane.setLayout(null);
			{
				
				sendMsg_tf.setBounds(12, 6, 285, 21);
				buttonPane.add(sendMsg_tf);
				sendMsg_tf.setColumns(10);
			}
			{
				send_btn.setFont(new Font("굴림", Font.PLAIN, 11));
				
				send_btn.setBounds(302, 5, 57, 23);
				send_btn.setActionCommand("OK");
				buttonPane.add(send_btn);
				getRootPane().setDefaultButton(send_btn);
			}
			{
				check_btn.setFont(new Font("굴림", Font.PLAIN, 11));
				
				check_btn.setBounds(371, 5, 58, 23);
				check_btn.setActionCommand("Cancel");
				buttonPane.add(check_btn);
			}
		}
	}

	private void init() {
		setTitle(to_user+"의 쪽지");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(12, 10, 410, 208);
			contentPanel.add(scrollPane);
			{
				memo_ta.setEditable(false);
				scrollPane.setViewportView(memo_ta);
				memo_ta.setText(message);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(0, 226, 434, 35);
			contentPanel.add(buttonPane);
			buttonPane.setLayout(null);
			{
				
				sendMsg_tf.setBounds(12, 6, 285, 21);
				buttonPane.add(sendMsg_tf);
				sendMsg_tf.setColumns(10);
			}
			{
				send_btn.setFont(new Font("굴림", Font.PLAIN, 11));
				
				send_btn.setBounds(302, 5, 57, 23);
				send_btn.setActionCommand("OK");
				buttonPane.add(send_btn);
				getRootPane().setDefaultButton(send_btn);
			}
			{
				check_btn.setFont(new Font("굴림", Font.PLAIN, 11));
				
				check_btn.setBounds(371, 5, 58, 23);
				check_btn.setActionCommand("Cancel");
				buttonPane.add(check_btn);
			}
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	/*활용코드*/
	public TestMemo(TestClient client ,String this_user ,String user,String message) {
		this.client = client;
		this.this_user = this_user;
		this.to_user = user;
		this.message = message;
		init();
		addActionListener();addWindowListener (new WindowAdapter() { 
			public void windowClosing(WindowEvent e) { 
				TestMemo m = (TestMemo)e.getWindow();
				m.client.close_memo(m);
			} });
	}
	
	private void addActionListener() {
		send_btn.addActionListener(this);
		check_btn.addActionListener(this);
	}
		
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==send_btn) {
			String message = sendMsg_tf.getText();
			client.send_message("MEMO_PASS/"+this_user+"/"+to_user+"/"+message);
			client.close_memo(this);
		}else if (e.getSource()==check_btn) {
			client.close_memo(this);
		}
	}

	public static void main(String[] args) {
		try {
			TestMemo dialog = new TestMemo();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
