package newpro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class filereadwrite {
	static int totalNode;
	static struNode[] nodeInfo;
	public static class struNode{
		String name;
		String ip;
		int port;
		String[] neibor;
	}
	public static void read() {
		try {
			String filePath = "/Users/genzhou/Desktop/Java/workspace1/myLeetcode/runoob.txt";
			FileInputStream fin;
			fin = new FileInputStream(filePath);
			InputStreamReader reader = new InputStreamReader(fin);
			BufferedReader buffReader = new BufferedReader(reader);
			String strTmp = "";
			totalNode=Integer.parseInt(buffReader.readLine());
			buffReader.readLine();
			nodeInfo=new struNode[totalNode];
			for(int i=0;i<totalNode;i++) {
				struNode thisNode=new struNode();
				strTmp = buffReader.readLine();
				String[] thisList=strTmp.split(" ");
				thisNode.name=thisList[0];
				thisNode.ip=thisList[1];
				thisNode.port=Integer.parseInt(thisList[2]);
				nodeInfo[i]=thisNode;
				}
			for(int i=0;i<totalNode;i++) {
				strTmp = buffReader.readLine();
				nodeInfo[i].neibor=strTmp.split(" ");
			}
			buffReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(String name,int[] x) {
		try {
			int max=0;
			BufferedWriter out = new BufferedWriter(new FileWriter(name+".txt"));
			for(int i=0;i<x.length;i++) {
				max=Math.max(max, x[i]);
				for(int j=0;j<x.length;j++) {
					if(x[j]==i) {
						out.write(String.valueOf(j)+" ");
						System.out.println(x[j]);

					}
				}
				out.newLine();
			}
			out.write(String.valueOf(max));
			out.close();
			System.out.println("文件创建成功！");
		} catch (IOException e) {
		}
	}

}
