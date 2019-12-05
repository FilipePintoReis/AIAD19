package dataMining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVReader {
	private final static int NUMBER_OF_TEAMS = 5;
	private final static int NUMBER_OF_PERSONALITIES = 3;

	public static class Information {
		static Integer numberPlayers = 0;
		static Integer[][] personalities;
	}

	public static void main(String[] args)
	{
		readCsv("./input.csv");
	}

	public static void readCsv(String filePath) {
		BufferedReader csvReader = null;
		String line = "";
		String separator = ",";
		try {
			csvReader = new BufferedReader(new FileReader(filePath));

			while ((line = csvReader.readLine()) != null) {
				String[] split = line.split(separator);

				Information.numberPlayers = Integer.parseInt(split[0]);

				int teamNumber = 0;
				while(teamNumber < NUMBER_OF_TEAMS) {
					int persNumber = 0;
					while(persNumber < NUMBER_OF_PERSONALITIES)
					{
						Information.personalities[teamNumber][persNumber] =
								Integer.parseInt(split[teamNumber * NUMBER_OF_PERSONALITIES + persNumber]);
					}
				}

				System.out.println("N: " + Information.numberPlayers);
				System.out.println("T1: " + Information.personalities[0][0] + "," + Information.personalities[0][1] + "," + Information.personalities[0][2]);
			}
		}
		catch( IOException e) {
			e.printStackTrace();
		}
		finally {
			if(csvReader != null) {
				try {
					csvReader.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}