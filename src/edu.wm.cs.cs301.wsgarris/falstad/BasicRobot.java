package falstad;

/**
 * This class provides an API for manual and automatic driving algorithms to allow for the navigation of a maze
 * as built by any of the Falstad MazeBuilders. Has a zero argument default constructor, a field for battery power,
 * a field for if the robot is out of power, and fields for if it has various sensors. The BasicRobot supports all
 * Robot functions: turning, moving, seeing the goal, seeing the distance to next obstacle, etc.
 * 
 * This code implements the Robot interface by Peter Kemper.
 * @author William S. Garrison
 */
public class BasicRobot implements Robot {
	//instantiate necessary fields for battery and various sensors
	Maze maze;
	private float battery = 2500;
	private boolean isStopped = false;
	boolean rightSensor = true;
	boolean downSensor = true;
	boolean leftSensor = true;
	boolean upSensor = true;
	boolean roomSensor = true;


	@Override
	public void rotate(Turn turn) throws Exception {
		
		//rotate the robot in the maze: 1 for right, -1 for left, and 1 two times for turning around.
		//adjust battery accordingly
		try{
			switch(turn){
			case LEFT:
				maze.rotate(1);
				battery = battery - 3;
				break;
			case RIGHT:
				maze.rotate(-1);
				battery = battery - 3;
				break;
			case AROUND:
				maze.rotate(1);
				maze.rotate(1);
				battery = battery - 6;
				break;
			}
		}
		catch(Exception e){
		}
		
		//always check for battery level after draining operations
		//if done, call the maze to finish by the batteryfinish method
		if(battery<=0){
			isStopped = true;
			maze.batteryFinish();
		}
	}

	@Override
	public void move(int distance) throws Exception {
		int count = 0;
		
		//walk forward as many steps as the passed integer requires
		try{
			while(count<distance){
				maze.walk(1);
				battery = battery - 5;
				count += 1;
			}
		}
		catch(Exception e){
		}
		
		//always check for battery level after draining operations
		//if done, call the maze to finish by the batteryfinish method
		if(battery<=0){
			isStopped = true;
			maze.batteryFinish();
		}
	}

	@Override
	public int[] getCurrentPosition() throws Exception {
		
		//gets the current position from the maze in the form of an {x,y} two cell array
		try{
			return maze.getCurrentPosition();
		}
		catch(Exception e){
			return null;
		}
	}

	@Override
	public void setMaze(Maze maze) {
		
		//sets the essential maze for the robot
		this.maze = maze;
	}

	@Override
	public boolean isAtGoal() {
		
		//checks if the robot is at its goal
		return maze.isMazeExit();
	}

	@Override
	public boolean canSeeGoal(Direction direction) throws UnsupportedOperationException {
		
		//checks if the robot can see the goal square
		//always returns true if the robot is on its goal square
		boolean returnBool;
		if(this.distanceToObstacle(direction)==Integer.MAX_VALUE || maze.isMazeExit()){
			returnBool = true;
		}
		else{
			returnBool = false;
		}
		return returnBool;
	}

	@Override
	public boolean isInsideRoom() throws UnsupportedOperationException {
		
		//senses if the robot is in a room or not
		//if the bot has no room sensor, throw exception
		if(this.roomSensor==false){
			throw new UnsupportedOperationException();
		}
		return maze.isInRoom();
	}

	@Override
	public boolean hasRoomSensor() {
		
		//checks if the robot has a room sensor
		return this.roomSensor;
	}

	@Override
	public int[] getCurrentDirection() {
		
		//gets the current direction of the robot as a 2 cell array of {dx,dy} format
		return maze.getCurrentDirection();
	}

	@Override
	public float getBatteryLevel() {
		
		//returns the float of the battery level at the current time
		return battery;
	}

	@Override
	public void setBatteryLevel(float level) {
		
		//sets the battery level with float input from the user
		battery = level;
		if(battery==0){
			this.isStopped=true;
		}
	}

	@Override
	public float getEnergyForFullRotation() {
		
		//returns the energy for a 360degree rotation from this robot
		return 12;
	}

	@Override
	public float getEnergyForStepForward() {

		//returns the energy for a step forward from this robot
		return 5;
	}

	@Override
	public boolean hasStopped() {
		
		//returns if the robot is stopped or not as a boolean
		return isStopped;
	}

	@Override
	public int distanceToObstacle(Direction direction) throws UnsupportedOperationException {
		int returnDistance = 0;
		
		//relative to the robot's current direction, calls on the maze to calculate the distance to the
		//nearest obstacle, and returns it as an integer.
		//
		//if the robot does not have this sensor, throws exception
		try{
			switch(direction){
			case FORWARD:
				if(this.upSensor==false){
					throw new UnsupportedOperationException();
				}
				returnDistance = maze.wallDistance(0);
				battery = battery - 1;
				break;
			case RIGHT:
				if(this.rightSensor==false){
					throw new UnsupportedOperationException();
				}
				returnDistance = maze.wallDistance(1);
				battery = battery - 1;
				break;
			case BACKWARD:
				if(this.downSensor==false){
					throw new UnsupportedOperationException();
				}
				returnDistance = maze.wallDistance(2);
				battery = battery - 1;
				break;
			case LEFT:
				if(this.leftSensor==false){
					throw new UnsupportedOperationException();
				}
				returnDistance = maze.wallDistance(3);
				battery = battery - 1;
				break;
			default:
				break;
			}
			
		}
		catch(UnsupportedOperationException e){
			System.out.println("UnsupportedOperationException");
		}
		
		//always check for battery level after draining operations
		//if done, call the maze to finish by the batteryfinish method
		if(battery<0){
			isStopped = true;
			maze.batteryFinish();
		}
		return returnDistance;
	}

	@Override
	public boolean hasDistanceSensor(Direction direction) {
		boolean returnBool = false;
		
		
		//returns true if the robot has the requested sensor
		switch(direction){
		case FORWARD:
			returnBool = this.upSensor;
			break;
		case RIGHT:
			returnBool = this.rightSensor;
			break;
		case BACKWARD:
			returnBool = this.downSensor;
			break;
		case LEFT:
			returnBool = this.leftSensor;
			break;
		}
		return returnBool;
	}
	
	/**
	 * Test method to set the robot to not have any sensors.
	 */
	public void setAllFalse(){
		rightSensor = false;
		downSensor = false;
		leftSensor = false;
		upSensor = false;
		roomSensor = false;
	}
}
