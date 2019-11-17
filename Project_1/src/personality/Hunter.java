package personality;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import jade.core.AID;
import main.PlayerStruct;
import main.Utilities;

public class Hunter implements Personality {
	private static final int UNKNOWN = -1;

	@Override
	public Action decideAction(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		int value = ThreadLocalRandom.current().nextInt(0,100);
		boolean retval = value >= 10 ? true: false;

		int value2 = ThreadLocalRandom.current().nextInt(0,100);
		boolean retval2 = value2 >= 50 ? true: false;

		if(retval)
			return Action.Duel;
		else if (retval2)
			return Action.Negotiate;
		return Action.Abstain;
	}

	@Override
	public AID decideWhoToBattle(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		ArrayList<ArrayList<AID>> myTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> neutralTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> iKillTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> killsMeTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> idkTeam = new ArrayList<ArrayList<AID>>();
		idkTeam.add(new ArrayList<AID>());
		neutralTeam.add(new ArrayList<AID>());
		iKillTeam.add(new ArrayList<AID>());
		killsMeTeam.add(new ArrayList<AID>());
		myTeam.add(new ArrayList<AID>());

		playerMap.forEach((key, value)->{
			if(!value.isAlive())
			{
			}
			else if(value.getTeam() == UNKNOWN ) {
				ArrayList<AID> a = idkTeam.get(0);
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
					if(!key.getLocalName().equals(ownStruct.getAID().getLocalName()))
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
	public boolean acceptNegotiation(HashMap<AID, PlayerStruct> playerMap, AID proposedPlayer) {
		return playerMap.get(proposedPlayer).getTeam() == UNKNOWN;
	}

	@Override
	public AID decideWhoToNegotiate(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {
		ArrayList<ArrayList<AID>> neutralTeam = new ArrayList<ArrayList<AID>>(); 
		ArrayList<ArrayList<AID>> iKillTeam = new ArrayList<ArrayList<AID>>();  
		ArrayList<ArrayList<AID>> idkTeam = new ArrayList<ArrayList<AID>>();
		idkTeam.add(new ArrayList<AID>());
		neutralTeam.add(new ArrayList<AID>());
		iKillTeam.add(new ArrayList<AID>());

		playerMap.forEach((key, value)->{
			if(!value.isAlive())
			{
			}
			else if(value.getTeam() == UNKNOWN ) {
				ArrayList<AID> a = idkTeam.get(0);
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
				}
			}
		});

		if(!idkTeam.get(0).isEmpty()) {
			return idkTeam.get(0).get(0);
		}
		else if(!iKillTeam.get(0).isEmpty()) {
			return iKillTeam.get(0).get(0);
		}
		else if(!neutralTeam.get(0).isEmpty()) {
			return neutralTeam.get(0).get(0);
		}

		return null;
	}


	@Override
	public AID decideWhatToNegotiate(HashMap<AID, PlayerStruct> playerMap, PlayerStruct ownStruct) {

		AID[] retVal = {null};
		if(ownStruct.getTeam() != 1)
			playerMap.forEach((key, value)->{
				if(value.getTeam() == ownStruct.getTeam() - 1) {
					retVal[0] = key;
				}
			});

		else
			playerMap.forEach((key, value)->{
				if(value.getTeam() == 5) {
					retVal[0] = key;
				}
			});

		if(retVal[0] == null){
			playerMap.forEach((key, value)->{
				if(value.getTeam() != ownStruct.getTeam()) {
					retVal[0] = key;
				}
			});
		}
		return retVal[0];
	}
}
