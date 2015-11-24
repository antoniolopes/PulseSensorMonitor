package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import comm.SensorMonitor;

import data.CurrentDate;
import data.PulseData;

@SuppressWarnings("serial")
public class GUI extends JFrame implements SensorMonitor {

	private static final int INITIAL_WIDTH = 800;
	private static final int INITIAL_HEIGHT = 600;

	// Constants for GUI management
	private static final int LEFT_MARGIN = 1;
	private static final int TOP_MARGIN = 23;
	private static final int GRAPH_TOP_MARGIN = 50;
	private static final int BOTTOM_MARGIN = 1;
	private static final int RIGHT_MARGIN = 1;
	private static final int TEXT_POSITION = 30;
	private static final int TEXT_BOTTOM_MARGIN = 10;
	private static final int TEXT_SEPARATOR = 200;

	private static final Color BACKGROUND_COLOR = new Color(60, 60, 60);
	private static final Color TEXT_COLOR = new Color(240, 240, 240);
	private static final Color DATA_COLOR = new Color(250, 50, 50);
	private static final Color THRESH_COLOR = new Color(50, 150, 250);

	// Number of samples displayed on the graph (Last ones received, old ones fade away to the right of the GUI graph)
	private static final int BUFFER_SIZE = 50;

	// Thresholds for pulse sensing
	private static final int MAX_VALUE = 1024;
	private static final int MIN_VALUE = 0;

	// Current Date
	private CurrentDate curr_date = new CurrentDate();
	// Time elapsed counter
	private long starting_time = 0;
	// Pulse data samples
	private PulseData pulse_data = new PulseData();

	public GUI() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		this.setTitle("Pulse Sensor Monitor");
		this.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Identify if on Mac OS X and act accordingly
		String lcOSName = System.getProperty("os.name").toLowerCase();
		if (lcOSName.startsWith("mac os x")) {
			// Place the menu on the top bar instead of the application top
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			// Change the title of the menu. FIXME: not working, don't know why
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TerraSentes");
		}

		// Use the operating system's look and feel
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		// Create the menu bar
		JMenuBar menu_bar;
		menu_bar = new JMenuBar();
		JMenu menu;

		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription("File Menu");
		menu_bar.add(menu);

		JMenuItem menu_item;
		menu_item = new JMenuItem("Save to file...", KeyEvent.VK_T);
		menu_item.getAccessibleContext().setAccessibleDescription("Saves all collected data to file");
		menu.add(menu_item);

		// Action listener for the "Save to file..." menu option
		menu_item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();

				// Get current date to suggest a name for the file
				curr_date.refresh();

				// Suggest file name based on date and time
				fc.setSelectedFile(new File(curr_date.shortFormat() + "_samples_" + pulse_data.getNrSamples() + "_output.ser"));

				// Show Save dialog
				int returnVal = fc.showSaveDialog(GUI.this);

				// Save file
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					System.out.println("Saving to file: " + fc.getSelectedFile().getAbsolutePath());
					FileOutputStream fos = null;
					ObjectOutputStream out = null;
					try {
						fos = new FileOutputStream(fc.getSelectedFile().getAbsolutePath());
						out = new ObjectOutputStream(fos);

						synchronized (pulse_data) {
							out.writeObject(pulse_data);
						}

						out.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		menu_bar.add(menu);
		this.setJMenuBar(menu_bar);
	}

	@Override
	public void initMonitor() {
		System.out.println("Starting...");
	}

	@Override
	public void newDataReceived(String data) {
		if (starting_time == 0)
			starting_time = System.currentTimeMillis();
		// System.out.println("New data received: " + data);
		pulse_data.addSample(Integer.parseInt(data), System.currentTimeMillis());
		this.repaint();
	}

	@Override
	public void stopMonitor() {
		JOptionPane.showMessageDialog(this, "Sensor stopped sending data", "Warning", JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void errorReceived(String error) {
		JOptionPane.showMessageDialog(this, error, "Unexpected Error", JOptionPane.ERROR_MESSAGE);
	}

	public void init() {
		this.setVisible(true);
	}

	@Override
	public void infoMessageReceived(String message) {
		System.out.println("Info message: " + message);
	}

	public void paint(Graphics graphics) {
		super.paint(graphics);
		Graphics2D g = (Graphics2D) graphics;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Calculate the height of the main area
		int monitor_rect_height = (this.getHeight() - GRAPH_TOP_MARGIN - BOTTOM_MARGIN);

		// Draw the top rectangle
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(LEFT_MARGIN + 1, TOP_MARGIN + 1, this.getWidth() - (LEFT_MARGIN + 1) - (RIGHT_MARGIN + 1), GRAPH_TOP_MARGIN - TOP_MARGIN - 1);

		// Draw info on the top rectangle
		drawTopInfo(g);

		// Draw the main area rectangle
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(LEFT_MARGIN + 1, GRAPH_TOP_MARGIN + 1, this.getWidth() - (LEFT_MARGIN + 1) - (RIGHT_MARGIN + 1), monitor_rect_height - 1);

		// Only show graphs if more than 1 sample exists
		if (pulse_data.getNrSamples() > 1) {
			// Draw a line to separate the text from the graphs
			g.setColor(TEXT_COLOR);
			g.drawLine(TEXT_SEPARATOR, TOP_MARGIN, TEXT_SEPARATOR, getHeight());
			drawRawDataGraph(g, monitor_rect_height, MAX_VALUE, MIN_VALUE);
		} else { // If no data available, show a loading message instead
			drawLoadingText(g, "Waiting for data to be sent from the sensor...");
		}
	}

	private void drawTopInfo(Graphics2D g) {
		curr_date.refresh();
		int samples_per_second = 0;
		if (((int) ((System.currentTimeMillis() - starting_time) / 1000)) != 0)
			samples_per_second = pulse_data.getNrSamples() / ((int) ((System.currentTimeMillis() - starting_time) / 1000));
		String msg = "Time: " + curr_date + "       Sample buffer size: " + BUFFER_SIZE + "  |  # samples: " + pulse_data.getNrSamples() + "  |  # samples/sec: " + samples_per_second;
		g.setColor(TEXT_COLOR);
		g.drawChars(msg.toCharArray(), 0, msg.length(), TEXT_POSITION - 10, (TOP_MARGIN * 2) - 5);
	}

	private void drawRawDataGraph(Graphics2D g, int rect_height, int max, int min) {
		int lower_limit = 0;
		int upper_limit = pulse_data.getNrSamples() - 1;

		// Determine the limits of the raw data to use depending on the BUFFER_SIZE
		if (pulse_data.getNrSamples() > BUFFER_SIZE)
			// There are enough values to show on the graph
			lower_limit = pulse_data.getNrSamples() - BUFFER_SIZE;
		else
			// There aren't enough values to show on the graph
			lower_limit = 0;

		// Draw threshold line
		g.setColor(THRESH_COLOR);
		g.drawLine(TEXT_SEPARATOR, (rect_height + GRAPH_TOP_MARGIN) - map(PulseData.THRESH, min, max, 2, rect_height - 2), getWidth(), (rect_height + GRAPH_TOP_MARGIN) - map(PulseData.THRESH, min, max, 2, rect_height - 2));

		// Draw graph text
		drawDataGraphText(g, pulse_data.getBPM(starting_time), pulse_data.getLastDataSampleValue(), pulse_data.getMaxDataSample(), pulse_data.getMinDataSample(), TEXT_POSITION, GRAPH_TOP_MARGIN + rect_height, rect_height);

		for (int i = lower_limit; i != upper_limit - 1; i++) {
			// Determing horizontal position
			int x1 = TEXT_SEPARATOR + (getWidth() - map(i, lower_limit, upper_limit, TEXT_SEPARATOR + (RIGHT_MARGIN * 3), getWidth() - LEFT_MARGIN));
			int x2 = TEXT_SEPARATOR + (getWidth() - map(i + 1, lower_limit, upper_limit, TEXT_SEPARATOR + (RIGHT_MARGIN * 3), getWidth() - LEFT_MARGIN));

			// Drawing X Axis
			g.setColor(DATA_COLOR);
			int y1 = (rect_height + GRAPH_TOP_MARGIN) - map(pulse_data.getDataSample(i), min, max, 2, rect_height - 2);
			int y2 = (rect_height + GRAPH_TOP_MARGIN) - map(pulse_data.getDataSample(i + 1), min, max, 2, rect_height - 2);
			g.drawLine(x1, y1, x2, y2);
		}
	}

	private void drawDataGraphText(Graphics2D g, int bpm, int current, int max, int min, int text_left_margin, int text_base, int rect_height) {
		g.setColor(TEXT_COLOR);
		String curr = "" + current;
		//String max_s = "" + max;
		//String min_s = "" + min;
		g.drawChars(("Beats per min: " + bpm).toCharArray(), 0, ("Beats per min: " + bpm).length(), text_left_margin, text_base - (rect_height / 4 * 3) - TEXT_BOTTOM_MARGIN);
		g.drawChars(("Current value: " + curr).toCharArray(), 0, ("Current value: " + curr).length(), text_left_margin, text_base - (rect_height / 5 * 3) - TEXT_BOTTOM_MARGIN);
		//g.drawChars(("Maximum value: " + max_s).toCharArray(), 0, ("Maximum value: " + max_s).length(), text_left_margin, text_base - (rect_height / 5 * 2) - TEXT_BOTTOM_MARGIN);
		//g.drawChars(("Minimum value: " + min_s).toCharArray(), 0, ("Minimum value: " + min_s).length(), text_left_margin, text_base - (rect_height / 5) - TEXT_BOTTOM_MARGIN);
	}

	private void drawLoadingText(Graphics2D g, String loading_msg) {
		g.setColor(TEXT_COLOR);
		g.drawChars(loading_msg.toCharArray(), 0, loading_msg.length(), TEXT_POSITION, GRAPH_TOP_MARGIN * 2);
	}

	/*
	 * Mapping function: it maps "value" from a range of [in_min, in_max] to a range of [out_min, out_max]
	 */
	private int map(int value, int in_min, int in_max, int out_min, int out_max) {
		int div = (in_max - in_min);
		if (div == 0)
			div = 1;
		return ((value - in_min) * (out_max - out_min) / div) + out_min;
	}
}
