package database;

/**
 * Eccezione sollevata quando una interrogazione SQL restituisce un resultset vuoto.
 */
public class EmptySetException extends Exception {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore di classe senza messaggio.
	 */
	public EmptySetException() {
		super();
	}

	/**
	 * Costruttore di classe. Invoca il costruttore della superclasse
	 * passando il messaggio che descrive la causa dell'errore.
	 *
	 * @param message messaggio che descrive la causa dell'errore
	 */
	public EmptySetException(String message) {
		super(message);
	}
}
