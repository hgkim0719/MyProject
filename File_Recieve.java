import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;


public class File_Recieve extends JFrame implements ActionListener {
	//===========�ٸ� class �ڿ�===============
		TestClient client;

	//===============GUIȯ��==================
		private JPanel contentPane;
		private JTextArea filepath_ta = new JTextArea();
		private JProgressBar trans_progress_prg = new JProgressBar();
		private JButton file_select_btn = new JButton("ã��");
		private JButton trans_btn = new JButton("����");
		
	//==============�� �ۿ� �ڿ�================
		Socket socket;
		Filereciever fr;
		String target_user;
		FileDialog fileopen;
		int all_size;
		String filename;
		String filepath;
	    String file_type;

	
	public File_Recieve() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 335, 199);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel file_select_lb = new JLabel("�����������");
		file_select_lb.setForeground(Color.BLACK);
		file_select_lb.setHorizontalAlignment(SwingConstants.CENTER);
		file_select_lb.setBounds(12, 13, 57, 15);
		contentPane.add(file_select_lb);
		
		
		filepath_ta.setEditable(false);
		filepath_ta.setBounds(12, 38, 202, 24);
		contentPane.add(filepath_ta);
		
		
		file_select_btn.setBounds(226, 38, 77, 23);
		contentPane.add(file_select_btn);
		
		JLabel progress_lb = new JLabel("���ۻ���");
		progress_lb.setHorizontalAlignment(SwingConstants.CENTER);
		progress_lb.setBounds(12, 84, 57, 15);
		contentPane.add(progress_lb);
		
		
		trans_progress_prg.setBounds(12, 109, 202, 30);
		contentPane.add(trans_progress_prg);
		
		
		trans_btn.setBounds(226, 112, 77, 23);
		contentPane.add(trans_btn);
	}
	
	private void init() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 335, 199);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel file_select_lb = new JLabel("�����������");
		file_select_lb.setForeground(Color.BLACK);
		file_select_lb.setHorizontalAlignment(SwingConstants.CENTER);
		file_select_lb.setBounds(12, 13, 57, 15);
		contentPane.add(file_select_lb);
		
		
		filepath_ta.setEditable(false);
		filepath_ta.setBounds(12, 38, 202, 24);
		contentPane.add(filepath_ta);
		
		
		file_select_btn.setBounds(226, 38, 77, 23);
		contentPane.add(file_select_btn);
		
		JLabel progress_lb = new JLabel("���ۻ���");
		progress_lb.setHorizontalAlignment(SwingConstants.CENTER);
		progress_lb.setBounds(12, 84, 57, 15);
		contentPane.add(progress_lb);
		
		
		trans_progress_prg.setBounds(12, 109, 202, 30);
		contentPane.add(trans_progress_prg);
		
		
		trans_btn.setBounds(226, 112, 77, 23);
		contentPane.add(trans_btn);
		this.setVisible(true);
	}
	
	
	public File_Recieve(TestClient client,String filename,int all_size) {
		this.client = client;
		this.filename = filename;
		String[] temp_str = filename.split("\\.");
		int length = temp_str.length;
		System.out.println("temp_str�� length�� "+length+"�Դϴ�.");
		
		if(temp_str!=null)
			file_type = temp_str[temp_str.length-1];
		this.all_size = all_size;
				
		init();
		addActionListener();
		addWindowListener (new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				if (fr.isAlive()) {
					JOptionPane.showMessageDialog(null, "���� ������ ���� ���Դϴ�.");
				} else {
					if (socket!=null) {
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					File_Recieve this_fr = (File_Recieve)e.getWindow();
					this_fr.client.close_file_recieve(this_fr);
				}
			}
		});
	}
	private void addActionListener() {
		file_select_btn.addActionListener(this);
		trans_btn.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==file_select_btn) {
			
			select_file_showFrame();
			filepath_ta.setText(filepath);
			
		}else if (e.getSource()==trans_btn) {
			if (filepath.equals("")&&filepath==null) {
				JOptionPane.showMessageDialog(null, "������ �����ϼ���", "���", JOptionPane.WARNING_MESSAGE);
			} else {
				try{
					//FileTransmission Server�� sendfile_connection�� ����
					socket = new Socket(client.server_ip, 7778); //sendfile_connection�� ��Ʈ ��ȣ�� 7777
					System.out.println("������ ����Ǿ����ϴ�.");
					fr = new Filereciever(this,socket,filename,filepath,all_size);
					fr.start();
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(null, "file server ���� ����","�˸�",JOptionPane.ERROR_MESSAGE);		        }
			}
		}
		
	}
	
	private void select_file_showFrame(){
		fileopen = new FileDialog(this, "��������", FileDialog.SAVE);	//������
		fileopen.setVisible(true);
		
		String save_filename = fileopen.getFile();
		String filedir = fileopen.getDirectory();

		filepath = filedir+"\\"+save_filename+"."+file_type;//���ϰ�ο� �����̸��� ���� �з��Ѵ�.
	}

	class Filereciever extends Thread{
		File_Recieve fr;
		Socket socket;
		DataInputStream dis;
	    DataOutputStream dos;
	    FileOutputStream fos;
	    String filename;
	    String filepath;
	    File target_file;
	    int all_size;
	    public Filereciever(File_Recieve fr,Socket socket,String filename,String filepath,int all_size) {
	    	this.fr = fr;
	        this.socket = socket;
	        this.filename = filename;
	        this.filepath = filepath;
	        this.all_size = all_size;
	        try {
	        	// ������ ���ۿ� ��Ʈ�� ����
	        	dis = new DataInputStream(socket.getInputStream());
	            dos = new DataOutputStream(socket.getOutputStream());
	        	dos.writeUTF(filename);//������ ���� �����̸��� ������.
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public void run() {
	    	try {
				target_file = new File(filepath);
				fos = new FileOutputStream(target_file);
				trans_progress_prg.setMaximum(all_size);
				int len;
				int size = 0;
	            byte[] buffer = new byte[1024];
	            while ((len = dis.read(buffer)) != -1) {
	            	size+=len;//1024�� ��� ���Ѵ�.
	                fos.write(buffer);
	                trans_progress_prg.setValue(size);
	            }
	            if( size >= (int)all_size ){
	    			JOptionPane.showMessageDialog(null, "���������� �Ϸ� �Ǿ����ϴ�.");
	    		}
	            dos.close();
	            dis.close();
	            fos.close();
	            socket.close();
	            client.close_file_recieve(fr);
	        }catch (IOException e) {
	        	JOptionPane.showMessageDialog(null, "�������� ���� ������ ������ϴ�.");
	        }
	    }
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					File_Recieve frame = new File_Recieve();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


}
