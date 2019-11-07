package main;
import java.io.Serializable;
import java.util.HashMap;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import main.Utilities.*;

@SuppressWarnings("serial")
public class Player extends Agent
{
	private int teamNumber;
	private int groupNumber = -1;

	private HashMap<AID, PlayerStruct> playerMap = new HashMap<AID, PlayerStruct>();

	public void setup()
	{
		registerOnDFD();
		System.out.println(getLocalName());

		addBehaviour(new DuelPlayer());
		addBehaviour(new ListenForDuels());

		//		SequentialBehaviour playerBehaviour = new SequentialBehaviour(this);
		//		playerBehaviour.addSubBehaviour(new TeamListener());
		//
		//
		//
		//
		//		addBehaviour(playerBehaviour);
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
					msg.setContent("1");
					msg.setConversationId("duel");
					msg.addReceiver(new AID("player2", AID.ISLOCALNAME));
					send(msg);
					System.out.println("Sent message2");
					done = 1;
					break;
				case 1:
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
					msg = receive(mt);
					if(msg != null)
					{
						if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
						{
							Integer duelTeam = Integer.parseInt(msg.getContent());
							Outcome outcome = Utilities.getOutcome(1, duelTeam);
							replyOutcome(msg, outcome);
							handleOutcome(outcome);
							
						}
					}
				}
			}
			else 
			{
				done = 3;
			}
		}

		public void replyOutcome(ACLMessage msg, Outcome outcome) {
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.INFORM);
			reply.setContent(outcome.toString());
		}
		
		private void handleOutcome(Outcome result)
		{
			//TODO
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
		//TODO	
		}
		
		private void handleLoss()
		{
		//TODO	
		}
		
		private void handleSameTeam()
		{
		//TODO	
		}
		
		private void handleNeutral()
		{
		//TODO	
		}

		@Override
		public boolean done() {
			return done == 3;
		}

	}

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
					System.out.println("Received duel from " + msg.getSender().getLocalName() + " of team " + msg.getContent());
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					send(reply);
				}
				else block();
			}
			else done = true;
		}

		@Override
		public boolean done() {
			return done;
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
	
	private class TeamListener extends CyclicBehaviour
	{

		@Override
		public void action() {		
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = receive(mt);
			if(msg != null)
			{
				switch(msg.getConversationId()) {
				case "team-number":
					teamNumber = Integer.parseInt(msg.getContent());
					System.out.println(myAgent.getLocalName() + " " + teamNumber);
					break;
				case "player-list":
					try {
						turnPlayerArrayIntoMap(msg.getContentObject(), playerMap);
						playerMap.put(myAgent.getAID(), new PlayerStruct(teamNumber));
					} catch (UnreadableException e) {					
						e.printStackTrace();
						System.err.println("Couldn't retrieve player List from message.");
					}
					System.out.println(playerMap.toString());
					break;

				case "duel-challenge":
				{

				}
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
		}
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
		System.out.println("Player-agent" + getAID().getLocalName() + "terminating.");
	}
}
