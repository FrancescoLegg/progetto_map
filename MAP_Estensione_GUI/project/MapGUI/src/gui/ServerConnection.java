package gui;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Gestisce la connessione socket con MapServer e implementa, lato client,
 * lo stesso protocollo usato da {@code map7Client.MainTest} (comandi 0, 1,
 * 2, 3), più il nuovo comando 4 per ottenere la struttura dell'albero da
 * disegnare.
 */
public class ServerConnection {

	/** Socket di comunicazione con il server. */
	private Socket socket;

	/** Stream di scrittura verso il server. */
	private ObjectOutputStream out;

	/** Stream di lettura dal server. */
	private ObjectInputStream in;

	/**
	 * Apre la connessione verso il server all'indirizzo e porta indicati.
	 *
	 * @param host indirizzo del server
	 * @param port porta su cui il server è in ascolto
	 * @throws IOException se la connessione non può essere stabilita
	 */
	public void connect(String host, int port) throws IOException {
		socket = new Socket(host, port);
		// stesso ordine del client testuale: output prima di input
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
	}

	/**
	 * Chiude la connessione con il server.
	 */
	public void disconnect() {
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			// connessione comunque da considerarsi chiusa
		}
	}

	/**
	 * Indica se la connessione è attualmente aperta.
	 *
	 * @return true se connesso al server
	 */
	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	/**
	 * Comando 0 + 1: carica i dati dalla tabella indicata e, se riesce,
	 * apprende l'albero di regressione.
	 *
	 * @param tableName nome della tabella da cui apprendere
	 * @return "OK" in caso di successo, altrimenti il messaggio di errore
	 * @throws IOException            se si verifica un errore di comunicazione
	 * @throws ClassNotFoundException se la risposta non può essere deserializzata
	 */
	public String learnFromTable(String tableName) throws IOException, ClassNotFoundException {
		out.writeObject(0);
		out.writeObject(tableName);
		String answer = (String) in.readObject();
		if (!answer.equals("OK"))
			return answer;

		out.writeObject(1);
		return (String) in.readObject();
	}

	/**
	 * Comando 2: carica un albero di regressione precedentemente salvato.
	 *
	 * @param fileName nome base del file (senza estensione) da caricare
	 * @return "OK" in caso di successo, altrimenti il messaggio di errore
	 * @throws IOException            se si verifica un errore di comunicazione
	 * @throws ClassNotFoundException se la risposta non può essere deserializzata
	 */
	public String loadArchive(String fileName) throws IOException, ClassNotFoundException {
		out.writeObject(2);
		out.writeObject(fileName);
		return (String) in.readObject();
	}

	/**
	 * Comando 4: richiede al server la struttura completa dell'albero
	 * corrente e la ricostruisce come {@link TreeNodeData}.
	 *
	 * @return la radice dell'albero ricostruito
	 * @throws IOException            se si verifica un errore di comunicazione
	 * @throws ClassNotFoundException se la risposta non può essere deserializzata
	 * @throws TreeNotAvailableException se il server non ha un albero disponibile
	 */
	public TreeNodeData fetchTreeSnapshot() throws IOException, ClassNotFoundException, TreeNotAvailableException {
		out.writeObject(4);
		String status = (String) in.readObject();
		if (!status.equals("OK")) {
			String message = (String) in.readObject();
			throw new TreeNotAvailableException(message);
		}
		return readSnapshot();
	}

	/**
	 * Legge ricorsivamente un nodo dallo stream, secondo il protocollo di
	 * {@code ServerOneClient.sendTreeSnapshot}.
	 *
	 * @return il nodo letto, con l'intero sotto-albero ricostruito
	 * @throws IOException            se si verifica un errore di comunicazione
	 * @throws ClassNotFoundException se un oggetto letto non può essere deserializzato
	 */
	private TreeNodeData readSnapshot() throws IOException, ClassNotFoundException {
		TreeNodeData node = new TreeNodeData();
		boolean isLeaf = (Boolean) in.readObject();
		node.leaf = isLeaf;
		if (isLeaf) {
			node.leafValue = (String) in.readObject();
		} else {
			String query = (String) in.readObject();
			int n = (Integer) in.readObject();
			String[] labels = parseBranchLabels(query, n);
			node.attributeLabel = extractAttributeName(labels[0]);
			for (int i = 0; i < n; i++) {
				TreeNodeData child = readSnapshot();
				child.edgeLabel = labels[i];
				node.children.add(child);
			}
		}
		return node;
	}

	/**
	 * Estrae, dal testo restituito da {@code formulateQuery()} (una riga per
	 * ramo, nel formato "indice:etichetta"), la sola etichetta di ciascun
	 * ramo, da mostrare sull'arco corrispondente.
	 *
	 * @param query testo con una riga "indice:etichetta" per ramo
	 * @param n     numero di rami attesi
	 * @return le etichette, nell'ordine dei rami (0, 1, 2, ...)
	 */
	private String[] parseBranchLabels(String query, int n) {
		String[] labels = new String[n];
		String[] lines = query.split("\n");
		for (String line : lines) {
			if (line.isEmpty())
				continue;
			int colon = line.indexOf(':');
			if (colon < 0)
				continue;
			try {
				int idx = Integer.parseInt(line.substring(0, colon));
				if (idx >= 0 && idx < n)
					labels[idx] = line.substring(colon + 1);
			} catch (NumberFormatException e) {
				// riga malformata: ignorata
			}
		}
		for (int i = 0; i < n; i++)
			if (labels[i] == null)
				labels[i] = "?";
		return labels;
	}

	/**
	 * Estrae il nome dell'attributo da un'etichetta di ramo del tipo
	 * "X=A" o "Y&lt;=2.0", prendendo i caratteri prima del primo operatore
	 * di confronto ("=", "&lt;" o "&gt;").
	 *
	 * @param branchLabel etichetta completa di un ramo (es. "Y&lt;=2.0")
	 * @return il solo nome dell'attributo (es. "Y")
	 */
	private String extractAttributeName(String branchLabel) {
		if (branchLabel == null)
			return "?";
		for (int i = 0; i < branchLabel.length(); i++) {
			char c = branchLabel.charAt(i);
			if (c == '=' || c == '<' || c == '>')
				return branchLabel.substring(0, i);
		}
		return branchLabel;
	}
}
