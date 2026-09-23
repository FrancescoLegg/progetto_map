package tree;
import  data.Data;
import java.io.Serializable;

/**
 * Classe astratta per modellare l'astrazione dell'entità nodo dell'albero di decisione
 * 
 */
public abstract class Node implements Serializable {
    /** Indentificativo di versione per la serializzazione */
    private static final long serialVersionUID = 1L;

    /** Contatore dei nodi generati dall'albero */
    static int idNodeCount = 0;

    /** Identificativo numerico del nodo */
    int idNode;

    /** Indice nell'array del training set del primo esempio coperto dal nodo corrente */
    int beginExampleIndex;

    /**indice nell'array del trainig set dell'ultimo esempio coperto dal nodo corrente */
    int endExampleIndex;

    /** Valore dello SSE calcolato, rispetto all'attributo di classe */
    double variance;

    /**
     * Costruttore di classe
     * 
     * @param trainingSet           Training Set completo
     * @param beginExampleIndex     Indice del primi esempio
     * @param endExampleIndex       Indice dell'ultimo esempio
     */
    Node(Data trainingSet, int beginExampleIndex, int endExampleIndex){
        this.idNode = idNodeCount++;
        this.beginExampleIndex = beginExampleIndex;
        this.endExampleIndex = endExampleIndex;

        //Calcolo la media dei valori di classe nel sotto-insieme
        int n = endExampleIndex - beginExampleIndex+1; //calcolo il numero di esempi nel training set
        double sum=0;
        for(int i=beginExampleIndex; i<= endExampleIndex; i++)
            sum+=trainingSet.getClassValue(i);
        double mean = sum / n;

        //Calcolo SSE
        double sse = 0;
        for(int i=beginExampleIndex; i<= endExampleIndex; i++){
            double diff = trainingSet.getClassValue(i) - mean;
            sse += diff * diff;
        }
        
        this.variance = sse;
    }

    /** 
     * Restitusice il valore del membro idNode
     * 
     * @return il valore del memebro idNode
     */
    int getIdNode(){return idNode;}

    /**
     * Restituisce il valore del memebro beginExampleIndex
     * 
     * @return il valore di beginExampleIndex
     */
    int getBeginExampleIndex(){return  beginExampleIndex;}

    /**
     * Restituisce il valore di endExampleIndex
     *  
     * @return il valore di endExampleIndex
     */
    int getEndExampleIndex(){return endExampleIndex;}

    /**
     * Restitusice il valore della varianza
     * 
     * @return valore della varianza
     */
    double getVariance(){return variance;}

    abstract int getNumberOfChildren();

    public String toString(){
        return "Nodo: [Examples:" + beginExampleIndex + "-" + endExampleIndex + "] variance:" + variance;
    }
}
