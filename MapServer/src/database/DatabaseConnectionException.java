package database;

/**
 * Eccezione sollevata in caso di fallimento nella connessione al database.
 */
public class DatabaseConnectionException extends Exception {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore di classe. Invoca il costruttore della superclasse
	 * passando il messaggio che descrive la causa dell'errore.
	 *
	 * @param message messaggio che descrive la causa dell'errore
	 */
	public DatabaseConnectionException(String message) {
		super(message);
	}
}
