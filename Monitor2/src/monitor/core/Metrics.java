package monitor.core;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Metrics {

	// Lista klientow
	private List<SocketChannel> clients;

	// Nazwa zasobu
	private String name;

	// Sensor przesylający metryke
	private SocketChannel sensor;

	// Wartosc metryki
	private String value;

	public Metrics(String name, SocketChannel sensor, String value) {
		this.name = name;
		this.sensor = sensor;
		this.value = value;
		clients = new ArrayList<SocketChannel>();
	}

	public void addClient(SocketChannel client) {
		clients.add(client);
	}

	public void removeClient(SocketChannel client) {
		clients.remove(client);
	}

	public List<SocketChannel> getClients() {
		return clients;
	}

	public String getName() {
		return name;
	}

	public SocketChannel getSensor() {
		return sensor;
	}

	public String getValue() {
		return value;
	}

	public void setClients(List<SocketChannel> clients) {
		this.clients = clients;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSensor(SocketChannel sensor) {
		this.sensor = sensor;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
