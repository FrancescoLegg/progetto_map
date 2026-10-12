package gui;

/**
 * Eccezione sollevata quando si richiede la struttura dell'albero (comando
 * 4) ma il server non ne ha ancora appreso o caricato uno.
 */
public class TreeNotAvailableException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message messaggio restituito dal server
	 */
	public TreeNotAvailableException(String message) {
		super(message);
	}
}
