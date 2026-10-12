package server;

/**
 * Eccezione sollevata in caso di acquisizione di un valore mancante o fuori
 * range per un attributo di un nuovo esempio da classificare.
 */
public class UnknownValueException extends Exception {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Costruttore di classe. Invoca il costruttore della superclasse
	 * passando il messaggio che descrive la causa dell'errore.
	 *
	 * @param message messaggio che descrive la causa dell'errore
	 */
	public UnknownValueException(String message) {
		super(message);
	}
}
