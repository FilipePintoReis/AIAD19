package main;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

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
import main.Utilities.Outcome;

@SuppressWarnings("serial")
public class Overseer extends Agent
{
	public final static int NUMBER_OF_TEAMS = 5;	
	private final int ROUND_SLEEP = 50;

	//Sum of *_PROB should be 1.0

	private float team_personality_prob[][] = {
			{
				0, 100, 0
			},
			{
				100, 0, 0
			},
			{
				0, 0, 100
			},
			{
				0, 100, 0
			},
			{
				0, 0, 100
			}
	};

	private final int TIME_TO_WAKE = 5 * 1000;

	private HashMap<String, PlayerStruct> playerMap;

	private String[] players;

	private boolean inRound = false;

	public void setup()
	{
		playerMap = new HashMap<>();
		//		personalityDistribution = Utilities.personalityDistribution(PASSIVE_PROB, NEGOTIATOR_PROB, HUNTER_PROB, NUMBER_OF_TEAMS);
		//addBehaviour(new CheckPlayers(this, TIME_TO_WAKE));
	}

	private void parseArguments() {
		Object[] args = getArguments();
		for( int i = 0; i < args.length; i++)
		{
			System.out.println(args[i].toString());
		}
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
				players = new String[result.length];
				for(int i = 0; i < result.length; i++)
					players[i] = result[i].getName().getLocalName();

			} catch(FIPAException fe) 
			{
				fe.printStackTrace();
			}

			for(int i = 0; i < players.length; i++)
			{
				System.out.println(players[i]);
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
					MessageHandler.addReceiver(informTeam, players[playerIndex]);
					playerMap.put(players[playerIndex], new PlayerStruct(players[playerIndex] , i));
					playerIndex++;
				}
				send(informTeam);
			}

			//Send each player a copy of all available players, without knowing the teams
			ACLMessage playersListMsg = MessageHandler.prepareMessageObject(ACLMessage.INFORM, null, "player-list", players);
			for(int i = 0; i < players.length; i++)
			{
				MessageHandler.addReceiver(playersListMsg, players[i]);
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
			if(!gameEnd()) {
				if (!inRound)
					roundStart();
				else {
					try {
						Thread.sleep(ROUND_SLEEP);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			else
				stopGame();
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

		private void sendStartRound(String players) {
			ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.REQUEST, players, "round-start", "ROUND" + roundNumber.toString());
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
					playerMap.get(msg.getSender().getLocalName()).turnDead();
					propagateDeath(msg.getContent());
					break;
				}
				}
			}
			else block();
		}

		private void propagateDeath(String playerName) {
			ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.PROPAGATE, null, "player-death", playerName);
			System.out.println("DEATH " + playerName);
			for(int i = 0; i < players.length; i++)
			{
				MessageHandler.addReceiver(msg, players[i]);
			}
			send(msg);
		}
	}

	private boolean gameEnd() {
		boolean retVal = true;

		for(HashMap.Entry<String, PlayerStruct> entryI: playerMap.entrySet())
		{
			Integer teamI = entryI.getValue().getTeam();
			if(entryI.getValue().isAlive()) {
				for(HashMap.Entry<String, PlayerStruct> entryJ: playerMap.entrySet())
				{
					if(entryJ.getValue().isAlive())
						if(Utilities.getOutcome(entryJ.getValue().getTeam(), teamI) == Outcome.LOSS)
							retVal = false;
				}
			}
		}
		return retVal;
	}

	private void stopGame()
	{
		sendTerminateMsg();

		int[] teamSurvivors = {0, 0, 0, 0, 0};
		for(HashMap.Entry<String, PlayerStruct> entryJ: playerMap.entrySet())
		{
			if(entryJ.getValue().isAlive())
			{
				teamSurvivors[entryJ.getValue().getTeam() - 1]++;
			}
		}
		System.out.println("\n\n\nFinal headcount:");
		for(int i = 0; i < teamSurvivors.length; i++)
		{
			System.out.print("Team " + (i + 1) + ": " + teamSurvivors[i] + " survivor");
			if(teamSurvivors[i] > 1 || teamSurvivors[i] == 0) System.out.println("s.");
			else System.out.println(".");
		}
	}

	private void sendTerminateMsg() {
		ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.PROPAGATE, null, "terminate", null);
		for(int i = 0; i < players.length; i++)
		{
			MessageHandler.addReceiver(msg, players[i]);
		}
		send(msg);
		doDelete();
	}
}
