package data;

/**
 * Questa classe astratta modella un generico attributo discreto o continuo.
 */
public abstract class Attribute {
    /** Nome simbolico dell'attributo. */
    String name;
    
    /** Identificativo numerico dell'attributo. */
    int index;

    /**
     * Costruttore di classe. Inizializza i valori dei membri name e index.
     *
     * @param name  nome simbolico dell'attributo
     * @param index identificativo numerico dell'attributo
     */
    Attribute(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     * Restituisce il valore nel membro name.
     * 
     * @return il nome dell'attributo
     */
    public String getName() {
        return name;
    }

    /**
     * Restituisce il valore nel membro index.
     *
     * @return l'identificativo numerico dell'attributo
     */
    public int getIndex() {
        return index;
    }

    @Override
   public String toString() {
       return name;
   }
}
