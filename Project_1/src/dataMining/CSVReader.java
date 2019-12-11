package dataMining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CSVReader {
	private final static int NUMBER_OF_TEAMS = 5;
	private final static int NUMBER_OF_PERSONALITIES = 3;

	private final static String PATH_TO_PROJECT = System.getProperty("user.dir"); 
	private final static String PATH_TO_CSV = "./src/dataMining/input.csv";

	public static class Information {
		static Integer numberPlayers = 0;
		static Integer[][] personalities = new Integer[NUMBER_OF_TEAMS][NUMBER_OF_PERSONALITIES];
	}

	public static void main(String[] args)
	{
		System.out.println(PATH_TO_PROJECT + PATH_TO_CSV);

		runJade();
		//		readCsv( PATH_TO_PROJECT + PATH_TO_CSV);
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
						persNumber++;
					}
					teamNumber++;
				}

				System.out.println("N: " + Information.numberPlayers);
				System.out.println("T1: " + Information.personalities[0][0] + "," + Information.personalities[0][1] + "," + Information.personalities[0][2]);
				System.out.println("T2: " + Information.personalities[1][0] + "," + Information.personalities[1][1] + "," + Information.personalities[1][2]);
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

	/*
	 * -agents overseer:main.Overseer;player1:main.Player;player2:main.Player;player3:main.Player;player4:main.Player;player5:main.Player;player6:main.Player;player7:main.Player;player8:main.Player;player9:main.Player;player10:main.Player;
	 */

	public static void runJade()
	{
		int number = 2;
		int personality[] = {10, 40, 50};

		final String overseerName = "overseer",
				overseerClass = "Overseer",
				playerName = "player",
				playerClass = "Player";

		String command = "java -cp jade.jar jade.Boot -gui";

		String arguments = "-agents '" + overseerName + ":" + overseerClass + ";";

		for(int i = 0; i < number; i++)
		{
			arguments += playerName + i + ":" + playerClass + ";";
		}
		arguments += "'";
		System.out.print(command + arguments);

		runCMD(command + arguments);
		
//		try
//		{			
//			Runtime run = Runtime.getRuntime();
//			Process proc = run.exec(command + arguments);
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//
//		}
	}
	private static void runCMD(String cmd) {	
		try {
			runProcess(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void printLines(String cmd, InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(
				new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			System.out.println(line);
		}
	}

	private static void runProcess(String command) throws Exception {
		Process pro = Runtime.getRuntime().exec(command);
		printLines(command + " stdout:", pro.getInputStream());
		printLines(command + " stderr:", pro.getErrorStream());
		pro.waitFor();
		System.out.println(command + " exitValue() " + pro.exitValue());
	}
}
