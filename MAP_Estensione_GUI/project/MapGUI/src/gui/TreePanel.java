package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

/**
 * Pannello che disegna graficamente la struttura di un albero di
 * regressione: un riquadro per ogni nodo (azzurro per gli split, verde per
 * le foglie), collegato ai figli da linee etichettate con la condizione di
 * split.
 */
public class TreePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Larghezza di ciascun riquadro-nodo, in pixel. */
	private static final int NODE_WIDTH = 120;

	/** Altezza di ciascun riquadro-nodo, in pixel. */
	private static final int NODE_HEIGHT = 44;

	/** Spazio orizzontale tra due foglie adiacenti, in pixel. */
	private static final int H_GAP = 26;

	/** Spazio verticale tra due livelli dell'albero, in pixel. */
	private static final int V_GAP = 70;

	/** Radice dell'albero attualmente disegnato, null se nessuno. */
	private TreeNodeData root;

	/** Prossimo slot orizzontale libero per una foglia, usato durante il layout. */
	private int nextSlot;

	/** Centro (in coordinate del pannello) di ciascun nodo, calcolato dal layout. */
	private Map<TreeNodeData, Point> centers = new HashMap<TreeNodeData, Point>();

	/**
	 * Imposta l'albero da disegnare e richiede il ridisegno del pannello.
	 *
	 * @param root radice dell'albero da disegnare, o null per svuotare il pannello
	 */
	public void setTree(TreeNodeData root) {
		this.root = root;
		revalidate();
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		if (root == null)
			return new Dimension(500, 300);
		int leaves = root.countLeaves();
		int levels = root.height();
		int width = leaves * (NODE_WIDTH + H_GAP) + H_GAP;
		int height = levels * (NODE_HEIGHT + V_GAP) + V_GAP;
		return new Dimension(Math.max(width, 500), Math.max(height, 300));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (root == null) {
			g2.setColor(Color.GRAY);
			g2.drawString("Nessun albero caricato. Apprendi o carica un albero, poi premi \"Visualizza albero\".", 20, 30);
			return;
		}

		centers.clear();
		nextSlot = 0;
		layoutNode(root, 0);
		drawEdges(g2, root);
		drawNodes(g2, root);
	}

	/**
	 * Calcola ricorsivamente la posizione (centro) di ciascun nodo del
	 * sotto-albero: le foglie occupano slot orizzontali progressivi, i nodi
	 * di split si posizionano al centro dei propri figli.
	 *
	 * @param node  nodo corrente
	 * @param depth profondità del nodo (0 per la radice), usata per la coordinata verticale
	 */
	private void layoutNode(TreeNodeData node, int depth) {
		int y = depth * (NODE_HEIGHT + V_GAP) + V_GAP / 2 + NODE_HEIGHT / 2;

		if (node.leaf || node.children.isEmpty()) {
			int x = nextSlot * (NODE_WIDTH + H_GAP) + H_GAP + NODE_WIDTH / 2;
			nextSlot++;
			centers.put(node, new Point(x, y));
			return;
		}

		int firstX = -1, lastX = -1;
		for (TreeNodeData child : node.children) {
			layoutNode(child, depth + 1);
			Point p = centers.get(child);
			if (firstX == -1)
				firstX = p.x;
			lastX = p.x;
		}
		centers.put(node, new Point((firstX + lastX) / 2, y));
	}

	/**
	 * Disegna ricorsivamente gli archi tra un nodo e i suoi figli, con la
	 * relativa etichetta (la condizione di split) al centro dell'arco.
	 *
	 * @param g2   contesto grafico su cui disegnare
	 * @param node nodo da cui partono gli archi verso i figli
	 */
	private void drawEdges(Graphics2D g2, TreeNodeData node) {
		if (node.leaf)
			return;
		Point p = centers.get(node);
		for (TreeNodeData child : node.children) {
			Point cp = centers.get(child);

			g2.setColor(new Color(130, 130, 130));
			g2.setStroke(new BasicStroke(1.6f));
			g2.draw(new Line2D.Double(p.x, p.y + NODE_HEIGHT / 2.0, cp.x, cp.y - NODE_HEIGHT / 2.0));

			int mx = (p.x + cp.x) / 2;
			int my = (p.y + cp.y) / 2;
			g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 12f));
			FontMetrics fm = g2.getFontMetrics();
			String label = child.edgeLabel;
			int tw = fm.stringWidth(label);

			g2.setColor(Color.WHITE);
			g2.fillRect(mx - tw / 2 - 3, my - fm.getHeight() / 2, tw + 6, fm.getHeight());
			g2.setColor(new Color(60, 60, 60));
			g2.drawString(label, mx - tw / 2, my + fm.getAscent() / 2 - 2);

			drawEdges(g2, child);
		}
	}

	/**
	 * Disegna ricorsivamente i riquadri dei nodi: azzurro per gli split (con
	 * il nome dell'attributo), verde per le foglie (con il valore di classe
	 * predetto).
	 *
	 * @param g2   contesto grafico su cui disegnare
	 * @param node nodo da disegnare, con i suoi discendenti
	 */
	private void drawNodes(Graphics2D g2, TreeNodeData node) {
		Point p = centers.get(node);
		int x = p.x - NODE_WIDTH / 2;
		int y = p.y - NODE_HEIGHT / 2;
		RoundRectangle2D box = new RoundRectangle2D.Double(x, y, NODE_WIDTH, NODE_HEIGHT, 14, 14);

		g2.setColor(node.leaf ? new Color(198, 239, 206) : new Color(198, 219, 239));
		g2.fill(box);
		g2.setColor(new Color(90, 90, 90));
		g2.setStroke(new BasicStroke(1.4f));
		g2.draw(box);

		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
		g2.setColor(Color.BLACK);
		String text = node.leaf ? ("class=" + formatValue(node.leafValue)) : node.attributeLabel;
		FontMetrics fm = g2.getFontMetrics();
		int tw = fm.stringWidth(text);
		g2.drawString(text, p.x - tw / 2, p.y + fm.getAscent() / 2 - 2);

		if (!node.leaf)
			for (TreeNodeData child : node.children)
				drawNodes(g2, child);
	}

	/**
	 * Formatta un valore numerico con due cifre decimali, per una
	 * visualizzazione più compatta; se non è un numero lo restituisce invariato.
	 *
	 * @param value il valore testuale da formattare
	 * @return il valore formattato
	 */
	private String formatValue(String value) {
		try {
			double d = Double.parseDouble(value);
			return String.format("%.2f", d);
		} catch (NumberFormatException e) {
			return value;
		}
	}
}
