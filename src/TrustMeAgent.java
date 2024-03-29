

import uchicago.src.sim.network.DefaultDrawableNode;
import uchicago.src.sim.gui.OvalNetworkItem;
import java.util.*;

public class TrustMeAgent extends DefaultDrawableNode {
	private boolean debug = false;
	
	private int who;
	private double spaceSizeX, spaceSizeY;
	
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
	 * Traits:
	 * 	private double neat
		private double outgoing
		private double nice
		private double active
		private double responsible
	 */
	
	
	private LinkedList<String> allAttributes;
	private LinkedList<String> keyAttributes; //NEAT, OUTGOING, NICE, ACTIVE, RESPONSIBLE 
	
	
	private double minimumTrust = 0.6; // confiança mínima necessária
	private int maxOptions = 3; // numero maximo de agentes com quem tentar conexão
	private double picky; //intervalo de avaliação de agentes [-picky, picky]
	/**
	 * picky example
	 * neat_agente1 = 0.5 & neat_agente2 = 0.4
	 * 0.5 - 0.4 = 0.1 <-- picky as 0.2 accepts!
	 */
	
	// record of trust placed on other agents
	Map<Integer, Double> agentTrust; // Map<agentID (on model), trustPlaced>
	Map<Integer, Double> agentAlpha; // Map<agentIndex, agentAlpha>
	
	
	LinkedList<Integer> bestOptions = new LinkedList<Integer>();

	// agentID to whom this one is connected (should it be placed here at all??) 
	int connectionId = -1;
	private boolean connected;

	private boolean useReputation = false; // whether reputation is used in the calculation of trust of not
	private int reputation = 0; // reputation of the agent
	

	
	///////////////////////OPERATIONS
	
	/**
	 * Constructor
	 */
	public TrustMeAgent(double spaceSizeX, double spaceSizeY, int who, int xpos, int ypos) {
	    //super(drawable);
	    this.spaceSizeX = spaceSizeX;
	    this.spaceSizeY = spaceSizeY;
	    
	    OvalNetworkItem drawable = new OvalNetworkItem (xpos, ypos);
    	setDrawable(drawable);
	    
	    this.who = who;
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
		
		//attribute rn_attributes to the list
		while (keyAttributes.size() != rn_attributes) {
			
			int attribute = rand.nextInt(5);
			
			addListAttribute(allAttributes.get(attribute), keyAttributes);
		}
	}
	
	///////////////////////////////////////////
	
	public void addListAttribute(String a, LinkedList<String> list) {
		if(!list.contains(a))
			list.add(a);
	}

	//Getter/Setters
	public boolean getConnected() { return connected; }
	public void setConnected(boolean c) { connected = c; }
	public int getWho(){ return who; }
	
	public void setReputation(int rep) { reputation = rep;}
	public int getReputation() { return reputation;}
	
	public void setAttr(double neat, double outgoing, double nice, double active, double responsible) {
		traits.put("neat",neat);
		traits.put("outgoing", outgoing);
		traits.put("nice", nice);
		traits.put("active", active);
		traits.put("responsible", responsible);
	}
	
	public void setPrefs(boolean neat, boolean outgoing, boolean nice, boolean active, boolean responsible) {
		keyAttributes.clear();
		
		if(neat)
			keyAttributes.add("neat");
		if(outgoing)
			keyAttributes.add("outgoing");
		if(nice)
			keyAttributes.add("nice");
		if(active)
			keyAttributes.add("active");
		if(responsible)
			keyAttributes.add("responsible");
	}
	
	public Map<Integer, Double> getAgentTrust() { return agentTrust;}
	
	/**
	 * Saves trust value of agent index to agentTrust
	 * @param index
	 * @param trust
	 */
	public void setAgentTrust(int index, double trust) {
		agentTrust.put(index, trust);
	}
	
	/**
	 * Set variables related to the agent's behaviour
	 */
	public void setBehaviourVariables(double pickyLevel, int cautionLevel, boolean useRep) {
		picky = pickyLevel;
		omega = Math.PI/(cautionLevel*1.0); // to make sure the division result is a double
		useReputation = useRep;
	}
	
	/**
	 * Generates random value between 0.0 and 1.0
	 * @return
	 */
	public double randVal() {
		Random rand = new Random();
		return rand.nextInt(11)/10.0;
	}
	
	/**
	 * Generates random value between 0 and upLimit
	 * @param upLimit
	 * @return
	 */
	public double randInteger(int upLimit) {
		Random rand = new Random();
		return rand.nextInt(upLimit);
	}
	
	/**
	 * Print agent traits
	 * @return
	 */
	public String printTraits() {
		String agent = "";
		
		agent += "\n neat: " + Double.toString(traits.get("neat"));
		agent += "\n outgoing: " + Double.toString(traits.get("outgoing"));
		agent += "\n nice: " + Double.toString(traits.get("nice"));
		agent += "\n active: " + Double.toString(traits.get("active"));
		agent += "\n responsible: " + Double.toString(traits.get("responsible"));
		
		return agent;
	}
	
	
	/**
	 * Get trust value assigned to agent with id 'index'
	 * @param index
	 * @return
	 */
	public Double getTrustIn(int index) {
		return agentTrust.get(index);
	}
	
	
	/** 
	 * Check if attribute chosen to evaluate are within the chosen picky range
	 * @param agent
	 * @param attribute
	 * @return
	 */
	public boolean pickyRange(TrustMeAgent agent, String attribute) {
		double attrValue1 = traits.get(attribute);
		double attrValue2 = agent.traits.get(attribute);

		double comp = attrValue1 - attrValue2;
		
		// accepted if the attribute of agent 2 is bigger than the one of agent 1 
		//or if the difference isn't bigger than the picky value
		if (Math.abs(comp) <= picky || attrValue1 < attrValue2) {
			return true;
		}

		return false;
	}
	
	/**
	 * Apply random mutation to this agent
	 */
	public void mutate() {
		
		Random rand = new Random();
		
		// number of attributes to be mutates
		int rn_attributes = rand.nextInt(2); 
		
		for (int i = 0; i != rn_attributes; i++) {

			// chooses random attribute to modify
			int attr = rand.nextInt(5);
			
			// mutates the attribute by adding the values -1, 0, or 1
			double attrModifier = (rand.nextInt(3)/10.0) - 1;
			double newAttr = traits.get(allAttributes.get(attr)) + attrModifier;
			
			//if the random value makes the attribute negative
			newAttr = Math.abs(newAttr);
			
			//if the random value makes the attribute higher than the maximum range
			if(newAttr > 1.0) {
				newAttr = 1.0;
			}
	
			// makes change to agent
			traits.put(allAttributes.get(attr), newAttr);
		}
	}
	
	/**
	 * Calculate trust in agent
	 * @param agent
	 * @return
	 */
	public double getTrust(TrustMeAgent agent) {
		
		// trust = delta * sin(alpha) + delta
		// alpha = alpha0 + lambda*omega
				
		double lambda = 0.0;
		
		double posTraits = 0;
		double negTraits = 0;
		
		int index = agent.getWho();
		
		//checks list elements
		int numAttr = traits.size();
		for (int i=0; i!=numAttr; i++) {
			
			String trait = allAttributes.get(i);
			
	
			// checks if it fulfills key attributes
			if (keyAttributes.contains(trait)) {
				
				if (pickyRange(agent, trait)) 
					posTraits++;
				else
					negTraits++;
			}
			
		}
		
		// takes reputation into account
		if (useReputation) {
			int rep = agent.getReputation();
			
			if (rep == 1)
				posTraits++;
			else if (rep == -1)
				negTraits++;
		}
			
		
		// takes into account positive and negative traits
		lambda = lambdaPos*posTraits + lambdaNeg*negTraits;
		
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
			if (debug && who==-1) System.out.println("new alpha: " + newAlpha);
			if (newAlpha > alpha1)
				agentAlpha.put(index, alpha1);
			else if (newAlpha < alpha0)
				agentAlpha.put(index, alpha0);
			else
				agentAlpha.put(index, newAlpha);
		}
				
		double trust = delta * Math.sin(agentAlpha.get(index)) + delta;
		
		if (debug)
			System.out.println("agent " + who + ", trust in " + agent.getWho() + ": " + trust);
	
		return trust;
	}
	
	
	
	/***
	 * Evaluates trust of the new agent, and puts it in an ordered list of choices 
	 * @param agentId - 'external agent' to be evaluated
	 */
	public void evaluateOption(int agentId) {
		double newTrustVal = agentTrust.get(agentId);		
		
		if (agentTrust.size()==0) {
			System.out.println("WARNING: " + who + " has no records!");
		}
			
		
		// if agent already on bestOptions, remove it and update the list 
		if (bestOptions.contains(agentId)) {
			bestOptions.remove((Object)agentId);
		}
		
		// agent has to have a minimum trust level
		// makes no sense to consider someone with trust 0.1, even if it's the best option 
		if (newTrustVal < minimumTrust) 
			return;
		
		
		// insert new trust value on list
		if (bestOptions.size() == 0) {
			bestOptions.add(agentId);
			return;
		}
		
		for (int i=0; i!=bestOptions.size(); i++) {
			
			double oldTrust = agentTrust.get(bestOptions.get(i));
			if (newTrustVal > oldTrust) {
				bestOptions.set(i, agentId);
				return;
			}
		}
		
		if (bestOptions.size() < maxOptions) {
			bestOptions.add(agentId);
			return;
		}

		if (bestOptions.size() == 0)
			System.out.println("WARNING 2: " + who + " passed evaluation with " + bestOptions.toString());

	}
	
	/**
	 * Clear bestOptions list
	 */
	public void purgeBestOptions() {
		bestOptions.clear();
	}
	

	/**
	 * Reply to connection request from agent
	 * @param agent
	 * @return
	 */
	public boolean acceptRequest(TrustMeAgent agent) {

		if (connected)
			return false;
		
		else if (bestOptions.contains(agent.getWho())) {
	
			connectionId = agent.getWho();
			connected = true;
			
			return true;
		}
		
		// minimim level of trust required
		else if (agentTrust.get(agent.getWho()) >= 0.7) {
			
			connectionId = agent.getWho();
			connected = true;
			
			return true;
		}
		
		return false;
	}
	
	
	/***
	 * Takes opinion of the other agent into account
	 * @param agent
	 */
	public void getOpinion(TrustMeAgent agent) {
		
		double trustOnAgent = agentTrust.get(agent.getWho());
		
		System.out.println(who + " getting opinion from " + agent.getWho() + "................ ");
		
		// trust records of the other agent
		Map<Integer,Double> trustInfo = agent.getAgentTrust();
		
		Set<Integer> agentIds = trustInfo.keySet();
		Iterator<Integer> agentIdIt = agentIds.iterator();
		
		while (agentIdIt.hasNext()) {
			int agentId = agentIdIt.next();
			
			// if agents have contacts in common, update trust info with foreign agent's opinon
			if (agentTrust.containsKey(agentId)) {
				
				double originalTrust = agentTrust.get(agentId);
				double foreignTrust = trustInfo.get(agentId);
				
				/*
				 * foreignTrust can never weight more than 30% of total trust calculation, because direct experience is still more important
				 * foreignTrust is also dependent on the trust deposited on the informer
				 */
				double opinionWeight = 0.30*trustOnAgent;
				double newTrust = (1.0 - opinionWeight) * originalTrust + opinionWeight * foreignTrust;
				
				if (debug && agentId==2) //only print for agent 3 (for testing)
					System.out.println("opinionWeight: " + opinionWeight);
					System.out.println("oldTrust: " + originalTrust + "\nnewTrust: " + newTrust);
				
				
				// updates trust 
				agentTrust.put(agentId, newTrust);
			}
		}
	}	
}