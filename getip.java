package newpro;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class getip {

	public static void main(String[] args) {
		try {
			InetAddress address=InetAddress.getLocalHost();
			System.out.println(address.getHostAddress());
			String hostname=address.getHostName();
			System.out.println(hostname);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
