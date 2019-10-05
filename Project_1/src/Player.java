import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Player extends Agent
{
	public void setup()
	{
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
				ACLMessage msgBirth = MessageHandler.getBirthMessage();
				send(msgBirth);
				System.out.println("Message sent: " + msgBirth.getContent());
				msgSent++;
				block(4000);
				break;
			}
			case 1:
			{
				ACLMessage msgDeath = MessageHandler.getDeathMessage();
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
