package main;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("serial")
public class Overseer extends Agent
{
	private final int TIME_TO_WAKE = 5 * 1000;

	public final int NUMBER_OF_TEAMS = 5;

	private HashMap<AID, PlayerStruct> playerMap;

	private AID[] players;

	private boolean inRound = false;


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

			for(int i = 0; i < players.length; i++)
			{
				System.out.println(players[i].getLocalName());
			}

			informPlayerTeams();

			myAgent.addBehaviour(new GameLoop());
			myAgent.addBehaviour(new MessageListener());
		}

		private void informPlayerTeams() {
			int playerIndex = 0;

			//Send each player their team Number
			for(Integer i = 0; i < NUMBER_OF_TEAMS; i++)
			{
				ACLMessage informTeam = MessageHandler.prepareMessage(ACLMessage.INFORM, null, "team-number", i.toString());
				for(int j = 0; j < players.length / NUMBER_OF_TEAMS; j++)
				{
					informTeam.addReceiver(players[playerIndex]);
					playerMap.put(players[playerIndex], new PlayerStruct(i));
					playerIndex++;
				}
				send(informTeam);
			}

			//Send each player a copy of all available players, without knowing the teams
			ACLMessage playersListMsg = MessageHandler.prepareMessageObject(ACLMessage.INFORM, null, "player-list", players);
			for(int i = 0; i < players.length; i++)
			{
				playersListMsg.addReceiver(players[i]);
			}
			send(playersListMsg);
		}
	}


	private class GameLoop extends SimpleBehaviour {
		private int playerStart = 0;
		private int playerIndex = 0;
		private Integer roundNumber = 0;

		@Override
		public void action() {
			if (!inRound)
				roundStart();
			else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		private void roundStart()
		{
			if(playerIndex == players.length) playerIndex = 0;
			if(playerIndex == playerStart) {
				playerStart = ThreadLocalRandom.current().nextInt(0, players.length);
				playerIndex = playerStart;
				roundNumber++;
			}
			sendStartRound(players[playerIndex]);
			playerIndex++;
			inRound = true;
		}

		private void sendStartRound(AID player) {
			ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.REQUEST, player, "round-start", roundNumber.toString());
			send(msg);
		}

		@Override
		public boolean done() {
			// TODO Define done function
			return false;
		}
	}

	private class MessageListener extends CyclicBehaviour
	{
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = receive(mt);
			if (msg != null) {
				System.out.println(msg.getContent());
				switch(msg.getConversationId())
				{
				case "round-start": {
					if (msg.getContent().equals("DONE"))
						inRound = false;
					else
						System.err.println("Received unexpected message for end of round.");
					break;
				}
				case "inform-death": {
					playerMap.get(msg.getSender()).turnDead();
					propagateDeath(msg.getContent());
				}
				}
			}
			else block();
		}

		private void propagateDeath(String playerName) {
			ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.PROPAGATE, null, "player-death", playerName);
			for(int i = 0; i < players.length; i++)
			{
				msg.addReceiver(players[i]);
			}
			send(msg);
		}
	}
}
