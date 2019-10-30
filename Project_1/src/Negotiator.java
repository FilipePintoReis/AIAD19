import java.util.HashMap;

import jade.core.AID;

public class Negotiator implements Personality {
	private static final int UNKNOWN = -1;
	private static final int ALIVE = 0;
	private static final int DEAD = 1;
	
	@Override
	public boolean decideToBattle(HashMap<AID, PlayerStruct> playerMap, Integer team, Integer group) {
		Integer[] existentPlayers = {0};
		Integer[] knownPlayers = {0};
		Integer[] neutralPlayers = {0};
		playerMap.forEach((key, value)->{
			if(value.state == ALIVE) {
				existentPlayers[0]++;
			}
			if(value.team != UNKNOWN ) {
				knownPlayers[0]++;
				if(value.team == team || Utilities.isNeutral(team, value.team)) {
					neutralPlayers[0]++;
				}
			}
		});
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
