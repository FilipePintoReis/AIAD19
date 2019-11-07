package personality;

import java.util.ArrayList;
import java.util.HashMap;
import jade.core.AID;
import main.PlayerStruct;
import main.Utilities;

public class Negotiator implements Personality {
	private static final int UNKNOWN = -1;
	
	@Override
	public boolean decideToBattle(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		Integer[] existentPlayers = {0};
		Integer[] knownPlayers = {0};
		Integer[] neutralPlayers = {0};
		playerMap.forEach((key, value)->{
			if(value.getState() == PlayerStruct.State.ALIVE) {
				existentPlayers[0]++;
			}
			if(value.getTeam() != UNKNOWN ) {
				knownPlayers[0]++;
				if(value.getTeam() == ownStruct.getTeam() || Utilities.isNeutral(ownStruct.getTeam(), value.getTeam())) {
					neutralPlayers[0]++;
				}
			}
		});
		return false;
	}

	@Override
	public AID decideWhoToBattle(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		ArrayList<ArrayList<AID>> myTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> neutralTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> iKillTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> killsMeTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> idkTeam = new ArrayList<ArrayList<AID>>();
		playerMap.forEach((key, value)->{
			if(value.getTeam() == UNKNOWN ) {
					idkTeam.get(0).add(key);
			}
			if(Utilities.isNeutral(ownStruct.getTeam(), value.getTeam())) {
				neutralTeam.get(0).add(key);
			}
			if(Utilities.diesHorribly(ownStruct.getTeam(), value.getTeam())) {
				iKillTeam.get(0).add(key);
			}
			if(Utilities.killsTheEnemy(ownStruct.getTeam(), value.getTeam())) {
				killsMeTeam.get(0).add(key);
			}
			if(Utilities.isOnMyTeam(ownStruct.getTeam(), value.getTeam())) {
				myTeam.get(0).add(key);
			}
		});
		
		if(!myTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!neutralTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!iKillTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!idkTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		else if(!killsMeTeam.get(0).isEmpty()) {
			return myTeam.get(0).get(0);
		}
		
		return null;
	}

	@Override
	public boolean decideInitiateNegotiation(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		return true;
	}
	
	@Override
	public boolean acceptNegotiation(HashMap<AID, PlayerStruct> playerMap, AID proposedPlayer) {
		boolean[] retVal = {false};
		
		playerMap.forEach((key, value)->{
			if(key == proposedPlayer && value.getTeam() == UNKNOWN) {
				retVal[0] = true;
			}
		});
		
		return retVal[0];
	}
	
	@Override
	public AID decideWhatToNegotiate(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		
		AID[] retVal = {null};
		if(ownStruct.getTeam() != 1)
			playerMap.forEach((key, value)->{
				if(value.getTeam() == ownStruct.getTeam()  - 1) {
					retVal[0] = key;
				}
			});
		
		else
			playerMap.forEach((key, value)->{
				if(value.getTeam() == 5) {
					retVal[0] = key;
				}
			});
		return retVal[0];
	}
}
