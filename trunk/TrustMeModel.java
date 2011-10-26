package TrustMe;

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
	
	public static void main(String[] args) {
		SimInit init = new SimInit();
		init.loadModel(new TrustMeModel(), null, false);
	}
}