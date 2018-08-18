package falstad;

import java.util.ArrayList;
import java.util.Iterator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import falstad.Robot.Turn;
import ui.GeneratingActivity;
import ui.PlayActivity;

/**
 * Class handles the user interaction for the maze. 
 * It implements a state-dependent behavior that controls the display and reacts to key board input from a user. 
 * After refactoring the original code from an applet into a panel, it is wrapped by a MazeApplication to be a java application 
 * and a MazeApp to be an applet for a web browser. At this point user keyboard input is first dealt with a key listener
 * and then handed over to a Maze object by way of the keyDown method.
 *
 * This code is refactored code from Maze.java by Paul Falstad, www.falstad.com, Copyright (C) 1998, all rights reserved
 * Paul Falstad granted permission to modify and use code for teaching purposes.
 * Refactored by Peter Kemper
 */
// MEMO: original code: public class Maze extends Applet {
//public class Maze extends Panel {
public class Maze {

	// Model View Controller pattern, the model needs to know the viewers
	// however, all viewers share the same graphics to draw on, such that the share graphics
	// are administered by the Maze object
	final private ArrayList<Viewer> views = new ArrayList<Viewer>() ; 
	GraphicsWrapper gwrap ; // graphics to draw on, shared by all views

	private int state;			// keeps track of the current GUI state, one of STATE_TITLE,...,STATE_FINISH, mainly used in redraw()
	// possible values are defined in Constants
	// user can navigate 
	// title -> generating -(escape) -> title
	// title -> generation -> play -(escape)-> title
	// title -> generation -> play -> finish -> title
	// STATE_PLAY is the main state where the user can navigate through the maze in a first person view

	private boolean pause = false;//state of the automatic driver animation
	private int percentdone = 0; // describes progress during generation phase
	private boolean showMaze;		 	// toggle switch to show overall maze on screen
	private boolean showSolution;		// toggle switch to show solution in overall maze on screen
	private boolean solving;			// toggle switch 
	private boolean mapMode; // true: display map of maze, false: do not display map of maze
	// map_mode is toggled by user keyboard input, causes a call to draw_map during play mode

	//static final int viewz = 50;    
	int viewx, viewy, angle;
	int dx, dy;  // current direction
	int px, py ; // current position on maze grid (x,y)
	int savedStartX;
	int savedStartY;
	int walkStep;
	int viewdx, viewdy; // current view direction

	//test number
	int testNum = 0;

	// debug stuff
	boolean deepdebug = false;
	boolean allVisible = false;
	boolean newGame = false;

	// properties of the current maze
	int mazew; // width of maze
	int mazeh; // height of maze
	int expectedpartitiers;//number of expected partitiers
	int rooms;//number of rooms
	Cells mazecells ; // maze as a matrix of cells which keep track of the location of walls
	Distance mazedists ; // a matrix with distance values for each cell towards the exit
	Cells seencells ; // a matrix with cells to memorize which cells are visible from the current point of view
	// the FirstPersonDrawer obtains this information and the MapDrawer uses it for highlighting currently visible walls on the map
	
	//Instantiate a robot and robot driver to allow interaction with these programs.
	Robot rob;
	RobotDriver robDriv;
	PlayActivity playAct;
	
	//Boolean to tell the maze to call RobotDriver's drive2exit or not
	boolean autoRun = false;
	
	//display vars for display in MazeView
	String titleState = "title";
	int difftype = 0;
	
	BSPNode rootnode ; // a binary tree type search data structure to quickly locate a subset of segments
	// a segment is a continuous sequence of walls in vertical or horizontal direction
	// a subset of segments need to be quickly identified for drawing
	// the BSP tree partitions the set of all segments and provides a binary search tree for the partitions
	

	// Mazebuilder is used to calculate a new maze together with a solution
	// The maze is computed in a separate thread. It is started in the local Build method.
	// The calculation communicates back by calling the local newMaze() method.
	MazeBuilder mazebuilder;

	
	// fixing a value matching the escape key
	final int ESCAPE = 27;
	// fixing a value matching the enter key
	final static int ENTER = 10;
	// fixing a value matching the backspace key
	final int BACK_SPACE = 8;
	
	//constants for F keys
	final int F1 = 112;
	final int F2 = 113;
	final int F3 = 114;
	final int F4 = 115;
	final int F5 = 116;
	final int F6 = 117;

	// generation method used to compute a maze
	int method = 0 ; // 0 : default method, Falstad's original code
	// method == 1: Prim's algorithm
	// method == 2: Eller's algorithm

	int zscale = Constants.VIEW_HEIGHT/2;

	private RangeSet rset;
	
	/**
	 * Constructor
	 */
	public Maze() {
		super() ;
	}
	/**
	 * Constructor that also selects a particular generation method
	 */
	public Maze(int method)
	{
		super() ;
		// 0 is default, 1 is Prim's, 2 is Eller's
			if (1 == method){
				this.method = 1;
			}
			if (2 == method){
				this.method = 2;
			}
	}
	
	/**
	 * Method to initialize internal attributes. Called separately from the constructor. 
	 */
	public void init() {
		state = Constants.STATE_TITLE;
		rset = new RangeSet();
	}
	
	/**
	 * Method obtains a new Mazebuilder and has it compute new maze, 
	 * it is only used in keyDown()
	 * @param skill level determines the width, height and number of rooms for the new maze
	 */
	private void build(int skill) {
		Log.v("Building", "maze build method");
		// switch screen
		state = Constants.STATE_GENERATING;
		percentdone = 0;
		// select generation method
		switch(method){
		case 2 : mazebuilder = new MazeBuilderPrim(); //generate with Prim's algorithm, formerly Eller's
		break;
		case 1 : mazebuilder = new MazeBuilderPrim(); // generate with Prim's algorithm
		break ;
		case 0: // generate with Falstad's original algorithm (0 and default), note the missing break statement
		default : mazebuilder = new MazeBuilder(); 
		break ;
		}
		// adjust settings and launch generation in a separate thread
		mazew = Constants.SKILL_X[skill];
		mazeh = Constants.SKILL_Y[skill];
		expectedpartitiers = Constants.SKILL_PARTCT[skill];
		rooms = Constants.SKILL_ROOMS[skill];
		mazebuilder.build(this, mazew, mazeh, Constants.SKILL_ROOMS[skill], Constants.SKILL_PARTCT[skill]);
		try {
			mazebuilder.buildThread.join();
		} catch (InterruptedException e) {
			System.out.println("Oops, you got interrupted!");
		}
		// mazebuilder performs in a separate thread and calls back by calling newMaze() to return newly generated maze
	}
	
	/**
	 * Call back method for MazeBuilder to communicate newly generated maze as reaction to a call to build()
	 * @param root node for traversals, used for the first person perspective
	 * @param cells encodes the maze with its walls and border
	 * @param dists encodes the solution by providing distances to the exit for each position in the maze
	 * @param startx current position, x coordinate
	 * @param starty current position, y coordinate
	 */
	public void newMaze(BSPNode root, Cells c, Distance dists, int startx, int starty) {
		if (Cells.deepdebugWall)
		{   // for debugging: dump the sequence of all deleted walls to a log file
			// This reveals how the maze was generated
			c.saveLogFile(Cells.deepedebugWallFileName);
		}
		// adjust internal state of maze model
		
		this.percentdone = 100;
		Message msg = Message.obtain();
		Bundle b = new Bundle();
		b.putInt("prog", percentdone); 
        msg.setData(b); 
        GeneratingActivity.genHandler.sendMessage(msg);
		
		
		showMaze = showSolution = solving = false;
		mazecells = c ;
		mazedists = dists;
		seencells = new Cells(mazew+1,mazeh+1) ;
		rootnode = root ;
		setCurrentDirection(1, 0) ;
		savedStartX = startx;
		savedStartY = starty;
		setCurrentPosition(startx,starty) ;
		walkStep = 0;
		viewdx = dx<<16; 
		viewdy = dy<<16;
		angle = 0;
		mapMode = true;
		showMaze = true;
		showSolution = true;
		
		rob = new BasicRobot();
		rob.setMaze(this);
		robDriv.setRobot(rob);
		robDriv.setDistance(mazedists);
		robDriv.setDimensions(mazew, mazeh);
		
		cleanViews() ;
		// register views for the new maze
		// mazew and mazeh have been set in build() method before mazebuider was called to generate a new maze.
		// reset map_scale in mapdrawer to a value of 10
		addView(new FirstPersonDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,
				Constants.MAP_UNIT,Constants.STEP_SIZE, mazecells, seencells, 10, mazedists.getDists(), mazew, mazeh, root, this)) ;
		// order of registration matters, code executed in order of appearance!
		addView(new MapDrawer(Constants.VIEW_WIDTH,Constants.VIEW_HEIGHT,Constants.MAP_UNIT,Constants.STEP_SIZE, mazecells, seencells, 10, mazedists.getDists(), mazew, mazeh, this)) ;
		
	}
	
	/**
	 * Start the maze play
	 */
	public void runMan(PlayActivity p){
		state = Constants.STATE_PLAY;
		playAct = p;
		
		PlayActivity.graphHandler.postDelayed(update, 10);
		//notifyViewerRedraw();
	}
	
	final Runnable update = new Runnable(){
		public void run(){
			notifyViewerRedraw();
		}
	};
	
	/**
	 * Start automatic maze play
	 */
	public void autoRun(PlayActivity p){
		state = Constants.STATE_PLAY;
		pause = false;
		playAct = p;
		Log.v("autorun", "in the condition");
		
		final Runnable driveTask = new Runnable(){
			public void run(){
				try {
					robDriv.drive2Exit();
					Log.v("autorun", "autorun executed");
				} catch (Exception e) {
					Log.v("autorun", "exception thrown");
					e.printStackTrace();
				}
				playAct.batteryEdit();
				notifyViewerRedraw();
				if(!isMazeExit() && getBattery()>0 && pause==false){
					PlayActivity.graphHandler.postDelayed(this, 10);
				}else if(pause==true){
					PlayActivity.graphHandler.removeCallbacks(this);
					notifyViewerRedraw();
				}
			}
		};
		
		PlayActivity.graphHandler.postDelayed(driveTask, 10);
		
	}
	
	public void pause(){
		this.pause=true;
	}
	
	public void up(){
		try {
			((ManualDriver) robDriv).keyInput('k');
		} catch (Exception e) {}
	}
	
	public void left(){
		try {
			((ManualDriver) robDriv).keyInput('h');
		} catch (Exception e) {}
	}
	
	public void right(){
		try {
			((ManualDriver) robDriv).keyInput('l');
		} catch (Exception e) {}
	}
	
	public void down(){
		try {
			((ManualDriver) robDriv).keyInput('j');
		} catch (Exception e) {}
	}

	/////////////////////////////// Methods for the Model-View-Controller Pattern /////////////////////////////
	/**
	 * Register a view
	 */
	public void addView(Viewer view) {
		views.add(view) ;
	}
	/**
	 * Unregister a view
	 */
	public void removeView(Viewer view) {
		views.remove(view) ;
	}
	/**
	 * Remove obsolete FirstPersonDrawer and MapDrawer
	 */
	private void cleanViews() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			if ((v instanceof FirstPersonDrawer)||(v instanceof MapDrawer))
			{
				//System.out.println("Removing " + v);
				it.remove() ;
			}
		}

	}
	/**
	 * Notify all registered viewers to redraw their graphics
	 */
	private void notifyViewerRedraw() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			// viewers draw on the buffer graphics
			v.redraw(gwrap, state, px, py, viewdx, viewdy, walkStep, Constants.VIEW_OFFSET, rset, angle) ;
			//
		}
	}
	/** 
	 * Notify all registered viewers to increment the map scale
	 */
	private void notifyViewerIncrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.incrementMapScale() ;
		}
	}
	
	/** 
	 * Notify all registered viewers to decrement the map scale
	 */
	private void notifyViewerDecrementMapScale() {
		// go through views and notify each one
		Iterator<Viewer> it = views.iterator() ;
		while (it.hasNext())
		{
			Viewer v = it.next() ;
			v.decrementMapScale() ;
		}
	}
	////////////////////////////// get methods ///////////////////////////////////////////////////////////////
	boolean isInMapMode() { 
		return mapMode ; 
	} 
	boolean isInShowMazeMode() { 
		return showMaze ; 
	} 
	boolean isInShowSolutionMode() { 
		return showSolution ; 
	} 
	public String getPercentDone(){
		return String.valueOf(percentdone) ;
	}
	public int getPercentDoneInt(){
		return percentdone;
	}
	public GraphicsWrapper getPanel() {
		return gwrap ;
	}
	////////////////////////////// set methods ///////////////////////////////////////////////////////////////
	////////////////////////////// Actions that can be performed on the maze model ///////////////////////////
	private void setCurrentPosition(int x, int y)
	{
		px = x ;
		py = y ;
	}
	private void setCurrentDirection(int x, int y)
	{
		dx = x ;
		dy = y ;
	}
	
	
	void buildInterrupted() {
		state = Constants.STATE_TITLE;
		Log.v("Interrupt", "Build Interrupted");
		mazebuilder = null;
	}

	final double radify(int x) {
		return x*Math.PI/180;
	}


	/**
	 * Allows external increase to percentage in generating mode with subsequence graphics update
	 * @param pc gives the new percentage on a range [0,100]
	 * @return true if percentage was updated, false otherwise
	 */
	public boolean increasePercentage(int pc) {
		
		Log.v("Building", "maze increase percentage" + pc);
		
		if (percentdone < pc && pc <= 100) {
			percentdone = pc;
			Message msg = Message.obtain();
			Bundle b = new Bundle();
			b.putInt("prog", percentdone); 
            msg.setData(b); 
            
            GeneratingActivity.genHandler.sendMessage(msg);
            return true;
		}else{
			return false;
		}
	}

	



	/////////////////////// Methods for debugging ////////////////////////////////
	private void dbg(String str) {
		//System.out.println(str);
	}

	private void logPosition() {
		if (!deepdebug)
			return;
		dbg("x="+viewx/Constants.MAP_UNIT+" ("+
				viewx+") y="+viewy/Constants.MAP_UNIT+" ("+viewy+") ang="+
				angle+" dx="+dx+" dy="+dy+" "+viewdx+" "+viewdy);
	}


	////////////////////////////////////Methods for Robots///////////////////////////////////////////
	
	//returns the current px and py of the self position tracker in the maze
	public int[] getCurrentPosition(){
		 return new int[]{px, py};
	}
	
	public int[] getStartPosition(){
		 return new int[]{savedStartX, savedStartY};
	}
	
	//checks to see if the position is outside of the maze
	public boolean isOutsideMaze(){
		return px < 0 || py < 0 || px >= mazew || py >= mazeh;
	}
	
	//checks if the internal position is at the exit cell
	public boolean isMazeExit(){
		if(mazecells.isExitPosition(px, py)){
			this.drive2exitFinish();
		}else if(getBattery()<=0){
			playAct.finishIt();
		}
		return mazecells.isExitPosition(px, py);
	}
	
	//checks if the position is in a room
	public boolean isInRoom(){
		return mazecells.isInRoom(px, py);
	}
	
	//gets the current direction as {dx,dy} for the maze.
	//x= 1 is right, -1 is left for y = 0
	//y= 1 is up, -1 is down for x = 0
	public int[] getCurrentDirection(){
		 return new int[]{dx, dy};
	}
	
	//gets the current cell's distance from the exit as told my the distance matrix
	public int getCurrentDistance(){
		return mazedists.getDistance(px, py);
	}
	
	//returns this maze's distance object
	public Distance getDistances(){
		return mazedists;
	}
	
	//returns this maze's distance object
	public int[][] getIntArrayDistances(){
		return mazedists.getDists();
	}
	
	public Cells getCells(){
		return mazecells;
	}
	
	//returns this maze's width
	public int getWidth(){
		return mazew;
	}
	
	//returns this maze's height
	public int getHeight(){
		return mazeh;
	}
	
	//sets this maze's width
	public void setWidth(int w){
		mazew = w;
	}
	
	//sets this maze's height
	public void setHeight(int h){
		mazeh = h;
	}
	
	//sets a RobotDriver for robot operations on the maze
	public void setDriver(RobotDriver inputDriv){
		robDriv = inputDriv;
	}
	
	/**
	 * used by simplekeylistener, which only knows about the maze
	 * 
	 * @return the amount of battery used up until this point
	 */
	public int getBatteryUsed(){
		return (int)robDriv.getEnergyConsumption();
	}
	
	/**
	 * gets the current battery level of the robot for playactivity
	 * 
	 * @return the current battery level
	 */
	public int getBattery(){
		return (int) this.rob.getBatteryLevel();
	}
	
	/**
	 * used by simplekeylistener
	 * @return gets how many steps have been taken from manualdriver
	 */
	public int getPathSteps(){
		return robDriv.getPathLength();
	}
	
	/**
	 * used by basicrobot
	 * 
	 * used to end the game when the battery runs out
	 */
	void batteryFinish(){
		this.state = Constants.STATE_FINISH;
	}
	
	/**
	 * used by PlayActivity to see if the game is done
	 * @return
	 */
	public boolean getFinishState(){
		if(state==Constants.STATE_FINISH){
			return true;
		}else{
			return false;
		}

	}
	
	/**
	 * returns if the robot has stopped
	 * 
	 * @return true if the robot has stopped, false if not
	 */
	public boolean getRobotStopped(){
		return this.rob.hasStopped();
	}
	
	/**
	 * gets the distance to the next wall in a specified direction for basicrobot relative to its body
	 * 
	 * 0 is up, 1 is right, 2 is down, 3 is left
	 * 
	 * information about current direction is obtained from dx and dy in the maze
	 * though the code is long, it is quite simple: the robot specifies which side of its body
	 * (relative to the direction in which it is currently facing) it would like to sense
	 * the method then goes along the maze matrix in that direction, sensing for walls as it moves out in a line
	 * if it senses a wall along the line, it stops and returns how many steps it took.
	 * if it runs into the exit square, it returns Integer.MAX_VALUE
	 * 
	 * @param direction
	 * @return
	 */
	
	
	public int wallDistance(int direction){
		int returnDist = 0;
		boolean hitwall = false;
		int x = px;
		int y = py;
		
		if(dx==0){ //facing up or down
			if(dy==1){ //facing up
				switch(direction){
				case 0:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnBottom(x, y)){
							returnDist = returnDist +1;
							y = y+1;
						}
						else{
							hitwall=true;
						}
					}
					break;
				case 1:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						}else if(mazecells.hasNoWallOnRight(x, y)){
							returnDist = returnDist +1;
							x = x+1;
						}else{
							hitwall=true;
						}
					}
					break;
				case 2:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnTop(x, y)){
							returnDist = returnDist +1;
							y = y-1;
						}else{
							hitwall=true;
						}
					}
					break;
				case 3:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnLeft(x, y)){
							returnDist = returnDist +1;
							x = x-1;
						}else{
							hitwall=true;
						}
					}
					break;
				}
			}else{ //facing down
				switch(direction){
				case 0:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnTop(x, y)){
							returnDist = returnDist +1;
							y = y-1;
						}else{
							hitwall=true;
						}
					}
					break;
				case 1:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnLeft(x, y)){
							returnDist = returnDist +1;
							x = x-1;
						}else{
							hitwall=true;
						}
					}
					break;
				case 2:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnBottom(x, y)){
							returnDist = returnDist +1;
							y = y+1;
						}else{
							hitwall=true;
						}
					}
					break;
				case 3:
					while(hitwall==false){
						if(mazecells.isExitPosition(x, y)){
							returnDist = Integer.MAX_VALUE;
							hitwall=true;
						
						}else if(mazecells.hasNoWallOnRight(x, y)){
							returnDist = returnDist +1;
							x = x+1;
						}else{
							hitwall=true;
						}
					}
					break;
				}
			}
		}else if(dx==1){ //facing right
			switch(direction){
			case 0:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnRight(x, y)){
						returnDist = returnDist +1;
						x = x+1;
					}else{
						hitwall=true;
					}
				}
				break;
			case 1:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnTop(x, y)){
						returnDist = returnDist +1;
						y = y-1;
					}else{
						hitwall=true;
					}
				}
				break;
			case 2:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnLeft(x, y)){
						returnDist = returnDist +1;
						x = x-1;
					}else{
						hitwall=true;
					}
				}
				break;
			case 3:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnBottom(x, y)){
						returnDist = returnDist +1;
						y = y+1;
					}else{
						hitwall=true;
					}
				}
				break;
			}
		}else{//facing left
			switch(direction){
			case 0:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnLeft(x, y)){
						returnDist = returnDist +1;
						x = x-1;
					}else{
						hitwall=true;
					}
				}
				break;
			case 1:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnBottom(x, y)){
						returnDist = returnDist +1;
						y = y+1;
					}else{
						hitwall=true;
					}
				}
				break;
			case 2:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnRight(x, y)){
						returnDist = returnDist +1;
						x = x+1;
					}else{
						hitwall=true;
					}
				}
				break;
			case 3:
				while(hitwall==false){
					if(mazecells.isExitPosition(x, y)){
						returnDist = Integer.MAX_VALUE;
						hitwall=true;
					
					}else if(mazecells.hasNoWallOnTop(x, y)){
						returnDist = returnDist +1;
						y = y-1;
					}else{
						hitwall=true;
					}
				}
				break;
			}
		}
		
		//return the distance to the nearest wall
		return returnDist;
	}
	
	/**
	 * used by solving drivers from the isMazeExit method
	 * used to end the game when the bot gets to the exit
	 */
	private void drive2exitFinish(){
		this.state = Constants.STATE_FINISH;
		playAct.finishIt();
	}
	
	/**
	 * sets the graphics wrapper for the maze to use to draw the game on the play screen
	 */
	public void setWrapper(GraphicsWrapper wrapper) {
		this.gwrap = wrapper;
	}
	
	/**
	 * increment test variable to ensure that the Maze used in each activity is the same maze
	 */
	public void incTest(){
		testNum += 1;
	}
	
	/**
	 * @return test variable to ensure that the Maze used in each activity is the same maze
	 */
	public int getTest(){
		return testNum;
	}
	
	/**
	 * Starts the maze builder, which will result in the creation and return of the maze by the builder to the
	 * maze class.
	 * 
	 * Used by GeneratingActivity.
	 */
	public void startBuild(){
		Log.v("Starting build", "executing method");
		build(difftype);
	}
	/**
	 * Sets the difficulty by the seekbar
	 */
	public void setDifficulty(int a){
		difftype = a;
	}
	
	/**
	 * Gets the difficulty for the filesystem
	 */
	public int getDifficulty(){
		return difftype;
	}
	
	/**
	 * Sets the maze generation method
	 */
	public void setGenerateMethod(int a){
		method = a;
	}
	
	/**
	 * Sets the maze generation method
	 */
	public int getGenerateMethod(){
		return method;
	}
	
	/**
	 * Gets the expected partitier count
	 */
	public int getPartitiers(){
		return expectedpartitiers;
	}
	
	/**
	 * Gets the room count
	 */
	public int getRooms(){
		return rooms;
	}
	
	/**
	 * Gets the BSP root
	 */
	public BSPNode getBSP(){
		return rootnode;
	}
	
	/**
	 * 
	 */
	public void playMethodManual(){
		robDriv = new ManualDriver();
		robDriv.setRobot(rob);
		this.autoRun=false;
		Log.v("Runtype", "manual");
	}
	
	/**
	 * 
	 */
	public void playMethodWizard(){
		robDriv = new Wizard();
		robDriv.setRobot(rob);
		robDriv.setDistance(mazedists);
		this.autoRun = true;
		Log.v("Runtype", "wizard");
	}
	
	/**
	 * 
	 */
	public void playMethodWallF(){
		robDriv = new WallFollower();
		robDriv.setRobot(rob);
		this.autoRun = true;
		Log.v("Runtype", "follow");
	}
	
	/**
	 * 
	 */
	public void playMethodMouse(){
		robDriv = new CuriousMouse();
		robDriv.setRobot(rob);
		robDriv.setDimensions(mazew, mazeh);
		this.autoRun = true;
		Log.v("Runtype", "crazymouse");
	}
	
	///////////////////////////////////////////////////////////////////////////////
	/**
	 * Helper method for walk()
	 * @param dir
	 * @return true if there is no wall in this direction
	 */
	protected boolean checkMove(int dir) {
		// obtain appropriate index for direction (CW_BOT, CW_TOP ...) 
		// for given direction parameter
		int a = angle/90;
		if (dir == -1)
			a = (a+2) & 3;
		// check if cell has walls in this direction
		// returns true if there are no walls in this direction
		return mazecells.hasMaskedBitsFalse(px, py, Constants.MASKS[a]) ;
	}



	protected void rotateStep() {
		angle = (angle+1800) % 360;
		viewdx = (int) (Math.cos(radify(angle))*(1<<16));
		viewdy = (int) (Math.sin(radify(angle))*(1<<16));
		moveStep();
	}

	@SuppressWarnings("static-access")
	protected void moveStep() {
		PlayActivity.graphHandler.postDelayed(update, 10);
		//notifyViewerRedraw() ;
		try {
			Thread.currentThread().sleep(25);
		} catch (Exception e) { }
	}

	private void rotateFinish() {
		setCurrentDirection((int) Math.cos(radify(angle)), (int) Math.sin(radify(angle))) ;
		PlayActivity.graphHandler.postDelayed(update, 10);
		logPosition();
	}

	private void walkFinish(int dir) {
		setCurrentPosition(px + dir*dx, py + dir*dy) ;
		
		if (isEndPosition(px,py)) {
			state = Constants.STATE_FINISH;
			PlayActivity.graphHandler.postDelayed(update, 10);
			//notifyViewerRedraw() ;
		}
		
		robDriv.incrementPathLength();
		
		walkStep = 0;
		logPosition();
	}

	/**
	 * checks if the given position is outside the maze
	 * @param x
	 * @param y
	 * @return true if position is outside, false otherwise
	 */
	private boolean isEndPosition(int x, int y) {
		return x < 0 || y < 0 || x >= mazew || y >= mazeh;
	}



	synchronized void walk(int dir) {
		if (!checkMove(dir))
			return;
		for (int step = 0; step != 4; step++) {
			walkStep += dir;
			moveStep();
		}
		walkFinish(dir);
	}

	synchronized protected void rotate(int dir) {
		final int originalAngle = angle;
		final int steps = 4;

		for (int i = 0; i != steps; i++) {
			angle = originalAngle + dir*(90*(i+1))/steps;
			rotateStep();
		}
		rotateFinish();
		//System.out.println("Current dx: " + dx + " Current dy: " + dy);
	}



	/**
	 * Method incorporates all reactions to keyboard input in original code, 
	 * after refactoring, Java Applet and Java Application wrapper call this method to communicate input.
	 */
	public boolean keyDown(char key) {

		switch (state) {
		// if screen shows title page, allows the user to control the menus according to the directions on screen
		// to allow for selecting difficulty up to 15, maze generation method, and the method of solving the maze

		case Constants.STATE_GENERATING:
			if (key == 'b') {
				mazebuilder.interrupt();
				buildInterrupted();
			}
			
			switch(key){
			case '\t': case 'm':
				mapMode = !mapMode; 	
				Log.v("map", "made it");
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ; 
				break;
			case 'z':
				showMaze = !showMaze; 	
				Log.v("walls", "made it");
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ; 
				break;
			case 's':
				showSolution = !showSolution; 	
				Log.v("sol", "made it");
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ;
				break;
			}break;
		// if user explores maze, 
		// react to input for directions and interrupt signal (ESCAPE key)	
		// react to input for displaying a map of the current path or of the overall maze (on/off toggle switch)
		// react to input to display solution (on/off toggle switch)
		// react to input to increase/reduce map scale
		case Constants.STATE_PLAY:
			switch (key) {
			case 'k': case '8':
				walk(1);
				break;
			case 'h': case '4':
				rotate(1);
				break;
			case 'l': case '6':
				rotate(-1);
				break;
			case 'j': case '2':
				walk(-1);
				break;
			case ESCAPE: case 65385:
				if (solving)
					solving = false;
				else
					state = Constants.STATE_TITLE;
				break;
			case ('w' & 0x1f): 
			{ 
				setCurrentPosition(px + dx, py + dy) ;
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ;
				break;
			}
			case '\t': case 'm':
				mapMode = !mapMode; 	
				Log.v("map", "made it");
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ; 
				break;
			case 'z':
				showMaze = !showMaze; 	
				Log.v("walls", "made it");
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ; 
				break;
			case 's':
				showSolution = !showSolution; 	
				Log.v("sol", "made it");
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ;
				break;
			case ('s' & 0x1f):
				if (solving)
					solving = false;
				else {
					solving = true;
				}
			break;
			case '+': case '=':
			{
				notifyViewerIncrementMapScale() ;
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
				break ;
			}
			case '-':
				notifyViewerDecrementMapScale() ;
				PlayActivity.graphHandler.postDelayed(update, 10);
				//notifyViewerRedraw() ; // seems useless but it is necessary to make the screen update
				break ;
			}
			break;
		// if we are finished, return to initial state with title screen	
		case Constants.STATE_FINISH:
			state = Constants.STATE_TITLE;
			robDriv.clearPathLength();
			this.rob.setBatteryLevel(2500);
			break;
		} 
		return true;
	}




	


}
