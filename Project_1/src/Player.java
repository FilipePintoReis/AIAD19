import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Player extends Agent
{
	AID overseer;
	public void setup()
	{
		overseer = new AID("overseer", AID.ISLOCALNAME);
		addBehaviour(new PlayerBehaviour());
	}
	private class PlayerBehaviour extends CyclicBehaviour
	{
		private int msgSent = 0;

		@Override
		public void action() {
			switch(msgSent)
			{
			case 0:
			{
				ACLMessage msgBirth = new ACLMessage(ACLMessage.INFORM);
				msgBirth.setContent("BIRTH");
				msgBirth.addReceiver(overseer);
				send(msgBirth);
				System.out.println("Message sent: " + msgBirth.getContent());
				block(4000);
				msgSent++;
				break;
			}
			case 1:
			{
				ACLMessage msgDeath = new ACLMessage(ACLMessage.INFORM);
				msgDeath.setContent("DEATH");
				msgDeath.addReceiver(overseer);
				send(msgDeath);
				System.out.println("Message sent: " + msgDeath.getContent());
				block(4000);
				msgSent++;
				break;
			}
			default: block();
			}
		}
	}
}
