package tree;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;
import data.DiscreteAttribute;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TreeSet;

/**
 * Modella l'intero albero di regressione come insieme di sotto-alberi.
 */
public class RegressionTree implements Serializable {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/** Radice del sotto-albero corrente. */
	Node root;

	/** Array di sotto-alberi originanti nel nodo root: uno per ogni figlio del nodo. */
	RegressionTree childTree[];

	/**
	 * Istanzia un sotto-albero dell'intero albero.
	 */
	RegressionTree() {
	}

	/**
	 * Istanzia un sotto-albero dell'intero albero e avvia l'induzione
	 * dell'albero dagli esempi di training in input. Il numero di esempi
	 * per foglia è fissato al 10% della dimensione del training set.
	 *
	 * @param trainingSet training set completo
	 */
	public RegressionTree(Data trainingSet) {

		learnTree(trainingSet, 0, trainingSet.getNumberOfExamples() - 1, trainingSet.getNumberOfExamples() * 10 / 100);
	}

	/**
	 * Verifica se il sotto-insieme corrente può essere coperto da un nodo foglia,
	 * controllando che il numero di esempi compresi tra begin ed end sia minore
	 * o uguale a numberOfExamplesPerLeaf.
	 *
	 * @param trainingSet             training set completo
	 * @param begin                   indice del primo esempio del sotto-insieme
	 * @param end                     indice dell'ultimo esempio del sotto-insieme
	 * @param numberOfExamplesPerLeaf numero minimo di esempi che una foglia deve contenere
	 * @return true se il sotto-insieme può essere coperto da una foglia
	 */
	boolean isLeaf(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		return (end - begin + 1) <= numberOfExamplesPerLeaf;
	}

	/**
	 * Per ciascun attributo indipendente istanzia, sfruttando l'RTTI, il
	 * DiscreteNode o il ContinuousNode associato e lo inserisce in un TreeSet,
	 * che mantiene i nodi ordinati per splitVariance crescente; il primo
	 * elemento è quindi lo split migliore. Ordina poi la porzione di
	 * trainingSet corrente (tra begin ed end) rispetto all'attributo del nodo
	 * selezionato.
	 *
	 * @param trainingSet training set completo
	 * @param begin       indice del primo esempio del sotto-insieme
	 * @param end         indice dell'ultimo esempio del sotto-insieme
	 * @return il nodo di split migliore per il sotto-insieme di training
	 */
	SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		TreeSet<SplitNode> ts = new TreeSet<SplitNode>();
		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			Attribute a = trainingSet.getExplanatoryAttribute(i);
			SplitNode currentNode;
			if (a instanceof DiscreteAttribute)
				currentNode = new DiscreteNode(trainingSet, begin, end, (DiscreteAttribute) a);
			else
				currentNode = new ContinuousNode(trainingSet, begin, end, (ContinuousAttribute) a);
			ts.add(currentNode);
		}
		// il TreeSet è ordinato per splitVariance crescente: il primo è il migliore
		SplitNode bestNode = ts.first();
		// ogni nodo candidato ha riordinato il sotto-insieme: riordino per l'attributo vincente
		trainingSet.sort(bestNode.getAttribute(), begin, end);
		return bestNode;
	}

	/**
	 * Genera un sotto-albero con il sotto-insieme di input istanziando un nodo
	 * fogliare o un nodo di split. In quest'ultimo caso determina il miglior
	 * nodo di split e ricorsivamente apprende un sotto-albero per ciascun ramo.
	 * Se il nodo di split non origina figli, il nodo diventa fogliare.
	 *
	 * @param trainingSet             training set completo
	 * @param begin                   indice del primo esempio del sotto-insieme
	 * @param end                     indice dell'ultimo esempio del sotto-insieme
	 * @param numberOfExamplesPerLeaf numero massimo di esempi che una foglia può contenere
	 */
	void learnTree(Data trainingSet, int begin, int end, int numberOfExamplesPerLeaf) {
		if (isLeaf(trainingSet, begin, end, numberOfExamplesPerLeaf)) {
			// determina la media dei valori di classe nella partizione corrente
			root = new LeafNode(trainingSet, begin, end);
		} else // split node
		{
			root = determineBestSplitNode(trainingSet, begin, end);

			if (root.getNumberOfChildren() > 1) {
				childTree = new RegressionTree[root.getNumberOfChildren()];
				for (int i = 0; i < root.getNumberOfChildren(); i++) {
					childTree[i] = new RegressionTree();
					childTree[i].learnTree(trainingSet, ((SplitNode) root).getSplitInfo(i).beginIndex,
							((SplitNode) root).getSplitInfo(i).endIndex, numberOfExamplesPerLeaf);
				}
			} else
				root = new LeafNode(trainingSet, begin, end);

		}
	}

	/**
	 * Stampa le informazioni dell'intero albero, compresa un'intestazione.
	 */
	public void printTree() {
		System.out.println("********* TREE **********\n");
		System.out.println(toString());
		System.out.println("*************************\n");
	}

	/**
	 * Concatena in una String tutte le informazioni di root e childTree[]
	 * correnti. Se root è un nodo di split vengono concatenate anche le
	 * informazioni dei rami.
	 *
	 * @return la rappresentazione testuale dell'intero albero
	 */
	public String toString() {
		String tree = root.toString() + "\n";

		if (root instanceof LeafNode) {

		} else // split node
		{
			for (int i = 0; i < childTree.length; i++)
				tree += childTree[i];
		}
		return tree;
	}

	/**
	 * Scandisce ogni ramo dell'albero dalla radice alla foglia concatenando le
	 * informazioni dei nodi di split fino al nodo foglia, e stampa le regole.
	 */
	public void printRules() {
		System.out.println("********* RULES **********");
		printRules("");
		System.out.println("*************************");
	}

	/**
	 * Supporta il metodo printRules(). Concatena alle condizioni in current
	 * quelle del ramo corrente: se root è di split il metodo viene invocato
	 * ricorsivamente sui sotto-alberi, se è una foglia stampa la regola completa.
	 *
	 * @param current condizioni (in AND) dei nodi di split dei livelli superiori
	 */
	void printRules(String current) {
		if (root instanceof LeafNode) {
			System.out.println(current + " ==> Class=" + ((LeafNode) root).getPredictedClassValue());
		} else {
			SplitNode split = (SplitNode) root;
			for (int i = 0; i < childTree.length; i++) {
				String cond = split.getAttribute().getName() + split.getSplitInfo(i).getComparator()
						+ split.getSplitInfo(i).getSplitValue();
				childTree[i].printRules(current.equals("") ? cond : current + " AND " + cond);
			}
		}
	}

	/**
	 * Indica se il nodo radice del sotto-albero corrente è una foglia.
	 * Permette al server di guidare la predizione un passo alla volta,
	 * senza dover accedere direttamente al campo root (package-private).
	 *
	 * @return true se root è un nodo fogliare
	 */
	public boolean isLeafNode() {
		return root instanceof LeafNode;
	}

	/**
	 * Restituisce il valore di classe predetto, valido solo quando
	 * {@link #isLeafNode()} restituisce true.
	 *
	 * @return il valore di classe predetto dalla foglia
	 */
	public Double getLeafPrediction() {
		return ((LeafNode) root).getPredictedClassValue();
	}

	/**
	 * Restituisce il numero di rami originanti dal nodo di split corrente,
	 * valido solo quando {@link #isLeafNode()} restituisce false.
	 *
	 * @return il numero di figli del nodo di split
	 */
	public int getNumberOfChildren() {
		return root.getNumberOfChildren();
	}

	/**
	 * Concatena le informazioni di ciascun test del nodo di split corrente in
	 * una stringa, una riga per ramo, valido solo quando {@link #isLeafNode()}
	 * restituisce false.
	 *
	 * @return la stringa con le domande relative a ciascun ramo dello split
	 */
	public String formulateQuery() {
		return ((SplitNode) root).formulateQuery();
	}

	/**
	 * Restituisce il sotto-albero figlio individuato dall'indice child,
	 * valido solo quando {@link #isLeafNode()} restituisce false.
	 *
	 * @param child indice del ramo scelto
	 * @return il sotto-albero corrispondente al ramo indicato
	 */
	public RegressionTree getChild(int child) {
		return childTree[child];
	}

	/**
	 * Serializza l'albero di regressione corrente in un file.
	 *
	 * @param nomeFile nome del file in cui salvare l'albero
	 * @throws FileNotFoundException se il file non può essere creato
	 * @throws IOException           se si verifica un errore durante la scrittura
	 */
	public void salva(String nomeFile) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(nomeFile));
		out.writeObject(this);
		out.close();
	}

	/**
	 * Carica un albero di regressione precedentemente salvato in un file.
	 *
	 * @param nomeFile nome del file in cui è salvato l'albero
	 * @return l'albero di regressione contenuto nel file
	 * @throws FileNotFoundException se il file non esiste
	 * @throws IOException           se si verifica un errore durante la lettura
	 * @throws ClassNotFoundException se la classe dell'oggetto serializzato non viene trovata
	 */
	public static RegressionTree carica(String nomeFile) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(nomeFile));
		RegressionTree tree = (RegressionTree) in.readObject();
		in.close();
		return tree;
	}
}