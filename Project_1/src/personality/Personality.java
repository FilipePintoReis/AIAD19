package personality;

import java.util.HashMap;

import main.PlayerStruct;

public interface Personality {
	
	public enum Action {
		Duel,
		Negotiate,
		Abstain
	}
	
	public Action decideAction(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct);
	public String decideWhoToBattle(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct);
	public boolean acceptNegotiation(HashMap<String, PlayerStruct> playerMap, String proposedItem);
	public String decideWhatToNegotiate(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct);
	public String decideWhoToNegotiate(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct);
}
