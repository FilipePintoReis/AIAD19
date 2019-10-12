import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent 
{
	protected void setup()
	{
		// Printout a welcome message
		System.out.println("Hallo! Buyer-agent "+getAID().getName()+" is ready."); 
		Object[] args = getArguments();

		if(args != null && args.length > 0) {
			targetBookTitle = (String) args[0];
			System.out.println("Tryinng to buy" + targetBookTitle);
			addBehaviour(new TickerBehaviour(this, 60000) { 
				protected void onTick() {
					myAgent.addBehaviour(new RequestPerformer());       
				}     
			} ); 
		}

		else {
			System.out.println("No book title specified");
			doDelete();
		}

	}

	protected void takeDown() {
		System.out.println("Buyer-agent" + getAID().getName() + "teminating");
	}

	private String targetBookTitle;
	private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME), new AID("seller2", AID.ISLOCALNAME)};

	public class OverbearingBehaviour extends Behaviour
	{

		@Override
		public void action() {
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			for (int i = 0; i < sellerAgents.length; ++i) {  
				cfp.addReceiver(sellerAgents[i]);
			}
			cfp.setContent(targetBookTitle); 
			myAgent.send(cfp);

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return true;
		}

	}


	private class RequestPerformer extends Behaviour
	{
		private AID bestSeller; // The agent who provides the best offer
		private int bestPrice; // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;

		@Override
		public void action() {
			switch(step)
			{
			case 0:
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for(int i = 0; i < sellerAgents.length; ++i)
					cfp.addReceiver(sellerAgents[i]);

				cfp.setContent(targetBookTitle);
				cfp.setConversationId("book-trade");
				cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
				send(cfp);

				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;

			case 1:
				//Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null)
				{
					// Reply received
					if(reply.getPerformative() == ACLMessage.PROPOSE)
					{
						int price = Integer.parseInt(reply.getContent());
						if (bestSeller == null || price < bestPrice) 
						{	//This is the best offer at present
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}

					repliesCnt++;
					if(repliesCnt >= sellerAgents.length) {
						// We received all replies
						step = 2;
					}
				}
				else
					block();
				break;
			case 2:
			}

			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
		}
	} 