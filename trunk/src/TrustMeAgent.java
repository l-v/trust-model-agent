

import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TrustMeAgent implements Drawable {
	private int who;
	private int group;
	private int x, y;
	private Color color;
	private Object2DTorus space;
	
	
	//decision variables
	//or rather, abilities of each player that will enable to calculate
	// the trust the other player has on him
	// I'd say the values range within 0 to 1 to make it easier
	// something like... :
	Map<String,Double> traits = new HashMap<String,Double>();
	
	
	
	// record of trust placed on other agents
	// make list of all other agents with respective trust?
	// confusing, save for later on... 
	// Map<agentID (on model), trustPlaced>
	Map<Integer, Double> agentTrust;
	
	

	public TrustMeAgent(Object2DTorus space, int who){
		this.who = who;
		this.group = who;
		this.space = space;
		
		agentTrust = new HashMap<Integer, Double>();
		
		// Initialize frequency table from command line
		/*for (String a : args) {
            Integer freq = m.get(a);
            m.put(a, (freq == null) ? 1 : freq + 1);
        }*/
	}

	public void draw(SimGraphics g) {
		g.drawFastCircle(color);
	}

	public void setXY(int x, int y) {
		if (space.getObjectAt(this.x, this.y)==this)
			space.putObjectAt(this.x, this.y,null);
		this.x = x;
		this.y = y;
		space.putObjectAt(x,y,this);
	}

	//Getter/Setters
	public int getX() { return x; }
	public int getY() { return y; }
	public int getWho(){ return who; }

	public void setGroup(int group) { this.group = group; }
	public int getGroup() { return group; }

	public void setColor(Color color) { this.color = color; }
	public Color getColor(){ return color; }
	
	
	//TODO -> not sure if this works or makes any sense...
	// the number of desirable traits with value of 0.6 or more
	public int getPosTraits() {
		return 2;
	}
	// TODO
	// the number of traits with value of 0.4 or less
	public int getNegTraits() {
		return 1;
	}
}
