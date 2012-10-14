package monitor.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import monitor.core.Console;
import monitor.core.Metrics;
import monitor.core.Monitor;

@WebServlet("/subscriptions")
public class Subscriptions extends HttpServlet {
	private Monitor monitor;

	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		this.monitor = Console.getMonitor();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		StringBuilder sb = new StringBuilder();

		// Przetwarzanie parametrów
		Map<String, String[]> params = req.getParameterMap();
		if (params.entrySet().isEmpty()) {
			// lista subskrypcji
			if (monitor != null) {
				for (Entry<String, Metrics> entry : Monitor.getMetrics()
						.entrySet()) {
					sb.append(entry.getKey().replace("-", ";") + ";");
				}
			}
		} else {
			String clientName = params.get("client")[0];
			String supply = params.get("stock")[0];
			String metrics = params.get("metrics")[0];
			String action = params.get("action")[0];
			if (clientName != null && supply != null && metrics != null
					&& action != null) {

				SocketChannel client = Monitor.getConnectedClients().get(
						clientName);
				if (client != null) {
					if (action.equalsIgnoreCase("subscribe")) {
						sb.append(monitor.subscribeFor(client, supply + ";"
								+ metrics + ";"));
					} else if (action.equalsIgnoreCase("cancel")) {
						sb.append(monitor.unsubscribeFor(client, supply + ";"
								+ metrics + ";"));
					}
				} else {
					sb.append("Podany klient nie istnieje");
				}
			}
		}

		// Wypisywanie
		out.println(sb);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
