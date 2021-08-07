package newpro;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
public class one {
	public static List<Socket> userMap=new LinkedList<>();
	public static ServerSocket serverSocket;
	public static boolean isRunning=true;
	public static int closeNum=0;
	public static int neiborNum=0;
	public static int port;
	public static int totalNum=0;
	public static int myRoundNum=0;
	public static String myname="0";
	public static String[] lastList=new String[101];
	public static int[] k= {0,100,100,100,100};
	public static int colect=1;
	public static void main(String[] args) {
		System.out.println("this is one");
		lastList[0]=getKList();
		one thisclass=new one();
		filereadwrite.read();
		totalNum=filereadwrite.totalNode;
		neiborNum=filereadwrite.nodeInfo[Integer.parseInt(myname)].neibor.length;
		port=filereadwrite.nodeInfo[Integer.parseInt(myname)].port;
		thisclass.startServer();
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			String[] nerbornum=filereadwrite.nodeInfo[Integer.parseInt(myname)].neibor;
			for(String x:nerbornum) {
				int thisNodeNUm=Integer.parseInt(x);
				String ipString=filereadwrite.nodeInfo[thisNodeNUm].ip;
				int neiborPort=filereadwrite.nodeInfo[thisNodeNUm].port;
				Socket socket=new Socket(ipString,neiborPort);
				userMap.add(socket);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		thisclass.startClient();
	}

	void startServer() {
		new Thread(new one.ServerListerner()).start();
	}

	void startClient() {
		new Thread(new Client()).start();
	}


	class ServerListerner implements Runnable{
		
		@Override
		public void run() {
			try {
				serverSocket=new ServerSocket(port);
				while(isRunning) {
					System.out.println("Server waiting");
					Socket socket=serverSocket.accept();
					new Thread(new Server(socket)).start();
				}
				System.out.println("server closed!");
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	class Server implements Runnable{
		Socket mySocket;
		private DataInputStream reader;
		private DataOutputStream writter;
		private boolean isRunning;
		private String username;

		public Server(Socket socket) {
			this.mySocket=socket;
			try {
				reader=new DataInputStream(mySocket.getInputStream());
				writter=new DataOutputStream(mySocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		public void SendMessage(String message) {
			try {
				writter.writeUTF(message);
				writter.flush();
			} catch (Exception e) {
				StopServer();
			}
		}

		public String GetMessage() {
			String data = "";
			try {
				data = reader.readUTF();
			} catch (Exception e) {
				StopServer();
			}
			return data;
		}

		public void StopServer() {
			try {
				isRunning=false;
				reader.close();
				writter.close();
				mySocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			isRunning=true;
			while (isRunning) {
				if(!isRunning) {
					break;
				}
				String receiveMSG=GetMessage();
				String[] receiveList=receiveMSG.split("&");
				if(receiveList[0].equals("STOP")) {
					System.out.println(receiveMSG);
					synchronized (receiveList) {
						closeNum++;
					}
					break;
				}
				int recNum=Integer.parseInt(receiveList[0]);
				
				if(recNum>myRoundNum && colect<totalNum) {
					System.out.println("server receive:"+receiveMSG+" myround="+myRoundNum+" Send NO");
					SendMessage("NO&"+myname);
				}else {
					System.out.println("server receive:"+receiveMSG+" myround="+myRoundNum+" Send "+(colect<totalNum?lastList[recNum]:getKList()+"&"+myname));
					SendMessage(colect<totalNum?lastList[recNum]:(lastList[recNum]==null || lastList[recNum]=="")?(getKList()+"&"+myname):lastList[recNum]);
				}
			}

			if(!mySocket.isClosed()) {
				try {
					mySocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static synchronized String getKList() {
		String res=k[0]+"&"+k[1]+"&"+k[2]+"&"+k[3]+"&"+k[4];
		return res;
	}

	static synchronized void setKList(String rec) {
		String[] recList=rec.split("&");
		for(int i=0;i<totalNum;i++) {
			if(Integer.parseInt(recList[i])<100 && k[i]==100) {
				k[i]=Integer.parseInt(recList[i])+1;
				colect++;
			}else if(Integer.parseInt(recList[i])<100) {
				k[i]=Math.min(k[i], Integer.parseInt(recList[i])+1);
			}
		}
	}

	class Client implements Runnable{
		public Client() {
			isRunning=true;
		}

		@Override
		public void run() {
			int round=0;
			while (colect<totalNum) {
				System.out.println(round+" "+lastList[round]);
				myRoundNum=round;
				int size=userMap.size();
				Thread[] thread=new Thread[size];
				int count=0;
				while (count<size) {

					Socket socket=userMap.get(count);
					thread[count]=new Thread(new ClientSend(socket));
					thread[count].start();
					count++;
				}
				System.out.println(round+"wait");
				for(int i=0;i<size;i++) {
					try {
						thread[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("next round");
				round++;
				lastList[round]=getKList();
				
			}

			int count=0;
			int size=userMap.size();
			while (count<size) {
				try {
					Socket socket=userMap.get(count);
					new DataOutputStream(socket.getOutputStream()).writeUTF("STOP&"+myname);

				} catch (IOException e) {
					e.printStackTrace();
				}
				count++;
			}
			
			System.out.println("closeNUm="+closeNum+" neiborNum="+neiborNum);
			while (closeNum<neiborNum) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			while (!userMap.isEmpty()) {
				try {
					Socket socket=userMap.get(userMap.size()-1);
					if(!socket.isClosed()) {
						socket.close();
					}
					userMap.remove(userMap.size()-1);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			
			System.out.println("Client closed!"+getKList());
			filereadwrite readFilereadwrite=new filereadwrite();
			readFilereadwrite.write(myname, k);
			userMap=null;

		}
	}

	class ClientSend implements Runnable{
		Socket mySocket;
		private DataInputStream reader;
		private DataOutputStream writter;

		public ClientSend(Socket insocket) {
			this.mySocket=insocket;
			try {
				reader=new DataInputStream(mySocket.getInputStream());
				writter=new DataOutputStream(mySocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void SendMessage(String message) {
			try {
				writter.writeUTF(message);
				writter.flush();
			} catch (Exception e) {
				StopServer();
			}
		}

		public String GetMessage() {
			String data = "";
			try {
				data = reader.readUTF();
			} catch (Exception e) {
				StopServer();
			}
			return data;
		}

		public void StopServer() {
			try {
				reader.close();
				writter.close();
				mySocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String post="Cat 1";
			post=String.valueOf(myRoundNum)+"&"+myname;
			System.out.println("client send round:"+post);
			SendMessage(post);
			String receive=GetMessage();
			String[] receiveList=receive.split("&");
			System.out.println("client receive:"+receive);
			while (receiveList[0].equals("NO")) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("client send round:"+post);
				SendMessage(post);
				receive=GetMessage();
				receiveList=receive.split("&");
				System.out.println("client receive:"+receive);
			}
			setKList(receive);

		}

	}
}
