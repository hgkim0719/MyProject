import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

public class FileTransmission {

/*������ �ۼ����� �����ϰ� �ϱ� ���� �޼��� �ۼ��� ������Ʈ, ���� �۽� ���� ��Ʈ, ���� ���� ���� ��Ʈ�� ������ ����Ѵ�.*/	
	
//	================class chatserver �ڿ�================
	ChatServer server;
		
//	==================���� ���� ���� ��Ʈ��ũ �ڿ�=========================		
	private ServerSocket sendfile_server_socket;
	private Socket sendfile_client_socket;
	private int sendfile_port=7777;
	
//	==================���� �۽� ���� ��Ʈ��ũ �ڿ�=========================
	private ServerSocket recievefile_server_socket;
	private Socket recievefile_client_socket;
	private int recievefile_port=7778;
	
// 	==================�� ����Ʈ �ڿ�==================================	
	Vector<SendFile_UserIofo> sendfile_user_vc = new Vector<>();
	Vector<RecieveFile_UserInfo> recievefile_user_vc = new Vector<>();
	
//	==================��� �޼���====================================		
	public FileTransmission(ChatServer main_server) {
		this.server=main_server;
		server_start();
	}
		
	private void server_start() {
		try {
			sendfile_server_socket = new ServerSocket(sendfile_port);
			recievefile_server_socket = new ServerSocket(recievefile_port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "�̹� ������� ��Ʈ","�˸�",JOptionPane.ERROR_MESSAGE);
		}// 
		
		if(sendfile_server_socket!=null)
		{
			sendfile_connection();
		}
		if(recievefile_server_socket!=null)
		{
			recievefile_connection();
		}

	}
	
	private void sendfile_connection() {// accept �κ��� thread�� �����Ͽ� Ŭ���̾�Ʈ ������ ���� ��� �Ѵ�. 
		
		Thread th = new Thread(new Runnable() {
			public void run() {
				
				while(true) {
					try {
						System.out.println("sendfile ���� ���� ���� ����� ���� �����");
						sendfile_client_socket = sendfile_server_socket.accept();//����� ���� ���� ���
						System.out.println("sendfile ���� ���� ���� ����� ����!");
							SendFile_UserIofo user = new SendFile_UserIofo(sendfile_client_socket);
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

	private void recievefile_connection() {// accept �κ��� thread�� �����Ͽ� Ŭ���̾�Ʈ ������ ���� ��� �Ѵ�.
		Thread th = new Thread(new Runnable() {
			public void run() {
				
				while(true) {
					try {
						System.out.println("recievefile ���� ���� ���� ����� ���� �����");
						recievefile_client_socket = recievefile_server_socket.accept();//����� ���� ���� ���
						System.out.println("recievefile ���� ���� ���� ����� ����!");
							RecieveFile_UserInfo user = new RecieveFile_UserInfo(recievefile_client_socket);
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
	
	class SendFile_UserIofo extends Thread{
//		=========��Ʈ��ũ �ڿ�===============
		private Socket user_socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private FileOutputStream fos;
//		=========�� �ۿ� �ڿ� ===============
		private String filedir = "C:\\Users\\singi\\Desktop\\server\\";//����ȭ�鿡 �ִ� server������ ���� ���� ������ ����.
		private String filename_at_server;
		File target_file;//�ٸ� Ŭ������ �����ؾ� �ϱ� ������ �����ڸ� defualt�� �Ѵ�.
		
		
		public SendFile_UserIofo(Socket socket) {
			this.user_socket = socket;
			try {
				dis = new DataInputStream(user_socket.getInputStream());
				dos = new DataOutputStream(user_socket.getOutputStream());
				
				filename_at_server = dis.readUTF();//this_user@to_user@filename
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
			}
			
		}
		public void run() {
			try {
				target_file = new File(filedir+filename_at_server);
				fos = new FileOutputStream(target_file);
				int len;
	            byte[] buffer = new byte[1024];
	            while ((len = dis.read(buffer)) != -1) {
	                fos.write(buffer);
	            }
	            
	            System.out.println(target_file.getName()+"�� ���������� �Ϸ� �Ǿ����ϴ�.");
	            dos.close();
	            dis.close();
	            fos.close();
	            user_socket.close();				
			}catch (IOException e) {
				JOptionPane.showMessageDialog(null, "socket ������ ��Ŵ","�˸�",JOptionPane.ERROR_MESSAGE);
				target_file.delete();
			}
		}
		
	}
	
	class RecieveFile_UserInfo extends Thread{

//		=========��Ʈ��ũ �ڿ�===============
		private Socket user_socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private FileInputStream fis;
//		=========�� �ۿ� �ڿ� ===============
		private String filedir = "C:\\Users\\singi\\Desktop\\server\\";
		private String filename_at_server;
		File target_file;
		
		
		public RecieveFile_UserInfo(Socket socket) {
			this.user_socket = socket;
			try {
				dis = new DataInputStream(user_socket.getInputStream());
				dos = new DataOutputStream(user_socket.getOutputStream());
				
				filename_at_server = dis.readUTF();//this_user@to_user@filename
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream���� ����","�˸�",JOptionPane.ERROR_MESSAGE);
			}
			
		}
		public void run() {
			try {
	    		target_file = new File(filedir+filename_at_server);
	    		fis = new FileInputStream(target_file);
	    		int len=0;
	    		byte[] buffer = new byte[1024];
	    		while ((len = fis.read(buffer)) != -1) {
	                dos.write(buffer);
	            }
	    		
	    		dos.close();
				fis.close();
				user_socket.close();
				System.out.println("�Ϸ�");
				if(target_file.exists())
			    {
			     boolean deleteFlag = target_file.delete();
			      
			     if(deleteFlag)
			      System.out.println("���ϻ��� ������");
			     else
			      System.out.println("���� ���� ������");
			    }else{
			     System.out.println("������ �������� �ʽ��ϴ�.");
			    }
			}catch (IOException e) {
				JOptionPane.showMessageDialog(null, "socket ������ ��Ŵ","�˸�",JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
