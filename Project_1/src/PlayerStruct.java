import jade.core.AID;

public class PlayerStruct {
	public AID myAID;
	public Integer team;
	public Integer state; // 0 alive 1 is dead
	public Integer group;
	private static final int ALIVE = 0;
	private static final int DEAD = 1;
	
	PlayerStruct(Integer team, Integer state){
		this.team = team;
		this.state = state;
	}
	
	public void setGroup(Integer group) {
		this.group = group;
	}
}
