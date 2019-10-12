import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookSellerAgent extends Agent {

	private Hashtable catalogue;
	//	private BookSellerGui gui;

	protected void setup() {
		catalogue = new Hashtable();

		// Register the book-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);			
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		//		gui = new BookSellerGui(this);
		//		gui.show();

		addBehaviour(new OfferRequestServer());
		addBehaviour(new PurchaseOrdersServer());
	}


	private class OfferRequestServer extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP); 
			ACLMessage msg = myAgent.receive();
			if(msg!=null)
			{
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.get(title);
				if(price != null)
				{
					// Requested book available. Reply price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				}
				else
				{
					// Requested book not for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else block();
		}
	}

	private class PurchaseOrdersServer extends CyclicBehaviour
	{
		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null)
			{
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.remove(title);
				if (price != null)
				{
					reply.setPerformative(ACLMessage.INFORM);
					System.out.print(title + " sold to agent " + msg.getSender().getName());
				}
				else
				{
					// The requested book has been sold to another buyer in the meanwhile.
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-avaiable");
				}
				myAgent.send(reply);
			}
			else block();
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

		//		gui.dispose();
		// Printout dismissal message
		System.out.println("Seller-agent" + getAID().getName() + "terminating.");
	}

	public void updateCatalogue (final String title, final int price) {

		addBehaviour(new OneShotBehaviour(){
			public void action() {
				catalogue.put(title, new Integer(price));
				System.out.println(title+" inserted into catalogue. Price = "+price);
			}
		});
	}
}