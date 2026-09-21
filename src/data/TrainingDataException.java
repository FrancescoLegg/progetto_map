package data;

/**
 * Eccezione sollevata in caso di acquisizione errata del training set:
 * file inesistente, schema mancante, training set vuoto o privo di
 * variabile target numerica.
 */
public class TrainingDataException extends Exception {

    /**
     * Costruttore di classe. Invoca il costruttore della superclasse
     * passando il messaggio che descrive la causa dell'errore.
     *
     * @param message messaggio che descrive la causa dell'errore
     */
    public TrainingDataException(String message) {
        super(message);
    }
}