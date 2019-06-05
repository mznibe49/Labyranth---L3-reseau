import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;

public class Maze{
	public static final String RESET = "\u001B[0m";
	public static final String GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[31m";

	Square[][] square_table;

	public Maze(){
		this.square_table = new Square[10][25];
		for(int i = 0 ; i < this.square_table.length ; i++){
			for(int j = 0 ; j < this.square_table[i].length ; j++){
				this.square_table[i][j] = new Square();
			}
		}
	}

	/*
		Si j = 1 -> pas de left
		Si j = this.maze.getWidth() - 2 -> pas de right
		Si i = 1 -> pas de up
		Si i = this.maze.getHeight() - 2 -> pas de down
	*/
	public Maze(int height, int width){
		this.square_table = new Square[height][width];
		//On commence par remplacer toutes les cases par un mur soit 'W'
		for(int i = 0 ; i < height ; i++){
			for(int j = 0 ; j < width ; j++){
				if(i == 0 || i == height-1 || j == 0 || j == width-1){
					this.square_table[i][j] = new Square('L');
				} else{
					this.square_table[i][j] = new Square('W');
				}
			}
		}

		//On choisit de manière random une case de départ d'un premier chemin aléatoire
		int nb_random_height = new Random().nextInt(height-2) + 1;
		int nb_random_width = new Random().nextInt(width-2) + 1;

		Coordinates current = new Coordinates(nb_random_height, nb_random_height);

		boolean free_square_found = true;

		while(free_square_found){
			this.square_table[current.getX()][current.getY()].setType(' ');
			ArrayList<Coordinates> workables_directions = new ArrayList<Coordinates>();
			ArrayList<Coordinates> directions = this.getFreeSquares(current);
			if(directions.size() > 0){
				for(Coordinates d : directions){
					if(this.getFreeSquaresNotNext(d)){
						workables_directions.add(d);
					}
				}

				if(workables_directions.size() > 0){
					int random_direction = new Random().nextInt(workables_directions.size());
					Coordinates next = workables_directions.get(random_direction);
					current.setX(next.getX());
					current.setY(next.getY());
				} else {
					free_square_found = false;
				}
			} else {
				free_square_found = false;
			}
		}
		/* ----------- Fin du premier chemin aléatoire --------------- */

		ArrayList<Coordinates> workables_squares = new ArrayList<Coordinates>();
		for(int i = 0 ; i < this.square_table.length ; i++){
			for(int j = 0 ; j < this.square_table[0].length ; j++){
				if(this.square_table[i][j].getType() == 'W'){
					workables_squares.add(new Coordinates(i,j));
				}
			}
		}

		for(Coordinates current_ws : workables_squares){
			//System.out.println("[Sq] (" + current_ws.getX() + "," + current_ws.getY() + ")");
			free_square_found = true;

			while(free_square_found){
				ArrayList<Coordinates> workables_directions = new ArrayList<Coordinates>();
				ArrayList<Coordinates> directions = this.getFreeSquares(current_ws);
				if(directions.size() > 0){
					for(Coordinates d : directions){
						if(this.getFreeSquaresNotNext(d)){
							workables_directions.add(d);
						}
					}

					if(workables_directions.size() > 0){
						int random_direction = new Random().nextInt(workables_directions.size());
						Coordinates next = workables_directions.get(random_direction);
						current_ws.setX(next.getX());
						current_ws.setY(next.getY());

						this.square_table[current_ws.getX()][current_ws.getY()].setType(' ');
					} else {
						free_square_found = false;
					}
				} else {
					free_square_found = false;
				}
			}
		}
	}

	public Maze(String maze_file, int height, int width){
		BufferedReader br = null;
		String current_line;
		this.square_table = new Square[height][width];
		int count_line = 0;

		try{
			br = new BufferedReader(new FileReader(maze_file));
			while((current_line = br.readLine()) != null){
				for(int i = 0 ; i < width ; i++)
					this.square_table[count_line][i] = new Square(current_line.charAt(i));
				count_line+=1;
			}
		} catch (IOException e){
			System.out.println("[File Error] " + maze_file);
		}
	}

	/*
		Renvoi une liste des coordonnées des cases libres
	*/
	public ArrayList<Coordinates> getFreeSquares(Coordinates square_coordinates){
		ArrayList<Coordinates> directions = new ArrayList<Coordinates>();

		if(this.square_table[square_coordinates.getX()-1][square_coordinates.getY()].getType() == 'W'){
			directions.add(new Coordinates(square_coordinates.getX()-1, square_coordinates.getY()));
		}
		if(this.square_table[square_coordinates.getX()+1][square_coordinates.getY()].getType() == 'W'){
			directions.add(new Coordinates(square_coordinates.getX()+1, square_coordinates.getY()));
		}
		if(this.square_table[square_coordinates.getX()][square_coordinates.getY()-1].getType() == 'W'){
			directions.add(new Coordinates(square_coordinates.getX(), square_coordinates.getY()-1));
		}
		if(this.square_table[square_coordinates.getX()][square_coordinates.getY()+1].getType() == 'W'){
			directions.add(new Coordinates(square_coordinates.getX(), square_coordinates.getY()+1));
		}

		return directions;
	}

	/*
		Renvoi true si la case n'a aucuns murs juxtaposés ou en diagonale (donc un seul)
	*/
	public boolean getFreeSquaresNotNext(Coordinates square_coordinates){
		int count = 0;
		if(this.square_table[square_coordinates.getX()-1][square_coordinates.getY()].getType() == ' '){
			count+=1;
		}
		if(this.square_table[square_coordinates.getX()+1][square_coordinates.getY()].getType() == ' '){
			count+=1;
		}
		if(this.square_table[square_coordinates.getX()][square_coordinates.getY()-1].getType() == ' '){
			count+=1;
		}
		if(this.square_table[square_coordinates.getX()][square_coordinates.getY()+1].getType() == ' '){
			count+=1;
		}

		return count == 1;
	}

	public void displayMazeUltra4K(){
		System.out.print("      ");
		for(int i = 0 ; i < this.getWidth() ; i++){
			if(i < 9)
				System.out.print(i + "  ");
			else
				System.out.print(i + " ");
		}
		System.out.println();
		for(int i = 0 ; i < this.square_table.length ; i++){
			if(i < 10)
				System.out.print("["+i+"]  ");
			else
				System.out.print("["+i+"] ");
			for(int j = 0 ; j < this.square_table[i].length ; j++){
				System.out.print("");
				if(this.square_table[i][j].getPlayer() != null){
					System.out.print("\u001B[32;7m" + " P " + "\u001B[0m");
				} else if(this.square_table[i][j].getGhost() != null){
					System.out.print("\u001B[34;7m" + " G " + "\u001B[0m");
				} else if(this.square_table[i][j].getType() == 'W'){
					System.out.print("\u001B[37;7m" + "   " + "\u001B[0m");
				} else {
					System.out.print(" \u001B[37m.\u001B[0m ");
				}
				System.out.print("");
			}
			System.out.print("\n");
		}
	}

	public Square[][] getMaze(){
		return this.square_table;
	}

	public int getWidth(){
		return this.square_table[0].length;
	}

	public int getHeight(){
		return this.square_table.length;
	}

	public Square getSquare(int x, int y){
		return this.square_table[x][y];
	}
}
