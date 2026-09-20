/**
 * Estende la classe {@link Attribute} e rappresenta un attributo discreto.
 */
public class DiscreteAttribute extends Attribute {
    /** Array di oggetti String, uno per ciascun valore discreto che l'attributo può assumere. */
    String values[];

    /**
     * Costruttore. Invoca il costruttore della super-classe e avvalora 
     * l'array values[] con i valori discreti in input.
     * 
     * @param name      nome simbolico dell'attributo.
     * @param index     identificativo numerico dell'attributo.
     * @param values    array di oggetti string con i valori che l'attributo può assumere.
     */
    DiscreteAttribute(String name, int index, String values[]) {
        super(name, index);
        this.values = values;
    }

    /**
     * Restituisce la cardinalità dell'array values[].
     *
     * @return il numero di valori discreti dell'attributo.
     */
    int getNumberOfDistinctValues() {
        return values.length;
    }

    /**
     * Restituisce il valore dell'elemento i dell'array values[].
     * 
     * @param i indice di un solo valore discreto rispetto all'array values[].
     * @return  valore discreto con indice il parametro di input.
     */
    String getValue(int i) {
        return values[i];
    }
}
