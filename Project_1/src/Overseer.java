import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;


//TODO register in DF
@SuppressWarnings("serial")
public class Overseer extends Agent
{
	private HashMap<AID, Integer> playerMap;

	//TODO temporary array, convert to map
	private AID[] players;


	public void setup()
	{
		playerMap = new HashMap<>();
		addBehaviour(new OverseerBehaviour());
	}

	private class OverseerBehaviour extends CyclicBehaviour
	{
		@Override
		public void action()
		{
			ACLMessage msg = receive();
			if(msg != null)
			{
				String msgContent = msg.getContent();
				switch(msgContent){
				case "BIRTH": 
				{
					//TODO choose team number
					Integer teamNumber = 1;
					announceBirth(msg.getSender(), teamNumber);
					break;
				}
				case "DEATH":
				{
					announceDeath(msg.getSender());
					break;
				}
				}
			}
			else block();
		}
		private void announceBirth(AID newbornPlayer, Integer teamNumber)
		{
			// TODO Send messages to all existing agents about new birth
			playerMap.put(newbornPlayer, teamNumber);
			System.out.println("New Birth: " + newbornPlayer.getLocalName() + "\tTeam: " + teamNumber);
			System.out.println(playerMap.toString());
		}

		private void announceDeath(AID deceasedPlayer)
		{
			Integer teamNumber = playerMap.get(deceasedPlayer);
			playerMap.remove(deceasedPlayer);
			//TODO Send message to all alive agents about new death
			System.out.println("New Death: " + deceasedPlayer.getLocalName() + "\tTeam: " + teamNumber);
			System.out.println(playerMap.toString());

		}
	}

/*
 * Checks regularly for new players
 */
	private class CheckPlayer extends TickerBehaviour 
	{

		public CheckPlayer(Agent agent, long period)
		{
			super(agent, period);
		}

		@Override
		protected void onTick() {
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("player");
			template.addServices(sd);

			try
			{
				DFAgentDescription[] result = DFService.search(myAgent, template);
				players = new AID[result.length];
				for(int i = 0; i < result.length; i++)
					players[i] = result[i].getName();

			} catch(FIPAException fe) 
			{
				fe.printStackTrace();
			}
		}
	}
}
