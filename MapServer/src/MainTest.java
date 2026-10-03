import server.MultiServer;

/**
 * Avvia il server multi-client in ascolto sulla porta indicata come
 * argomento da riga di comando, oppure sulla porta di default 8080 se
 * nessun argomento viene fornito.
 */
public class MainTest {

	/**
	 * @param args args[0], opzionale: porta su cui il server deve mettersi in ascolto
	 */
	public static void main(String[] args) {
		int port = 8080;
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Porta non valida, uso la porta di default " + port);
			}
		}
		new MultiServer(port);
	}
}
