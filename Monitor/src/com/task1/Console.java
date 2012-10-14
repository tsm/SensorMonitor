package com.task1;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Console extends JFrame {
	JPanel jPanel = new JPanel();
	JScrollPane jScrollPane = new JScrollPane();
	JTextArea console = new JTextArea();
	static int[] listen_ports = { 26123 /* , 80 */};

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

		for (int i = 0; i < listen_ports.length; i++) {
			Monitor monitor = new Monitor(listen_ports[i], this);
			new Thread(monitor).start();
		}
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

	public static void main(String[] args) {
		// uruchomienie Serwera
		Console webserver = new Console();
		webserver.setVisible(true);
	}
}
