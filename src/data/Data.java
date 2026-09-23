package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rappresenta un training/test set per un problema di apprendimento con
 * attributi esplicativi (discreti) e un attributo di classe continuo.
 */
public class Data {

	/** Matrice dei dati: righe = esempi, colonne = attributi esplicativi + classe. */
	private Object data[][];

	/** Numero di esempi (righe) presenti nel dataset. */
	private int numberOfExamples;

	/** Insieme (lista) degli attributi esplicativi (descrittivi) del dataset. */
	private List<Attribute> explanatorySet = new LinkedList<Attribute>();

	/** Attributo di classe, sempre continuo. */
	private ContinuousAttribute classAttribute;

	/**
	 * Costruisce il dataset leggendo lo schema e i dati dal file indicato.
	 *
	 * @param fileName nome del file contenente il training set
	 * @throws TrainingDataException se il file è inesistente, lo schema è mancante
	 *                               o errato, il training set è vuoto o privo di
	 *                               variabile target numerica
	 */
	public Data(String fileName) throws TrainingDataException {

		File inFile = new File(fileName);

		Scanner sc;
		try {
			sc = new Scanner(inFile);
		} catch (FileNotFoundException e) {
			throw new TrainingDataException(e.toString());
		}

		try {
			String line = sc.nextLine();
			if (!line.contains("@schema"))
				throw new TrainingDataException("Errore nello schema");
			String s[] = line.split(" ");

			Integer.parseInt(s[1]);
			short iAttribute = 0;
			line = sc.nextLine();
			while (!line.contains("@data")) {
				s = line.split(" ");
				if (s[0].equals("@desc")) { // aggiungo l'attributo allo spazio descrittivo
                    if(s.length >= 3){
                        // @desc motor discrete  A,B,C,D,E -> attributo discreto
                        Set<String>discreteValues = new TreeSet<String>();
                        for(String v: s[2].split(","))
                            discreteValues.add(v);
                        explanatorySet.add(new DiscreteAttribute(s[1], iAttribute, discreteValues));
                    }else
                        // @desc Y -> attributo continuo
                        explanatorySet.add(new ContinuousAttribute(s[1],iAttribute));
                }else if(s[0].equals("@target"))
                    classAttribute = new  ContinuousAttribute(s[1], iAttribute);
				iAttribute++;
				line = sc.nextLine();

			}

			if (classAttribute == null)
				throw new TrainingDataException("Schema privo della variabile target");

			// avvalorare numero di esempi
			numberOfExamples = new Integer(line.split(" ")[1]);
			if (numberOfExamples <= 0)
				throw new TrainingDataException("Training set vuoto");

			// popolare data
			data = new Object[numberOfExamples][explanatorySet.size() + 1];
			short iRow = 0;
			while (sc.hasNextLine()) {
				line = sc.nextLine();
				s = line.split(","); // E,E,5,4, 0.28125095
				for (short jColumn = 0; jColumn < s.length - 1; jColumn++){
					if(explanatorySet.get(jColumn) instanceof ContinuousAttribute)
                        data[iRow][jColumn] = new Double(s[jColumn]);
                    else
                        data[iRow][jColumn] = s[jColumn];
                }
				data[iRow][s.length - 1] = new Double(s[s.length - 1]);
				iRow++;

			}
		} catch (NoSuchElementException e) {
			throw new TrainingDataException("Errore nello schema: file incompleto");
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			throw new TrainingDataException("Errore nel formato dei dati: " + e);
		} finally {
			sc.close();
		}
	}

	/**
	 * Restituisce il numero di esempi presenti nel dataset.
	 *
	 * @return il numero di esempi (righe) del dataset
	 */
	public int getNumberOfExamples() {
		return numberOfExamples;
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
		return (Double) data[exampleIndex][explanatorySet.size()];
	}

	/**
	 * Restituisce il valore di un attributo esplicativo per un dato esempio.
	 *
	 * @param exampleIndex   indice dell'esempio (riga)
	 * @param attributeIndex indice dell'attributo esplicativo (colonna)
	 * @return il valore dell'attributo esplicativo richiesto per l'esempio indicato
	 */
	public Object getExplanatoryValue(int exampleIndex, int attributeIndex) {
		return data[exampleIndex][attributeIndex];
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
	 * corrisponde a un esempio, con i valori degli attributi esplicativi e
	 * il valore di classe separati da virgola.
	 *
	 * @return la rappresentazione testuale del dataset
	 */
	public String toString() {
		String value = "";
		for (int i = 0; i < numberOfExamples; i++) {
			for (int j = 0; j < explanatorySet.size(); j++)
				value += data[i][j] + ",";

			value += data[i][explanatorySet.size()] + "\n";
		}
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
	 * Scambia tra loro (in place) l'intera riga (esempio) di indice {@code i}
	 * con quella di indice {@code j} nella matrice dei dati.
	 *
	 * @param i indice del primo esempio da scambiare
	 * @param j indice del secondo esempio da scambiare
	 */
	private void swap(int i, int j) {
		Object temp;
		for (int k = 0; k < getNumberOfExplanatoryAttributes() + 1; k++) {
			temp = data[i][k];
			data[i][k] = data[j][k];
			data[j][k] = temp;
		}

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
	 * @return l'indice della posizione di separazione dopo la partizione
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
	 * Metodo di test: carica il dataset {@code servo.dat}, lo stampa, e poi,
	 * per ciascun attributo esplicativo, ordina l'intero dataset rispetto a
	 * quell'attributo e ne stampa il risultato.
	 *
	 * @param args argomenti da riga di comando (non utilizzati)
	 * @throws TrainingDataException se il training set non viene acquisito correttamente
	 */
	public static void main(String args[]) throws TrainingDataException {
		Data trainingSet = new Data("servo.dat");
		System.out.println(trainingSet);

		for (int jColumn = 0; jColumn < trainingSet.getNumberOfExplanatoryAttributes(); jColumn++) {
			System.out.println("ORDER BY " + trainingSet.getExplanatoryAttribute(jColumn));
			trainingSet.quicksort(trainingSet.getExplanatoryAttribute(jColumn), 0, trainingSet.getNumberOfExamples() - 1);
			System.out.println(trainingSet);
		}

	}

}
