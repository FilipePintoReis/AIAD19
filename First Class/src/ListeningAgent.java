import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ListeningAgent extends Agent {
	public void setup() {
		addBehaviour(new ListeningBehaviour());
	}

	class ListeningBehaviour extends CyclicBehaviour {

		@Override
		public void action() {
			ACLMessage msg = receive();
			if (msg!=null){
				System.out.println(
						" - " + myAgent.getLocalName() + " <- " + 
								msg.getContent());
				ACLMessage reply = msg.createReply();
                reply.setPerformative( ACLMessage.INFORM );
                reply.setContent(" Howdy" );
                send(reply);
			}
			block();
		}		
	}
}
