

import uchicago.src.sim.network.DefaultDrawableNode;
//import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.NetworkDrawable;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.gui.SimGraphics;
import java.awt.*;
import java.util.*;

public class TrustMeAgent extends DefaultDrawableNode {
	private int who;
	private int group;
	private Color color;
	
	//////Node stuff
	private double xPos, yPos;
	////
	
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
	
	//private enum attributesIndex { NEAT, OUTGOING, NICE, ACTIVE, RESPONSIBLE }
	private LinkedList<String> allAttributes;
	private LinkedList<String> keyAttributes; //NEAT, OUTGOING, NICE, ACTIVE, RESPONSIBLE 
	
	private int maxOptions = 3; // numero maximo de agentes com quem tentar conexão
	private double diffQuotient = 0.5; // diferença máxima aceite
	private double picky = 0.2; //intervalo de avalia��o de agentes [-picky, picky]
	//neat_agente1 = 0.5 & neat_agente2 = 0.4
	//0.5 - 0.4 = 0.1 <-- picky as 0.2 accepts!
	
	// record of trust placed on other agents
	// make list of all other agents with respective trust?
	// confusing, save for later on... 
	// Map<agentID (on model), trustPlaced>
	Map<Integer, Double> agentTrust;
	
	Map<Integer, Double> agentAlpha; // Map<agentIndex, agentAlpha>
	
	
	LinkedList<Integer> bestOptions = new LinkedList<Integer>();
	// agentID to whom this one is connected (should it be placed here at all??) 
	int connectionId = -1;
	boolean connected;
	
	// overall trust based on the default values
	//double overallTrust = 0.0;
	//double alpha = -1;
	
	///////////////////////NODE
	public TrustMeAgent(double xpos, double ypos/*, NetworkDrawable drawable*/, int who) {
	    //super(drawable);
	    this.xPos = xpos;
	    this.yPos = ypos;
	    
	    OvalNetworkItem drawable = new OvalNetworkItem (xPos, yPos);
    	setDrawable(drawable);
	    
	    this.who = who;
		this.group = who;
		connected = false;
		
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
		
		allAttributes = new LinkedList<String>();
		allAttributes.add("neat");
		allAttributes.add("outgoing");
		allAttributes.add("nice");
		allAttributes.add("active");
		allAttributes.add("responsible");
		
		keyAttributes = new LinkedList<String>();
		
		//attribute rn_attributes to tha list
		while (keyAttributes.size() != rn_attributes) {
			
			int attribute = rand.nextInt(5);
			
			addListAttribute(allAttributes.get(attribute), keyAttributes);
			/*
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
			}*/
		}
	}
	
	///////////////////////////////////////////
	
	public void addListAttribute(String a, LinkedList<String> list) {
		if(!list.contains(a))
			list.add(a);
	}

	//Getter/Setters
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
	
	//check if number of attributes chosen to evaluate are within the chosen picky range
	public boolean pickyRange(TrustMeAgent agent, String attribute) {
		double attrValue1 = traits.get(attribute);
		double attrValue2 = agent.traits.get(attribute);

		double comp = attrValue1 - attrValue2;

		// aceite se attrb do agente 2 foi maior que o de 1, 
		//ou se diferen�a n�o for maior que picky
		if (Math.abs(comp) <= picky || attrValue1 < attrValue2)
			return true;
		
		return false;
	}
	
	public void mutate() {
		
		Random rand = new Random();
		int rn_attributes = rand.nextInt(5);
		
		LinkedList<String> mutateAttr = new LinkedList<String>(); 
		
		while (mutateAttr.size() != rn_attributes) {

			int attribute = rand.nextInt(5);
			addListAttribute(allAttributes.get(attribute), mutateAttr);
		}

		for (int i=0; i!=rn_attributes; i++) {

			traits.put(mutateAttr.get(i),randVal());
		}
	}
	
	public double getTrust(TrustMeAgent agent) {
		
		// trust = delta * sin(alpha) + delta
		// alpha = alpha0 + lambda*omega
				
		double lambda = 0.0;
		
		double posTraits = 0;
		double negTraits = 0;
		
		int index = agent.who;
		
		//checks list elements
		int numAttr = traits.size();
		for (int i=0; i!=numAttr; i++) {
			
			String trait = allAttributes.get(i);
			
			// checks if agents are too different
			/*if (Math.abs(traits.get(trait) - agent.traits.get(trait)) > diffQuotient)
				return -1;
			*/
			
			// checks if it fulfills key attributes
			/*else*/ if (keyAttributes.contains(trait)) {
				
				if (pickyRange(agent, trait)) 
					posTraits++;
				else
					negTraits++;
			}
			
		}
		/*int numAttr = keyAttributes.size();
		for (int i=0; i!=numAttr; i++) {
			
			String traitWanted = keyAttributes.get(i);
			if (pickyRange(agent, traitWanted)) 
				posTraits++;
			else
				negTraits++;
		}*/
		
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
	
	
	
	/***
	 * Evaluates trust of the new agent, and puts it in an ordered list of choices 
	 * @param agentId - 'external agent' to be evaluated
	 */
	public void evaluateOption(int agentId) {
		
		int numOptions = bestOptions.size();
		double newTrustVal = agentTrust.get(agentId);
		
		
		for (int i=0; i!= maxOptions; i++) {
			
			// if no options have been added yet, add new option to the ordered list
			if (numOptions < maxOptions) {
				
				if (i>=numOptions) {
					bestOptions.add(agentId);
				}
				
				else if (newTrustVal > agentTrust.get(bestOptions.get(i))){
					bestOptions.add(i, agentId);
				}
			}
			
			// checks if new agent is better than the ones previously evaluated
			else if (newTrustVal > agentTrust.get(bestOptions.get(i))) {
				bestOptions.set(i, agentId);
				return;
			}
		}

	}
	
	public void purgeBestOptions() {
		bestOptions.clear();
	}
	
	
	public boolean acceptRequest(TrustMeAgent agent) {
		
		System.out.println("agent.who = " + agent.who);
		
		if (connected)
			return false;
		
		else if (bestOptions.contains(agent.getWho())) {
			
			// still not sure where connections will be made, therefore
			// private int connectionId still temporary solution
			connectionId = agent.getWho();
			connected = true;
			
			return true;
		}
		
		//TODO: can any other agents be accepted? 
		// insert minimum trust level here (if we're still doing that)
		else if (agentTrust.get(agent.getWho()) > 0.7) {
			
			connectionId = agent.getWho();
			connected = true;
			
			return true;
		}
		
		return false;
	}
	
	
}
