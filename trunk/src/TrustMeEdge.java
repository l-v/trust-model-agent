
import java.awt.Color;

import uchicago.src.sim.gui.DrawableEdge;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.network.DefaultEdge;
import uchicago.src.sim.network.Node;

public class TrustMeEdge extends DefaultEdge implements DrawableEdge {
	private Color color;

	  public TrustMeEdge() {}

	  /**
	   * Construct edge between nodes
	   * @param from
	   * @param to
	   * @param color
	   */
	  public TrustMeEdge(Node from, Node to, Color color) {
	    super(from, to, "");
	    this.color = color;
	  }

	  /**
	   * Sets edge color
	   * @param c
	   */
	  public void setColor(Color c) {
	    color = c;
	  }

	  /** 
	   * Draws edge
	   */
	  public void draw(SimGraphics g, int fromX, int toX, int fromY, int toY) {
	    g.drawDirectedLink(color, fromX, toX, fromY, toY);
	  }
}
