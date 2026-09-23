package tree;

import java.util.ArrayList;
import java.util.List;

import data.Attribute;
import data.ContinuousAttribute;
import data.Data;

/**
 * Estende la classe {@link SplitNode} e modella l'entità nodo di split
 * relativo ad un attributo indipendente continuo.
 */
public class ContinuousNode extends SplitNode {

	/**
	 * Costruttore di classe. Istanzia un oggetto invocando il costruttore
	 * della superclasse con il parametro attribute.
	 *
	 * @param trainingSet         training set completo
	 * @param beginExampelIndex   indice del primo esempio del sotto-insieme di training
	 * @param endExampleIndex     indice dell'ultimo esempio del sotto-insieme di training
	 * @param attribute           attributo indipendente continuo sul quale si definisce lo split
	 */
	public ContinuousNode(Data trainingSet, int beginExampelIndex, int endExampleIndex, ContinuousAttribute attribute) {
		super(trainingSet, beginExampelIndex, endExampleIndex, attribute);
	}

	/**
	 * Determina i possibili split candidati per l'attributo continuo indicato
	 * (fornito dal docente). Sfrutta il fatto che il sotto-insieme di training è
	 * già ordinato rispetto all'attributo: scorre gli esempi, e ogni volta che il
	 * valore cambia valuta un possibile punto di taglio (split binario "&lt;=" / "&gt;"),
	 * calcolando lo SSE complessivo delle due partizioni risultanti. Tiene traccia
	 * del punto di taglio con SSE complessivo minore e lo memorizza in mapSplit.
	 * Rimuove infine il ramo "&gt;" se risultasse vuoto (tutti gli esempi nella
	 * stessa partizione).
	 *
	 * @param trainingSet       training set completo
	 * @param beginExampleIndex indice del primo esempio del sotto-insieme di training
	 * @param endExampleIndex   indice dell'ultimo esempio del sotto-insieme di training
	 * @param attribute         attributo indipendente continuo sul quale si definisce lo split
	 */
	 void setSplitInfo(Data trainingSet,int beginExampleIndex, int endExampleIndex, Attribute attribute){
			//Update mapSplit defined in SplitNode -- contiene gli indici del partizionamento
			Double currentSplitValue= (Double)trainingSet.getExplanatoryValue(beginExampleIndex,attribute.getIndex());
			double bestInfoVariance=0;
			List <SplitInfo> bestMapSplit=null;
			
			for(int i=beginExampleIndex+1;i<=endExampleIndex;i++){
				Double value=(Double)trainingSet.getExplanatoryValue(i,attribute.getIndex());
				if(value.doubleValue()!=currentSplitValue.doubleValue()){
				//	System.out.print(currentSplitValue +" var ");
					double localVariance=new LeafNode(trainingSet, beginExampleIndex,i-1).getVariance();
					double candidateSplitVariance=localVariance;
					localVariance=new LeafNode(trainingSet, i,endExampleIndex).getVariance();
					candidateSplitVariance+=localVariance;
					//System.out.println(candidateSplitVariance);
					if(bestMapSplit==null){
						bestMapSplit=new ArrayList<SplitInfo>();
						bestMapSplit.add(new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
						bestMapSplit.add(new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
						bestInfoVariance=candidateSplitVariance;
					}
					else{		
												
						if(candidateSplitVariance<bestInfoVariance){
							bestInfoVariance=candidateSplitVariance;
							bestMapSplit.set(0, new SplitInfo(currentSplitValue, beginExampleIndex, i-1,0,"<="));
							bestMapSplit.set(1, new SplitInfo(currentSplitValue, i, endExampleIndex,1,">"));
						}
					}
					currentSplitValue=value;
				}
			}
			mapSplit=bestMapSplit;
			//rimuovo split inutili (che includono tutti gli esempi nella stessa partizione)
			
			if((mapSplit.get(1).beginIndex==mapSplit.get(1).getEndIndex())){
				mapSplit.remove(1);
				
			}
			
	 }

	/**
	 * Confronta il valore in input con gli split candidati in mapSplit, usando
	 * il comparatore ("&lt;=" o "&gt;") memorizzato in ciascuno SplitInfo.
	 *
	 * @param value valore continuo dell'attributo che si vuole testare
	 * @return l'indice del ramo (posizione in mapSplit) per cui il test è positivo,
	 *         -1 se nessun ramo corrisponde
	 */
	int testCondition(Object value) {
		double v = ((Double) value).doubleValue();
		for (int i = 0; i < mapSplit.size(); i++) {
			SplitInfo info = mapSplit.get(i);
			double splitValue = ((Double) info.getSplitValue()).doubleValue();
			if (info.getComparator().equals("<=")) {
				if (v <= splitValue)
					return i;
			} else { // ">"
				if (v > splitValue)
					return i;
			}
		}
		return -1; // nessun ramo corrisponde
	}

	/**
	 * Invoca il metodo della superclasse specializzandolo per gli attributi continui.
	 *
	 * @return la rappresentazione testuale del nodo di split continuo
	 */
	public String toString() {
		return "CONTINUOUS " + super.toString();
	}
}
