import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.HashMap;

public class Overseer extends Agent
{
	HashMap<AID, Integer> playerMap;
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
}
