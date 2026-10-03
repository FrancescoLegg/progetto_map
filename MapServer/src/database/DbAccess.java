package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Realizza l'accesso alla base di dati MySQL tramite JDBC.
 */
public class DbAccess {

	/** Nome completo della classe del driver JDBC per MySQL. */
	private final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";

	/** Prefisso dell'URL di connessione per il DBMS MySQL. */
	private final String DBMS = "jdbc:mysql";

	/** Identificativo del server su cui risiede la base di dati. */
	private String SERVER = "localhost";

	/** Nome della base di dati. */
	private String DATABASE = "MapDB";

	/** Porta su cui il DBMS MySQL accetta le connessioni. */
	private final String PORT = "3306";

	/** Nome dell'utente per l'accesso alla base di dati. */
	private String USER_ID = "MapUser";

	/** Password di autenticazione per l'utente identificato da USER_ID. */
	private String PASSWORD = "map";

	/** Gestisce la connessione al database. */
	private Connection conn;

	/**
	 * Impartisce al class loader l'ordine di caricare il driver MySQL e
	 * inizializza la connessione. Solleva e propaga una eccezione di tipo
	 * DatabaseConnectionException in caso di fallimento nella connessione
	 * al database (driver non trovato, credenziali errate, server non
	 * raggiungibile).
	 *
	 * @throws DatabaseConnectionException se la connessione al database fallisce
	 */
	public void initConnection() throws DatabaseConnectionException {
		try {
			Class.forName(DRIVER_CLASS_NAME);
		} catch (ClassNotFoundException e) {
			throw new DatabaseConnectionException("Driver non trovato: " + e.toString());
		}

		String connectionUrl = DBMS + "://" + SERVER + ":" + PORT + "/" + DATABASE
				+ "?user=" + USER_ID + "&password=" + PASSWORD
				+ "&serverTimezone=UTC&useSSL=false";
		try {
			conn = DriverManager.getConnection(connectionUrl);
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Connessione al database fallita: " + e.toString());
		}
	}

	/**
	 * Restituisce la connessione al database.
	 *
	 * @return l'oggetto Connection attivo
	 */
	public Connection getConnection() {
		return conn;
	}

	/**
	 * Chiude la connessione al database.
	 */
	public void closeConnection() {
		try {
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			System.out.println(e.toString());
		}
	}
}
