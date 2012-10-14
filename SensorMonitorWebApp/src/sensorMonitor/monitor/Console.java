package sensorMonitor.monitor;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console extends JFrame {
	private JPanel jPanel = new JPanel();
	private JScrollPane jScrollPane = new JScrollPane();
	private JTextArea console = new JTextArea();
	private int listen_port = 26123;
	private static Monitor monitor = null;

	public JTextArea getConsole() {
		return console;
	}

	public static Monitor getMonitor() {
		return monitor;
	}

	public static void setMonitor(Monitor monitor) {
		Console.monitor = monitor;
	}

	/**
	 * Konstruktor
	 */
	public Console() {
		init();
	}

	/**
	 * Inicjalizacja JFrama
	 */
	private void init() {
		this.setTitle("Konsola");
		this.setSize(600, 300);
		console.setBorder(BorderFactory.createLoweredBevelBorder());
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		console.setBackground(new Color(0, 0, 0));
		console.setForeground(new Color(255, 255, 255));
		console.setEditable(false);
		console.setColumns(70);
		console.setRows(30);

		jScrollPane.getViewport().add(console);
		jPanel.add(jScrollPane);
		this.getContentPane().add(jPanel, BorderLayout.WEST);

		setMonitor(new Monitor(listen_port, this));
		new Thread(monitor).start();
	}

	/**
	 * Funkcja wpisuje podany tekst do konsoli
	 * 
	 * @param text
	 *            Tekst do wpisania
	 */
	public void writeToConsole(String text) {
		console.append(text);
	}
}
