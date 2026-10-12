package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Finestra principale della GUI di visualizzazione dell'albero di
 * regressione. Si collega a MapServer tramite lo stesso protocollo usato
 * dal client testuale, e aggiunge il comando 4 per ottenere e disegnare la
 * struttura dell'albero.
 */
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	/** Connessione al server, condivisa tra i gestori dei bottoni. */
	private ServerConnection connection = new ServerConnection();

	private JTextField hostField = new JTextField("127.0.0.1", 10);
	private JTextField portField = new JTextField("8080", 5);
	private JButton connectButton = new JButton("Connetti");

	private JTextField tableField = new JTextField("provaC", 10);
	private JButton learnButton = new JButton("Apprendi da tabella");

	private JTextField archiveField = new JTextField("provaC", 10);
	private JButton loadButton = new JButton("Carica archivio");

	private JButton visualizeButton = new JButton("Visualizza albero");

	private JLabel statusLabel = new JLabel("Non connesso");

	private TreePanel treePanel = new TreePanel();

	/**
	 * Costruisce la finestra principale e inizializza l'interfaccia.
	 */
	public MainWindow() {
		super("Visualizzatore Albero di Regressione");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		add(buildControlPanel(), BorderLayout.NORTH);
		add(new JScrollPane(treePanel), BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);

		setLearnLoadEnabled(false);
		visualizeButton.setEnabled(false);

		connectButton.addActionListener(e -> onConnect());
		learnButton.addActionListener(e -> onLearn());
		loadButton.addActionListener(e -> onLoadArchive());
		visualizeButton.addActionListener(e -> onVisualize());

		setSize(900, 650);
		setLocationRelativeTo(null);
	}

	/**
	 * Costruisce il pannello dei controlli nella parte superiore della finestra.
	 *
	 * @return il pannello pronto per essere aggiunto alla finestra
	 */
	private JPanel buildControlPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

		panel.add(new JLabel("Host:"));
		panel.add(hostField);
		panel.add(new JLabel("Porta:"));
		panel.add(portField);
		panel.add(connectButton);

		panel.add(new JLabel("  Tabella:"));
		panel.add(tableField);
		panel.add(learnButton);

		panel.add(new JLabel("  Archivio:"));
		panel.add(archiveField);
		panel.add(loadButton);

		panel.add(visualizeButton);

		return panel;
	}

	/**
	 * Abilita o disabilita i controlli di apprendimento/caricamento,
	 * disponibili solo dopo la connessione al server.
	 *
	 * @param enabled true per abilitarli
	 */
	private void setLearnLoadEnabled(boolean enabled) {
		learnButton.setEnabled(enabled);
		loadButton.setEnabled(enabled);
	}

	/**
	 * Gestisce il bottone "Connetti": apre la connessione al server in
	 * background, senza bloccare l'interfaccia grafica.
	 */
	private void onConnect() {
		String host = hostField.getText().trim();
		int port;
		try {
			port = Integer.parseInt(portField.getText().trim());
		} catch (NumberFormatException e) {
			statusLabel.setText("Porta non valida");
			return;
		}

		connectButton.setEnabled(false);
		statusLabel.setText("Connessione a " + host + ":" + port + " in corso...");

		new SwingWorker<Void, Void>() {
			private Exception error;

			@Override
			protected Void doInBackground() {
				try {
					connection.connect(host, port);
				} catch (Exception e) {
					error = e;
				}
				return null;
			}

			@Override
			protected void done() {
				if (error != null) {
					statusLabel.setText("Connessione fallita: " + error);
					connectButton.setEnabled(true);
				} else {
					statusLabel.setText("Connesso a " + host + ":" + port);
					setLearnLoadEnabled(true);
				}
			}
		}.execute();
	}

	/**
	 * Gestisce il bottone "Apprendi da tabella": invia i comandi 0 e 1 al
	 * server in background.
	 */
	private void onLearn() {
		String table = tableField.getText().trim();
		setButtonsEnabledDuringOperation(false);
		statusLabel.setText("Apprendimento da " + table + " in corso...");

		new SwingWorker<String, Void>() {
			private Exception error;

			@Override
			protected String doInBackground() {
				try {
					return connection.learnFromTable(table);
				} catch (Exception e) {
					error = e;
					return null;
				}
			}

			@Override
			protected void done() {
				setButtonsEnabledDuringOperation(true);
				if (error != null) {
					statusLabel.setText("Errore di comunicazione: " + error);
					return;
				}
				String result = getSafeResult();
				if ("OK".equals(result)) {
					statusLabel.setText("Albero appreso da " + table);
					visualizeButton.setEnabled(true);
				} else {
					statusLabel.setText(result);
					visualizeButton.setEnabled(false);
				}
			}

			private String getSafeResult() {
				try {
					return get();
				} catch (Exception e) {
					return "Errore: " + e;
				}
			}
		}.execute();
	}

	/**
	 * Gestisce il bottone "Carica archivio": invia il comando 2 al server in
	 * background.
	 */
	private void onLoadArchive() {
		String archive = archiveField.getText().trim();
		setButtonsEnabledDuringOperation(false);
		statusLabel.setText("Caricamento di " + archive + " in corso...");

		new SwingWorker<String, Void>() {
			private Exception error;

			@Override
			protected String doInBackground() {
				try {
					return connection.loadArchive(archive);
				} catch (Exception e) {
					error = e;
					return null;
				}
			}

			@Override
			protected void done() {
				setButtonsEnabledDuringOperation(true);
				if (error != null) {
					statusLabel.setText("Errore di comunicazione: " + error);
					return;
				}
				String result = getSafeResult();
				if ("OK".equals(result)) {
					statusLabel.setText("Albero caricato da " + archive);
					visualizeButton.setEnabled(true);
				} else {
					statusLabel.setText(result);
					visualizeButton.setEnabled(false);
				}
			}

			private String getSafeResult() {
				try {
					return get();
				} catch (Exception e) {
					return "Errore: " + e;
				}
			}
		}.execute();
	}

	/**
	 * Gestisce il bottone "Visualizza albero": invia il comando 4 al server
	 * in background e passa il risultato a {@link TreePanel}.
	 */
	private void onVisualize() {
		setButtonsEnabledDuringOperation(false);
		statusLabel.setText("Richiesta struttura albero in corso...");

		new SwingWorker<TreeNodeData, Void>() {
			private Exception error;

			@Override
			protected TreeNodeData doInBackground() {
				try {
					return connection.fetchTreeSnapshot();
				} catch (Exception e) {
					error = e;
					return null;
				}
			}

			@Override
			protected void done() {
				setButtonsEnabledDuringOperation(true);
				if (error != null) {
					statusLabel.setText("Impossibile visualizzare l'albero: " + error.getMessage());
					return;
				}
				try {
					treePanel.setTree(get());
					statusLabel.setText("Albero visualizzato");
				} catch (Exception e) {
					statusLabel.setText("Errore: " + e);
				}
			}
		}.execute();
	}

	/** Stato di abilitazione di "Visualizza albero" prima dell'ultima operazione in corso. */
	private boolean visualizeWasEnabled = false;

	/**
	 * Disabilita (o riabilita) i tre bottoni di comando durante un'operazione
	 * di rete, per evitare richieste concorrenti sulla stessa connessione.
	 * Quando disabilita, ricorda lo stato di "Visualizza albero" per
	 * ripristinarlo correttamente; il chiamante può comunque sovrascriverlo
	 * esplicitamente in base all'esito dell'operazione appena conclusa.
	 *
	 * @param enabled false per disabilitarli (inizio operazione), true per
	 *                riabilitarli (fine operazione)
	 */
	private void setButtonsEnabledDuringOperation(boolean enabled) {
		if (!enabled)
			visualizeWasEnabled = visualizeButton.isEnabled();
		learnButton.setEnabled(enabled);
		loadButton.setEnabled(enabled);
		visualizeButton.setEnabled(enabled ? visualizeWasEnabled : false);
	}

	/**
	 * Avvia l'applicazione, creando ed esponendo la finestra principale sull'Event Dispatch Thread.
	 *
	 * @param args argomenti da riga di comando (non utilizzati)
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
	}
}
