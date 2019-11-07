package main;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import java.io.IOException;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Overseer extends Agent
{
	private final int TIME_TO_WAKE = 5 * 1000;
	
	public final int NUMBER_OF_TEAMS = 5;

	private HashMap<AID, PlayerStruct> playerMap;

	private AID[] players;

	public void setup()
	{
		playerMap = new HashMap<>();
		addBehaviour(new CheckPlayers(this, TIME_TO_WAKE));
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
					playerMap.put(players[playerIndex], new PlayerStruct(i));
					

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
}