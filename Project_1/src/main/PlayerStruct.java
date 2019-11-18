package main;

public class PlayerStruct {
	public enum State {
		ALIVE,
		DEAD
	}
	
	private String name;
	private Integer team = null;
	private State state; // 0 alive 1 is dead
	private Integer group;
	
	PlayerStruct(String name, Integer team){
		this.name = name;
		this.team = team;
		this.state = State.ALIVE;
	}
	
	public String getName() { return this.name; }
	
	public Integer getTeam() { return this.team;}
	
	public Integer getGroup() { return this.group; }
	
	public State getState() { return this.state;	}
	
	public void turnDead() { this.state = State.DEAD;}
	
	public boolean isAlive() { return state == State.ALIVE;	}
	
	public void setGroup(Integer group) { this.group = group; }

	public void setTeam(int oppTeam) { this.team = oppTeam;	}
}


