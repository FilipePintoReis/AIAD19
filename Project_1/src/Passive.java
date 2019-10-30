import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import jade.core.AID;

public class Passive implements Personality {

	@Override
	public boolean decideToBattle(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group) {
		int value = ThreadLocalRandom.current().nextInt(0,101);
		boolean retval = value <= 10 ? true: false;
		return retval;
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
