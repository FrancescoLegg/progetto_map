package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server multi-client per l'esecuzione remota del processo di data mining.
 * Accetta le richieste di connessione e, per ciascuna, avvia un thread
 * dedicato ({@link ServerOneClient}).
 */
public class MultiServer {

	/** Porta di default su cui il server è in ascolto. */
	private int PORT = 8080;

	/**
	 * Costruttore di classe. Inizializza la porta su cui il server sarà in
	 * ascolto e avvia il ciclo di accettazione delle connessioni.
	 *
	 * @param port porta su cui il server deve mettersi in ascolto
	 */
	public MultiServer(int port) {
		this.PORT = port;
		run();
	}

	/**
	 * Istanzia un oggetto ServerSocket che si mette in attesa di richieste di
	 * connessione da parte dei client. Ad ogni nuova richiesta di connessione
	 * istanzia un nuovo ServerOneClient, dedicato a quel singolo client.
	 */
	private void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("Impossibile aprire la porta " + PORT + ": " + e);
			return;
		}

		System.out.println("Server in ascolto sulla porta " + PORT + "...");

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("Nuova connessione da " + socket.getInetAddress());
				new ServerOneClient(socket);
			} catch (IOException e) {
				System.out.println("Errore nell'accettazione di una connessione: " + e);
			}
		}
	}
}
