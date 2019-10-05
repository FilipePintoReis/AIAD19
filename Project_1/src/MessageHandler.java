import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class MessageHandler {
	private static AID overseer = new AID("overseer", AID.ISLOCALNAME);

	public static ACLMessage getBirthMessage()
	{
		ACLMessage msgBirth = new ACLMessage(ACLMessage.INFORM);
		msgBirth.setContent("BIRTH");
		msgBirth.addReceiver(overseer);
		return msgBirth;
	}
	public static ACLMessage getDeathMessage()
	{
		ACLMessage msgDeath = new ACLMessage(ACLMessage.INFORM);
		msgDeath.setContent("DEATH");
		msgDeath.addReceiver(overseer);
		return msgDeath;
	}
}
