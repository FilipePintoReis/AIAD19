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
	private Personality personality;
	private PlayerStruct myStruct;

	public int teamNumber;
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
	//TODO for testing
	private class DuelPlayer extends SimpleBehaviour 
	{
		private int done = 0;
		@Override
		public void action() {
			if(myAgent.getLocalName().equals("player1"))
			{
				ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
				switch(done)
				{
				case 0:
					System.out.println(Outcome.VICTORY.toString());
					System.out.println(myAgent.getLocalName() + " got in duel");
					System.out.println("Sent message");
					msg = new ACLMessage(ACLMessage.PROPOSE);
					msg.setContent("2");
					msg.setConversationId("duel");
					msg.addReceiver(new AID("player2", AID.ISLOCALNAME));
					send(msg);
					System.out.println("Sent message2");
					done = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
					msg = receive(mt);
					if(msg != null)
					{
						Outcome outcome = Utilities.adjustOutcome(Outcome.valueOf(msg.getContent()));
						handleOutcome(outcome);
						done = 3;
					}
				}
			}
			else 
			{
				done = 3;
			}
		}



		@Override
		public boolean done() {
			return done == 3;
		}

	}
	//TODO for testing
	private class ListenForDuels extends SimpleBehaviour
	{
		private boolean done = false;
		@Override
		public void action() {
			if(myAgent.getLocalName().equals("player2")) {
				System.out.println(myAgent.getLocalName() + " got in lsten");
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				ACLMessage msg = receive(mt);
				if(msg != null)
				{	
					System.out.println("OWO");
					Integer duelTeam = Integer.parseInt(msg.getContent());
					teamNumber = 1; //TODO remove
					Outcome outcome = Utilities.getOutcome(teamNumber, duelTeam);
					replyOutcome(msg, outcome);
					handleOutcome(outcome);
				}
				else block();
			}
			else done = true;
		}

		@Override
		public boolean done() {
			return done;
		}

		private void replyOutcome(ACLMessage msg, Outcome outcome) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			reply.setContent(outcome.toString());
			send(reply);
		}
	}

	private class DeathNote extends SimpleBehaviour{

		@Override
		public void action() {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent(getAID().toString());
			msg.setConversationId("dead");
			msg.addReceiver(new AID("Overseer", AID.ISLOCALNAME));
		}

		@Override
		public boolean done() {
			return false;
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
				}
			}
			else block();
		}

		private void turnPlayerArrayIntoMap(Serializable playerArray, HashMap<AID, PlayerStruct> playerMap) {
			AID[] array = (AID[]) playerArray;
			for(int i = 0; i < array.length; i++)
			{
				playerMap.put(array[i], new PlayerStruct(-1));
			}
			myStruct = new PlayerStruct(teamNumber);
			playerMap.put(myAgent.getAID(), myStruct);
		}

		@Override
		public boolean done() {
			if(hasTeam && hasPlayerList)
			{
				myAgent.addBehaviour(new RoundListener());
				myAgent.addBehaviour(new DeathListener());
				return true;
			}
			else return false;
		}
	}

	private class RoundListener extends SimpleBehaviour {
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
			if(myStruct.isAlive()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("JOB " + myAgent.getLocalName());
			}
			else {
				System.out.println("I am dead, let me sleep.");
			}
		}

		private void sendEndRound(ACLMessage msg)
		{
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			reply.setContent("DONE");
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
		//TODO	What to do on Loss (death)
		//inform overseer I have died
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(overseer);
		msg.setConversationId("inform-death");
		msg.setContent(this.getLocalName());
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
