import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import jade.core.AID;

public class Hunter implements Personality {
	private static final int UNKNOWN = -1;
	private static final int ALIVE = 0;
	private static final int DEAD = 1;
	
	@Override
	public boolean decideToBattle(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		int value = ThreadLocalRandom.current().nextInt(0,101);
		boolean retval = value >= 10 ? true: false;
		return retval;
	}

	@Override
	public AID decideWhoToBattle(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		ArrayList<ArrayList<AID>> myTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> neutralTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> iKillTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> killsMeTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> idkTeam = new ArrayList<ArrayList<AID>>();
		playerMap.forEach((key, value)->{
			if(value.team == UNKNOWN ) {
					idkTeam.get(0).add(key);
			}
			if(Utilities.isNeutral(ownStruct.team, value.team)) {
				neutralTeam.get(0).add(key);
			}
			if(Utilities.diesHorribly(ownStruct.team, value.team)) {
				iKillTeam.get(0).add(key);
			}
			if(Utilities.killsTheEnemy(ownStruct.team, value.team)) {
				killsMeTeam.get(0).add(key);
			}
			if(Utilities.isFromMyTeam(ownStruct.team, value.team)) {
				myTeam.get(0).add(key);
			}
		});
		
		if(!iKillTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!idkTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!myTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!neutralTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!killsMeTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		
		return null;
	}

	@Override
	public boolean decideInitiateNegotiation(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		int value = ThreadLocalRandom.current().nextInt(0,101);
		boolean retval = value >= 50 ? true: false;
		return retval;
	}
	
	@Override
	public boolean acceptNegotiation(HashMap<AID, PlayerStruct> playerMap, AID proposedPlayer) {
		boolean[] retVal = {false};
		
		playerMap.forEach((key, value)->{
			if(key == proposedPlayer && value.team == UNKNOWN) {
				retVal[0] = true;
			}
		});
		
		return retVal[0];
	}

	@Override
	public AID decideWhatToNegotiate(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		
		AID[] retVal = {null};
		if(ownStruct.team != 1)
			playerMap.forEach((key, value)->{
				if(value.team == ownStruct.team-1) {
					retVal[0] = key;
				}
			});
		
		else
			playerMap.forEach((key, value)->{
				if(value.team == 5) {
					retVal[0] = key;
				}
			});
		return retVal[0];
	}
}
