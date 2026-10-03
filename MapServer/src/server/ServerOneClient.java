package server;

import data.Data;
import data.TrainingDataException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import tree.RegressionTree;

/**
 * Gestisce, in un thread dedicato, la comunicazione con un singolo client:
 * acquisizione dei dati di training da una tabella del database,
 * apprendimento (o caricamento) dell'albero di regressione, e predizione
 * interattiva di nuovi esempi.
 *
 * <p>Protocollo (un oggetto Integer come comando, seguito da eventuali
 * parametri):</p>
 * <ul>
 * <li>0 + nome tabella &rarr; carica i dati di training dalla tabella indicata</li>
 * <li>1 &rarr; apprende l'albero di regressione dai dati caricati e lo salva su file</li>
 * <li>2 + nome file &rarr; carica un albero di regressione precedentemente salvato</li>
 * <li>3 &rarr; avvia una sessione di predizione interattiva</li>
 * </ul>
 * In risposta a ciascun comando, il server scrive "OK" seguito da eventuali
 * dati, oppure un messaggio di errore (il risultato di {@code toString()}
 * dell'eccezione sollevata). Durante una sessione di predizione, il server
 * scrive "QUERY" seguito dal testo della domanda relativa allo split
 * corrente, attende la risposta (un intero, l'indice del ramo scelto), e
 * ripete finché non raggiunge una foglia (risponde "OK" e il valore
 * predetto) o riceve una risposta non valida (risponde con il messaggio di
 * {@link UnknownValueException}).
 */
public class ServerOneClient extends Thread {

	/** Socket di comunicazione con il client. */
	private Socket socket;

	/** Stream di lettura degli oggetti inviati dal client. */
	private ObjectInputStream in;

	/** Stream di scrittura degli oggetti da inviare al client. */
	private ObjectOutputStream out;

	/** Dati di training correnti, caricati dal comando 0. */
	private Data trainingSet;

	/** Albero di regressione corrente, appreso (comando 1) o caricato (comando 2). */
	private RegressionTree tree;

	/** Nome (tabella o file) usato nell'ultimo comando 0 o 2, riusato come base per il nome del file serializzato. */
	private String currentName;

	/**
	 * Costruttore di classe. Inizializza gli attributi socket, in e out e
	 * avvia il thread.
	 *
	 * @param s socket di comunicazione con il client
	 * @throws IOException se si verifica un errore nell'apertura degli stream
	 */
	public ServerOneClient(Socket s) throws IOException {
		this.socket = s;
		// l'ordine di creazione (output prima di input) deve essere lo stesso
		// su entrambi i lati della connessione, altrimenti la coppia
		// ObjectOutputStream/ObjectInputStream resta bloccata in attesa
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.in = new ObjectInputStream(socket.getInputStream());
		start();
	}

	/**
	 * Gestisce le richieste del client: legge ripetutamente un comando e lo
	 * esegue, finché il client non chiude la connessione.
	 */
	public void run() {
		try {
			while (true) {
				int command = (Integer) in.readObject();
				switch (command) {
				case 0:
					handleLoadData();
					break;
				case 1:
					handleLearnTree();
					break;
				case 2:
					handleLoadTree();
					break;
				case 3:
					handlePredict();
					break;
				default:
					out.writeObject("Comando sconosciuto: " + command);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// il client ha chiuso la connessione: il thread termina
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				// ignorato: la connessione è comunque da considerarsi chiusa
			}
		}
	}

	/**
	 * Gestisce il comando 0: carica i dati di training dalla tabella il cui
	 * nome è stato inviato dal client.
	 */
	private void handleLoadData() throws IOException, ClassNotFoundException {
		String tableName = (String) in.readObject();
		currentName = tableName;
		try {
			trainingSet = new Data(tableName);
			out.writeObject("OK");
		} catch (TrainingDataException e) {
			out.writeObject(e.toString());
		}
	}

	/**
	 * Gestisce il comando 1: apprende l'albero di regressione dai dati di
	 * training caricati in precedenza e lo salva su file.
	 */
	private void handleLearnTree() throws IOException {
		try {
			tree = new RegressionTree(trainingSet);
			try {
				tree.salva(currentName + ".dmp");
			} catch (IOException e) {
				// il salvataggio non riesce: l'albero resta comunque disponibile in memoria
				System.out.println("Impossibile salvare l'albero: " + e);
			}
			out.writeObject("OK");
		} catch (Exception e) {
			out.writeObject(e.toString());
		}
	}

	/**
	 * Gestisce il comando 2: carica un albero di regressione precedentemente
	 * salvato nel file il cui nome è stato inviato dal client.
	 */
	private void handleLoadTree() throws IOException, ClassNotFoundException {
		String fileName = (String) in.readObject();
		currentName = fileName;
		try {
			tree = RegressionTree.carica(fileName + ".dmp");
			out.writeObject("OK");
		} catch (Exception e) {
			out.writeObject(e.toString());
		}
	}

	/**
	 * Gestisce il comando 3: avvia una sessione di predizione interattiva,
	 * scambiando con il client una domanda ("QUERY") e la relativa risposta
	 * (l'indice del ramo scelto) ad ogni split, finché non si raggiunge una
	 * foglia o il ramo scelto non è valido.
	 */
	private void handlePredict() throws IOException, ClassNotFoundException {
		if (tree == null) {
			out.writeObject("Nessun albero disponibile: apprendi o carica un albero prima di effettuare una predizione.");
			return;
		}

		RegressionTree current = tree;
		while (true) {
			if (current.isLeafNode()) {
				out.writeObject("OK");
				out.writeObject(current.getLeafPrediction().toString());
				return;
			}

			out.writeObject("QUERY");
			out.writeObject(current.formulateQuery());
			int path = (Integer) in.readObject();

			if (path < 0 || path >= current.getNumberOfChildren()) {
				out.writeObject(new UnknownValueException("The answer should be an integer between 0 and "
						+ (current.getNumberOfChildren() - 1) + "!").toString());
				return;
			}

			current = current.getChild(path);
		}
	}
}
