package data;

import database.Column;
import database.DatabaseConnectionException;
import database.DbAccess;
import database.EmptySetException;
import database.Example;
import database.TableData;
import database.TableSchema;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rappresenta un training/test set per un problema di apprendimento con
 * attributi esplicativi (discreti o continui) e un attributo di classe
 * continuo, caricato da una tabella di un database relazionale.
 */
public class Data {

	/** Lista delle transazioni (esempi) lette dalla tabella. */
	private List<Example> data = new ArrayList<Example>();

	/** Insieme (lista) degli attributi esplicativi (descrittivi) del dataset. */
	private List<Attribute> explanatorySet = new LinkedList<Attribute>();

	/** Attributo di classe, sempre continuo. */
	private ContinuousAttribute classAttribute;

	/**
	 * Costruisce il dataset caricando schema ed esempi di addestramento dalla
	 * tabella di nome tableName presente nel database.
	 *
	 * @param tableName nome della tabella contenente i dati di training
	 * @throws TrainingDataException se la connessione al database fallisce, la
	 *                                tabella non esiste, ha meno di due colonne,
	 *                                ha zero tuple, oppure l'attributo
	 *                                corrispondente all'ultima colonna non è numerico
	 */
	public Data(String tableName) throws TrainingDataException {

		DbAccess db = new DbAccess();
		try {
			db.initConnection();
		} catch (DatabaseConnectionException e) {
			throw new TrainingDataException(e.toString());
		}

		try {
			TableSchema schema;
			try {
				schema = new TableSchema(db, tableName);
			} catch (SQLException e) {
				throw new TrainingDataException("Errore nell'accesso alla tabella " + tableName + ": " + e);
			}

			if (schema.getNumberOfAttributes() < 2)
				throw new TrainingDataException("La tabella " + tableName + " deve avere almeno due colonne");

			Column classColumn = schema.getColumn(schema.getNumberOfAttributes() - 1);
			if (!classColumn.isNumber())
				throw new TrainingDataException("L'attributo di classe (ultima colonna) non è numerico");

			TableData tableData = new TableData(db);

			// popolo explanatorySet dalle colonne, tranne l'ultima (attributo di classe)
			for (int i = 0; i < schema.getNumberOfAttributes() - 1; i++) {
				Column c = schema.getColumn(i);
				if (c.isNumber()) {
					explanatorySet.add(new ContinuousAttribute(c.getColumnName(), i));
				} else {
					Set<String> discreteValues = new TreeSet<String>();
					try {
						for (Object v : tableData.getDistinctColumnValues(tableName, c))
							discreteValues.add((String) v);
					} catch (SQLException e) {
						throw new TrainingDataException("Errore nel recupero dei valori di " + c.getColumnName() + ": " + e);
					}
					explanatorySet.add(new DiscreteAttribute(c.getColumnName(), i, discreteValues));
				}
			}

			classAttribute = new ContinuousAttribute(classColumn.getColumnName(), schema.getNumberOfAttributes() - 1);

			try {
				data = tableData.getTransazioni(tableName);
			} catch (SQLException e) {
				throw new TrainingDataException("Errore nell'esecuzione della query su " + tableName + ": " + e);
			} catch (EmptySetException e) {
				throw new TrainingDataException("La tabella " + tableName + " non contiene tuple");
			}

		} finally {
			db.closeConnection();
		}
	}

	/**
	 * Restituisce il numero di esempi presenti nel dataset.
	 *
	 * @return il numero di esempi (righe) del dataset
	 */
	public int getNumberOfExamples() {
		return data.size();
	}

	/**
	 * Restituisce il numero di attributi esplicativi del dataset.
	 *
	 * @return il numero di attributi esplicativi
	 */
	public int getNumberOfExplanatoryAttributes() {
		return explanatorySet.size();
	}

	/**
	 * Restituisce il valore dell'attributo di classe per l'esempio indicato.
	 *
	 * @param exampleIndex indice dell'esempio (riga) di cui si vuole il valore di classe
	 * @return il valore (continuo) della classe per l'esempio richiesto
	 */
	public Double getClassValue(int exampleIndex) {
		return (Double) data.get(exampleIndex).get(explanatorySet.size());
	}

	/**
	 * Restituisce il valore di un attributo esplicativo per un dato esempio.
	 *
	 * @param exampleIndex   indice dell'esempio (riga)
	 * @param attributeIndex indice dell'attributo esplicativo (colonna)
	 * @return il valore dell'attributo esplicativo richiesto per l'esempio indicato
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data.get(exampleIndex).get(attributeIndex);
	}

	/**
	 * Restituisce l'attributo esplicativo in posizione {@code index}.
	 *
	 * @param index indice dell'attributo esplicativo richiesto
	 * @return l'attributo esplicativo corrispondente all'indice indicato
	 */
	public Attribute getExplanatoryAttribute(int index) {
		return explanatorySet.get(index);
	}

	/**
	 * Restituisce l'attributo di classe del dataset.
	 *
	 * @return l'attributo di classe (continuo)
	 */
	ContinuousAttribute getClassAttribute() {
		return classAttribute;
	}

	/**
	 * Restituisce una rappresentazione testuale del dataset: ogni riga
	 * corrisponde a un esempio.
	 *
	 * @return la rappresentazione testuale del dataset
	 */
	public String toString() {
		String value = "";
		for (Example e : data)
			value += e.toString() + "\n";
		return value;
	}

	/**
	 * Ordina il sottoinsieme di esempi compreso tra {@code beginExampleIndex} e
	 * {@code endExampleIndex} (estremi inclusi) rispetto ai valori assunti
	 * dall'attributo indicato, utilizzando l'algoritmo quicksort.
	 *
	 * @param attribute         attributo rispetto al quale ordinare gli esempi
	 * @param beginExampleIndex indice di inizio (incluso) del sottoinsieme da ordinare
	 * @param endExampleIndex   indice di fine (incluso) del sottoinsieme da ordinare
	 */
	public void sort(Attribute attribute, int beginExampleIndex, int endExampleIndex) {

		quicksort(attribute, beginExampleIndex, endExampleIndex);
	}

	/**
	 * Scambia tra loro (in place) l'esempio di indice {@code i} con quello di
	 * indice {@code j} nella lista dei dati.
	 *
	 * @param i indice del primo esempio da scambiare
	 * @param j indice del secondo esempio da scambiare
	 */
	private void swap(int i, int j) {
		Collections.swap(data, i, j);
	}

	/**
	 * Partiziona il vettore degli esempi, compresi tra gli indici {@code inf} e
	 * {@code sup}, rispetto al valore (di tipo stringa) assunto dall'attributo
	 * discreto indicato nell'elemento centrale, e restituisce il punto di
	 * separazione (pivot) risultante dalla partizione.
	 *
	 * @param attribute attributo discreto rispetto al quale partizionare
	 * @param inf       indice inferiore (incluso) dell'intervallo da partizionare
	 * @param sup       indice superiore (incluso) dell'intervallo da partizionare
	 * @return l'indice della posizione di separazione (pivot) dopo la partizione
	 */
	private int partition(DiscreteAttribute attribute, int inf, int sup) {
		int i, j;

		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		String x = (String) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {

			while (i <= sup && ((String) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;

			}

			while (((String) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;

			}

			if (i < j) {
				swap(i, j);
			} else
				break;
		}
		swap(inf, j);
		return j;

	}

	/**
	 * Partiziona il vettore degli esempi, compresi tra gli indici {@code inf} e
	 * {@code sup}, rispetto al valore (numerico) assunto dall'attributo
	 * continuo indicato nell'elemento centrale, e restituisce il punto di
	 * separazione (pivot) risultante dalla partizione.
	 *
	 * @param attribute attributo continuo rispetto al quale partizionare
	 * @param inf       indice inferiore (incluso) dell'intervallo da partizionare
	 * @param sup       indice superiore (incluso) dell'intervallo da partizionare
	 * @return l'indice della posizione di separazione dopo la partizione
	 */
	private int partition(ContinuousAttribute attribute, int inf, int sup) {
		int i, j;

		i = inf;
		j = sup;
		int med = (inf + sup) / 2;
		Double x = (Double) getExplanatoryValue(med, attribute.getIndex());
		swap(inf, med);

		while (true) {

			while (i <= sup && ((Double) getExplanatoryValue(i, attribute.getIndex())).compareTo(x) <= 0) {
				i++;

			}

			while (((Double) getExplanatoryValue(j, attribute.getIndex())).compareTo(x) > 0) {
				j--;

			}

			if (i < j) {
				swap(i, j);
			} else
				break;
		}
		swap(inf, j);
		return j;

	}

	/**
	 * Algoritmo quicksort per l'ordinamento (ricorsivo, in place) degli esempi
	 * compresi tra gli indici {@code inf} e {@code sup}, rispetto ai valori
	 * assunti dall'attributo indicato, usando come relazione d'ordine totale
	 * "&lt;=". Sfrutta l'RTTI per distinguere se l'attributo è discreto o
	 * continuo e invocare la relativa partizione.
	 *
	 * @param attribute attributo (discreto o continuo) rispetto al quale ordinare
	 * @param inf       indice inferiore (incluso) dell'intervallo da ordinare
	 * @param sup       indice superiore (incluso) dell'intervallo da ordinare
	 */
	private void quicksort(Attribute attribute, int inf, int sup) {

		if (sup >= inf) {

			int pos;
			if (attribute instanceof DiscreteAttribute)
				pos = partition((DiscreteAttribute) attribute, inf, sup);
			else
				pos = partition((ContinuousAttribute) attribute, inf, sup);

			if ((pos - inf) < (sup - pos + 1)) {
				quicksort(attribute, inf, pos - 1);
				quicksort(attribute, pos + 1, sup);
			} else {
				quicksort(attribute, pos + 1, sup);
				quicksort(attribute, inf, pos - 1);
			}

		}

	}

	/**
	 * Metodo di test: carica il dataset dalla tabella {@code provaC} e lo stampa.
	 *
	 * @param args argomenti da riga di comando (non utilizzati)
	 * @throws TrainingDataException se il training set non viene acquisito correttamente
	 */
	public static void main(String args[]) throws TrainingDataException {
		Data trainingSet = new Data("provaC");
		System.out.println(trainingSet);
	}

}
