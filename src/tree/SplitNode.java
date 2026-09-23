package tree;

import data.Attribute;
import data.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe astratta per modellare l'astrazione dell'entità nodo di split
 * (continuo o discreto) dell'albero di decisione.
 */
abstract class SplitNode extends Node implements Comparable<SplitNode> {

	/** Identificativo di versione per la serializzazione. */
	private static final long serialVersionUID = 1L;

	/**
	 * Inner class che aggrega tutte le informazioni riguardanti un nodo di split:
	 * il valore che definisce lo split, l'intervallo di esempi coperto e
	 * l'operatore di confronto usato nel test.
	 */
	class SplitInfo implements Serializable {

		/** Identificativo di versione per la serializzazione. */
		private static final long serialVersionUID = 1L;

		/** Valore (di un attributo indipendente) che definisce lo split. */
		Object splitValue;

		/** Indice del primo esempio coperto da questa partizione. */
		int beginIndex;

		/** Indice dell'ultimo esempio coperto da questa partizione. */
		int endIndex;

		/** Numero dello split (nodo figlio) originante dal nodo corrente. */
		int numberChild;

		/** Operatore matematico che definisce il test nel nodo corrente ("=" per valori discreti). */
		String comparator="=";

		/**
		 * Costruttore per split a valori discreti: usa il comparatore di default "=".
		 *
		 * @param splitValue  valore che definisce lo split
		 * @param beginIndex  indice del primo esempio coperto dalla partizione
		 * @param endIndex    indice dell'ultimo esempio coperto dalla partizione
		 * @param numberChild numero dello split (nodo figlio) originante
		 */
		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild){
			this.splitValue=splitValue;
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
			this.numberChild=numberChild;
		}

		/**
		 * Costruttore per split generici (usato per valori continui), con
		 * comparatore esplicito.
		 *
		 * @param splitValue  valore che definisce lo split
		 * @param beginIndex  indice del primo esempio coperto dalla partizione
		 * @param endIndex    indice dell'ultimo esempio coperto dalla partizione
		 * @param numberChild numero dello split (nodo figlio) originante
		 * @param comparator  operatore matematico che definisce il test ("&lt;=" o "&gt;")
		 */
		SplitInfo(Object splitValue,int beginIndex,int endIndex,int numberChild, String comparator){
			this.splitValue=splitValue;
			this.beginIndex=beginIndex;
			this.endIndex=endIndex;
			this.numberChild=numberChild;
			this.comparator=comparator;
		}

		/**
		 * Restituisce l'indice del primo esempio coperto dalla partizione.
		 *
		 * @return l'indice di inizio della partizione
		 */
		int getBeginindex(){
			return beginIndex;			
		}

		/**
		 * Restituisce l'indice dell'ultimo esempio coperto dalla partizione.
		 *
		 * @return l'indice di fine della partizione
		 */
		int getEndIndex(){
			return endIndex;
		}

		/**
		 * Restituisce il valore che definisce lo split.
		 *
		 * @return il valore dello split
		 */
		 Object getSplitValue(){
			return splitValue;
		}

		/**
		 * Concatena in una String le informazioni della partizione: numero
		 * del ramo, operatore, valore di split e intervallo di esempi coperto.
		 *
		 * @return la rappresentazione testuale della partizione
		 */
		public String toString(){
			return "child " + numberChild +" split value"+comparator+splitValue + "[Examples:"+beginIndex+"-"+endIndex+"]";
		}

		/**
		 * Restituisce l'operatore matematico che definisce il test.
		 *
		 * @return il comparatore ("=", "&lt;=" o "&gt;")
		 */
		 String getComparator(){
			return comparator;
		}
	
		
	}

	/** Attributo indipendente sul quale lo split è generato. */
	Attribute attribute;	

	/** Lista degli split candidati, uno per ciascuna partizione generata. */
	List<SplitInfo> mapSplit = new ArrayList<SplitInfo>();
	
	/** Valore di varianza (SSE) risultante dal partizionamento indotto dallo split corrente. */
	double splitVariance;
		
	/**
	 * Genera le informazioni necessarie per ciascuno degli split candidati e
	 * popola mapSplit. Metodo astratto: l'implementazione dipende dal tipo
	 * (discreto o continuo) dell'attributo usato per lo split.
	 *
	 * @param trainingSet         training set completo
	 * @param beginExampelIndex   indice del primo esempio del sotto-insieme di training
	 * @param endExampleIndex     indice dell'ultimo esempio del sotto-insieme di training
	 * @param attribute           attributo indipendente sul quale si definisce lo split
	 */
	abstract void setSplitInfo(Data trainingSet,int beginExampelIndex, int endExampleIndex, Attribute attribute);
	
	/**
	 * Modella la condizione di test rispetto a tutti gli split candidati:
	 * ad ogni valore di test corrisponde un ramo dello split. Metodo astratto:
	 * l'implementazione dipende dal tipo (discreto o continuo) dell'attributo.
	 *
	 * @param value valore dell'attributo che si vuole testare rispetto a tutti gli split
	 * @return l'indice del ramo (posizione in mapSplit) per cui il test è positivo
	 */
	abstract int testCondition (Object value);
	
	/**
	 * Costruttore di classe. Invoca il costruttore della superclasse, ordina i
	 * valori dell'attributo di input per gli esempi tra beginExampleIndex ed
	 * endExampleIndex e sfrutta questo ordinamento per determinare i possibili
	 * split e popolare mapSplit. Computa infine lo SSE (splitVariance) per
	 * l'attributo usato nello split, come somma degli SSE calcolati su
	 * ciascuna partizione collezionata in mapSplit.
	 *
	 * @param trainingSet         training set completo
	 * @param beginExampleIndex   indice del primo esempio del sotto-insieme di training
	 * @param endExampleIndex     indice dell'ultimo esempio del sotto-insieme di training
	 * @param attribute           attributo indipendente sul quale si definisce lo split
	 */
	SplitNode(Data trainingSet, int beginExampleIndex, int endExampleIndex, Attribute attribute){
			super(trainingSet, beginExampleIndex,endExampleIndex);
			this.attribute=attribute;
			trainingSet.sort(attribute, beginExampleIndex, endExampleIndex); // order by attribute
			setSplitInfo(trainingSet, beginExampleIndex, endExampleIndex, attribute);
						
			//compute variance
			splitVariance=0;
			for(int i=0;i<mapSplit.size();i++){
					double localVariance=new LeafNode(trainingSet, mapSplit.get(i).getBeginindex(),mapSplit.get(i).getEndIndex()).getVariance();
					splitVariance+=(localVariance);
			}
	}
	
	/**
	 * Restituisce l'attributo usato per lo split.
	 *
	 * @return l'attributo indipendente sul quale è definito lo split
	 */
	Attribute getAttribute(){
		return attribute;
	}
	
	/**
	 * Restituisce la varianza (SSE) risultante dal partizionamento indotto
	 * dallo split corrente.
	 *
	 * @return il valore di splitVariance
	 */
	double getVariance(){
		return splitVariance;
	}
	
	/**
	 * Restituisce il numero dei rami (nodi figli) originanti dal nodo corrente.
	 *
	 * @return la cardinalità di mapSplit
	 */
	int getNumberOfChildren(){
		return mapSplit.size();
	}
	
	/**
	 * Restituisce le informazioni per il ramo indicizzato da child in mapSplit.
	 *
	 * @param child indice del ramo richiesto
	 * @return lo SplitInfo corrispondente al ramo indicato
	 */
	SplitInfo getSplitInfo(int child){
		return mapSplit.get(child);
	}

	
	/**
	 * Concatena le informazioni di ciascun test (attributo, operatore e
	 * valore) in una String finale, una riga per ciascun ramo. Necessario
	 * per la predizione di nuovi esempi.
	 *
	 * @return la stringa con le domande relative a ciascun ramo dello split
	 */
	String formulateQuery(){
		String query = "";
		for(int i=0;i<mapSplit.size();i++)
			query+= (i + ":" + attribute + mapSplit.get(i).getComparator() +mapSplit.get(i).getSplitValue())+"\n";
		return query;
	}
	
	/**
	 * Concatena le informazioni di ciascun test (attributo, esempi coperti,
	 * varianza di split) in una String finale.
	 *
	 * @return la rappresentazione testuale del nodo di split
	 */
	public String toString(){
		String v= "SPLIT : attribute=" +attribute +" "+ super.toString()+  " Split Variance: " + getVariance()+ "\n" ;
		
		for(int i=0;i<mapSplit.size();i++){
			v+= "\t"+mapSplit.get(i)+"\n";
		}
		
		return v;
	}

	/**
	 * Confronta il nodo corrente con quello in input rispetto alla splitVariance.
	 *
	 * @param o nodo di split da confrontare con il nodo corrente
	 * @return 0 se le varianze sono uguali, -1 se quella del nodo corrente è minore,
	 *         1 se è maggiore
	 */
	public int compareTo(SplitNode o) {
		if (this.getVariance() == o.getVariance())
			return 0;
		else if (this.getVariance() < o.getVariance())
			return -1;
		else
			return 1;
	}
}
