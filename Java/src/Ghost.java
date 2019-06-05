public class Ghost{
	int id = 0, x, y;
	boolean hidden;

	public Ghost(int id){
		this.id = id;
		this.hidden = false;
	}

	public int getId(){
		return this.id;
	}

	public void setHidden(boolean hidden){
		this.hidden = hidden;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
	}
}