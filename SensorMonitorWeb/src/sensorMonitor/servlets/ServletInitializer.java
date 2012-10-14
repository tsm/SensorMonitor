package sensorMonitor.servlets;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import sensorMonitor.monitor.Console;

public class ServletInitializer extends HttpServlet {

	private int port = 26123;

	@Override
	public void init() throws ServletException {
		super.init();

		try {
			InetAddress address = InetAddress.getLocalHost();
			System.out.println(address.getHostAddress());
			// uruchomienie konsoli
			Console console = new Console(address.getHostAddress(), port);
			console.setVisible(true);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		System.out.println("****************************************");
		System.out.println("*** Servlet Initialized successfully ***");
		System.out.println("****************************************");

	}
}
