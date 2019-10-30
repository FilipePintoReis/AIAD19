
public class Utilities {
	public static boolean isNeutral(Integer teamNumber, Integer comparisonN) {
		switch(teamNumber) {
			case 1:
				return (comparisonN == 3 || comparisonN == 4);
			case 2:
				return (comparisonN == 4 || comparisonN == 5);
			case 3:
				return (comparisonN == 5 || comparisonN == 1);
			case 4:
				return (comparisonN == 1 || comparisonN == 2);
			case 5:
				return (comparisonN == 2 || comparisonN == 3);
			default:
				return false;
		}
	}
}
