package main;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import main.Utilities.*;
import personality.*;

@SuppressWarnings("serial")
public class Player extends Agent
{
	private int ROUND_SLEEP = 100;

	private static final int UNKNOWN = -1;
	private Personality personality = null;
	private PlayerStruct myStruct;

	private boolean inNegotiation = false;
	private String negotiationClient = null;
	
	public Integer teamNumber;

	private HashMap<String, PlayerStruct> playerMap = new HashMap<String, PlayerStruct>();

	@Override
	public void setup()
	{
		registerOnDFD();

		SequentialBehaviour playerBehaviour = new SequentialBehaviour(this);
		playerBehaviour.addSubBehaviour(new TeamListener());
		playerBehaviour.addSubBehaviour(new RoundListener());

		addBehaviour(playerBehaviour);
	}

	private void registerOnDFD() {
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
	}

	private class TeamListener extends SimpleBehaviour
	{
		private boolean hasTeam = false;
		private boolean hasPlayerList = false;
		@Override
		public void action() {		
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				switch(msg.getConversationId()) {
				case "team-number":
					if(!hasTeam) {
						teamNumber = Integer.parseInt(msg.getContent());
						hasTeam = true;
					}
					break;
				case "player-list":
					if(!hasPlayerList) {
						try {
							turnPlayerArrayIntoMap(msg.getContentObject(), playerMap);
						} catch (UnreadableException e) {					
							e.printStackTrace();
							System.err.println("Couldn't retrieve player List from message.");
						}
						hasPlayerList = true;
					}
					break;
				case "personality":
					if(personality == null) {
						String pers = msg.getContent();
						switch(pers){
						case "HUNTER":
							personality = new Hunter();
							break;
						case "PASSIVE":
							personality = new Passive();
							break;
						case "NEGOTIATOR":
							personality = new Negotiator();
							break;
						}
						System.out.println(myAgent.getLocalName() + " of team " + teamNumber + " is " + pers);
					}
					break;
				}
			}
			else block();
		}

		private void turnPlayerArrayIntoMap(Serializable playerArray, HashMap<String, PlayerStruct> playerMap) {
			String[] array = (String[]) playerArray;
			for(int i = 0; i < array.length; i++)
			{
				playerMap.put(array[i], new PlayerStruct(array[i], UNKNOWN));
			}
			myStruct = new PlayerStruct(this.myAgent.getLocalName(), teamNumber);
			playerMap.put(myAgent.getLocalName(), myStruct);
		}

		@Override
		public boolean done() {
			if(hasTeam && hasPlayerList && personality != null)
			{
				myAgent.addBehaviour(new RoundListener());
				myAgent.addBehaviour(new InterPlayerListener());
				myAgent.addBehaviour(new DeathListener());
				return true;
			}
			else return false;
		}
	}

	private class RoundListener extends CyclicBehaviour {
		private int actionPhase;
		private boolean inRound;
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if( msg != null && msg.getConversationId().equals("round-start")) {
				inRound = true;
				roundAction();
				sendEndRound(msg);
			}
			else block();
		}

		private void roundAction() {
			if(myStruct.isAlive()) {
				System.out.println("\n" + myAgent.getLocalName() + ": TURN " + myAgent.getLocalName() + " of " + teamNumber);
				try {
					Thread.sleep(ROUND_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				switch(personality.decideAction(playerMap, myStruct))
				{
				case Duel:
					actionPhase = 0;
					String opponent = personality.decideWhoToBattle(playerMap, myStruct);
					if(opponent != null) {
						duelPlayer(opponent);
					}
					else System.out.println(myAgent.getLocalName() + ": SKIP");
					break;
				case Negotiate:
					actionPhase = 0;
					String item = personality.decideWhatToNegotiate(playerMap, myStruct);
					String client = personality.decideWhoToNegotiate(playerMap, myStruct);
					if(item != null && client != null)
						negotiate(client, item);
					break;
				case Abstain:
					System.out.println(myAgent.getLocalName() + ": ABSTAIN");
				default:
				}
				shareMapWithTeam();
				actionPhase = 0;
			}
			else {
				//				System.out.println("SLEEP " + myAgent.getLocalName());
			}
		}

		private void negotiate(String client, String item) {
			while(actionPhase != 2)
			{
				switch(actionPhase)
				{
				case 0:
					ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.PROPOSE, client, "negotiation", item);
					System.out.println(myAgent.getLocalName() + ": PROPOSE 1: " + item + " to " + client);
					send(msg);
					actionPhase = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchSender(new AID(client, AID.ISLOCALNAME));
					ACLMessage response = receive(mt);
					if(response != null)
					{
						switch(response.getPerformative())
						{
						case ACLMessage.ACCEPT_PROPOSAL:
						{
							String[] args = response.getContent().split(":");
							String clientProposal = args[0];
							System.out.println(myAgent.getLocalName() + ": Client Accepted - " + response.getContent() + " from " + response.getSender().getLocalName() + " about " + response.getConversationId());
							if(!hasInfo(clientProposal) && personality.acceptNegotiation(playerMap, clientProposal))
							{
								Integer clientProposalTeam = Integer.parseInt(args[1]);
								playerMap.get(clientProposal).setTeam(clientProposalTeam);

								Integer itemTeam = playerMap.get(item).getTeam();
								ACLMessage reply = MessageHandler.prepareReply(response, ACLMessage.ACCEPT_PROPOSAL, itemTeam.toString());
								System.out.println(myAgent.getLocalName() + ": COUNTER  " + response.getSender().getLocalName());
								send(reply);	
								actionPhase = 2;
							}
							else
							{
								System.out.println(myAgent.getLocalName() + ": REJECT to  " + response.getSender().getLocalName());
								ACLMessage reply = MessageHandler.prepareReply(response, ACLMessage.REJECT_PROPOSAL, null);
								send(reply);	
								actionPhase = 2;
							}
							break;
						}
						case ACLMessage.REJECT_PROPOSAL:
						{
							actionPhase = 2;
							break;
						}
						}
					}
					else block();
					break;
				}
			}
		}

		private void duelPlayer(String opponent) {
			while(actionPhase != 2) {
				switch(actionPhase)
				{
				case 0:
					System.out.println(myAgent.getLocalName() + ": CHALLENGE " + opponent);
					ACLMessage challengeMsg = MessageHandler.prepareMessage(ACLMessage.PROPOSE, opponent, "duel", teamNumber.toString());
					send(challengeMsg);
					actionPhase = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchSender(new AID(opponent, AID.ISLOCALNAME));
					ACLMessage outcomeMessage = receive(mt);
					if(outcomeMessage != null)
					{

						String msgContent = outcomeMessage.getContent();
						System.out.println(myAgent.getLocalName() + ": Duel Response " + msgContent + " from " + outcomeMessage.getSender().getLocalName() + " about " + outcomeMessage.getConversationId());
						String[] msgArgs =  msgContent.split(":");
						Outcome outcome;
						switch(msgArgs[0])
						{
						case "1":
						case "2":
						case "3":
						case "4":
						case "5":
							int oppTeam = Integer.parseInt(msgArgs[0]);
							outcome = Utilities.getOutcome(teamNumber, oppTeam);
							handleOutcome(outcome, opponent, oppTeam);
							break;
						default:
							outcome = Utilities.adjustOutcome(Outcome.valueOf(msgArgs[0]));
							handleOutcome(outcome, opponent ,Integer.parseInt(msgArgs[1]));
						}

						actionPhase = 2;
					}
					else block();
					break;
				}
			}
		}

		private void shareMapWithTeam() {
			String[] teammates = getTeamArray();
			HashMap<String, Integer> shareMap = createShareMap();
			ACLMessage msg = MessageHandler.prepareMessageObject(ACLMessage.PROPOSE, null, "share-map", shareMap);
			for(int i = 0; i < teammates.length; i++)
			{
				MessageHandler.addReceiver(msg, teammates[i]);
			}
			send(msg);
		}

		private HashMap<String, Integer> createShareMap() {
			HashMap<String, Integer> shareMap = new HashMap<String, Integer>();
			for(HashMap.Entry<String, PlayerStruct> entry: playerMap.entrySet())
			{
				if(entry.getValue().getTeam() != UNKNOWN)
				{
					shareMap.put(entry.getKey(), entry.getValue().getTeam());
				}
			}
			return shareMap;
		}

		private String[] getTeamArray() {
			String[] teammates = new String[playerMap.size()/Overseer.NUMBER_OF_TEAMS];
			int i = 0;
			for(HashMap.Entry<String, PlayerStruct> entry: playerMap.entrySet())
			{
				if(entry.getValue().isAlive() && entry.getValue().getTeam() != UNKNOWN &&
						Outcome.SAME_TEAM == Utilities.getOutcome(myStruct.getTeam(), entry.getValue().getTeam()) && 
						!entry.getKey().equals(myAgent.getAID().getLocalName()))
					teammates[i] = entry.getKey();

			}
			return teammates;
		}

		private void sendEndRound(ACLMessage msg)
		{
			if(inRound = true) {
				ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.INFORM, "DONE");
				inRound = false;
				send(reply);
			}
		}


	}

	private class DeathListener extends SimpleBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				switch(msg.getConversationId())
				{
				case "player-death":
					playerMap.get(msg.getContent()).turnDead();
					break;
				case "terminate":
					myAgent.doDelete();
					break;
				}
			}
			else block();
		}
		@Override
		public boolean done() {
			return !myStruct.isAlive();
		}
	}

	private class InterPlayerListener extends SimpleBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				switch(msg.getConversationId())
				{
				case "duel":
					Integer duelTeam = Integer.parseInt(msg.getContent());
					Outcome outcome = Utilities.getOutcome(teamNumber, duelTeam);
					replyOutcome(msg, outcome, teamNumber);
					handleOutcome(outcome, msg.getSender().getLocalName(), duelTeam);
					break;
				case "negotiation":
					String proposedItem = msg.getContent();
					ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.REJECT_PROPOSAL, null);
					if(!hasInfo(proposedItem) && personality.acceptNegotiation(playerMap, proposedItem)) {
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						String item = personality.decideWhatToCounterNegotiate(playerMap, myStruct, proposedItem);
						Integer itemTeam = playerMap.get(item).getTeam();
						reply.setContent(item + ":" + itemTeam.toString());
						System.out.println(myAgent.getLocalName() + ": PROPOSE 2: " + item + ":" + itemTeam.toString());

						send(reply);
						awaitProposerResponse(msg.getSender().getLocalName(), proposedItem);
					}
					else {
						System.out.println(myAgent.getLocalName() + ": Reject proposal of " + proposedItem + " from " + msg.getSender().getLocalName());
						send(reply);
					}
					break;
				case "share-map":
					try {

						updateMap( msg.getContentObject());

					} catch (UnreadableException e) {					
						e.printStackTrace();
						System.err.println("Couldn't retrieve player List from message.");
					}
					break;
				}
			}
			else block();
		}

		private void awaitProposerResponse(String string, String proposedItem) {
			ACLMessage response;
			do {
				MessageTemplate mt = MessageTemplate.MatchSender(new AID(string, AID.ISLOCALNAME));
				response = receive(mt);
				if(response != null)
				{
					switch(response.getPerformative())
					{
					case ACLMessage.ACCEPT_PROPOSAL:
						Integer proposedItemTeam = Integer.parseInt(response.getContent());
						playerMap.get(proposedItem).setTeam(proposedItemTeam);
						break;
					case ACLMessage.REJECT_PROPOSAL:
						break;
					}
				}
				else block();
			}
			while(response != null);
		}

		private void updateMap(Serializable serializable) {
			@SuppressWarnings("unchecked")
			HashMap<String, Integer> newMap = (HashMap<String, Integer>) serializable;
			for(Entry<String, Integer> entry: newMap.entrySet())
			{
				if(entry.getValue() != UNKNOWN)
				{
					playerMap.get(entry.getKey()).setTeam(entry.getValue());
				}
			}
		}

		private void replyOutcome(ACLMessage msg, Outcome outcome, Integer teamNumber) {
			ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.ACCEPT_PROPOSAL, outcome.toString() + ":" + teamNumber);
			send(reply);
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	private void handleOutcome(Outcome result, String opponent, int oppTeam)
	{
		switch(result)
		{
		case VICTORY:
		{
			handleVictory();
			break;
		}
		case LOSS:
		{
			handleLoss();
			break;
		}
		case SAME_TEAM:
		{
			handleSameTeam(opponent);
			break;
		}
		case NEUTRAL:
		{
			handleNeutral(opponent, oppTeam);
			break;
		}
		}
	}

	private void handleVictory()
	{
		//Nothing to do
	}

	private void handleLoss()
	{
		ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.INFORM, "overseer", "inform-death", this.getLocalName());
		send(msg);
	}

	private void handleSameTeam(String opponent)
	{
		playerMap.get(opponent).setTeam(teamNumber);
	}

	private void handleNeutral(String opponent, int oppTeam)
	{
		playerMap.get(opponent).setTeam(oppTeam);
	}

	public boolean hasInfo(String a){
		boolean[] retVal = {false};
		playerMap.forEach((key, value)->{
			if(a == key) {
				if(value.getTeam() == UNKNOWN)
					retVal[0] = true;
			}
		});
		return retVal[0];
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
		System.out.println("Player-agent " + getAID().getLocalName() + " terminating.");
	}
}
