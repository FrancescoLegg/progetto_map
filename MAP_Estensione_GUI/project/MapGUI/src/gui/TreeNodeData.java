package gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresentazione, lato client, della struttura di un albero di
 * regressione ricevuta dal server tramite il comando di rete 4.
 * Non dipende in alcun modo dalle classi del package {@code tree} del
 * server: è un semplice contenitore di dati, costruito leggendo lo stream
 * secondo il protocollo definito in {@code ServerOneClient.sendTreeSnapshot}.
 */
public class TreeNodeData {

	/** true se il nodo è una foglia. */
	public boolean leaf;

	/** Valore di classe predetto, valido solo se {@link #leaf} è true. */
	public String leafValue;

	/** Etichetta da mostrare sull'arco che porta a questo nodo (vuota per la radice). */
	public String edgeLabel = "";

	/** Nome dell'attributo di split, da mostrare dentro il riquadro, valido solo se {@link #leaf} è false. */
	public String attributeLabel = "";

	/** Figli del nodo, valido solo se {@link #leaf} è false. */
	public List<TreeNodeData> children = new ArrayList<TreeNodeData>();

	/**
	 * Restituisce il numero totale di foglie nel sotto-albero, usato dal
	 * pannello di disegno per calcolare la larghezza necessaria.
	 *
	 * @return il numero di foglie del sotto-albero radicato in questo nodo
	 */
	public int countLeaves() {
		if (leaf)
			return 1;
		int count = 0;
		for (TreeNodeData child : children)
			count += child.countLeaves();
		return Math.max(count, 1);
	}

	/**
	 * Restituisce l'altezza (numero di livelli) del sotto-albero.
	 *
	 * @return l'altezza del sotto-albero radicato in questo nodo
	 */
	public int height() {
		if (leaf || children.isEmpty())
			return 1;
		int max = 0;
		for (TreeNodeData child : children)
			max = Math.max(max, child.height());
		return 1 + max;
	}
}
