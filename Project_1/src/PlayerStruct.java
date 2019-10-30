
public class PlayerStruct {
	public Integer team;
	public Integer state; // 0 alive 1 is dead
	private static final int ALIVE = 0;
	private static final int DEAD = 1;
	
	PlayerStruct(Integer team, Integer state){
		this.team = team;
		this.state = state;
	}
}
