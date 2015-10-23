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
	private final double LINE_FORWARD_DURATION = 0.25;

	// Empty constructor.
	public RobotRover()
	{
	}

	// Drive forward until we hit something, back up, 
	// turn left, drive forward again, then turn right and repeat.
	public void avoidObstacles()
	{
		// End program by pressing two middle buttons together
		while (Button.readButtons() != Button.ID_ESCAPE)
		{
			moveForwardUntilPressed();
			
			sleep(1);

			moveBackward(1);

			turnLeft();

			moveForward(1);

			turnRight();
		}

		System.out.println("Done");
	}
	
	// Same as avoidObstacles(), with ultrasonic sensor instead of touch
	public void avoidObstaclesUltrasonic()
	{
		// End program by pressing two middle buttons together
		while (Button.readButtons() != Button.ID_ESCAPE)
		{
			moveForwardUntilNearObject();
			
			sleep(1);
	
			moveBackward(1);
	
			turnLeft();
	
			moveForward(1);
	
			turnRight();
		}
	
		System.out.println("Done");
	}

	// Pause the robot.
	private void sleep(double time)
	{
		System.out.println("Sleeping for " + time + " seconds.");

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

	// Follow a black line with input from light sensor
	public void followLine()
	{
		LightSensor t1 = new LightSensor(SensorPort.S1);
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
		
		// End program by pressing two middle buttons together
		while (Button.readButtons() != Button.ID_ESCAPE)
		{
			while (t1.readValue() >= 24)
				Motor.B.forward();

			Motor.B.stop(true);

			while (t1.readValue() < 24)
				Motor.C.forward();

			Motor.C.stop(true);
		}

		//System.out.println("Done");
	}
	
	// Follow a line with color sensor until a color is hit
	// Returns true if endpoint, false if intersection
	public boolean followLineColor() {
		ColorSensor colorSensor = new ColorSensor(SensorPort.S1);
		
		Motor.B.setSpeed(200);
		Motor.C.setSpeed(200);
		
		// End program by pressing two middle buttons together
		while (Button.readButtons() != Button.ID_ESCAPE)
		{
			ColorSensor.Color color = colorSensor.getColor();
			
			int colorId = color.getColor();
			
			if (colorId == SensorConstants.BLUE) {
				return false;
			} else if (colorId == SensorConstants.RED) {
				return true;
			}
			
			while (colorId != SensorConstants.BLACK) {
				Motor.B.forward();
			}
			
			Motor.B.stop(true);
			
			moveForward(LINE_FORWARD_DURATION);
		}
		
		return true;
	}

	// Drive forward until we hit something.
	public void moveForwardUntilPressed()
	{
		TouchSensor t2 = new TouchSensor(SensorPort.S2);
		TouchSensor t3 = new TouchSensor(SensorPort.S3);

		moveForward(-1);

		while (!t2.isPressed() && !t3.isPressed())
			Thread.yield();

		stop();
	}
	
	// Drive forward until an object gets close
	public void moveForwardUntilNearObject()
	{
		
		UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S4);
	
		moveForward(-1);
		
		// While distance between nearest object and sensor is greater than 30 cm
		while (sonic.getDistance() > 30)
			Thread.yield();
	
		stop();
	}

	// Move forward for a specified amount of time (or infinite).
	// Pass -1 as an argument for forward infinitely
	public void moveForward(double time)
	{
		if (time == -1)
			System.out.println("Going forward infinitely!");
		else
			System.out.println("Going forward for " + time + " seconds!");

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
}