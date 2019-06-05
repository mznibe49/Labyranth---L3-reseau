/* Couple de position */
public class Coordinates{
	int x, y;
	char direction;
	
	public Coordinates(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	/* Couple de position et la direction utilisée pour l'atteindre (IA du Ghost) */
	public Coordinates(int x, int y, char direction){
		this.x = x;
		this.y = y;
		this.direction = direction;
	}
	
	int getX(){
		return this.x;
	}

	int getY(){
		return this.y;
	}
	
	void setX(int x){
		this.x = x;
	}

	void setY(int y){
		this.y = y;
	}

	char getDirection(){
		return this.direction;
	}
}