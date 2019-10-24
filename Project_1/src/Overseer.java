import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;
import java.util.HashMap;

public class Overseer extends Agent
{
	public final int NUMBER_OF_TEAMS = 5;

	private HashMap<AID, Integer> playerMap;

	//TODO temporary array, convert to map
	private AID[] players;

	public void setup()
	{
		playerMap = new HashMap<>();
		addBehaviour(new CheckPlayers(this, 5000));
	}

	/*
	 * Checks for new players
	 */
	private class CheckPlayers extends WakerBehaviour 
	{
		public CheckPlayers(Agent a, long timeout) {
			super(a, timeout);
		}

		protected void onWake() {
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
			informPlayerTeams();
		}



		private void informPlayerTeams() {
			int playerIndex = 0;

			//Send each player their team Number
			for(Integer i = 0; i < NUMBER_OF_TEAMS; i++)
			{
				ACLMessage informTeam = new ACLMessage(ACLMessage.INFORM);
				for(int j = 0; j < players.length / NUMBER_OF_TEAMS; j++)
				{
					informTeam.addReceiver(players[playerIndex++]);
					playerMap.put(players[playerIndex], i);
					

				}
				informTeam.setContent(i.toString());
				informTeam.setConversationId("team-number");
				send(informTeam);
			}

			//Send each player a copy of all available players, without knowing the teams
			ACLMessage playersListMsg = new ACLMessage(ACLMessage.INFORM);
			for(int i = 0; i < players.length; i++)
			{
				playersListMsg.addReceiver(players[i]);
			}
			try {
				playersListMsg.setContentObject(players);
			} catch (IOException e) {
				e.printStackTrace();
			}
			playersListMsg.setConversationId("player-list");
			send(playersListMsg);
		}
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
