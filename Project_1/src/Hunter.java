import java.util.HashMap;

import jade.core.AID;

public class Hunter implements Personality {

	@Override
	public boolean decideToBattle(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group) {
		return false;
		// TODO Auto-generated method stub

	}

	@Override
	public AID decideWhoToBattle(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group) {
		return null;
		// TODO Auto-generated method stub

	}

	@Override
	public boolean decideToNegotiate(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group) {
		return false;
		// TODO Auto-generated method stub

	}

	@Override
	public AID decideWhatToNegotiate(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group) {
		return null;
		// TODO Auto-generated method stub

	}

}
