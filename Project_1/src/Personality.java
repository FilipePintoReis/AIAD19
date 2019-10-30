import java.util.HashMap;

import jade.core.AID;

public interface Personality {
	public boolean decideToBattle(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group);
	public AID decideWhoToBattle(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group);
	public boolean decideToNegotiate(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group);
	public AID decideWhatToNegotiate(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group);
}
