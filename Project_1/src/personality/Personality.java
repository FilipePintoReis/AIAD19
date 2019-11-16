package personality;

import java.util.HashMap;
import jade.core.AID;
import main.PlayerStruct;

public interface Personality {
	
	public enum Action {
		Duel,
		Negotiate,
		Abstain
	}
	
	public Action decideAction(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct);
	public AID decideWhoToBattle(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct);
	public boolean acceptNegotiation(HashMap<AID, PlayerStruct> playerMap, AID proposedPlayer);
	public AID decideWhatToNegotiate(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct);
}
