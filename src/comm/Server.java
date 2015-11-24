package comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server extends Thread {

	private SensorMonitor monitor;
	private String host;
	private int port;

	public Server(String host, int port, SensorMonitor monitor) {
		this.host = host;
		this.port = port;
		this.monitor = monitor;
	}

	public void run() {
		ServerSocket ssocket;
		try {
			monitor.initMonitor();

			ssocket = new ServerSocket(port, 1, InetAddress.getByName(host));
			monitor.infoMessageReceived("Listening on " + InetAddress.getByName(host) + " on port " + port);

			Socket csocket = ssocket.accept();
			monitor.infoMessageReceived("Accepted connection: " + csocket);

			BufferedReader in = new BufferedReader(new InputStreamReader(csocket.getInputStream()));
			String line = in.readLine();

			while (line != null) {
				monitor.newDataReceived(line);
				line = in.readLine();
			}

			monitor.infoMessageReceived("Finished");
			monitor.stopMonitor();
		} catch (UnknownHostException e) {
			monitor.errorReceived("Unknown host: " + e.getMessage());
		} catch (IOException e) {
			monitor.errorReceived("IO Exception: " + e.getMessage());
		}
	}
}
