package main;
import jade.core.AID;

public class PlayerStruct {
	public enum State {
		ALIVE,
		DEAD
	}
	
	private AID myAID;
	private Integer team;
	private State state; // 0 alive 1 is dead
	private Integer group;
	
	PlayerStruct(AID aid, Integer team){
		this.myAID = aid;
		this.team = team;
		this.state = State.ALIVE;
	}
	
	public AID getAID() { return this.myAID; }
	
	public void setAID(AID aid) { this.myAID = aid;}
	
	public Integer getTeam() { return this.team;}
	
	public Integer getGroup() { return this.group; }
	
	public State getState() { return this.state;	}
	
	public void turnDead() { this.state = State.DEAD;}
	
	public boolean isAlive() { return state == State.ALIVE;	}
	
	public void setGroup(Integer group) { this.group = group; }
}


