import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimpleModel;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.space.Object2DTorus;



public class TrustModel extends SimpleModel {
	private int numberOfAgents;
	
	private DisplaySurface dsurf;
	private Object2DTorus space;
	
	public TrustModel() {
		name = "My Hello World Model";
	}
	
	public void setup() {
		super.setup();
		numberOfAgents = 3;
		autoStep = true;
		shuffle = true;
	}
	

	public String[] getInitParam() {
		return new String[] { "numberOfAgents"};
	}
	
	public void buildModel() {
		for(int i=0; i<numberOfAgents; i++)
			agentList.add(new TrustAgent(i));
	}
	

	protected void preStep() {
		System.out.println("Initiating step " +
				getTickCount());

		// check connections every n ticks
		// break those that are bad
		
		// do mutation - random and 30ticks (let user CHOOSE!)
		
	}
	
	protected void postStep() {
		System.out.println("Done step " +
				getTickCount());
	}
}