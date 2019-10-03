import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class QueryListeningAgent extends Agent {
	public void setup() {
		addBehaviour(new QueryListeningBehaviour());
	}

	class QueryListeningBehaviour extends CyclicBehaviour {
		AID listener = new AID("listener", AID.ISLOCALNAME);
		@Override
		public void action() {
			ACLMessage reply = receive();
			if(reply == null){
				ACLMessage msg = new ACLMessage( ACLMessage.INFORM );
				msg.setContent("Just wanted to say hi!" );
				msg.addReceiver(listener);
				send(msg);
			}
			else
				System.out.println("Reply: " + reply.getContent());
			block();
		}
	}
}
