package frc.robot.systems;

import edu.wpi.first.wpilibj.Joystick;

public class Controls {

    public Joystick in;
    public double inputStickDeadband;
    private double[] rr0 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private double[] rr1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private double[] rrM = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private double add0, add1, addM;

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

    public double stickWithRamp(int axisNumber) {
        if (axisNumber == 0) {
            return shift0();
        } else if (axisNumber == 1) {
            return shift1();
        } else {
            return stick(axisNumber);
        }
    }

    public double shift0() {
        add0 = 0;
        for (int i = 0; i < rr0.length - 1; i++) {
            rr0[i] = rr0[i+1];
            add0 += rr0[i];
        }
        rr0[rr0.length - 1] = stick(0);
        add0 += stick(1);
        return add0/rr0.length;
    }

    public double shift1() {
        add1 = 0;
        for (int i = 0; i < rr1.length - 1; i++) {
            rr1[i] = rr1[i+1];
            add1 += rr1[i];
        }
        rr1[rr1.length - 1] = stick(1);
        add1 += stick(1);
        return add1/rr1.length;
    }

    public double shiftM() {
        addM = 0;
        for (int i = 0; i < rrM.length - 1; i++) {
            rrM[i] = rrM[i+1];
            addM += rrM[i];
        }
        rrM[rrM.length - 1] = magnitude();
        addM += rrM[rrM.length - 1];
        return addM/rrM.length;
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

    public double magnitudeWithRamp() {
        return shiftM();
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
