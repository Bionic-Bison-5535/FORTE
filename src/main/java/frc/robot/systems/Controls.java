package frc.robot.systems;

import edu.wpi.first.wpilibj.Joystick;

public class Controls {

    public Joystick in;
    public double inputStickDeadband;
    public int A, B, X, Y, LEFT, RIGHT, BACK, START, LEFT_STICK, RIGHT_STICK;

    public Controls(int inputNumber, double InputStickDeadband) {
        in = new Joystick(inputNumber);
        inputStickDeadband = InputStickDeadband;
        A = 1;
        B = 2;
        X = 3;
        Y = 4;
        LEFT = 5;
        RIGHT = 6;
        BACK = 7;
        START = 8;
        LEFT_STICK = 9;
        RIGHT_STICK = 10;
    }

    private double deadband(double num_input, double db) {
		if (Math.abs(num_input) < db) {
			return 0;
		} else {
			return num_input;
		}
	}
    
    public double stick(int axisNumber) {
        return deadband(in.getRawAxis(axisNumber), inputStickDeadband);
    }
    
    public int pov() {
        return in.getPOV();
    }
    
    public boolean active() {
        return (stick(0) != 0 || stick(1) != 0);
    }
    
    /**
     * Function for getting boolean button status on controller
     * @param buttonNumber The number of the button
     * @return true if pressed, false if not pressed
     */
    public boolean get(int buttonNumber) {
        return in.getRawButton(buttonNumber);
    }
    
    public boolean onPress(int buttonNumber) {
        return in.getRawButtonPressed(buttonNumber);
    }
    
    public boolean onRelease(int buttonNumber) {
        return in.getRawButtonReleased(buttonNumber);
    }

}
