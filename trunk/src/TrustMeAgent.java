package trustMe;

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
	
	Map<Integer, Double> agentAlpha; // Map<agentIndex, agentAlpha>
	
	
	// overall trust based on the default values
	double overallTrust = 0.0;
	//double alpha = -1;
	
	public void addListAttribute(String a) {
		if(!keyAttributes.contains(a))
			keyAttributes.add(a);
	}

	public TrustMeAgent(Object2DTorus space, int who){
		this.who = who;
		this.group = who;
		this.space = space;
		
		agentTrust = new HashMap<Integer, Double>();
		agentAlpha = new HashMap<Integer, Double>();
		
		traits.put("neat",randVal());
		traits.put("outgoing", randVal());
		traits.put("nice", randVal());
		traits.put("active", randVal());
		traits.put("responsible", randVal());
		
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
	
	public void setAgentTrust(int index, double trust) {
		agentTrust.put(index, trust);
	}
	
	public double randVal() {
		Random rand = new Random();
		return rand.nextInt(11)/10.0;
	}
	
	public String printTraits() {
		String agent = "";
		
		agent += "\n neat: " + Double.toString(traits.get("neat"));
		agent += "\n outgoing: " + Double.toString(traits.get("outgoing"));
		agent += "\n nice: " + Double.toString(traits.get("nice"));
		agent += "\n active: " + Double.toString(traits.get("active"));
		agent += "\n responsible: " + Double.toString(traits.get("responsible"));
		
		return agent;
	}
	
	public Double getTrustIn(int index) {
		return agentTrust.get(index);
	}
	
	
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
	
	
	public double getTrust(TrustMeAgent agent, int index) {
		
		// trust = delta * sin(alpha) + delta
		// alpha = alpha0 + lambda*omega
				
		double lambda = 0.0;
		
		double posTraits = 0;
		double negTraits = 0;
		
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
		System.out.println("lambda: " + lambda);

		// if alpha wasn't initialized yet
		if(!agentAlpha.containsKey(index)) {
			double alpha = alpha0 + lambda*omega;
			agentAlpha.put(index, alpha);
		}
				
		// Calculates alpha 
		// and verifies that alpha is between the limits [alpha0; alpha1]
		else if (agentAlpha.get(index) <= alpha1) {
			
			double oldAlpha = agentAlpha.get(index);
			
			double newAlpha = oldAlpha + lambda*omega;
			System.out.println("new alpha: " + newAlpha);
			if (newAlpha > alpha1)
				agentAlpha.put(index, alpha1);
			else if (newAlpha < alpha0)
				agentAlpha.put(index, alpha0);
			else
				agentAlpha.put(index, newAlpha);
		}
		
		System.out.println("alpha0: " + alpha0);
		System.out.println("alpha: " + agentAlpha.get(index));
		System.out.println("alpha1: " + alpha1);
				
		double trust = delta * Math.sin(agentAlpha.get(index)) + delta;
		return trust;
	}
}
