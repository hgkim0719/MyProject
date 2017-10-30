import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class TestLogin extends JFrame implements ActionListener {

//==========class Client 자원========
	TestClient client;
	
	
//=============GUI자원=============
	private JPanel contentPane;
	private JTextField ip_tf;
	private JTextField port_tf;
	private JTextField id_tf;
	private JPasswordField pw_pf;
	private JButton start_btn = new JButton("START");

	/*윈도우 폼에 대한 정의*/
	public TestLogin() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 244, 265);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel server_lb = new JLabel("Server IP");
		server_lb.setHorizontalAlignment(SwingConstants.CENTER);
		server_lb.setBounds(12, 46, 57, 15);
		contentPane.add(server_lb);
		
		ip_tf = new JTextField();
		ip_tf.setBounds(81, 43, 135, 21);
		contentPane.add(ip_tf);
		ip_tf.setColumns(10);
		
		JLabel serverPort_lb = new JLabel("Port");
		serverPort_lb.setHorizontalAlignment(SwingConstants.CENTER);
		serverPort_lb.setBounds(12, 74, 57, 15);
		contentPane.add(serverPort_lb);
		
		port_tf = new JTextField();
		port_tf.setColumns(10);
		port_tf.setBounds(81, 71, 135, 21);
		contentPane.add(port_tf);
		
		JLabel id_lb = new JLabel("ID");
		id_lb.setHorizontalAlignment(SwingConstants.CENTER);
		id_lb.setBounds(12, 105, 57, 15);
		contentPane.add(id_lb);
		
		id_tf = new JTextField();
		id_tf.setColumns(10);
		id_tf.setBounds(81, 102, 135, 21);
		contentPane.add(id_tf);
		
		JLabel pw_lb = new JLabel("PW");
		pw_lb.setHorizontalAlignment(SwingConstants.CENTER);
		pw_lb.setBounds(12, 136, 57, 15);
		contentPane.add(pw_lb);
		
		pw_pf = new JPasswordField();
		pw_pf.setEchoChar('*');
		pw_pf.setColumns(10);
		pw_pf.setBounds(81, 133, 135, 21);
		contentPane.add(pw_pf);
		
		start_btn.setBounds(12, 182, 204, 23);
		contentPane.add(start_btn);
		
		this.setVisible(true);
	}
	
	private void init() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 244, 265);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel server_lb = new JLabel("Server IP");
		server_lb.setHorizontalAlignment(SwingConstants.CENTER);
		server_lb.setBounds(12, 46, 57, 15);
		contentPane.add(server_lb);
		
		ip_tf = new JTextField();
		ip_tf.setBounds(81, 43, 135, 21);
		contentPane.add(ip_tf);
		ip_tf.setColumns(10);
		
		JLabel serverPort_lb = new JLabel("Port");
		serverPort_lb.setHorizontalAlignment(SwingConstants.CENTER);
		serverPort_lb.setBounds(12, 74, 57, 15);
		contentPane.add(serverPort_lb);
		
		port_tf = new JTextField();
		port_tf.setColumns(10);
		port_tf.setBounds(81, 71, 135, 21);
		contentPane.add(port_tf);
		
		JLabel id_lb = new JLabel("ID");
		id_lb.setHorizontalAlignment(SwingConstants.CENTER);
		id_lb.setBounds(12, 105, 57, 15);
		contentPane.add(id_lb);
		
		id_tf = new JTextField();
		id_tf.setColumns(10);
		id_tf.setBounds(81, 102, 135, 21);
		contentPane.add(id_tf);
		
		JLabel pw_lb = new JLabel("PW");
		pw_lb.setHorizontalAlignment(SwingConstants.CENTER);
		pw_lb.setBounds(12, 136, 57, 15);
		contentPane.add(pw_lb);
		
		pw_pf = new JPasswordField();
		pw_pf.setEchoChar('*');
		pw_pf.setColumns(10);
		pw_pf.setBounds(81, 133, 135, 21);
		contentPane.add(pw_pf);
		
		
		start_btn.setBounds(12, 182, 204, 23);
		contentPane.add(start_btn);
		
		this.setVisible(true);
	}

	/*활용 코드*/
	public TestLogin(TestClient client) {
		this.client = client;
		init();
		addActionListener();
		
	}
	
	private void addActionListener() {
		start_btn.addActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==start_btn) {
			String pw_str = new String(pw_pf.getPassword());
			client.setBase(ip_tf.getText(), Integer.valueOf(port_tf.getText()),
							id_tf.getText(), pw_str );
		}
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TestLogin frame = new TestLogin();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
