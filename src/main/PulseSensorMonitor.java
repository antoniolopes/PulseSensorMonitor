package main;

import gui.GUI;

import javax.swing.UnsupportedLookAndFeelException;

import comm.Server;

public class PulseSensorMonitor {

	private static final String HOST = "192.168.1.66";
	private static final int PORT = 9999;

	private GUI gui;
	private Server server;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		new PulseSensorMonitor().execute();
	}

	private void execute() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		gui = new GUI();
		gui.init();
		server = new Server(HOST, PORT, gui);
		server.start();
	}

}
