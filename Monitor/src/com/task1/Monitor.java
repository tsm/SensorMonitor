package com.task1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Monitor implements Runnable {
	private int port;
	private int monitorId;
	private Console targetConsole;
	private Map<String, Metrics> metrics;

	private static int monitorNum = 0;

	public Monitor(int port, Console target) {
		monitorNum++;
		this.port = port;
		this.targetConsole = target;
		this.monitorId = monitorNum;
		this.metrics = new HashMap<String, Metrics>();
	}

	/**
	 * Wypisywanie tekstu w konsoli
	 * 
	 * @param text
	 *            Tekst do wypisania
	 */
	private void sendMsg(String text) {
		sendMsg(text, true);
	}

	/**
	 * Wypisywanie tekstu w konsoli
	 * 
	 * @param text
	 *            Tekst do wypisania
	 * @param newLine
	 *            Czy po dodanym tekście ma występować znak nowej linii
	 */
	private void sendMsg(String text, boolean newLine) {
		this.targetConsole.writeToConsole("Monitor " + monitorId + ": " + text);
		if (newLine) {
			this.targetConsole.writeToConsole("\n");
		}
	}

	/**
	 * Główny wątek
	 */
	@Override
	public void run() {
		sendMsg("Gotowy, Oczekuję na połączenia...");

		try {
			// Bufor
			ByteBuffer buf = ByteBuffer.allocate(1024);

			// Stworzenie i konfiguracja selektora
			Selector selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(port));
			server.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys()
						.iterator();
				while (iter.hasNext()) {
					SocketChannel client;
					SelectionKey key = iter.next();
					iter.remove();

					switch (key.readyOps()) {
					case SelectionKey.OP_ACCEPT:

						// Oczekiwanie na połączenie
						client = ((ServerSocketChannel) key.channel()).accept();

						// Rejestracja w selektorze
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
						sendMsg(client.socket().getInetAddress()
								+ " połączony.");
						break;

					case SelectionKey.OP_READ:
						client = (SocketChannel) key.channel();

						// Wyczyszczenie bufora i jego ponowne wczytanie
						buf.clear();
						int bytesRead = client.read(buf);

						// Przetwarzanie bufora
						// if (bytesRead != -1) {
						// sendMsg(client.socket().getInetAddress()
						// + " rozłączony.");
						// key.cancel();
						// } else {
						// Odczytanie tekstu
						buf.flip();
						String message = new String(buf.array(),
								buf.position(), buf.remaining());
						if (message.startsWith("SEND")) {
							// Otrzymywanie metryki
							saveMetricsValue(client, message);
						} else if (message.startsWith("START")) {
							// Obsługa rozpoczęcia subskrypcji
							subscribeFor(client, message);
						} else if (message.startsWith("DELETE")) {
							// Obsługa kończenia subskrypcji
							unsubscribeFor(client, message);
						} else if (message.startsWith("STOP")
								|| message.startsWith("QUIT")) {
							// Rozłączanie klienta
							disconnectClient(key, client);
						}
						// Ignorowanie pozostałych
						sendMsg(message);
						// }

						break;
					default:
						// Obsługa niezłapanych SelectionKey
						System.out.println("Other SelectionKey: "
								+ key.readyOps());
						break;
					}
				}
			}

		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
		sendMsg("CLOSED");
	}

	private void disconnectClient(SelectionKey key, SocketChannel client)
			throws IOException {
		sendMsg(client.socket().getInetAddress() + " rozłączony.");
		key.cancel();
		client.socket().close();
		client.close();

		List<String> toRemove = new ArrayList<String>();
		// Usunięcie metryki jeśli klient taką dostarczał lub wypisanie z
		// subskrypcji
		for (Entry<String, Metrics> entry : metrics.entrySet()) {
			entry.getValue().getClients().remove(client);
			if (entry.getValue().getSensor().equals(client)) {
				toRemove.add(entry.getKey());
			}
		}
		for (String keyString : toRemove) {
			metrics.remove(keyString);
		}
	}

	/**
	 * Zapisuje ostatnie otrzymane wartości metryk
	 * 
	 * @param client
	 *            host od którego otrzymano metrykę
	 * @param message
	 *            wiadomość
	 */
	private void saveMetricsValue(SocketChannel client, String what) {
		String[] splitted = what.replace("SEND", "").split(";");
		if (splitted.length < 3) {
			sendMsg("Nieprawidłowa składnia żądania");
			return;
		}
		// Tworzenie lub update istniejącej metryki
		String metricsName = splitted[0].trim() + "-" + splitted[1].trim();
		if (metrics.containsKey(metricsName)) {
			metrics.get(metricsName).setValue(splitted[3].trim());
		} else {
			metrics.put(metricsName, new Metrics(metricsName, client,
					splitted[2].trim()));
		}
	}

	/**
	 * Rozpoczęcie subskrypcji na dany zasób
	 * 
	 * @param who
	 * @param what
	 * @return
	 */
	private boolean subscribeFor(SocketChannel who, String what) {
		String[] splitted = what.replace("START", "").split(";");
		if (splitted.length < 2) {
			sendMsg("Nieprawidłowa składnia żądania");
			return false;
		}
		// Dodanie subskrypcji
		String metricsName = splitted[0].trim() + "-" + splitted[1].trim();
		if (metrics.containsKey(metricsName)) {
			metrics.get(metricsName).addClient(who);
			sendMsg("Dodano subskrybcję na: " + metricsName);
		} else {
			sendMsg("Nie znaleziono podanej pary zasob-metryka!");
		}
		return true;
	}

	/**
	 * Kończenie subskrypcji zasobu dla danego klienta
	 * 
	 * @param who
	 * @param what
	 * @return
	 */
	private boolean unsubscribeFor(SocketChannel who, String what) {
		String[] splitted = what.replace("DELETE", "").split(";");
		if (splitted.length < 2) {
			sendMsg("Nieprawidłowa składnia żądania");
			return false;
		}
		// Usunięcie subskrypcji
		String metricsName = splitted[0].trim() + "-" + splitted[1].trim();
		if (metrics.containsKey(metricsName)) {
			metrics.get(metricsName).removeClient(who);
			sendMsg("Usunięto subskrybcję na: " + splitted[0].trim() + " "
					+ splitted[1].trim());
		}
		return true;
	}
}
