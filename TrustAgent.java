import uchicago.src.sim.engine.Stepable;

public class TrustAgent implements Stepable {
	private int id;
	private int trustValue;
	
	public TrustAgent(int id) {
		this.id = id;
		
		// initialize with initial/default value of trust
	}
	
	
	public void step() {
		System.out.println(id + " Hello World!");
		
		//formula - calculate trust value for each agent
		
		// post-step or not?
		// lan�a 'pedido' -> outro aceita ou recusa
		// lan�ar 1~3 pedidos sequencias at� ser aceite
	}
}