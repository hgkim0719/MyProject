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

/*데이터 송수신을 월할하게 하기 위해 메세지 송수신 전용포트, 파일 송신 전용 포트, 파일 수신 전용 포트로 나누어 통신한다.*/	
	
//	================class chatserver 자원================
	ChatServer server;
		
//	==================파일 수신 전용 네트워크 자원=========================		
	private ServerSocket sendfile_server_socket;
	private Socket sendfile_client_socket;
	private int sendfile_port=7777;
	
//	==================파일 송신 전용 네트워크 자원=========================
	private ServerSocket recievefile_server_socket;
	private Socket recievefile_client_socket;
	private int recievefile_port=7778;
	
// 	==================각 리스트 자원==================================	
	Vector<SendFile_UserIofo> sendfile_user_vc = new Vector<>();
	Vector<RecieveFile_UserInfo> recievefile_user_vc = new Vector<>();
	
//	==================멤버 메서드====================================		
	public FileTransmission(ChatServer main_server) {
		this.server=main_server;
		server_start();
	}
		
	private void server_start() {
		try {
			sendfile_server_socket = new ServerSocket(sendfile_port);
			recievefile_server_socket = new ServerSocket(recievefile_port);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "이미 사용중인 포트","알림",JOptionPane.ERROR_MESSAGE);
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
	
	private void sendfile_connection() {// accept 부분을 thread로 생성하여 클라이언트 접속을 무한 대기 한다. 
		
		Thread th = new Thread(new Runnable() {
			public void run() {
				
				while(true) {
					try {
						System.out.println("sendfile 파일 전송 서버 사용자 접속 대기중");
						sendfile_client_socket = sendfile_server_socket.accept();//사용자 접속 무한 대기
						System.out.println("sendfile 파일 전송 서버 사용자 접속!");
							SendFile_UserIofo user = new SendFile_UserIofo(sendfile_client_socket);
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

	private void recievefile_connection() {// accept 부분을 thread로 생성하여 클라이언트 접속을 무한 대기 한다.
		Thread th = new Thread(new Runnable() {
			public void run() {
				
				while(true) {
					try {
						System.out.println("recievefile 파일 전송 서버 사용자 접속 대기중");
						recievefile_client_socket = recievefile_server_socket.accept();//사용자 접속 무한 대기
						System.out.println("recievefile 파일 전송 서버 사용자 접속!");
							RecieveFile_UserInfo user = new RecieveFile_UserInfo(recievefile_client_socket);
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
	
	class SendFile_UserIofo extends Thread{
//		=========네트워크 자원===============
		private Socket user_socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private FileOutputStream fos;
//		=========그 밖에 자원 ===============
		private String filedir = "C:\\Users\\singi\\Desktop\\server\\";//바탕화면에 있는 server폴더를 서버 전용 폴더로 쓴다.
		private String filename_at_server;
		File target_file;//다른 클레스도 접근해야 하기 때문에 접근자를 defualt로 한다.
		
		
		public SendFile_UserIofo(Socket socket) {
			this.user_socket = socket;
			try {
				dis = new DataInputStream(user_socket.getInputStream());
				dos = new DataOutputStream(user_socket.getOutputStream());
				
				filename_at_server = dis.readUTF();//this_user@to_user@filename
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Stream설정 에러","알림",JOptionPane.ERROR_MESSAGE);
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
	            
	            System.out.println(target_file.getName()+"의 파일전송이 완료 되었습니다.");
	            dos.close();
	            dis.close();
	            fos.close();
	            user_socket.close();				
			}catch (IOException e) {
				JOptionPane.showMessageDialog(null, "socket 연결이 끊킴","알림",JOptionPane.ERROR_MESSAGE);
				target_file.delete();
			}
		}
		
	}
	
	class RecieveFile_UserInfo extends Thread{

//		=========네트워크 자원===============
		private Socket user_socket;
		private DataInputStream dis;
		private DataOutputStream dos;
		private FileInputStream fis;
//		=========그 밖에 자원 ===============
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
				JOptionPane.showMessageDialog(null, "Stream설정 에러","알림",JOptionPane.ERROR_MESSAGE);
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
				System.out.println("완료");
				if(target_file.exists())
			    {
			     boolean deleteFlag = target_file.delete();
			      
			     if(deleteFlag)
			      System.out.println("파일삭제 성공함");
			     else
			      System.out.println("파일 삭제 실패함");
			    }else{
			     System.out.println("파일이 존재하지 않습니다.");
			    }
			}catch (IOException e) {
				JOptionPane.showMessageDialog(null, "socket 연결이 끊킴","알림",JOptionPane.ERROR_MESSAGE);
			}
		}
	}

}
