import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class Player extends Agent
{
	private int teamNumber;

	public void setup()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("player");
		sd.setName("JADE-battle-royale");
		dfd.addServices(sd);

		try 
		{
			DFService.register(this, dfd);
		} catch (FIPAException fe) 
		{
			fe.printStackTrace();
		}

		addBehaviour(new TeamListener());
	}

	private class TeamListener extends CyclicBehaviour
	{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				Integer msgContent = Integer.parseInt(msg.getContent());
				System.out.println(myAgent.getLocalName() + " : " + msgContent);
			}
			else block();
		}
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
