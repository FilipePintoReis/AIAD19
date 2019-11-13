package personality;

import java.util.ArrayList;
import java.util.HashMap;
import jade.core.AID;
import main.PlayerStruct;
import main.Utilities;
import main.Utilities.Outcome;

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
				Outcome outcome = Utilities.getOutcome(ownStruct.getTeam(), value.getTeam());
				if(outcome == Outcome.SAME_TEAM || outcome == Outcome.NEUTRAL) {
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
			else {
				switch(Utilities.getOutcome(ownStruct.getTeam(), value.getTeam()))
				{
				case VICTORY:
				{
					iKillTeam.get(0).add(key);
					break;
				}
				case LOSS:
				{
					killsMeTeam.get(0).add(key);
					break;
				}
				case SAME_TEAM:
				{
					myTeam.get(0).add(key);
					break;
				}
				case NEUTRAL:
				{
					neutralTeam.get(0).add(key);
					break;
				}
				}
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
