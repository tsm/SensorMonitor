package sensorMonitor.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import sensorMonitor.monitor.Console;

public class ServletInitializer extends HttpServlet {

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();

		// uruchomienie konsoli
		Console console = new Console();
		console.setVisible(true);

		System.out.println("****************************************");
		System.out.println("*** Servlet Initialized successfully ***");
		System.out.println("****************************************");

	}
}
