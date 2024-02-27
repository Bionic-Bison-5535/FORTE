package frc.robot.systems;

import edu.wpi.first.wpilibj.Joystick;

public class Controls {

    public Joystick in;
    public double inputStickDeadband;
    
    public static final int A = 1;
    public static final int B = 2;
    public static final int X = 3;
    public static final int Y = 4;
    public static final int LEFT = 5;
    public static final int RIGHT = 6;
    public static final int BACK = 7;
    public static final int START = 8;
    public static final int LEFT_STICK = 9;
    public static final int RIGHT_STICK = 10;

    public Controls(int inputNumber, double InputStickDeadband) {
        in = new Joystick(inputNumber);
        inputStickDeadband = InputStickDeadband;
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
        return (stick(0) != 0 || stick(1) != 0 || stick(4) != 0 || stick(5) != 0 || pov() != -1);
    }

    public double direction() {
        return in.getDirectionDegrees();
    }

    public double magnitude() {
        return in.getMagnitude();
    }
    
    /**
     * Function for getting boolean button status on controller
     * @param buttonNumber The number of the button
     * @return true if pressed, false if not pressed
     */
    public boolean button(int buttonNumber) {
        return in.getRawButton(buttonNumber);
    }
    
    public boolean onPress(int buttonNumber) {
        return in.getRawButtonPressed(buttonNumber);
    }
    
    public boolean onRelease(int buttonNumber) {
        return in.getRawButtonReleased(buttonNumber);
    }

    /** Clears previous instances of presses and releases of buttons */
    public void refreshController() {
        for (int i=1; i<=10; i++) {
            in.getRawButtonPressed(i);
            in.getRawButtonReleased(i);
        }
    }

    public boolean a() {
        return in.getRawButton(A);
    }

    public boolean b() {
        return in.getRawButton(B);
    }

    public boolean x() {
        return in.getRawButton(X);
    }

    public boolean y() {
        return in.getRawButton(Y);
    }

    public boolean left() {
        return in.getRawButton(LEFT);
    }

    public boolean right() {
        return in.getRawButton(RIGHT);
    }

    public boolean back() {
        return in.getRawButton(BACK);
    }

    public boolean start() {
        return in.getRawButton(START);
    }

    public boolean left_stick() {
        return in.getRawButton(LEFT_STICK);
    }

    public boolean right_stick() {
        return in.getRawButton(RIGHT_STICK);
    }

}
