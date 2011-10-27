

import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.space.Object2DTorus;

public class TrustMeModel extends SimpleModel {

	private DisplaySurface dsurf;
	private int spaceSize = 50;

	
	private double p1ManagementOfOwnMoney = 0.5;
	private double p1CookingAbilities = 0.4;
	
	
	/***
	 *  sinalpha parameters
	 */
	double delta = 0.5;
	double alpha0 = 3.0*Math.PI/2.0;
	double alpha1 = 5.0*Math.PI/2.0;
	
	double omega = Math.PI/12.0; // time/steps to reach maximum trust level
	double lambdaPos = 1.0; // weight of positive attributes
	double lambdaNeg = -1.5; // weight of negative attributes
	
	
	
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

	    Object2DDisplay display = new Object2DDisplay(space);
	    dsurf.addDisplayable(display,"Buttons Space");

	    dsurf.display();
	}

	public void step() {
	   // int size = agentList.size();
	}
	
	
	public void sinalpha() {
		// trust = delta * sin(alpha) + d
		// alpha = alpha0 + lambda*omega
		
		double lambda = 0.0;
		int numAgents = agentList.size();
		
		for (int i=0; i!=numAgents; i++) {
			TrustMeAgent agent = (TrustMeAgent) agentList.get(i);
			
			// no idea if the formula works like this yet, just making it up =P
			lambda = lambdaPos*agent.getPosTraits() + lambdaNeg*agent.getNegTraits();
		}
		
		
		double alpha = alpha0 + lambda*omega;
		double trust = delta * Math.sin(alpha) + delta;
		
		// missing: apply trust values to agents
	}
	
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
	
	
}