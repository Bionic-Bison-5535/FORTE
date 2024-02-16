package frc.robot.systems;

import edu.wpi.first.wpilibj.DigitalInput;

public class Launch {

    private Motor leftThruster, rightThruster, feed;
    private DigitalInput note;
    private Tim launchTimer = new Tim();
    public int intakeStage = 0;
    public int launchStage = 0;
    public boolean holdingNote = true;

	public Launch(Motor LaunchLeftMotor, Motor LaunchRightMotor, Motor FeedMotor, int sensorPin) {
		leftThruster = LaunchLeftMotor;
        rightThruster = LaunchRightMotor;
        feed = FeedMotor;
        note = new DigitalInput(sensorPin);
	}

    public boolean iseenote() {
        return !note.get();
    }

    public void intake() {
        if (intakeStage == 0) {
			intakeStage = 1;
		}
    }

    public void stopIntake() {
        intakeStage = 0;
        feed.set(0);
        leftThruster.set(0);
        rightThruster.set(0);
    }

    public void LAUNCHprep() { // Fires up thrusters while running
        leftThruster.set(2);
        rightThruster.set(2);
    }

    public void LAUNCHstart() { // Starts automatic launch sequence
        launchTimer.reset();
        launchStage = 1;
    }

    public void LAUNCH() { // LAUNCH (officially)
        launchTimer.reset();
        launchStage = 2;
    }

    public void amp() { // Launch at low speed
        launchTimer.reset();
        launchStage = 3;
    }

    public void update() {

        // Intake System:
        if (!holdingNote) {
            if (intakeStage == 1) { // Intake note until detected
                feed.set(0.15);
                if (iseenote()) {
                    feed.set(0);
                    intakeStage = 2;
                }
            } else if (intakeStage == 2) { // If note went out of range, bring back into view
                if (iseenote()) {
                    intakeStage = 3;
                } else {
                    leftThruster.set(-0.05);
                    rightThruster.set(-0.05);
                    feed.set(-0.04);
                }
            }
            if (intakeStage == 3) { // Bring note to exact launch position
                feed.set(-0.035);
                if (!iseenote()) {
                    feed.set(0);
                    intakeStage = 0;
                    holdingNote = true;
                }
            }
        } else {
            intakeStage = 0;
        }

        // Launch System:
        if (holdingNote) {
            if (launchStage == 1) { // Fire up thrusters
                if (launchTimer.get() > 0.5) {
                    launchTimer.reset();
                    launchStage = 2;
                }
                leftThruster.set(2);
                rightThruster.set(2);
            }
            if (launchStage == 2) { // Push note into thrusters
                leftThruster.set(2);
                rightThruster.set(2);
                feed.set(2);
                if (launchTimer.get() > 0.3) { // Stop launcher
                    feed.set(0);
                    leftThruster.set(0);
                    rightThruster.set(0);
                    launchStage = 0;
                    holdingNote = false;
                }
            }
            if (launchStage == 3) { // Launch into amp
                leftThruster.set(0.15);
                rightThruster.set(0.15);
                feed.set(0.4);
            }
        } else {
            launchStage = 0;
        }

    }

}
