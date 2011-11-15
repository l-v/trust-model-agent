
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.NetworkRecorder;
import uchicago.src.sim.space.Object2DTorus;
import uchicago.src.sim.util.Random;

public class TrustMeModel extends SimpleModel {

	private DisplaySurface dsurf;
	private Schedule schedule;
	
	private int spaceSizeX = 400;
	private int spaceSizeY = 400;
	private int numAgents = 10;

	
	public HashMap<Integer, Integer> agentsPaired = new HashMap<Integer, Integer>();
	
	public void setNumAgents (int n) {
		numAgents = n;
	}

	public int getNumAgents () {
		return numAgents;
	}

	public int getSpaceSizeX () {
		return spaceSizeX;
	}

	public void setSpaceSizeX (int size) {
		spaceSizeX = size;
	}

	public int getSpaceSizeY () {
		return spaceSizeY;
	}

	public void setSpaceSizeY (int size) {
		spaceSizeY = size;
	}
	
	public void begin () {
	    buildModel ();
	    buildDisplay ();
	    buildSchedule ();
	    dsurf.display ();
	}

	public void setup() {
		//super.setup();
		Random.createUniform ();
		
		if (dsurf != null) 
			dsurf.dispose();
		
		dsurf = null;
	    schedule = null;

	    System.gc ();
		
		dsurf = new DisplaySurface(this,"TrustMe");
		//super.
		registerDisplaySurface("TrustMe",dsurf);
		schedule = new Schedule (1);
		agentList = new ArrayList (numAgents);
	}
	
	public String[] getInitParam () {
	    String[] params = {"numAgents", "spaceSizeX", "spaceSizeY" };
	    return params;
	}
	
	public Schedule getSchedule () {
	    return schedule;
	}

	public void buildModel() {	    
	    // add agents with random values
	    for (int i = 0; i != numAgents; i++) {
	    	
	    	// create the Oval nodes.
	    	int x = Random.uniform.nextIntFromTo (0, spaceSizeX - 1);
	    	int y = Random.uniform.nextIntFromTo (0, spaceSizeY - 1);
	    	OvalNetworkItem drawable = new OvalNetworkItem (x, y);
	    	
			// cria o agente
			TrustMeAgent agent = new TrustMeAgent(spaceSizeX, spaceSizeY, drawable, i);
			agent.setNodeLabel ("Oval - " + i);
			agent.setBorderColor (Color.orange);
			agent.setBorderWidth (4);
			
			//adicionar à lista de agentes do modelo
			agentList.add(agent);
	    }
	    
	    
	   //Object2DDisplay display = new Object2DDisplay(space);
	   //dsurf.addDisplayable(display,"Buttons Space");
	   dsurf.display();	    
	}
	
	public void buildDisplay () {
	    Network2DDisplay display = new Network2DDisplay (agentList, spaceSizeX, spaceSizeY);

	    dsurf.addDisplayableProbeable (display, "TrustMe View");
	    dsurf.addZoomable (display);
	    dsurf.setBackground (java.awt.Color.white);
	    addSimEventListener (dsurf);
	}

	public void buildSchedule () {
		
		schedule.scheduleActionBeginning (0, new BasicAction () {
			public void execute() {
				// calculate trust for all agents
				int numAgents = agentList.size();
				for (int i = 0; i != numAgents; i++) {
			
				if (i!=0)
					continue;
				
				TrustMeAgent agent = (TrustMeAgent) agentList.get(i);
			
				//complexidade n^2
				for(int j = 0; j != numAgents; j++) {
					//agent.overallTrust = sinalpha(agent);
					
	
					if (j!=0 && j!=1)
						continue;
					
					if (j!=i) {
						double trust = agent.getTrust((TrustMeAgent) agentList.get(j), j);
						
						// if agent is not too different, consider him an option
						if (trust != -1)
							agent.setAgentTrust(j, trust);
						/*if (trust < 1 &&  i==0 && j==1)
							System.out.println(trust);*/
						
					}
				}
				// TODO: erase prints of stuff for testing
				/*if (agent.overallTrust<1.0 && i==0)
					System.out.println(agent.overallTrust);*/
				
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
				//	System.out.println("\nTraits 1: " + ((TrustMeAgent) agentList.get(1)).printTraits());

				//	String output2 = myFormatter.format(((TrustMeAgent) agentList.get(1)).getTrustIn(0));
				//	System.out.println("Trust 1 in 0: " +  output2 + "\n------------");
				//System.out.println("Trust 1 in 0: " + ((TrustMeAgent) agentList.get(1)).getTrustIn(0) + "\n------------");
				
				dsurf.updateDisplay ();
			}
		});
	}
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
	
}