/*******************************************************************
* @authors Theodore Miller, Danny Nsouli, Nathan Walker, Sam Hanna *
********************************************************************
*
* This program contains all of the methods to drive the NXT robot.
*
*/

import lejos.nxt.*;

public class RobotRover
{
	private final int speed = 300;
	public final double LINE_FORWARD_DURATION = 0.1;
	public final int BLACK = 1;
	public final int GREY = 2;
	public final int BLUE = 3;
	public final int BROWN = 4;

	// Empty constructor.
	public RobotRover()
	{
	}

	public int getColor()
	{
		double colors[] = getColorSensorHSV();
		
		if (colors[0] > 320 && colors[0] < 380 & colors[1] > 0.1)
			return BLACK;
		else if (colors[0] > 260 && colors[0] < 280 && colors[2] <= 0.1)
			return GREY;
		else if (colors[0] > 200 && colors[0] < 220)
			return BLUE;
			
		
		return BROWN;
	}

	/**
	 * Has the robot pause/wait for a given amount of time
	 * 
	 * @param The amount of time for the robot to wait, ie 0.1 = 1/10 of a second
	 */
	public void sleep(double time)
	{
		try
		{
			int t = (int)(time * 1000);
			Thread.sleep(t);
		}

		catch(InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * Gets the side in which we are off/need to adjust to in order to stay straight
	 * 
	 * @return 1 = turn right, -1 = turn left, 0 if no adjustment needed/error
	 */
	public int getOffSide()
	{
		UltrasonicSensor usLeft = new UltrasonicSensor(SensorPort.S3); // left
		UltrasonicSensor usRight = new UltrasonicSensor(SensorPort.S4); // right
		
//		System.out.println("Left is: " + usLeft.getDistance());
//		System.out.println("Right is: " + usRight.getDistance());
		
		if ((usLeft.getDistance() <= 10) && (usRight.getDistance() > 10))
			return 1; // we should adjust to turn right
		else if ((usLeft.getDistance() > 10) && (usRight.getDistance() <= 10))
			return -1; // we should adjust to turn left
		
		return 0; // error/no adjustment needed
	}
	
	// Follow a line with color sensor until a color is hit
	// Returns a result object (see below)
	public MovementResult followLineUntilStopped(double maxDistance)
	{
		ColorSensor colorSensor = new ColorSensor(SensorPort.S1);
		TouchSensor touchSensor = new TouchSensor(SensorPort.S2);
				
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
		
		double distanceTravelled = 0.0;
		boolean isEndpoint = false;
		boolean isWall = false;
		
		distanceTravelled += LINE_FORWARD_DURATION;
		moveForward(LINE_FORWARD_DURATION);
				
		while (true)
		{
			if (touchSensor.isPressed())
			{
				System.out.println("Oops, hit a wall");
				isWall = true;
				break;
			}		
			
			/*boolean shouldMoveLeft = true;
			int degrees = 0;
			while (getColor() == BROWN)
			{
				Motor.B.setSpeed(100);
				Motor.C.setSpeed(100);
				degrees += 5;
				for (int i = degrees; i > 0; i -= 5)
				{
					if (shouldMoveLeft) {
						Motor.B.rotate(degrees, true);
						Motor.C.rotate(-degrees, false);
					} else {
						Motor.C.rotate(degrees, true);
						Motor.B.rotate(-degrees, false);
					}
					
					if (getColor() != BROWN) {
						break;
					}
				}
				
				shouldMoveLeft = !shouldMoveLeft;
			}*/
			
			Motor.B.setSpeed(200);
			Motor.C.setSpeed(200);
			
			if (getColor() == BLUE)
			{
				System.out.println("Blue detected");
				break;
			}
			else if (getColor() == GREY)
			{
				System.out.println("Grey detected");
				// Should be changed to look for endpoint color
				isEndpoint = true;
				break;
			}
			
			findLine();
			
			distanceTravelled += LINE_FORWARD_DURATION;
			moveForward(LINE_FORWARD_DURATION);
		}

		
		MovementResult res = new MovementResult(isEndpoint, isWall, distanceTravelled);

		return res;
	}
	
	public void findLine() // pathfinder
	{
		boolean side = true; // left = true right = false
		int currentDegrees = 0;
		int i = 10;
		int j = 0;
		
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
		
		while (getColor() != BLACK)
		{
			Motor.B.setSpeed(200);
			Motor.C.setSpeed(200);
			while (getColor() != BLACK && j < i)
			{
				if (side)
				{
					currentDegrees--;
					Motor.B.rotate(1, true);
					Motor.C.rotate(-1, true);
				}
				else
				{
					Motor.B.rotate(-1, true);
					Motor.C.rotate(1, true);
					currentDegrees++;
				}
					
				if (getColor() == BLACK)
					break;

				j++;
			}
				
			if (getColor() == BLACK)
				break;
			
			i *= 2; // go twice the distance because we need to account for the distance we went in the opposite direction
			side = !side; // change the side we scan on
			
			if (currentDegrees >= 150) // if we're making left turns then we should move forward a bit and then reset and try again
			{
				Motor.B.rotate(currentDegrees - 15, true);
				Motor.C.rotate(-(currentDegrees - 15), false);
				moveBackward(0.15);
				currentDegrees = 0;
				i = 10;
				j = 0;
			}
		}
		
		stop();
	}

	// black is 16 <=
	// white is > 290, < 310
	// wood is > 18, < 24
	public double getColorSensorH()
	{
		return getColorSensorHSV()[0];
	}

	// Credit to Dr. Drumwright for this code
	public double[] getColorSensorHSV()
	{
		ColorSensor colorSensor = new ColorSensor(SensorPort.S1);

		ColorSensor.Color colors = colorSensor.getColor();
		

		double[] hsv = new double[3];
		
		// read colors
		int r = colors.getRed();
		int b = colors.getBlue();
		int g = colors.getGreen();
		
		double min = Math.min(r, Math.min(b,g));
		double max = Math.max(r, Math.max(b, g));
		double delta = max - min;
		hsv[2] = max/255; //set v to max as a percentage
		if (max != 0)
		{
			hsv[1] = delta/max;
		}
		else{ //r = b = g =0 
			hsv[1] = 0; //s = 0;		// s = 0, v is undefined
			hsv[0] = -1; //h = -1;
			return hsv;
		}
		
		if (r == max){
			hsv[0] = (g-b)/delta; //h 
		}
		else{
			if (g == max)
				hsv[0] = 2 + (b - r)/delta; //h
			else
				hsv[0] = 4 + (r - g)/delta; //h
		}
		
		hsv[0] *=60;	//degrees
		if (hsv[0] < 0)
			hsv[0] +=360;
		
		return hsv;
	}

	// Move forward for a specified amount of time (or infinite).
	// Pass -1 as an argument for forward infinitely
	public void moveForward(double time)
	{
		if (time == -1)
			System.out.println("Going forward infinitely!");
		else
			//System.out.println("Going forward for " + time + " seconds!");

		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);

		if (time == -1)
		{
			Motor.B.forward();
			Motor.C.forward();
		}
		else
		{
			Motor.B.forward();
			Motor.C.forward();

			sleep(time);

			stop();
		}
	}

	// Drive backwards for a specified amount of time (or infinite).
	// Pass -1 as an argument for forward infinitely
	public void moveBackward(double time)
	{
		if (time == -1)
			System.out.println("Going backward infinitely!");
		else
			System.out.println("Going backward for " + time + " seconds!");


		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);

		if (time == -1)
		{
			Motor.B.backward();
			Motor.C.backward();
		}
		else
		{
			Motor.B.backward();
			Motor.C.backward();

			sleep(time);

			stop();
		}
	}

	// Slow down the motors then stop them.
	// This is because the motors will not stop synchronously.
	public void stop()
	{
		for (int i = Motor.B.getSpeed(); i > 0; i--)
		{
			Motor.B.setSpeed(i);
			Motor.C.setSpeed(i);
		}

		Motor.B.stop(true);
		Motor.C.stop(true);
	}

	// Turn 90 degrees left.
	public void turnLeft()
	{
		System.out.println("Turning left!");

		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);

		Motor.B.rotate(180, true);
		Motor.C.rotate(-180, true);

   		while(Motor.B.isMoving() && Motor.C.isMoving())
			Thread.yield();

		sleep(0.1);
	}

	// Turn 90 degrees right.
	public void turnRight()
	{
		System.out.println("Turning right!");

		Motor.B.setSpeed(speed);
		Motor.C.setSpeed(speed);

		Motor.B.rotate(-180, true);
		Motor.C.rotate(180, true);

   		while(Motor.B.isMoving() && Motor.C.isMoving())
			Thread.yield();

		sleep(0.1);
	}
	
	public void turnAround() {
		moveBackward(0.25);
		turnLeft();
		turnLeft();
		moveBackward(0.25);
	}
}