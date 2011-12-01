


import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.Histogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.AbstractGraphLayout;
import uchicago.src.sim.gui.CircularGraphLayout;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.FruchGraphLayout;
import uchicago.src.sim.gui.KamadaGraphLayout;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.network.Node;
import uchicago.src.sim.util.Random;

public class TrustMeModel extends SimModelImpl/*SimpleModel*/ {

	// Model variables
	private int updateEveryN = 5;
	private int initialSteps = 1;
	// Default values
	private int spaceSizeX = 400;
	private int spaceSizeY = 400;
	private int numAgents = 20;
	private ArrayList agentList = new ArrayList(numAgents);
	private int maxDegree = 5;
	
	private double numConnects = 0;
	private double numMutations = 0;
	private double averageTrust = 0.0; // average trust in the system
	
	private double mutationProbability = 1.0;
	private double trustBreak = 0.4; // minimum level of trust that can be reached before a connection breaks
	private int caution = 12; // how many steps does it take to gain the agents' full trust
	private double pickyLevel = 0.4; // how picky/choosy the agents are when evaluating each other - the bigger the value the less picky they are
	
	private boolean multipleConnections = false; // if true, agent can connect to other 3 agents; if false, only pairs are allowed
	
	
	public Map<Integer, Double> agentsPaired = new HashMap<Integer, Double>();
	
	// Implementation variables
	private String layoutType = "CircleLayout";
	private Integer agentStalk = 0;
	private DisplaySurface dsurf;
	private Schedule schedule;
	private AbstractGraphLayout graphLayout;
	
	// graphs
	private OpenSequenceGraph graph;
	private OpenSequenceGraph deterministicGraph;
	
	private BasicAction initialAction;
	private Histogram degreeDist;
	private boolean showHist = true;
	private boolean showPlot = true;
	
	private boolean showReputation = false;
	private boolean useReputation = false;
	
	private boolean learning = false;
	private int ticksToChange = 1500;
	
	private boolean opinionSharing = false; // agents share information amongst each other
	
	
	private Hashtable<Integer, Double> connectionTrust = new Hashtable<Integer, Double>();
	
	
	public TrustMeModel() {
		Vector<String> vect = new Vector<String>();
		vect.add("CircleLayout");
		vect.add("Fruch");
		vect.add("KK");
		vect.add("None");
		ListPropertyDescriptor pd = new ListPropertyDescriptor("LayoutType", vect);
		descriptors.put("LayoutType", pd);
		
		Vector<Integer> agentVect = new Vector<Integer>();
		for(int i = 0; i != numAgents; i++) {
			agentVect.add(i);
		}
		
		ListPropertyDescriptor agent_pd = new ListPropertyDescriptor("Agent To Stalk", agentVect);
		descriptors.put("Agent To Stalk", agent_pd);
	}
	
	
	public void setNumAgents (int n) { numAgents = n; }

	public int getNumAgents () { return numAgents; }

	public int getSpaceSizeX () { return spaceSizeX; }

	public void setSpaceSizeX (int size) { spaceSizeX = size; }

	public int getSpaceSizeY () { return spaceSizeY; }

	public void setSpaceSizeY (int size) { spaceSizeY = size; }
	
	public String getLayoutType() { return layoutType; }

	public void setLayoutType(String type) { layoutType = type; }

	public void setUpdateEveryN(int updates) { updateEveryN = updates; }

	public int getUpdateEveryN() { return updateEveryN; }
	
	public int getStartRemoveAfter() { return initialSteps; }

	public void setStartRemoveAfter(int steps) { initialSteps = steps; }
	
	public boolean getDegreeHist() { return showHist; }

	public void setDegreeHist(boolean val) { showHist = val; }

	public boolean getPlot() { return showPlot; }

	public void setPlot(boolean val) { showPlot = val; }
	
	public int getMaxDegree() { return maxDegree; }

	public void setMaxDegree(int degree) { maxDegree = degree; }
	
	public double getTrustBreak() { return trustBreak; }

	public void setTrustBreak(double breakPoint) { trustBreak = breakPoint; }
	
	public int getCaution() { return caution; }

	public void setCaution(int cautionLevel) { caution = cautionLevel; }
	
	public double getPickyLevel() { return pickyLevel; }

	public void setPickyLevel(double picky) { pickyLevel = picky; }
	
	public double getMutationProb() { return mutationProbability; }

	public void setMutationProb(double mutation) { mutationProbability = mutation; }
	
	public boolean getShowReputation() { return showReputation; }

	public void setShowReputation(boolean showRep) { showReputation = showRep; }
	
	public boolean getUseReputation() { return useReputation; }

	public void setUseReputation(boolean useRep) { useReputation = useRep; }
	
	public int getAgentToStalk() { return agentStalk; }
	
	public void setAgentToStalk(int a) { agentStalk = a; }
	
	
	
	public void begin () {
	    buildModel ();
	    buildDisplay ();
	    buildSchedule ();
	    dsurf.display ();
	    
	    if (showHist) 
	    	degreeDist.display();
	    if (showPlot) {
	    	graph.display();
	    	deterministicGraph.display();
	    }
	}

	public void setup() {
		System.out.println("Running setup begginning");
		//super.setup();
		Random.createUniform ();
		
		if (dsurf != null) 
			dsurf.dispose();
		if (graph != null)
			graph.dispose();
		if(deterministicGraph != null)
			deterministicGraph.display();
		
		dsurf = null;
	    schedule = null;
	    graph = null;
	    deterministicGraph = null;
	    degreeDist = null;

	    System.gc ();

		dsurf = new DisplaySurface(this,"TrustMe");
		
		registerDisplaySurface("TrustMe",dsurf);
		schedule = new Schedule();
		agentList = new ArrayList(); //new ArrayList(numAgents);
		
		System.out.println("Running setup end");
	}
	
	
	public String[] getInitParam () {
	    String[] params = {"numAgents", "spaceSizeX", "spaceSizeY", "updateEveryN", "LayoutType", "AgentToStalk", "MaxDegree", "DegreeHist", "Plot", 
	    					"pickyLevel", "Caution", "TrustBreak", "MutationProb",
	    					"showReputation", "useReputation"};
	    return params;
	}
	
	public Schedule getSchedule () { return schedule; }
	
	public String getName() { return "TrustMeModel"; }

	public void buildModel() {	    
		System.out.println("Running BuildModel begginning");
	    // add agents with random values
	    for (int i = 0; i != numAgents; i++) {
	    	
	    	// create the Oval nodes.
	    	int x = Random.uniform.nextIntFromTo (0, spaceSizeX - 5);
	    	int y = Random.uniform.nextIntFromTo (0, spaceSizeY - 5);
	    	
	    	// cria o agente
	    	TrustMeAgent agent = new TrustMeAgent(spaceSizeX, spaceSizeY, i, x, y);
	    	agent.setColor(Color.green);
	    	agent.setLabelColor(Color.black);
	    	agent.setNodeLabel (" " + i);
			agent.setBorderColor (Color.black);
			agent.setBorderWidth(2);
			
			agent.setBehaviourVariables(pickyLevel, caution, useReputation, opinionSharing, learning);
			
			// adds the agent to agentList
	    	agentList.add(agent);
	    }
	    
	    if (showHist) 
	    	makeHistogram();
	    
	    if (showPlot) 
	    	makePlot();
	   
	   System.out.println("Running BuildModel end");
	}
	
	public void buildDisplay () {
		System.out.println("Running BuildDisplay beggining");
		
		if (layoutType.equals("KK")) {
			graphLayout = new KamadaGraphLayout(agentList, spaceSizeX, spaceSizeY, dsurf, updateEveryN);
		} else if (layoutType.equals("Fruch")) {
			graphLayout = new FruchGraphLayout(agentList, spaceSizeX, spaceSizeY, dsurf, updateEveryN);
		} else if (layoutType.equals("CircleLayout")) {
			graphLayout = new CircularGraphLayout(agentList, spaceSizeX, spaceSizeY);
		}

		// these four lines hook up the graph layouts to the stop, pause, and
		// exit buttons on the toolbar. When stop, pause, or exit is clicked
		// the graph layouts will interrupt their layout as soon as possible.
		Controller c = (Controller) getController();
		c.addStopListener(graphLayout);
		c.addPauseListener(graphLayout);
		c.addExitListener(graphLayout);
		Network2DDisplay display;

		if(layoutType.equals("None")) {
			display = new Network2DDisplay (agentList, spaceSizeX, spaceSizeY);
		}
		else {
			display = new Network2DDisplay(graphLayout);
		}
		dsurf.addDisplayableProbeable(display, "TrustMe View");

		// add the display as a Zoomable. This means we can "zoom" in on
		// various parts of the network.
		dsurf.addZoomable(display);
		dsurf.setBackground(java.awt.Color.white);
		addSimEventListener(dsurf);
	    
	    System.out.println("Running BuildDisplay end");
	}
	
	public void initialAction() {
		
		// calculate trust for all agents
		int numAgents = agentList.size();
		for (int i = 0; i != numAgents; i++) {
			TrustMeAgent agent = (TrustMeAgent) agentList.get(i);

			//complexidade n^2
			for(int j = 0; j != numAgents; j++) {
				
				if (j!=i) {
					double trust = agent.getTrust((TrustMeAgent) agentList.get(j));

					// if agent is not too different, consider him an option
					if (trust != -1) {
						agent.setAgentTrust(j, trust);
						
						
						// if this agent is a good choice, try to add it to the best options of the first
						agent.evaluateOption(j);	
					}
				}
			}
		}
		
		if(!layoutType.equals("None")) {
			graphLayout.updateLayout();
		}
		dsurf.updateDisplay();
		
		if (showHist) 
			degreeDist.step();
	    if (showPlot) {
	    	graph.step();
	    	deterministicGraph.step();
	    }
	}
	
	public void mainAction() {
		
		double overallTrust = 0.0;
		
		// calculate trust for all agents
		int numAgents = agentList.size();
		for (int i = 0; i != numAgents; i++) {

			TrustMeAgent agent = (TrustMeAgent) agentList.get(i);

			if (agent.getConnected()) {
				
				double trust = agent.getTrust((TrustMeAgent) agentList.get(agent.connectionId));
				agent.setAgentTrust(agent.connectionId, trust);
				//((TrustMeAgent)agentList.get(i)).setAgentTrust(agent.connectionId, trust);
				
				if (i==0 || agent.connectionId==0) System.out.println("Debug: Trust of " + i + " in " + agent.connectionId + ": " + trust);
				
				
				// updates edge information
				connectionTrust(agent, trust);
				colorEdges(agent);
				
				//check if connection must be broken
				if(trust < trustBreak) { 
					breakConnection(agent);
					agent.evaluateOption(agent.connectionId);
				}
				
				
				// probability of mutation
				mutate(agent);
				
				continue;
			}
			
			//complexidade n^2
			for(int j = 0; j != numAgents; j++) {
				
				if (agent.getConnected())
					continue;
				

				if (j!=i) {
					double trust = agent.getTrust((TrustMeAgent) agentList.get(j));
		
					
					// TODO: functionality disabled
					// if agent is not too different, consider him an option
					if (trust != -1) {
						agent.setAgentTrust(j, trust);
						
						// if this agent is a good choice, try to add it to the best options
						agent.evaluateOption(j);
					}
					
				}
			}

			// tries to get a connection
			tryConnection(agent);

			mutate(agent);
			overallTrust += agent.getAverageTrust();	
		}
		
		// add to overallTrust calculation
		averageTrust = overallTrust/numAgents;
		
		if (showReputation || useReputation)
			calcReputation();

		DecimalFormat myFormatter = new DecimalFormat("###.##");
		//System.out.println("\nTraits 0: " + ((TrustMeAgent) agentList.get(0)).printTraits());
		//String output = myFormatter.format(((TrustMeAgent) agentList.get(0)).getTrustIn(1));
		//System.out.println("Trust 0 in 1: " + output);

		System.out.println("Trust 0 in 1: " + ((TrustMeAgent) agentList.get(0)).getTrustIn(1));
		//System.out.println("\nTraits 1: " + ((TrustMeAgent) agentList.get(1)).printTraits());

		//String output2 = myFormatter.format(((TrustMeAgent) agentList.get(1)).getTrustIn(0));
		//System.out.println("Trust 1 in 0: " +  output2 + "\n------------");
		//System.out.println("Trust 1 in 0: " + ((TrustMeAgent) agentList.get(1)).getTrustIn(0) + "\n------------");

		if(!layoutType.equals("None")) {
			graphLayout.updateLayout();
		}
		dsurf.updateDisplay();
		
		if (showHist) 
			degreeDist.step();
		if (showPlot) {
			graph.step();
			deterministicGraph.step();
		}
	}

	public void buildSchedule () {
		System.out.println("Running BuildSchedule beggining");
		
		initialAction = schedule.scheduleActionAt(1, this, "initialAction");
	    //schedule.scheduleActionAt(initialSteps, this, "removeInitialAction", Schedule.LAST);
	    schedule.scheduleActionBeginning(initialSteps + 1, this, "mainAction");
		
		System.out.println("Running BuildSchedule end");
	}

	/**
	 * Creates a histogram of the degree distribution.
	 */
	private void makeHistogram() {

		degreeDist = new Histogram("Degree Distribution", maxDegree + 1, 0,
				maxDegree + 1, this);

		degreeDist.createHistogramItem("Degree Distribution", agentList,
				"getOutDegree");
	}

	/**
	 * Creates a Plot of the Clustering Coefficient, the avg. density,
	 * and the component count.
	 */
	private void makePlot() {		
		graph = new OpenSequenceGraph("Connections between Agents", this);
		graph.setAxisTitles("time", "number of Connections");
		graph.setYRange(0, numAgents/2);
		
		// plot number of the current connections
		graph.addSequence("Number of connections", new Sequence() {
			public double getSValue() {
				return numConnects;
			}
		});
		
		deterministicGraph = new OpenSequenceGraph("Trust of Agent " + agentStalk, this);
		deterministicGraph.setAxisTitles("time", "trust");
		deterministicGraph.setYRange(0, 12);
		
		deterministicGraph.addSequence("Connection Trust", new Sequence() {
			public double getSValue() {
				
				Double d = 0.0;
				if (connectionTrust.containsKey(agentStalk))
					d=connectionTrust.get(agentStalk);
					
				return d*10;
			}
		});
		

		graph.display();
		deterministicGraph.display();
	}
	
	
	/**
	 * Applies mutation to agent according to the mutation probability
	 * @param agent
	 */
	public void mutate(TrustMeAgent agent) {
		
		double probMutate = agent.randInteger(100)+1;

		if (probMutate <= mutationProbability) {
			System.out.println("MUTATED!!!");
			agent.mutate();

			// color scheme
			if (!showReputation) {
				if (agent.getColor() == Color.green) 
					agent.setColor(Color.lightGray);
				else 
					agent.setColor(Color.green);
			}
			else {
				if (agent.getBorderColor() == Color.black) {
					agent.setBorderColor(Color.darkGray);
					agent.setBorderWidth(agent.getBorderWidth()+1);
				}
				else {
					agent.setBorderColor(Color.black);
					agent.setBorderWidth(agent.getBorderWidth()-1);
				}
			}
			
			numMutations++;
		}
	}

	
	public void tryConnection(TrustMeAgent agent) {
		
		for (int b=0; b!=agent.bestOptions.size(); b++) {
			
			int optionId = agent.bestOptions.get(b);
			TrustMeAgent ag = (TrustMeAgent)agentList.get(optionId);
			
			// sends request
			//if(ag.acceptRequest(agent)) {
			if (((TrustMeAgent)agentList.get(optionId)).acceptRequest(agent)) {
				
				// request was accepted, so create connection
				System.out.println("CONNECTED---" + agent.getWho() + "-" + optionId + "----------------");

				agent.setConnected(true);
				agent.connectionId = optionId;
				

				// creates edge
				TrustMeEdge edge = new TrustMeEdge ((Node)agent, (Node)agentList.get(optionId), Color.green);
				((Node)agent).addOutEdge (edge);
				
				
				connectionTrust(agent, agent.getTrustIn(optionId));
				
				
				int agentIndex = agent.getWho();
				//if (agentIndex==0 || agentIndex == 5 || agentIndex == 11) {
					System.out.println(agentIndex + " created edge to " + optionId);
					System.out.println(agentIndex + " now connected to " + ((TrustMeAgent)agentList.get(agentIndex)).connectionId);
					System.out.println(agentIndex + " best options are " + ((TrustMeAgent)agentList.get(agentIndex)).bestOptions.toString());
				//}
				
				numConnects++;
				
				
				//catchErrors TODO: remover este bloco quando estiver tudo funcional
				if (!(((TrustMeAgent)agentList.get(agentIndex)).getConnected() && ((TrustMeAgent)agentList.get(optionId)).getConnected())) {
					System.out.println("não coincide nas ligacoes!!");
					System.out.println("agList:" + agentIndex + "|who:" + ((TrustMeAgent)agentList.get(agentIndex)).getWho());
				
					System.out.println("connected:" + ((TrustMeAgent)agentList.get(agentIndex)).connectionId + "|bestOption:"+((TrustMeAgent)agentList.get(b)).getWho());
					System.out.println("agListReceptor:" + " isCon?->" + ((TrustMeAgent)agentList.get(agentIndex)).getConnected() + " |with who?->"+ ((TrustMeAgent)agentList.get(b)).connectionId);	
					return;
				}
				break;
			}
		}
		
		if (agent.getWho()==0 && ((TrustMeAgent)agentList.get(agent.getWho())).getConnected()) {
			System.out.println("0 registered connection to " + ((TrustMeAgent)agentList.get(0)).connectionId);
		}
		

	}
	
	/**
	 * Gets connection trust; Based on the average trust between the two agents connected
	 * @param agent
	 * @param trust
	 */
	public void connectionTrust(TrustMeAgent agent, double trust) {
		double connectedAgentTrust = ((TrustMeAgent)agentList.get(agent.connectionId)).getTrustIn(agent.getWho());
		double avgTrust = (trust + connectedAgentTrust)/2.0;
		
		connectionTrust.put(agent.getWho(), avgTrust);
	}
	
	
	/**
	 * Breaks an agent's connection
	 * @param agent
	 */
	public void breakConnection(TrustMeAgent agent) {

		agent.setConnected(false);
		Node agentNode = (Node)agent;
		

		//quebrar ligacao - como so tem uma, podemos fazer clear
		agentNode.clearOutEdges();
		agentNode.clearInEdges();

		int connectedAgentId = agent.connectionId;
		
		TrustMeAgent connectedAgent = (TrustMeAgent)agentList.get(connectedAgentId);
		Node connectedAgentNode = (Node) connectedAgent;
		
		connectedAgentNode.clearOutEdges();
		connectedAgentNode.clearInEdges();
		
		connectedAgent.setConnected(false);
		
		if(connectionTrust.containsKey(agent.getWho())) {
			connectionTrust.put(agent.getWho(), 0.0);
			connectionTrust.remove(agent.getWho());
		}
		else {
			connectionTrust.put(agent.connectionId, 0.0);
			connectionTrust.remove(agent.connectionId);
		}
		
		
		numConnects--;
		
		//TODO erase print junk in the grand finale
		System.out.println("BREAK " + agent.getWho() + "-" + connectedAgent);
		
		if (agent.getWho() == 0 || connectedAgentId == 0) {
		System.out.println("break: trust of " + agent.getWho() + " in " + connectedAgentId + ": " + agent.getTrustIn(connectedAgentId));
		System.out.println("break: trust of " + connectedAgentId + " in " + agent.getWho() + ": " + connectedAgent.getTrustIn(agent.getWho()));
		}
	}
	
	
	/**
	 * Colors edges according to the trust between connected agents
	 * Higer trust = red; Lower trust = yellow
	 * @param agent
	 * @param trust
	 */
	public void colorEdges(TrustMeAgent agent) {
		
		Color edgeColor = Color.red;
		Node agentNode = (Node)agent;
		
		double avgTrust;
		
		if(connectionTrust.containsKey(agent.getWho()))
			avgTrust = connectionTrust.get(agent.getWho());
		else
			avgTrust = connectionTrust.get(agent.connectionId);
		
		if (avgTrust > 0.9) {edgeColor = Color.red;}
		else if (avgTrust > 0.7) { edgeColor = Color.orange;}
		else {edgeColor = Color.yellow;}

		int outEdges = agentNode.getOutEdges().size();
		int inEdges = agentNode.getInEdges().size();
		
		if (outEdges > 0) {
			((TrustMeEdge)(agentNode.getOutEdges().get(0))).setColor(edgeColor);
		}
		if (inEdges > 0) {
			((TrustMeEdge)((Node)agentList.get(agent.getWho())).getOutEdges().get(0)).setColor(edgeColor);
		}	
	}
	
	
	/**
	 * Colors nodes according to reputation of agents in the system
	 * Reputation: bad = -1, neutral = 0, good = 1 
	 */
	public void calcReputation() {
		
		for (int i=0; i!=numAgents; i++) {
			
			float rep = 0;
			
			for (int j=0; j!=numAgents; j++) {
				
				if (j==i)
					continue;
				
				TrustMeAgent agent2 = (TrustMeAgent) agentList.get(j);
				double trust = agent2.getTrustIn(i);
				
				rep += repScale(trust);
			}
			
			//System.out.println("Trust " + i + ": " + rep);
			rep = rep/(numAgents-1);
			//System.out.println("Trust2 " + i + ": " + rep);
			if(i==0) System.out.println("Rep " + i + ": " + Math.round(rep));
			
			int agentReputation = Math.round(rep);
			TrustMeAgent agent = (TrustMeAgent)agentList.get(i);
			agent.setReputation(agentReputation);
			
			if (showReputation) {
				Color nodeColor = repColorCode(Math.round(rep));
				((TrustMeAgent)agentList.get(i)).setColor(nodeColor);
			}
		}
	}
	
	
	/**
	 * Converts trust value into a scale of [-1, 0], used to calculate reputation
	 * @param trust
	 * @return
	 */
	public int repScale(double trust) {
		
		if (trust >= 0.7) // [0.7,1.0] -> high rep
			return 1;
		else if (trust >=0.4) // [0.4, 0.6] -> medium rep
			return 0;
		else // [0.0, 0.3] -> low rep
			return -1;
	}
	
	/***
	 * Return the color corresponding to the reputation value
	 * High rep = white; medium = green; low = pink 
	 * @param repValue
	 * @return
	 */
	public Color repColorCode(int repValue) {
		
		switch (repValue) {
		case -1: return Color.pink;
		case 0: return Color.green;
		default: return Color.white;
		}
	}
	
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
}