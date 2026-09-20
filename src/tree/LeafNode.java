package tree;
import  data.Data;

/**
 * Estende la classe {@link Node} e modella l'entità nodo fogliare
 * dell'albero di decisione.
 */
public class LeafNode extends Node {

    /** Valore dell'attributo di classe espresso nella foglia corrente. */
    Double predictedClassValue;

    /**
     * Costruttore di classe. Istanzia un oggetto invocando il costruttore della
     * superclasse e avvalora predictedClassValue come media dei valori
     * dell'attributo di classe che ricadono nel sotto-insieme di training coperto.
     *
     * @param trainingSet       training set completo
     * @param beginExampleIndex indice del primo esempio del sotto-insieme coperto dalla foglia
     * @param endExampleIndex   indice dell'ultimo esempio del sotto-insieme coperto dalla foglia
     */
    LeafNode(Data trainingSet, int beginExampleIndex, int endExampleIndex) {
        super(trainingSet, beginExampleIndex, endExampleIndex);

        double sum = 0;
        for (int i = beginExampleIndex; i <= endExampleIndex; i++)
            sum += trainingSet.getClassValue(i);
        this.predictedClassValue = sum / (endExampleIndex - beginExampleIndex + 1);
    }

    /**
     * Restituisce il valore del membro predictedClassValue.
     *
     * @return il valore di classe predetto dalla foglia
     */
    Double getPredictedClassValue() {
        return predictedClassValue;
    }

    /**
     * Restituisce il numero di split originanti dal nodo foglia, ovvero 0.
     *
     * @return 0
     */
    int getNumberOfChildren() {
        return 0;
    }

    /**
     * Invoca il metodo della superclasse aggiungendo il valore di classe della foglia.
     *
     * @return la rappresentazione testuale della foglia
     */
    public String toString() {
        return "LEAF : class=" + predictedClassValue + " " + super.toString();
    }
}