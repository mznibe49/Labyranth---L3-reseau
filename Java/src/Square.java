public class Square{
	char type;
	Player player;
	Ghost ghost;

	public Square(){
		this.type = 'W';
		this.ghost = null;
		this.player = null;
	}

	public Square(char type){
		this.type = type;
		this.ghost = null;
		this.player = null;
	}

	char getType(){
		return this.type;
	}

	void setType(char type){
		this.type = type;
	}

	void update(Player player, Ghost ghost){
		this.player = player;
		this.ghost = ghost;
	}

	void updateGhost(Ghost ghost){
		this.ghost = ghost;
	}

	Player getPlayer(){
		return this.player;
	}

	Ghost getGhost(){
		return this.ghost;
	}
}