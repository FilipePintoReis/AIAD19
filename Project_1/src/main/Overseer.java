package main;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
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

@SuppressWarnings("serial")
public class Overseer extends Agent
{
	public final static int NUMBER_OF_TEAMS = 5;	

	//Sum of *_PROB should be 1.0

	private float team_personality_prob[][] = {
			{
				0, 0, 100
			},
			{
				0, 0, 100
			},
			{
				0, 0, 100
			},
			{
				0, 0, 100
			},
			{
				0, 0, 100
			}
	};

	private final int TIME_TO_WAKE = 5 * 1000;

	private HashMap<AID, PlayerStruct> playerMap;

	private AID[] players;

	private boolean inRound = false;

	public void setup()
	{
		playerMap = new HashMap<>();
		//		personalityDistribution = Utilities.personalityDistribution(PASSIVE_PROB, NEGOTIATOR_PROB, HUNTER_PROB, NUMBER_OF_TEAMS);
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
			informPersonalities();

			myAgent.addBehaviour(new GameLoop());
			myAgent.addBehaviour(new MessageListener());
		}

		private void informPlayerTeams() {
			int playerIndex = 0;

			//Send each player their team Number
			for(Integer i = 1; i <= NUMBER_OF_TEAMS; i++)
			{
				ACLMessage informTeam = MessageHandler.prepareMessage(ACLMessage.INFORM, null, "team-number", i.toString());
				for(int j = 0; j < players.length / NUMBER_OF_TEAMS; j++)
				{
					informTeam.addReceiver(players[playerIndex]);
					playerMap.put(players[playerIndex], new PlayerStruct(players[playerIndex] , i));
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

		private void informPersonalities() {
			int playerIndex = 0;
			for (int i = 0; i < NUMBER_OF_TEAMS; i++)
			{
				for (int j = 0; j < players.length / NUMBER_OF_TEAMS; j++)
				{
					String personality = getPersonalityChance(team_personality_prob[i]);
					ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.INFORM, players[playerIndex], "personality", personality);
					send(msg);
					playerIndex++;
				}
			}
		}

		private String getPersonalityChance(float[] chance) {
			int random = ThreadLocalRandom.current().nextInt(0, 100);
			if(random < chance[0])
				return "PASSIVE";

			random -= chance[0];
			if(random < chance[1])
				return "NEGOTIATOR";

			return "HUNTER";
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
			System.out.println(playerName + " has shinderuded");
			for(int i = 0; i < players.length; i++)
			{
				msg.addReceiver(players[i]);
			}
			send(msg);
		}

		private boolean gameEnd() {
			Integer[] retVal = {null};
			playerMap.forEach((key, value)->{
			Integer team = value.getTeam;
			if(team != 1)
			playerMap.forEach((key, value)->{
				if(value.getTeam() == team - 1 || value.getTeam() == team + 1) {
					retVal[0]++;
				}
			});

		else
			playerMap.forEach((key, value)->{
				if(value.getTeam() == 5) {
					retVal[0]++;
				}
			});
		}
		if(retVal[0] != null)
			return false;
		return true;
	}
}
