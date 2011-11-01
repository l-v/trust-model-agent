package TrustMe;

import java.awt.Color;
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
	
	
	// anything missing?
	
	

	
	
	
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
			
			TrustMeAgent agent = (TrustMeAgent) agentList.get(i);
			
			//complexidade n^2
			for(int j = i+1; j != numAgents; j++) {
				//agent.overallTrust = sinalpha(agent);
			}
						
			// TODO: erase prints of stuff for testing
			if (agent.overallTrust<1.0 && i==0)
				System.out.println(agent.overallTrust);
		}
		
	}
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
	
}