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
	static int listen_port = 26123;
	static int monitorsNum = 1;

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
		this.setSize(800, 500);
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

		for (int i = 0; i < monitorsNum; i++) {
			Monitor monitor = new Monitor(listen_port, this);
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
