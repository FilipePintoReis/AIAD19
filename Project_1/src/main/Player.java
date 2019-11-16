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
	private int ROUND_SLEEP = 50;

	private static final int UNKNOWN = -1;
	private AID overseer;
	private Personality personality = null;
	private PlayerStruct myStruct;

	public Integer teamNumber;

	private HashMap<AID, PlayerStruct> playerMap = new HashMap<AID, PlayerStruct>();

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
				overseer = msg.getSender();
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

		private void turnPlayerArrayIntoMap(Serializable playerArray, HashMap<AID, PlayerStruct> playerMap) {
			AID[] array = (AID[]) playerArray;
			for(int i = 0; i < array.length; i++)
			{
				playerMap.put(array[i], new PlayerStruct(array[i], UNKNOWN));
			}
			myStruct = new PlayerStruct(this.myAgent.getAID(), teamNumber);
			playerMap.put(myAgent.getAID(), myStruct);
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

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = receive(mt);
			if( msg != null && msg.getConversationId().equals("round-start")) {
				int roundNumber = Integer.parseInt(msg.getContent());
				roundAction();
				sendEndRound(msg);
			}
			else block();
		}

		private void roundAction() {
			System.out.println("TURN " + myAgent.getLocalName() + " of " + teamNumber);
			if(myStruct.isAlive()) {
				try {
					Thread.sleep(ROUND_SLEEP);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				switch(personality.decideAction(playerMap, myStruct))
				{
				case Duel:
					actionPhase = 0;
					AID opponent = personality.decideWhoToBattle(playerMap, myStruct);
					if(opponent != null) {
						duelPlayer(opponent);
					}
					else System.out.println("SKIP");
					break;
				case Negotiate:
					actionPhase = 0;
					AID item = personality.decideWhatToNegotiate(playerMap, myStruct);
					AID client = personality.decideWhoToNegotiate(playerMap, myStruct);
					negotiate(client, item);
					break;
				case Abstain:
				default:
				}
				shareMapWithTeam();
			}
			else {
				//				System.out.println("SLEEP " + myAgent.getLocalName());
			}
		}

		private void negotiate(AID client, AID item) {
			while(actionPhase != 2)
			{
				switch(actionPhase)
				{
				case 0:
					ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.PROPOSE, client, "negotiation", item.getLocalName());
					System.out.println("PROPOSE 1: " + item.getLocalName());
					send(msg);
					actionPhase = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchSender(client);
					ACLMessage response = receive(mt);
					if(response != null)
					{
						switch(response.getPerformative())
						{
						case ACLMessage.ACCEPT_PROPOSAL:
						{
							String[] args = response.getContent().split("//s+");
							AID clientProposal = new AID(args[0], AID.ISLOCALNAME);
							if(personality.acceptNegotiation(playerMap, clientProposal))
							{
								Integer clientProposalTeam = Integer.parseInt(args[1]);
								playerMap.get(clientProposal).setTeam(clientProposalTeam);

								Integer itemTeam = playerMap.get(item).getTeam();
								ACLMessage reply = MessageHandler.prepareReply(response, ACLMessage.ACCEPT_PROPOSAL, itemTeam.toString());
								send(reply);							
							}
							else
							{
								ACLMessage reply = MessageHandler.prepareReply(response, ACLMessage.REJECT_PROPOSAL, null);
								send(reply);	
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
				}
			}
		}

		private void duelPlayer(AID opponent) {
			while(actionPhase != 2) {
				switch(actionPhase)
				{
				case 0:
					System.out.println("CHALLENGE " + opponent.getLocalName());
					ACLMessage challengeMsg = MessageHandler.prepareMessage(ACLMessage.PROPOSE, opponent, "duel", teamNumber.toString());
					send(challengeMsg);
					actionPhase = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
					ACLMessage outcomeMessage = receive(mt);
					if(outcomeMessage != null)
					{
						String[] msgArgs =  outcomeMessage.getContent().split("\\s+");
						Outcome outcome = Utilities.adjustOutcome(Outcome.valueOf(msgArgs[0]));
						System.out.println("OUTCOME " + outcome);
						handleOutcome(outcome, opponent ,Integer.parseInt(msgArgs[1]));
						actionPhase = 2;
					}
					else block();
				}
			}
		}

		private void shareMapWithTeam() {
			AID[] teammates = getTeamArray();
			HashMap<AID, Integer> shareMap = createShareMap();
			ACLMessage msg = MessageHandler.prepareMessageObject(ACLMessage.PROPOSE, null, "share-map", shareMap);
			for(int i = 0; i < teammates.length; i++)
			{
				msg.addReceiver(teammates[i]);
			}
			send(msg);
		}

		private HashMap<AID, Integer> createShareMap() {
			HashMap<AID, Integer> shareMap = new HashMap<AID, Integer>();
			for(HashMap.Entry<AID, PlayerStruct> entry: playerMap.entrySet())
			{
				if(entry.getValue().getTeam() != UNKNOWN)
				{
					shareMap.put(entry.getKey(), entry.getValue().getTeam());
				}
			}
			return shareMap;
		}

		private AID[] getTeamArray() {
			AID[] teammates = new AID[playerMap.size()/Overseer.NUMBER_OF_TEAMS];
			int i = 0;
			for(HashMap.Entry<AID, PlayerStruct> entry: playerMap.entrySet())
			{
				if(entry.getValue().isAlive() && 
						Outcome.SAME_TEAM == Utilities.getOutcome(myStruct.getTeam(), entry.getValue().getTeam()) && 
						!entry.getKey().getLocalName().equals(myAgent.getAID().getLocalName()))
					teammates[i] = entry.getKey();

			}
			return teammates;
		}

		private void sendEndRound(ACLMessage msg)
		{
			ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.INFORM, "DONE");
			send(reply);
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
					AID deadPlayer = new AID(msg.getContent(), AID.ISLOCALNAME);
					playerMap.get(deadPlayer).turnDead();
					break;
				case "terminate":
					myAgent.doDelete();
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
					handleOutcome(outcome, msg.getSender(), duelTeam);
					break;
				case "negotiation":
					String proposed = msg.getContent();
					AID proposedItem = new AID(proposed, AID.ISLOCALNAME);
					ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.REJECT_PROPOSAL, null);
					if(!hasInfo(proposedItem) && personality.acceptNegotiation(playerMap, proposedItem)) {
						reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						AID item = personality.decideWhatToNegotiate(playerMap, myStruct);
						Integer itemTeam = playerMap.get(item).getTeam();
						reply.setContent(item.getLocalName() + " " + itemTeam.toString());
						System.out.println("PROPOSE 2: " + item.getLocalName());
						send(reply);
						awaitProposerResponse(msg.getSender(), proposedItem);
					}
					else {
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

		private void awaitProposerResponse(AID proposer, AID proposedItem) {
			ACLMessage response;
			do {
				MessageTemplate mt = MessageTemplate.MatchSender(proposer);
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
			HashMap<AID, Integer> newMap = (HashMap<AID, Integer>) serializable;
			for(Entry<AID, Integer> entry: newMap.entrySet())
			{
				if(entry.getValue() != UNKNOWN)
				{
					playerMap.get(entry.getKey()).setTeam(entry.getValue());
				}
			}
		}

		public boolean hasInfo(AID a){
			boolean[] retVal = {false};
			playerMap.forEach((key, value)->{
				if(a == key) {
					if(value.getTeam() == UNKNOWN)
						retVal[0] = true;
				}
			});
			return retVal[0];
		}


		private void replyOutcome(ACLMessage msg, Outcome outcome, Integer teamNumber) {
			ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.ACCEPT_PROPOSAL, outcome.toString() + " " + teamNumber);
			send(reply);
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	private void handleOutcome(Outcome result, AID opponent, int oppTeam)
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
		ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.INFORM, overseer, "inform-death", this.getLocalName());
		send(msg);
	}

	private void handleSameTeam(AID opponent)
	{
		playerMap.get(opponent).setTeam(teamNumber);
	}

	private void handleNeutral(AID opponent, int oppTeam)
	{
		playerMap.get(opponent).setTeam(oppTeam);
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
