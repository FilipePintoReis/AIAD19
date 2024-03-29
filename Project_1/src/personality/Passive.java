package personality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import main.PlayerStruct;
import main.Utilities;
import main.Utilities.Outcome;

public class Passive implements Personality {
	private static final int UNKNOWN = -1;

	@Override
	public Action decideAction(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		int value = ThreadLocalRandom.current().nextInt(0,101);
		boolean retval = value <= 10 ? true: false;
		boolean retval2 = value <= 30 ? true: false;
		if(retval)
			return Action.Duel;
		else if(retval2)
			return Action.Negotiate;
		return Action.Abstain;
	}

	@Override
	public String decideWhoToBattle(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		ArrayList<ArrayList<String>> myTeam = new ArrayList<ArrayList<String>>(); 
		ArrayList<ArrayList<String>> neutralTeam = new ArrayList<ArrayList<String>>(); 
		ArrayList<ArrayList<String>> iKillTeam = new ArrayList<ArrayList<String>>(); 
		ArrayList<ArrayList<String>> killsMeTeam = new ArrayList<ArrayList<String>>(); 
		ArrayList<ArrayList<String>> idkTeam = new ArrayList<ArrayList<String>>();
		idkTeam.add(new ArrayList<String>());
		neutralTeam.add(new ArrayList<String>());
		iKillTeam.add(new ArrayList<String>());
		killsMeTeam.add(new ArrayList<String>());
		myTeam.add(new ArrayList<String>());
		playerMap.forEach((key, value)->{
			if(!value.isAlive())
			{
			}
			else if(value.getTeam() == UNKNOWN ) {
				ArrayList<String> a = idkTeam.get(0);
				a.add(key);
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
					if(!key.equals(ownStruct.getName()))
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

		if(!iKillTeam.get(0).isEmpty()) {
			return iKillTeam.get(0).get(0);
		}
		else if(!idkTeam.get(0).isEmpty()) {
			return idkTeam.get(0).get(0);
		}
		else if(!killsMeTeam.get(0).isEmpty()) {
			return killsMeTeam.get(0).get(0);
		}

		return null;
	}

	@Override
	public boolean acceptNegotiation(HashMap<String, PlayerStruct> playerMap, String proposedPlayer) {
		int a = ThreadLocalRandom.current().nextInt(0, 100);
		return a > 30;
	}

	@Override
	public String decideWhoToNegotiate(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		ArrayList<ArrayList<String>> neutralTeam = new ArrayList<ArrayList<String>>(); 
		ArrayList<ArrayList<String>> iKillTeam = new ArrayList<ArrayList<String>>();  
		ArrayList<ArrayList<String>> idkTeam = new ArrayList<ArrayList<String>>();
		idkTeam.add(new ArrayList<String>());
		neutralTeam.add(new ArrayList<String>());
		iKillTeam.add(new ArrayList<String>());

		playerMap.forEach((key, value)->{
			if(!value.isAlive())
			{
			}
			else if(value.getTeam() == UNKNOWN ) {
				ArrayList<String> a = idkTeam.get(0);
				a.add(key);
			}
			else {
				switch(Utilities.getOutcome(ownStruct.getTeam(), value.getTeam()))
				{
				case VICTORY:
				{
					iKillTeam.get(0).add(key);
					break;
				}
				case NEUTRAL:
				{
					neutralTeam.get(0).add(key);
					break;
				}
				default:
				}
			}
		});

		if(!iKillTeam.get(0).isEmpty()) {
			return iKillTeam.get(0).get(0);
		}
		else if(!neutralTeam.get(0).isEmpty()) {
			return neutralTeam.get(0).get(0);
		}
		else if(!idkTeam.get(0).isEmpty()) {
			return idkTeam.get(0).get(0);
		}

		return null;
	}

	@Override
	public String decideWhatToNegotiate(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		String[] retVal = {null};
		playerMap.forEach((key, value)->{
			if(value.getTeam() != -1 && Utilities.getOutcome(ownStruct.getTeam(), value.getTeam()) == Outcome.LOSS) {
				retVal[0] = key;
			}
		});

		if(retVal[0] == null){
			playerMap.forEach((key, value)->{
				if(value.getTeam() != -1 && value.getTeam() != ownStruct.getTeam()) {
					retVal[0] = key;
				}
			});
		}
		if(retVal[0] == null){
			playerMap.forEach((key, value)->{
				if(value.getTeam() != -1) {
					retVal[0] = key;
				}
			});
		}
		return retVal[0];
	}

	public String decideWhatToCounterNegotiate(HashMap<String, PlayerStruct> playerMap, PlayerStruct ownStruct, String proposal) {
		String[] retVal = {null};
		playerMap.forEach((key, value)->{
			if(value.getTeam() != -1 && Utilities.getOutcome(ownStruct.getTeam(), value.getTeam()) == Outcome.LOSS && proposal != key) {
				retVal[0] = key;
			}
		});

		if(retVal[0] == null){
			playerMap.forEach((key, value)->{
				if(value.getTeam() != -1 && value.getTeam() != ownStruct.getTeam() && proposal != key) {
					retVal[0] = key;
				}
			});
		}
		if(retVal[0] == null){
			playerMap.forEach((key, value)->{
				if(value.getTeam() != -1 && proposal != key) {
					retVal[0] = key;
				}
			});
		}
		return retVal[0];
	}
}
