import java.util.Scanner;
import java.util.ArrayList;
import java.util.Random;

public class Test{

	public static void main(String[] args){
		start();
	}

 	static void start(){
 		Maze maze1 = new Maze("../mazes/maze_01", 15, 36);
 		Maze maze2 = new Maze("../mazes/maze_02", 10, 5);
 		Maze maze3 = new Maze("../mazes/maze_03", 5, 37);
		while(true){
			Random rand = new Random();
			int randomWidth = rand.nextInt(30 - 20 + 1) + 20;
			int randomHeight = rand.nextInt(20 - 10 + 1) + 10;
			System.out.println("W" + randomWidth);
			System.out.println("H" + randomHeight);

			Maze random_maze = new Maze(randomHeight, randomWidth);

		 	Player arthur = new Player("Arthur", 8998);
		 	Player nouille = new Player("Nouille", 6868);
		 	Player arthur4 = new Player("Arthur4", 2345);
		 	Player nouille4 = new Player("Nouille4", 9789);
		 	Player arthur5 = new Player("Arthur5", 8645);
		 	Player nouille2 = new Player("Nouille2", 9789);

	 		Player[] ps = {arthur, nouille, arthur4, nouille4, nouille2, arthur5};

	 		Ghost lucien_la_chance = new Ghost(3);
	 		Ghost casper = new Ghost(2);
	 		Ghost hahaha = new Ghost(6);

	 		Game g = new Game(random_maze, 1, "1", 1);
			g.addPlayerRandomly(arthur);
			g.addPlayerRandomly(nouille);
			g.addGhostRandomly(6);
	 		// g.addP(nouille, 4, 0);
	 		// g.addP(nouille2, 4, 1);
	 		// g.addP(new Player("prout"), 4, 2);
	 		// g.addP(nouille, 1, 1);
	 		// g.addP(nouille2, 2, 1);
	 		// g.addP(new Player("prout"), 3, 1);
	 		// g.addP(new Player("ppp"), 0,1);
			//
	 		// char[] random = {'u', 'd', 'r', 'l'};
			// int nb_random = (1 + (int)(Math.random() * ((random.length - 1) + 1))) - 1;
			//
	 		// int n = 2;

	 		// while(true){
	 		// 	for(Player p : g.getPlayers()){
	 		// 		System.out.println("[Player] " + p.getName() + " [Score] " + p.getScore());
			// 	}
			//
		 	g.getMaze().displayMazeUltra4K();
		}
 		// 	Scanner sc = new Scanner(System.in);
 		// 	System.out.println("Coordonnées ? d puis nb");
 		// 	char c = sc.next().charAt(0);
 		// 	int a = sc.nextInt();
 		// 	g.movePlayer(arthur, c, a);
 		// 	for(Ghost gh : gg){
 		// 		g.moveGhostRandomly(gh, 5);
 		// 	}
		//  	//g.moveGhostRandomly(lucien_la_chance, 2);
		//  	//nb_random = (1 + (int)(Math.random() * ((random.length - 1) + 1))) - 1;
	 	// 	// n-=1;
	 	// 	// for(Player p : ps){
	 	// 	// 	System.out.println("SCORE de " + p.getName() + " s: " + p.getScore());
	 	// 	// }
	 	// }
	}
}
