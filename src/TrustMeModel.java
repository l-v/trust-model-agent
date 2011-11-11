
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.HashMap;

import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DTorus;

public class TrustMeModel extends SimpleModel {

	private DisplaySurface dsurf;
	private int spaceSize = 50;
	private int numAgents = 50;

	
	public HashMap<Integer, Integer> agentsPaired = new HashMap<Integer, Integer>();
	
	
	/***
	 * Default values desirable agent attributes (for testing?)
	 */
	private double p1ManagementOfOwnMoney = 0.5;
	private double p1CookingAbilities = 0.4;
	

	public void setP1ManagementOfOwnMoney(double p1ManagementOfOwnMoney) {
		this.p1ManagementOfOwnMoney = p1ManagementOfOwnMoney;
	}

	public double getP1ManagementOfOwnMoney() {
		return p1ManagementOfOwnMoney;
	}

	public void setP1CookingAbilities(double p1CookingAbilities) {
		this.p1CookingAbilities = p1CookingAbilities;
	}

	public double getP1CookingAbilities() {
		return p1CookingAbilities;
	}

	
	
	public void setup() {
		super.setup();
		
		if (dsurf != null) 
			dsurf.dispose();
		
		dsurf = new DisplaySurface(this,"TrustMe");
		super.registerDisplaySurface("TrustMe",dsurf);
	}

	public void buildModel() {
	    Object2DTorus space = new Object2DTorus(spaceSize, spaceSize);

	    
	    // add agents with random values
	    for (int i = 0; i != numAgents; i++) {
	    	
			int x = getNextIntFromTo(0, spaceSize -1);
			int y = getNextIntFromTo(0, spaceSize - 1);
			Color color = new Color(
					getNextIntFromTo(0, 255),
					getNextIntFromTo(0, 255),
					getNextIntFromTo(0, 255)
					);
			
			// cria o agente
			TrustMeAgent agent = new TrustMeAgent(space, i);
			agent.setXY(x, y);
			agent.setColor(color);
			
			//adicionar à lista de agentes do modelo
			agentList.add(agent);
	    }
	    
	    
	    Object2DDisplay display = new Object2DDisplay(space);
	    dsurf.addDisplayable(display,"Buttons Space");
	    dsurf.display();	    
	}

	public void step() {
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
		
	}
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
	
}