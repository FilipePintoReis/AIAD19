package main;

import java.io.IOException;
import java.io.Serializable;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

class MessageHandler {
	protected static ACLMessage prepareMessage(int performative, String receiver, String conversationId, String content)
	{
		ACLMessage msg = new ACLMessage(performative);
		addReceiver(msg, receiver);
		msg.setConversationId(conversationId);
		msg.setContent(content);
		return msg;
	}

	protected static ACLMessage prepareReply(ACLMessage msg, int performative,String content)
	{
		ACLMessage reply = msg.createReply();
		reply.setPerformative(performative);
		reply.setContent(content);
		return reply;
	}

	protected static ACLMessage prepareMessageObject(int performative, String receiver, String conversationId, Serializable obj)
	{
		ACLMessage msg = new ACLMessage(performative);
		addReceiver(msg, receiver);
		msg.setConversationId(conversationId);
		try {
			msg.setContentObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Caught error in function prepareMessageObject.");
		}
		return msg;
	}

	protected static void addReceiver(ACLMessage msg, String receiverName)
	{
		if(msg != null && receiverName != null)
			msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
	}
}
