package main;

import java.util.ArrayList;

public class Utilities {
	private final static int NUMBER_OF_TEAMS = 5;

	public enum Outcome {
		VICTORY,
		LOSS,
		SAME_TEAM,
		NEUTRAL
	}
	
	public static Outcome getOutcome(Integer teamA, Integer teamB)
	{
		if(killsTheEnemy(teamA, teamB))
		{
			return Outcome.VICTORY;
		}
		if(diesHorribly(teamA, teamB))
		{
			return Outcome.LOSS;
		}
		if(isOnSameTeam(teamA, teamB))
		{
			return Outcome.SAME_TEAM;
		}
		else return Outcome.NEUTRAL;
	}

	public static Outcome adjustOutcome(Outcome outcome)
	{
		switch(outcome)
		{
		case VICTORY: return Outcome.LOSS;
		case LOSS: return Outcome.VICTORY;
		default: return outcome;
		}
	}
	
	private static boolean diesHorribly(Integer teamNumber, Integer comparisonN) {
		return getPrey(comparisonN) == teamNumber;
	}

	private static boolean killsTheEnemy(Integer teamNumber, Integer comparisonN) {
		return getPrey(teamNumber) == comparisonN;
	}

	private static boolean isOnSameTeam(Integer teamNumber, Integer comparisonN) {
		return teamNumber == comparisonN;
	}

	private static int getPrey(int teamNumber) {
		if (teamNumber == NUMBER_OF_TEAMS)
			return 1;
		else
			return teamNumber + 1;
	}
	
	public static ArrayList<Integer> personalityDistribution(float passiveP, float negotiatorP, float hunterP, Integer teamSize) {
		Integer pN = (int) (teamSize*passiveP);
		Integer nN = (int) (teamSize*negotiatorP);
		Integer hN = (int) (teamSize*hunterP);
		Integer missing = teamSize - (pN + nN + hN);
		Integer fpN = pN + missing;
		ArrayList<Integer> ret = new ArrayList<Integer>();
		ret.add(fpN);
		ret.add(nN);
		ret.add(hN);
		return ret;
	}
}
