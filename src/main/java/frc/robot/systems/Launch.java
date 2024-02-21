package frc.robot.systems;

import edu.wpi.first.wpilibj.DigitalInput;
import com.ctre.phoenix6.hardware.*;

public class Launch {

    private Motor leftThruster, rightThruster, feed, aimMotor;
    public CANcoder aimCoder;
    private DigitalInput note;
    private Tim launchTimer = new Tim();
    public int intakeStage = 0;
    public int launchStage = 0;
    public boolean holdingNote = true;

	public Launch(Motor LaunchLeftMotor, Motor LaunchRightMotor, Motor FeedMotor, Motor AimMotor, int aimCoderID, int sensorPin) {
        note = new DigitalInput(sensorPin);
		leftThruster = LaunchLeftMotor;
        rightThruster = LaunchRightMotor;
        feed = FeedMotor;
        aimMotor = AimMotor;
        aimCoder = new CANcoder(aimCoderID);
        aimMotor.setRotations(aimCoder.getPosition().getValue()*/* gear ratio */1);
	}

    public double aimPos() {
        return aimMotor.getEnc();
    }

    public boolean iseenote() {
        return !note.get();
    }

    public void intake() {
        if (intakeStage == 0) {
			intakeStage = 1;
            aimMotor.goTo(21.5);
		}
    }

    public void stopIntake() {
        intakeStage = 0;
        feed.set(0);
        leftThruster.set(0);
        rightThruster.set(0);
    }

    /** Fires up thrusters while function called */
    public void LAUNCHprep() {
        leftThruster.set(2);
        rightThruster.set(2);
    }

    /** Starts automatic launch sequence */
    public void LAUNCHstart() {
        launchTimer.reset();
        launchStage = 1;
    }

    /** LAUNCH (officially) */
    public void LAUNCH() {
        launchTimer.reset();
        launchStage = 2;
    }

    /** Launch at low-ish speed */
    public void amp() {
        launchTimer.reset();
        launchStage = 3;
    }

    /** Call periodically to run this system */
    public void update() {

        aimMotor.update();

        // Intake System:
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

        // Launch System:
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

    }

}
