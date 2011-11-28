

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.repastdemos.jiggle.JiggleEdge;
import uchicago.src.sim.analysis.NetSequenceGraph;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Controller;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.AbstractGraphLayout;
import uchicago.src.sim.gui.CircularGraphLayout;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.FruchGraphLayout;
import uchicago.src.sim.gui.KamadaGraphLayout;
import uchicago.src.sim.gui.LayoutWithDisplay;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.gui.RectNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;
import uchicago.src.sim.network.Node;
import uchicago.src.sim.util.Random;

public class TrustMeModel extends SimModelImpl/*SimpleModel*/ {

	// Model variables
	private int updateEveryN = 5;
	private int initialSteps = 1;
	// Default values
	private int spaceSizeX = 400;
	private int spaceSizeY = 400;
	private int numAgents = 10;
	private ArrayList agentList = new ArrayList(numAgents);
	
	public HashMap<Integer, Integer> agentsPaired = new HashMap<Integer, Integer>();
	
	// Implementation variables
	private String layoutType = "Fruch";
	private DisplaySurface dsurf;
	private Schedule schedule;
	private AbstractGraphLayout graphLayout;
	private NetSequenceGraph graph;
	private BasicAction initialAction;
	

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
	
	public void begin () {
	    buildModel ();
	    buildDisplay ();
	    buildSchedule ();
	    dsurf.display ();
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

	    System.gc ();

		dsurf = new DisplaySurface(this,"TrustMe");
		//super.
		registerDisplaySurface("TrustMe",dsurf);
		schedule = new Schedule();
		agentList = new ArrayList(); //new ArrayList(numAgents);
		
		System.out.println("Running setup end");
	}
	
	public String[] getInitParam () {
	    String[] params = {"numAgents", "spaceSizeX", "spaceSizeY", "updateEveryN", "LayoutType"};
	    return params;
	}
	
	public Schedule getSchedule () { return schedule; }
	
	public String getName() { return "TrustMeModel"; }

	public void buildModel() {	    
		System.out.println("Running BuildModel begginning");
	    // add agents with random values
	    for (int i = 0; i != numAgents; i++) {
	    	
	    	// create the Oval nodes.
	    	int x = Random.uniform.nextIntFromTo (0, spaceSizeX - 1);
	    	int y = Random.uniform.nextIntFromTo (0, spaceSizeY - 1);
	    	
	    	// cria o agente
	    	TrustMeAgent agent = new TrustMeAgent(spaceSizeX, spaceSizeY, i);
	    	agent.setNodeLabel ("Oval - " + i);
			agent.setBorderColor (Color.orange);
			agent.setBorderWidth (4);
	    	
			// adds the agent to agentList
	    	agentList.add(agent);
	    	/*
	    	OvalNetworkItem drawable = new OvalNetworkItem (x, y);
	    	
			// cria o agente
			TrustMeAgent agent = new TrustMeAgent(spaceSizeX, spaceSizeY, drawable, i);
			agent.setNodeLabel ("Oval - " + i);
			agent.setBorderColor (Color.orange);
			agent.setBorderWidth (4);
			
			// adds the agent to agentList
			agentList.add(agent);*/
	    }
	    
	    //Network2DDisplay display = new Network2DDisplay (agentList, spaceSizeX, spaceSizeY);
	   //Object2DDisplay display = new Object2DDisplay(space);
	   //dsurf.addDisplayable(display,"Buttons Space");
	   //dsurf.display();
	   
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
		/*
	    Network2DDisplay display = new Network2DDisplay (agentList, spaceSizeX, spaceSizeY);

	    dsurf.addDisplayableProbeable (display, "TrustMe View");
	    dsurf.addZoomable (display);
	    dsurf.setBackground (java.awt.Color.white);
	    addSimEventListener (dsurf); */
	    
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
			
			// probability of mutation
			double probMutate = agent.randVal();
			if (probMutate <= 0.25)
				agent.mutate();
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
			agent.purgeBestOptions();
			
			if (agent.connected) {
				
				//check if connection must be broken
				
				
				
				// probability of mutation
				double probMutate = agent.randVal();
				if (probMutate <= 0.25)
					agent.mutate();
				
				continue;
			}
			
			//complexidade n^2
			for(int j = 0; j != numAgents; j++) {
				//agent.overallTrust = sinalpha(agent);

				System.out.println("i:j  " + i+":"+j);
				if (((TrustMeAgent)agentList.get(j)).connected)
					continue;
				
				/*if (j!=0 && j!=1)
					continue;
				*/
				if (j!=i) {
					double trust = agent.getTrust((TrustMeAgent) agentList.get(j));
					System.out.println("i:j -> " + i+":"+j);
					// TODO: functionality disabled
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

	

			// tries to get a connection
			for (int b=0; b!=agent.bestOptions.size(); b++) {
				
				int optionId = agent.bestOptions.get(b);
				TrustMeAgent ag = (TrustMeAgent)agentList.get(optionId);
				
				// sends request
				if(ag.acceptRequest(agent)) {
					
					// request was accepted, so create connection
					System.out.println("CONNECTED--------------------");

					((TrustMeAgent)agentList.get(i)).connected = true;
					((TrustMeAgent)agentList.get(i)).connectionId = b;
					
					// creates edge
					TrustMeEdge edge = new TrustMeEdge ((Node)agentList.get(i), (Node)agentList.get(optionId), Color.red);
					((Node)agentList.get(i)).addOutEdge (edge);
					
					break;
				}
			}
			
			
			// probability of mutation
			double probMutate = agent.randVal();
			if (probMutate <= 0.25)
				agent.mutate();
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
	}

	public void buildSchedule () {
		System.out.println("Running BuildSchedule beggining");
		
		initialAction = schedule.scheduleActionAt(1, this, "initialAction");
	    //schedule.scheduleActionAt(initialSteps, this, "removeInitialAction", Schedule.LAST);
	    schedule.scheduleActionBeginning(initialSteps + 1, this, "mainAction");
		
		System.out.println("Running BuildSchedule end");
	}
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
}