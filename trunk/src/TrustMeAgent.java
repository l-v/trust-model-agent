package TrustMe;

import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.*;
import java.util.*;

public class TrustMeAgent implements Drawable {
	private int who;
	private int group;
	private int x, y;
	private Color color;
	private Object2DTorus space;
	
	
	/***
	 *  sinalpha parameters
	 */
	final double delta = 0.5;
	final double alpha0 = 3.0*Math.PI/2.0;
	final double alpha1 = 5.0*Math.PI/2.0;
	
	double omega = Math.PI/12.0; // time/steps to reach maximum trust level
	double lambdaPos = 1.0; // weight of positive attributes
	double lambdaNeg = -1.5; // weight of negative attributes
	
	
	//decision variables
	//or rather, abilities of each player that will enable to calculate
	// the trust the other player has on him
	// I'd say the values range within 0 to 1 to make it easier
	// something like... :
	private Map<String,Double> traits = new HashMap<String,Double>();
	
	/**
	 * 	private double neat = 0.5;
		private double outgoing = 0.5;
		private double nice = 0.5;
		private double active = 0.5;
		private double responsible = 0.5;
	 */
	
	//private enum keyAttributesIndex { NEAT, OUTGOING, NICE, ACTIVE, RESPONSIBLE }
	private LinkedList<String> keyAttributes; //NEAT, OUTGOING, NICE, ACTIVE, RESPONSIBLE 
	
	private double picky = 0.2; //intervalo de avaliação de agentes [-picky, picky]
	//neat_agente1 = 0.5 & neat_agente2 = 0.4
	//0.5 - 0.4 = 0.1 <-- picky as 0.2 accepts!
	
	// record of trust placed on other agents
	// make list of all other agents with respective trust?
	// confusing, save for later on... 
	// Map<agentID (on model), trustPlaced>
	Map<Integer, Double> agentTrust;
	
	
	// overall trust based on the default values
	double overallTrust = 0.0;
	double alpha = -1;
	
	public void addListAttribute(String a) {
		if(!keyAttributes.contains(a))
			keyAttributes.add(a);
	}

	public TrustMeAgent(Object2DTorus space, int who){
		this.who = who;
		this.group = who;
		this.space = space;
		
		agentTrust = new HashMap<Integer, Double>();
		
		traits.put("neat", 0.5);
		traits.put("outgoing", 0.5);
		traits.put("nice", 0.5);
		traits.put("active", 0.5);
		traits.put("responsible", 0.5);
		
		//number of attributes that the linked list will have
		Random rand = new Random();
		int rn_attributes = rand.nextInt(5); // de 0 a 4 <-- 5 atributos
		
		keyAttributes = new LinkedList<String>();
		
		//attribute rn_attributes to tha list
		while (keyAttributes.size() != rn_attributes) {
			
			int attribute = rand.nextInt(5);
			
			switch(attribute) {
			case 0:
				addListAttribute("neat");
				break;
			case 1:
				addListAttribute("outgoing");
				break;
			case 2:
				addListAttribute("nice");
				break;
			case 3:
				addListAttribute("active");
				break;
			case 4:
				addListAttribute("responsible");
				break;
			}
		}
		
		
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
	
	//check if number of attributes chosen to evaluate are within the chosen picky range
	public boolean pickyRange(TrustMeAgent agent, String attribute) {
		double attrValue1 = traits.get(attribute);
		double attrValue2 = agent.traits.get(attribute);

		double comp = attrValue1 - attrValue2;

		if (Math.abs(comp) <= picky)
			return true;
		
		return false;
	}
	
	
	public double getTrust(TrustMeAgent agent) {
		
		// trust = delta * sin(alpha) + d
		// alpha = alpha0 + lambda*omega
				
		double lambda = 0.0;
		
		int posTraits = 0;
		int negTraits = 0;
		
		//checks list elements
		int numAttr = keyAttributes.size();
		for (int i=0; i!=numAttr; i++) {
			
			String traitWanted = keyAttributes.get(i);
			if (pickyRange(agent, traitWanted)) 
				posTraits++;
			else
				negTraits++;
		}
		
		// takes into account positive and negative traits
		lambda = lambdaPos*posTraits + lambdaNeg*negTraits;


		// if alpha wasn't initialized yet
		if (agent.alpha == -1) {
			agent.alpha = alpha0 + lambda*omega;
		}
				
		// Calculates alpha 
		// and verifies that alpha is between the limits [alpha0; alpha1]
		else if (agent.alpha <= alpha1) {
					
			double newAlpha = agent.alpha + lambda*omega;

			if (newAlpha > alpha1)
				agent.alpha = alpha1;
			else
				agent.alpha = newAlpha;
		}
				
		double trust = delta * Math.sin(agent.alpha) + delta;
		return trust;
	}
}
