package falstad;

import java.awt.Event;

import falstad.Robot.Turn;

/**
 * This class is a manual driver for the navigation of a robot through the BasicRobot which operates on mazes created
 * by Falstad MazeBuilders. It allows the user to control the maze game through keyboard input, moving the robot
 * avatar through the maze trying to solve it.
 * 
 * This code implements the RobotDriver interface by Peter Kemper.
 * @author William S. Garrison
 */
public class ManualDriver implements RobotDriver {
	
	//initialize fields
	private int pathLength = 0;
	Robot baseRob;
	Distance internalDists;
	private int width;
	private int height;

	@Override
	public void setRobot(Robot r) {
		
		//sets the essential robot to this manualdriver
		this.baseRob = r;
		
	}

	@Override
	public void setDimensions(int width, int height) {
		
		//provides manualdriver with the info about the maze dimensions
		this.width = width;
		this.height = height;
	}

	@Override
	public void setDistance(Distance distance) {
		
		//sets the distance object passed to the manualdriver's internal distance field
		this.internalDists = distance;
	}

	@Override
	public boolean drive2Exit() throws Exception {
		// dummy for future use
		return false;
	}

	@Override
	public float getEnergyConsumption() {
		
		//based on the standard energy start for a robot, returns the energy used
		int returnBattery = 2500 - (int)baseRob.getBatteryLevel();
		return returnBattery ;
	}

	@Override
	public int getPathLength() {
		
		//returns the pathlength to the user
		return pathLength;
	}
	
	@Override
	public void incrementPathLength(){
		
		//adds 1 to the pathlength
		pathLength = pathLength + 1;
	}
	
	@Override
	public void clearPathLength(){
		
		//sets the pathlength to 0
		pathLength = 0;
	}
	
	/**
	 * 
	 * @param key
	 * @throws Exception if key input is invalid
	 */
	public void keyInput(int key) throws Exception {
		// possible inputs for key: 'k','j','h','l'
		try{
			switch (key) {
			case 'k': //UP
				baseRob.move(1);
				break;
			case 'h': //LEFT
				baseRob.rotate(Turn.LEFT);
				break;
			case 'l': //RIGHT
				baseRob.rotate(Turn.RIGHT);
				break;
			case 'j': //DOWN
				baseRob.rotate(Turn.AROUND);
				break;
			}
		}
		catch(Exception e){
		}
	}
}
