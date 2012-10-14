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

import javax.swing.text.DefaultCaret;

public class Monitor implements Runnable {
	private int port;
	private int monitorId;
	private Console targetConsole;
	private static Map<String, Metrics> metrics = new HashMap<String, Metrics>();
	private Map<SocketChannel, String> notificationList = new HashMap<SocketChannel, String>();

	// Bufor odczytu i zapisu
	private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
	private ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

	private static int monitorNum = 0;

	public Monitor(int port, Console target) {
		monitorNum++;
		this.port = port;
		this.targetConsole = target;
		DefaultCaret caret = (DefaultCaret) targetConsole.console.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.monitorId = monitorNum;
	}

	private void disconnectClient(SelectionKey key, SocketChannel client)
			throws IOException {
		sendMsg(client.socket().getInetAddress() + " roz³¹czony.");
		key.cancel();
		client.socket().close();
		client.close();

		List<String> toRemove = new ArrayList<String>();
		// Usuniecie metryki jesli klient taka dostarczal‚ lub wypisanie z
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
	 * G³ówny w¹tek
	 */
	@Override
	public void run() {
		sendMsg("Gotowy, Oczekujê na po³¹czenia...");
		try {
			// Stworzenie i konfiguracja selektora
			Selector selector = Selector.open();
			ServerSocketChannel server = ServerSocketChannel.open();
			server.configureBlocking(false);
			server.socket().bind(new InetSocketAddress(port));
			server.register(selector, SelectionKey.OP_ACCEPT);

			// Cykl zycia monitora
			while (true) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys()
						.iterator();
				while (iter.hasNext()) {
					SocketChannel client;
					ServerSocketChannel clientChannel;
					SelectionKey key = iter.next();
					iter.remove();

					if (key.isAcceptable()) {

						// Oczekiwanie na po³¹czenie
						clientChannel = (ServerSocketChannel) key.channel();
						client = clientChannel.accept();

						// Rejestracja w selektorze
						client.configureBlocking(false);
						client.register(selector, client.validOps());
						sendMsg(client.socket().getInetAddress()
								+ " po³¹czony.");
					} else if (key.isReadable()) {
						client = (SocketChannel) key.channel();

						// Wyczyszczenie bufora i jego ponowne wczytanie
						readBuffer.clear();
						int bytesRead = client.read(readBuffer);

						// Przetwarzanie bufora
						if (bytesRead == -1) {
							sendMsg(client.socket().getInetAddress()
									+ " rozlaczony.");
							key.cancel();
						} else {
							// Odczytanie tekstu
							readBuffer.flip();
							String message = new String(readBuffer.array(),
									readBuffer.position(),
									readBuffer.remaining());
							if (message.toLowerCase().startsWith("send")) {
								// Otrzymywanie metryki
								saveMetricsValue(client, message);
							} else if (message.toLowerCase()
									.startsWith("start")) {
								// Obs³uga rozpoczêcia subskrypcji
								subscribeFor(client, message);
							} else if (message.toLowerCase().startsWith(
									"delete")) {
								// Obs³uga koñczenia subskrypcji
								unsubscribeFor(client, message);
							} else if (message.toLowerCase().startsWith("stop")
									|| message.toLowerCase().startsWith("quit")) {
								// Roz³¹czanie klienta
								disconnectClient(key, client);
							} else {
								// Ignorowanie pozosta³ych
								sendMsg(message);
							}
						}
					} else if (key.isWritable()) {
						client = (SocketChannel) key.channel();
						if (notificationList.containsKey(client)) {
							String message = notificationList.get(client);
							writeBuffer.clear();
							if (message != null && !message.isEmpty()) {
								writeBuffer.put(message.getBytes());
								writeBuffer.flip();
							}
							client.write(writeBuffer);
							notificationList.remove(client);
						}
					} else {
						// Obs³uga niez³apanych SelectionKey
						System.out.println("Inna operacja klucza: "
								+ key.readyOps());
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Exception: " + e.getMessage());
			if (e.getMessage().contains("Address already in use: bind")) {
				sendMsg("Port nr " + port + " jest zajety");
			}
			e.printStackTrace();
		}
		sendMsg("CLOSED");
	}

	/**
	 * Zapisuje ostatnie otrzymane wartosci metryk
	 * 
	 * @param client
	 *            host od ktorego otrzymano metryke
	 * @param message
	 *            wiadomosc
	 * @throws IOException
	 */
	private void saveMetricsValue(SocketChannel client, String what)
			throws IOException {
		String[] splitted = what.replace("send", "").split(";");
		if (splitted.length < 3) {
			sendMsg("Nieprawidlowa skladnia zadania");
			return;
		}
		// Tworzenie lub update istniejacej metryki
		String metricsName = splitted[0].trim() + "-" + splitted[1].trim();
		if (metrics.containsKey(metricsName)) {
			Metrics m = metrics.get(metricsName);
			m.setValue(splitted[2].trim());
			sendMsg("Uaktualniono metryke: " + metricsName + "-"
					+ splitted[2].trim());

			// Przygotowanie wiadomosci dla subskrynentow
			String message = m.getName() + ";" + m.getValue();
			for (SocketChannel sc : m.getClients()) {
				notificationList.put(sc, message);
			}
		} else {
			metrics.put(metricsName, new Metrics(metricsName, client,
					splitted[2].trim()));
			sendMsg("Dodano metryke: " + metricsName + "-" + splitted[2].trim());
		}
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
	 *            Czy po dodanym tekœcie ma wystêpowaæ znak nowej linii
	 */
	private void sendMsg(String text, boolean newLine) {
		text = "Monitor " + monitorId + ": " + text;
		if (newLine && !text.endsWith("\n")) {
			text += "\n";
		}
		this.targetConsole.writeToConsole(text);
	}

	/**
	 * Rozpoczecie subskrypcji na dany zasob
	 * 
	 * @param who
	 * @param what
	 * @return
	 */
	private boolean subscribeFor(SocketChannel who, String what) {
		String[] splitted = what.replace("start", "").split(";");
		if (splitted.length < 2) {
			sendMsg("Nieprawidlowa skladnia zadania");
			return false;
		}
		// Dodanie subskrypcji
		String metricsName = splitted[0].trim() + "-" + splitted[1].trim();
		if (metrics.containsKey(metricsName)) {
			metrics.get(metricsName).addClient(who);
			sendMsg("Dodano subskrybcje na: " + metricsName);
		} else {
			sendMsg("Nie znaleziono podanej pary zasob-metryka!");
		}
		return true;
	}

	/**
	 * Konczenie subskrypcji zasobu dla danego klienta
	 * 
	 * @param who
	 * @param what
	 * @return
	 */
	private boolean unsubscribeFor(SocketChannel who, String what) {
		String[] splitted = what.replace("delete", "").split(";");
		if (splitted.length < 2) {
			sendMsg("Nieprawidlowa skladnia zadania");
			return false;
		}
		// UsuniÄ™cie subskrypcji
		String metricsName = splitted[0].trim() + "-" + splitted[1].trim();
		if (metrics.containsKey(metricsName)) {
			metrics.get(metricsName).removeClient(who);
			sendMsg("Usunieto subskrybcje na: " + splitted[0].trim() + " "
					+ splitted[1].trim());
		}
		return true;
	}
}
