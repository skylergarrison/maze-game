package falstad;

import java.util.*;

public class MazeBuilderEller extends MazeBuilder {
	
	public MazeBuilderEller() {
		super();
		System.out.println("MazeBuilderPrim uses Eller's algorithm to generate maze.");
	}
	public MazeBuilderEller(boolean det) {
		super(det);
		System.out.println("MazeBuilderPrim uses Eller's algorithm to generate maze.");
	}
	
	final HashSet<Integer> extantSetNumbers = new HashSet<Integer>();
	
	/**
	 * This method generates pathways into a maze which is completely filled with walls on every edge using
	 * Eller's algorithm. It goes through each cell and randomly takes down walls to generate pathways.
	 * The algorithm only considers one row at a time. It first checks if it's in a room, next to a room, or
	 * on top of a room. Then, it takes down right walls and unions the sets of the two cells. Then, it takes down
	 * lower walls, and marks the lower cells with its set. It makes sure each set in the current row has one lower
	 * connection, to ensure representation in the next row. At the last row, it connects all cells that aren't in
	 * the same set, to make sure all sets are connected and there are no dead cells in the maze.
	 */
	@Override
	protected void generatePathways() {
		int currentSetNumber = 1;
		int workingSet = 0;
		
		int[][] trackingArray = new int[width][height];
		for	(int y = height-1; y >= 0; y = y-1){
			for (int x = 0; x < width; x++) {
				trackingArray[x][y] = 0;
			}
		}
		
		for	(int y = height-1; y > 0; y = y-1){
			//boolean to keep track if in a long sequence of right connections there has been a connection down
			//new for each row
			boolean bulldozedDown = false;
			
			//get the x for each cell in the row, go through one at a time
			for (int x = 0; x < width; x++) {

				boolean bulldozedRight = false;
				
				//ensure the current cell is not in a room. if it is, move on.
				//if the cell below is a room, you have to do a different right movement algorithm

				if(!cells.isInRoom(x, y) && !cells.isInRoom(x, y-1)){
					
					//we're okay to proceed, put the x and y in a 2 integer array to represent the unique cell
					//if it's already marked, get the number of the set, and make the active index (workingSet)
					//the number of the current cell

					if(trackingArray[x][y]==0){
						trackingArray[x][y] = currentSetNumber;
						workingSet = currentSetNumber;
						extantSetNumbers.add(currentSetNumber);
						currentSetNumber += 1;
						bulldozedDown = false;
						bulldozedRight = false;
					}
					else if(x==0){
						workingSet = trackingArray[x][y];
						bulldozedRight = false;
						bulldozedDown = false;
					}
					else if(trackingArray[x][y]!=trackingArray[x-1][y]){
						workingSet = trackingArray[x][y];
						bulldozedRight = false;
						bulldozedDown = false;
					}
					else{
						workingSet = trackingArray[x][y];
					}
					
					
					//Normal Right Movement with no constraints
					if(cells.canGo(x, y, 1, 0)){
						int randInt = random.nextIntWithinInterval(0, 4);
						
						if((randInt==0 || randInt==1 || randInt==2) && trackingArray[x+1][y]!=workingSet){
							cells.deleteWall(x, y, 1, 0);
							bulldozedRight = true;
							
							//if in set already, change the number to own set, delete new number from sets
							//if not, just change number
							if(trackingArray[x+1][y]!=0){
								extantSetNumbers.remove(trackingArray[x+1][y]);
								trackingArray[x+1][y] = workingSet;
							}else{
								trackingArray[x+1][y] = workingSet;
							}
						}
					}
					
					//Down Movement, for a normal case with no 
					if(cells.canGo(x, y, 0, -1)){
						if(bulldozedDown == false && bulldozedRight == false){
							cells.deleteWall(x, y, 0, -1);
							trackingArray[x][y-1] = workingSet;
							bulldozedDown = true;
						}else{
							int randInt = random.nextIntWithinInterval(0, 5);
							
							if(randInt==1){
								cells.deleteWall(x, y, 0, -1);
								trackingArray[x][y-1] = workingSet;
							}
						}
					}
				}
				else if(!cells.isInRoom(x, y) && cells.isInRoom(x, y-1)){
					//the provisions for if there's a room below, go right always
					if(trackingArray[x+1][y]!=trackingArray[x][y]){
						if(trackingArray[x][y]==0){
							trackingArray[x][y] = currentSetNumber;
							workingSet = currentSetNumber;
							currentSetNumber += 1;
						}
						else{
							workingSet = trackingArray[x][y];
						}
						
						cells.deleteWall(x, y, 1, 0);
						
						//if in set already, change the number to own set, delete new number from sets
						//if not, just change number
						if(trackingArray[x+1][y]!=0){
							extantSetNumbers.remove(trackingArray[x+1][y]);
							trackingArray[x+1][y] = workingSet;
						}else{
							trackingArray[x+1][y] = workingSet;
						}
					}
				}else{
					//provisions for if there is a room to the right
					if(trackingArray[x][y]==0){
						trackingArray[x][y] = currentSetNumber;
						workingSet = currentSetNumber;
						currentSetNumber += 1;
					}
					else{
						workingSet = trackingArray[x][y];
					}
					
					cells.deleteWall(x, y, 0, -1);
					trackingArray[x][y-1] = workingSet;
				}
			}
		}
		
		//special last row case, connect sets that are not until you have only one
		workingSet = trackingArray[0][0];
		for (int x = 0; x < width; x++) {
			int y = 0;
			
			if(cells.canGo(x, y, 1, 0)){
				cells.deleteWall(x, y, 1, 0);
				if(!extantSetNumbers.isEmpty()){
					extantSetNumbers.remove(trackingArray[x+1][y]);
				}
			}
		}
		//System.out.println(extantSetNumbers.size());
		//Iterator<Integer> iterator = extantSetNumbers.iterator(); 
	      
		   //while (iterator.hasNext()){
		   //System.out.println("Value: "+iterator.next() + " ");  
		   //}
	}
}
