import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookSeller extends Agent {

	private Hashtable catalogue;
	private BookSellerGui gui;

	protected void setup() {
		catalogue = new Hashtable();

		gui = new BookSellerGui(this);
		gui.show();

		addBehaviour(new OfferRequestServer());
		addBehaviour(new PurchaseOrdersServer());
	}


	public class OfferRequestServer extends CyclicBehaviour {

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP); 
			ACLMessage msg = myAgent.receive();
			if(msg!=null)
			{
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.get(title);
				if(price != null)
				{
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				}
				else
				{
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else block();
		}
	}


	protected void takeDown() {

		gui.dispose();
		System.out.println("Seller-agent" + getAID().getName() + "terminating.");
	}

	public void updateCatalogue (final String title, final int price) {

		addBehaviour(new OneShotBehaviour(){
			public void action() {
				catalogue.put(title, new Integer(price));
			}
		});
	}

}
