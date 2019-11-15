package main;

import java.io.Serializable;
import java.util.HashMap;
import jade.core.AID;
import jade.core.Agent;
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
	private AID overseer;
	private Personality personality = null;
	private PlayerStruct myStruct;

	public Integer teamNumber;
	private int groupNumber = -1;

	private HashMap<AID, PlayerStruct> playerMap = new HashMap<AID, PlayerStruct>();

	@Override
	public void setup()
	{
		registerOnDFD();
		// TODO parseArguments();
		
		SequentialBehaviour playerBehaviour = new SequentialBehaviour(this);
		playerBehaviour.addSubBehaviour(new TeamListener());
		playerBehaviour.addSubBehaviour(new RoundListener());



		addBehaviour(playerBehaviour);
	}

	private void parseArguments()
	{
		Object[] args = getArguments();
		Integer personalityValue = Integer.parseInt(args[0].toString());
		switch(personalityValue)
		{
		case 1: personality = new Hunter(); break;
		case 2: personality = new Negotiator(); break;
		default: personality = new Passive();
		}
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
				playerMap.put(array[i], new PlayerStruct(array[i], -1));
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

	private class RoundListener extends SimpleBehaviour {
		private int duelPhase;
		private boolean roundDone;

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
			//TODO call action, probably has to do with personality7
			roundDone = false;
			if(myStruct.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(personality.decideToBattle(playerMap, myStruct))
				{
					duelPhase = 0;
					AID opponent = personality.decideWhoToBattle(playerMap, myStruct);
					duelPlayer(opponent);
				}


				System.out.println("JOB " + myAgent.getLocalName());
			}
			else {
				System.out.println("I am dead, let me sleep.");
			}
		}

		//TODO how  to listen to messages without rest of program continue
		private void duelPlayer(AID opponent) {
			boolean duelDone = false;
			while(duelPhase != 2) {
				switch(duelPhase)
				{
				case 0:
					System.out.println("Challenge " + opponent.getLocalName());
					ACLMessage challengeMsg = MessageHandler.prepareMessage(ACLMessage.PROPOSE, opponent, "duel", teamNumber.toString());
					send(challengeMsg);
					duelPhase = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
					ACLMessage outcomeMessage = receive(mt);
					if(outcomeMessage != null)
					{
						System.out.println("Response from " + outcomeMessage.getSender().getLocalName());
						Outcome outcome = Utilities.adjustOutcome(Outcome.valueOf(outcomeMessage.getContent()));
						handleOutcome(outcome);
						duelPhase = 2;
					}
					else block(); //TODO see if it doens frick up
				}
			}
		}

		private void sendEndRound(ACLMessage msg)
		{
			ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.INFORM, "DONE");
			send(reply);
		}

		@Override
		public boolean done() {
			// TODO stop listening to rounds probably on death
			return false;
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
				case "player-death":{
					AID deadPlayer = new AID(msg.getContent(), AID.ISLOCALNAME);
					playerMap.get(deadPlayer).turnDead();
					break;
				}
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
					System.out.println("Received duel from " + msg.getSender().getLocalName());
					Integer duelTeam = Integer.parseInt(msg.getContent());
					Outcome outcome = Utilities.getOutcome(teamNumber, duelTeam);
					replyOutcome(msg, outcome);
					handleOutcome(outcome);
					break;
				case "negotiation":
					System.out.println("Received negotiation from " + msg.getSender().getLocalName());
					break;
				case "group":
					System.out.println("Player " + msg.getSender().getLocalName() + " joined group.");
					break;
				}
			}
			else block();
		}

		private void replyOutcome(ACLMessage msg, Outcome outcome) {
			ACLMessage reply = MessageHandler.prepareReply(msg, ACLMessage.ACCEPT_PROPOSAL, outcome.toString());
			send(reply);
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}

	}

	private void handleOutcome(Outcome result)
	{
		//TODO Implement various handles
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
			handleSameTeam();
			break;
		}
		case NEUTRAL:
		{
			handleNeutral();
			break;
		}
		}
	}

	private void handleVictory()
	{
		//TODO	What to do on Victory
	}

	private void handleLoss()
	{
		ACLMessage msg = MessageHandler.prepareMessage(ACLMessage.INFORM, overseer, "inform-death", this.getLocalName());
		send(msg);
	}

	private void handleSameTeam()
	{
		//TODO	What to do if ally
	}

	private void handleNeutral()
	{
		//TODO	what else?
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
