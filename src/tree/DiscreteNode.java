package tree;
import data.Attribute;
import data.Data;
import data.DiscreteAttribute;

/**
 * Estende la classe {@link SplitNode} e modella l'entità nodo di split
 * relativo ad un attributo indipendente discreto.
 */
public class DiscreteNode extends SplitNode {

    /**
     * Costruttore di classe. Istanzia un oggetto invocando il costruttore
     * della superclasse con il parametro attribute.
     *
     * @param trainingSet         training set completo
     * @param beginExampelIndex   indice del primo esempio del sotto-insieme di training
     * @param endExampleIndex     indice dell'ultimo esempio del sotto-insieme di training
     * @param attribute           attributo indipendente discreto sul quale si definisce lo split
     */
    public DiscreteNode(Data trainingSet, int beginExampelIndex, int endExampleIndex, DiscreteAttribute attribute) {
        super(trainingSet, beginExampelIndex, endExampleIndex, attribute);
    }

    /**
     * Genera le informazioni per ciascuno degli split candidati e popola l'array
     * mapSplit[]. Sfrutta il fatto che il sotto-insieme di training è già ordinato
     * rispetto all'attributo: gli esempi con lo stesso valore sono contigui, quindi
     * viene creato uno {@link SplitInfo} per ciascun valore distinto presente
     * tra beginExampelIndex e endExampleIndex.
     *
     * @param trainingSet         training set completo
     * @param beginExampelIndex   indice del primo esempio del sotto-insieme di training
     * @param endExampleIndex     indice dell'ultimo esempio del sotto-insieme di training
     * @param attribute           attributo indipendente sul quale si definisce lo split
     */
    void setSplitInfo(Data trainingSet, int beginExampelIndex, int endExampleIndex, Attribute attribute) {
        int col = attribute.getIndex();

        // Conto i valori distinti del sotto-insieme (già ordinato)
        int numberOfValues = 1;
        for (int i = beginExampelIndex + 1; i <= endExampleIndex; i++) {
            if (!trainingSet.getExplanatoryValue(i, col).equals(trainingSet.getExplanatoryValue(i - 1, col)))
                numberOfValues++;
        }
        mapSplit = new SplitInfo[numberOfValues];

        // Creo uno SplitInfo per ogni partizione
        int child = 0;
        int start = beginExampelIndex;
        for (int i = beginExampelIndex + 1; i <= endExampleIndex; i++) {
            if (!trainingSet.getExplanatoryValue(i, col).equals(trainingSet.getExplanatoryValue(i - 1, col))) {
                // il valore è cambiato: chiudo la partizione [start, i-1]
                mapSplit[child] = new SplitInfo(trainingSet.getExplanatoryValue(start, col), start, i - 1, child);
                child++;
                start = i;
            }
        }
        // l'ultima partizione arriva fino a endExampleIndex
        mapSplit[child] = new SplitInfo(trainingSet.getExplanatoryValue(start, col), start, endExampleIndex, child);
    }

    /**
     * Confronta il valore in input con il valore splitValue di ciascuno
     * degli {@link SplitInfo} collezionati in mapSplit[].
     *
     * @param value valore discreto dell'attributo che si vuole testare
     * @return l'indice del ramo (posizione in mapSplit[]) per cui il test è positivo,
     *         -1 se nessun ramo corrisponde
     */
    int testCondition(Object value) {
        for (int i = 0; i < mapSplit.length; i++) {
            if (mapSplit[i].getSplitValue().equals(value))
                return i;
        }
        return -1; // nessun ramo corrisponde
    }

    /**
     * Invoca il metodo della superclasse specializzandolo per gli attributi discreti.
     *
     * @return la rappresentazione testuale del nodo di split discreto
     */
    public String toString() {
        return "DISCRETE " + super.toString();
    }
}