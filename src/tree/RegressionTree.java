package tree;

import data.Data;
import data.DiscreteAttribute;
import java.util.TreeSet;
import utility.Keyboard;

/**
 * Modella l'intero albero di regressione come insieme di sotto-alberi.
 */
public class RegressionTree {

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
	 * Per ciascun attributo indipendente istanzia il DiscreteNode associato e lo
	 * inserisce in un TreeSet, che mantiene i nodi ordinati per splitVariance
	 * crescente; il primo elemento è quindi lo split migliore. Ordina poi la
	 * porzione di trainingSet corrente (tra begin ed end) rispetto all'attributo
	 * del nodo selezionato.
	 *
	 * @param trainingSet training set completo
	 * @param begin       indice del primo esempio del sotto-insieme
	 * @param end         indice dell'ultimo esempio del sotto-insieme
	 * @return il nodo di split migliore per il sotto-insieme di training
	 */
	SplitNode determineBestSplitNode(Data trainingSet, int begin, int end) {
		TreeSet<SplitNode> ts = new TreeSet<SplitNode>();
		for (int i = 0; i < trainingSet.getNumberOfExplanatoryAttributes(); i++) {
			ts.add(new DiscreteNode(trainingSet, begin, end,
					(DiscreteAttribute) trainingSet.getExplanatoryAttribute(i)));
		}
		// il TreeSet è ordinato per splitVariance crescente: il primo è il migliore
		SplitNode bestNode = ts.first();
		// ogni DiscreteNode ha riordinato il sotto-insieme: riordino per l'attributo vincente
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
	 * Visualizza le informazioni di ciascuno split dell'albero e acquisisce da
	 * tastiera il valore dell'esempio da predire. Se root è una foglia termina
	 * l'acquisizione e restituisce la predizione, altrimenti invoca
	 * ricorsivamente il metodo sul figlio di root in childTree[] individuato
	 * dalla risposta acquisita.
	 *
	 * @return il valore di classe predetto per l'esempio acquisito
	 * @throws UnknownValueException se la risposta non permette di selezionare un ramo valido
	 */
	public Double predictClass() throws UnknownValueException {
		if (root instanceof LeafNode)
			return ((LeafNode) root).getPredictedClassValue();
		else {
			int risp;
			System.out.println(((SplitNode) root).formulateQuery());
			risp = Keyboard.readInt();
			if (risp == -1 || risp >= root.getNumberOfChildren())
				throw new UnknownValueException("The answer should be an integer between 0 and "
						+ (root.getNumberOfChildren() - 1) + "!");
			else
				return childTree[risp].predictClass();
		}
	}
}