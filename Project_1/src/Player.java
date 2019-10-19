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
	private int teamNumber = -1;

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
		addBehaviour(new PlayGame());
	}

	private class TeamListener extends CyclicBehaviour
	{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				teamNumber = Integer.parseInt(msg.getContent());
			}
			else block();
		}
	}

	private class PlayGame extends CyclicBehaviour
	{

		@Override
		public void action() {
			// TODO Auto-generated method stub

		}
	}

	protected void takeDown() {
		// Deregister from the yellow pages
		try
		{
			DFService.deregister(this);
		}
		catch (FIPAException fe)
		{
			fe.printStackTrace();
		}
		System.out.println("Player-agent" + getAID().getLocalName() + "terminating.");
	}
}
