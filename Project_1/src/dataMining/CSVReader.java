package dataMining;

public class CSVReader {
	private final int NUMBER_OF_TEAMS = 5;
	private final int NUMBER_OF_PERSONALITIES = 3;

	public class Information {
		Integer numberPlayers = 0;
		Integer[][] personalities;
	}

	public Information readCsv(String filePath) {
		BufferedReader csvReader = null;
		String line = "";
		String separator = ",";
		Information info = new Information();
		try {
			csvReader = new BufferedReader(new FileReader(filePath));

			while ((line = csvReader.readLine()) != null) {
				String[] split = line.split(separator);

				info.numberPlayers = Integer.parseInt(split[0]);
				
				int teamNumber = 0;
				while(teamNumber < NUMBER_OF_TEAMS) {
					int persNumber = 0;
					while(persNumber < NUMBER_OF_PERSONALITIES)
					{
						info.personalities[teamNumber][persNumber] =
								Integer.parseInt(split[teamNumber * NUMBER_OF_PERSONALITIES + persNumber]);
					}
				}
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
		
		return info;
	}
}