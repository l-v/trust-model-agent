


import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.Histogram;
import uchicago.src.sim.analysis.NetSequenceGraph;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.PlotModel;
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
	private double pickyLevel = 0.4; // how picky/choosy the agents are when evaluating each other
	
	private boolean multipleConnections = false; // if true, agent can connect to other 3 agents; if false, only pairs are allowed
	
	
	public HashMap<Integer, Integer> agentsPaired = new HashMap<Integer, Integer>();
	
	// Implementation variables
	private String layoutType = "CircleLayout";
	private DisplaySurface dsurf;
	private Schedule schedule;
	private AbstractGraphLayout graphLayout;
	private OpenSequenceGraph graph;
	private BasicAction initialAction;
	private Histogram degreeDist;
	private boolean showHist = true;
	private boolean showPlot = true;
	
	public TrustMeModel() {
		Vector<String> vect = new Vector<String>();
		vect.add("CircleLayout");
		vect.add("Fruch");
		vect.add("KK");
		vect.add("None");
		ListPropertyDescriptor pd = new ListPropertyDescriptor("LayoutType", vect);
		descriptors.put("LayoutType", pd);
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
	
	public void begin () {
	    buildModel ();
	    buildDisplay ();
	    buildSchedule ();
	    dsurf.display ();
	    
	    if (showHist) 
	    	degreeDist.display();
	    if (showPlot) 
	    	graph.display();
	}

	public void setup() {
		System.out.println("Running setup begginning");
		//super.setup();
		Random.createUniform ();
		
		if (dsurf != null) 
			dsurf.dispose();
		if (graph != null)
			graph.dispose();
		
		dsurf = null;
	    schedule = null;
	    graph = null;
	    degreeDist = null;

	    System.gc ();

		dsurf = new DisplaySurface(this,"TrustMe");
		//super.
		registerDisplaySurface("TrustMe",dsurf);
		schedule = new Schedule();
		agentList = new ArrayList(); //new ArrayList(numAgents);
		
		System.out.println("Running setup end");
	}
	
	//TODO add picky and 'cautioness' from sinalpha formula
	public String[] getInitParam () {
	    String[] params = {"numAgents", "spaceSizeX", "spaceSizeY", "updateEveryN", "LayoutType", "MaxDegree", "DegreeHist", "Plot", 
	    					"pickyLevel", "Caution", "TrustBreak", "MutationProb"};
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
			
			agent.setBehaviourVariables(pickyLevel, caution);
			
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
			/*
			if (i!=0)
				continue;
*/
			TrustMeAgent agent = (TrustMeAgent) agentList.get(i);

			//complexidade n^2
			for(int j = 0; j != numAgents; j++) {
				//agent.overallTrust = sinalpha(agent);
/*
				if (j!=0 && j!=1)
					continue;
*/
				if (j!=i) {
					double trust = agent.getTrust((TrustMeAgent) agentList.get(j));

					// if agent is not too different, consider him an option
					if (trust != -1) {
						agent.setAgentTrust(j, trust);
						
						
						// if this agent is a good choice, try to add it to the best options of the first
						agent.evaluateOption(j);	
					}
					/*if (trust < 1 &&  i==0 && j==1)
					System.out.println(trust);*/
				}
			}
		}
		
		DecimalFormat myFormatter = new DecimalFormat("###.##");
		System.out.println("\nTraits 0: " + ((TrustMeAgent) agentList.get(0)).printTraits());
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
	    if (showPlot) 
	    	graph.step();
	}
	
	public void mainAction() {
		// calculate trust for all agents
		int numAgents = agentList.size();
		for (int i = 0; i != numAgents; i++) {

			/*if (i!=0)
				continue;
*/
			TrustMeAgent agent = (TrustMeAgent) agentList.get(i);
		
			// cleans bestOptions list
			((TrustMeAgent)agentList.get(i)).purgeBestOptions();
			
			if (agent.getConnected()) {
				
				//check if connection must be broken
				
				double trust = agent.getTrust((TrustMeAgent) agentList.get(agent.connectionId));
				//agent.setAgentTrust(agent.connectionId, trust);
				((TrustMeAgent)agentList.get(i)).setAgentTrust(agent.connectionId, trust);
				
				if (i==0) System.out.println("Debug: Trust of 0 in " + agent.connectionId + ": " + trust);
				
				// edits connection color based on the level of trust
				// maximum trust: black; high trust: red; medium trust: orange 
				colorEdges(i, trust);
				
				if(trust < trustBreak) { //TODO mudar isto (0.0 é só para os testes)
					breakConnection(i);
				}
				
				
				// probability of mutation
				mutate(i);
				
				continue;
			}
			
			//complexidade n^2
			for(int j = 0; j != numAgents; j++) {
				//agent.overallTrust = sinalpha(agent);

			//	System.out.println("i:j  " + i+":"+j);
				if (((TrustMeAgent)agentList.get(j)).getConnected())
					continue;
				
				/*if (j!=0 && j!=1)
					continue;
				*/
				if (j!=i) {
					double trust = agent.getTrust((TrustMeAgent) agentList.get(j));
					//System.out.println("i:j -> " + i+":"+j);
					// TODO: functionality disabled
					// if agent is not too different, consider him an option
					if (trust != -1) {
						//agent.setAgentTrust(j, trust);
						((TrustMeAgent)agentList.get(i)).setAgentTrust(j, trust);
						
						// if this agent is a good choice, try to add it to the best options of the first
						//agent.evaluateOption(j);
						((TrustMeAgent)agentList.get(i)).evaluateOption(j);
					}
					
					/*if (trust < 1 &&  i==0 && j==1)
							System.out.println(trust);*/

				}
			}

			// tries to get a connection
			tryConnection(i);


			
			// probability of mutation
			mutate(i);
			/*double probMutate = agent.randVal();
			 * if (probMutate <= 0.05)
				agent.mutate();*/
		}

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
		if (showPlot) 
			graph.step();
	}

	public void buildSchedule () {
		System.out.println("Running BuildSchedule beggining");
		
		initialAction = schedule.scheduleActionAt(1, this, "initialAction");
	    //schedule.scheduleActionAt(initialSteps, this, "removeInitialAction", Schedule.LAST);
	    schedule.scheduleActionBeginning(initialSteps + 1, this, "mainAction");
		
		System.out.println("Running BuildSchedule end");
	}

	/*
	 * Creates a histogram of the degree distribution.
	 */
	private void makeHistogram() {

		degreeDist = new Histogram("Degree Distribution", maxDegree + 1, 0,
				maxDegree + 1, this);

		degreeDist.createHistogramItem("Degree Distribution", agentList,
				"getOutDegree");
	}

	/*
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
		/*
		graph.addSequence("Mutations", new Sequence() {
			public double getSValue() {
				return numMutations;
			}
		});
		*/

		graph.display();

		/*
		graph = new NetSequenceGraph("Network Stats", this, "./net.txt", PlotModel.CSV, agentList);
		graph.setAxisTitles("Time", "Statistic Value");
		graph.setXRange(0, 50);
		graph.setYRange(0, numAgents);*/
	}
	
	
	public void mutate(int agentIndex) {
		
		double probMutate = ((TrustMeAgent)agentList.get(agentIndex)).randInteger(100)+1;
		
		if (probMutate <= mutationProbability) {
			System.out.println("MUTATED!!!");
			((TrustMeAgent)agentList.get(agentIndex)).mutate();
			
			if (((TrustMeAgent)agentList.get(agentIndex)).getColor() == Color.green) {
				((TrustMeAgent)agentList.get(agentIndex)).setColor(Color.lightGray);
				
				numMutations++;
			}
			else {
				((TrustMeAgent)agentList.get(agentIndex)).setColor(Color.green);
				numMutations--;
			}
		}
	}

	
	public void tryConnection(int agentIndex) {
		
		TrustMeAgent agent = (TrustMeAgent)agentList.get(agentIndex);
		
		for (int b=0; b!=agent.bestOptions.size(); b++) {
			
			int optionId = agent.bestOptions.get(b);
			TrustMeAgent ag = (TrustMeAgent)agentList.get(optionId);
			
			// sends request
			//if(ag.acceptRequest(agent)) {
			if (((TrustMeAgent)agentList.get(optionId)).acceptRequest(agent)) {
				
				// request was accepted, so create connection
				System.out.println("CONNECTED--------------------");

				((TrustMeAgent)agentList.get(agentIndex)).setConnected(true);
				((TrustMeAgent)agentList.get(agentIndex)).connectionId = optionId;
				

				// creates edge
				TrustMeEdge edge = new TrustMeEdge ((Node)agentList.get(agentIndex), (Node)agentList.get(optionId), Color.green);
				((Node)agentList.get(agentIndex)).addOutEdge (edge);
				
				
				if (agentIndex==0) {
					System.out.println("0 created edge to " + optionId);
					System.out.println("0 now connected to " + ((TrustMeAgent)agentList.get(0)).connectionId);
				}
				
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
		
		if (agentIndex==0 && ((TrustMeAgent)agentList.get(agentIndex)).getConnected()) {
			System.out.println("0 registered connection to " + ((TrustMeAgent)agentList.get(0)).connectionId);
		}
		

	}
	
	public void breakConnection(int agentIndex) {

		((TrustMeAgent) agentList.get(agentIndex)).setConnected(false);
		

		//quebrar ligacao - como so tem uma, podemos fazer clear
		((Node)agentList.get(agentIndex)).clearOutEdges();
		((Node)agentList.get(agentIndex)).clearInEdges();

		int connectedAgent = ((TrustMeAgent)agentList.get(agentIndex)).connectionId;
		((TrustMeAgent)agentList.get(connectedAgent)).setConnected(false);
		
		((Node)agentList.get(connectedAgent)).clearOutEdges();
		((Node)agentList.get(connectedAgent)).clearInEdges();

		numConnects--;
	}
	
	public void colorEdges(int agentIndex, double trust) {
		
		Color edgeColor = Color.red;
		
		if (trust > 0.9) {edgeColor = Color.red;}
		else if (trust > 0.7) { edgeColor = Color.orange;}
		else {edgeColor = Color.yellow;}

		int outEdges = ((Node)agentList.get(agentIndex)).getOutEdges().size();
		int inEdges = ((Node)agentList.get(agentIndex)).getOutEdges().size();
		
		if (outEdges > 0) {
			((TrustMeEdge)((Node)agentList.get(agentIndex)).getOutEdges().get(0)).setColor(edgeColor);
		}
		if (inEdges > 0) {
			((TrustMeEdge)((Node)agentList.get(agentIndex)).getOutEdges().get(0)).setColor(edgeColor);
		}
		
	}
	
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
}