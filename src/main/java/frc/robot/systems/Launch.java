package frc.robot.systems;

import edu.wpi.first.wpilibj.DigitalInput;
import com.ctre.phoenix6.hardware.*;

public class Launch {

    private Motor leftThruster, rightThruster, feed, aimMotor;
    public CANcoder aimCoder;
    private DigitalInput note;
    private Tim launchTimer = new Tim();
    public int stage = 0;

    /** Encoder-based positions for launcher to go to */
    public class pos {
        /** Intake position */
        public static double intake = 19.5;
        /** Position for scoring in amp */
        public static double amp = 120;
        /** Position for scoring in speaker while pressed up against subwoofer */
        public static double closeup = 15;
    }

	public Launch(Motor LaunchLeftMotor, Motor LaunchRightMotor, Motor FeedMotor, Motor AimMotor, int aimCoderID, int sensorPin) {
        note = new DigitalInput(sensorPin);
		leftThruster = LaunchLeftMotor;
        rightThruster = LaunchRightMotor;
        feed = FeedMotor;
        aimMotor = AimMotor;
        aimCoder = new CANcoder(aimCoderID);
        aimMotor.setEnc(aimCoder.getPosition().getValue()*182);
        aimMotor.goTo(pos.intake);
	}

    public double aimPos() {
        return aimMotor.getEnc();
    }

    public void aim(double encValue) {
        aimMotor.goTo(encValue);
    }

    public boolean iseenote() {
        return !note.get();
    }

    public void intake() {
        if (stage == 0) {
			stage = 1;
            aimMotor.goTo(pos.intake);
		}
    }

    public void stopIntake() {
        stage = 0;
        feed.set(0);
        leftThruster.set(0);
        rightThruster.set(0);
    }

    /** Fires up thrusters while function called */
    public void LAUNCHprep() {
        leftThruster.set(2);
        rightThruster.set(0.8);
    }

    /** Starts automatic launch sequence */
    public void LAUNCHstart() {
        launchTimer.reset();
        stage = 11;
    }

    /** LAUNCH (officially) */
    public void LAUNCH() {
        launchTimer.reset();
        stage = 12;
    }

    /** Launch at low-ish speed at downward angle */
    public void amp() {
        stage = 21;
    }

    /** Call periodically to run this system */
    public void update() {

        aimMotor.update();

        // Intake System:
        if (stage == 1) { // Intake note until detected
            feed.set(0.15);
            if (iseenote()) {
                feed.set(0);
                stage = 2;
            }
        } else if (stage == 2) { // If note went out of range, bring back into view
            if (iseenote()) {
                stage = 3;
            } else {
                leftThruster.set(-0.05);
                rightThruster.set(-0.05);
                feed.set(-0.04);
            }
        }
        if (stage == 3) { // Bring note to exact launch position
            feed.set(-0.035);
            if (!iseenote()) { // Stop intake (finish process)
                feed.set(0);
                stage = 0;
            }
        }

        // Launch System:
        if (stage == 11) { // Fire up thrusters
            leftThruster.set(2);
            rightThruster.set(0.8);
            if (launchTimer.get() > 700) {
                launchTimer.reset();
                stage = 12;
            }
        }
        if (stage == 12) { // Push note into thrusters
            leftThruster.set(2);
            rightThruster.set(0.8);
            feed.set(2);
            if (launchTimer.get() > 300) { // Stop launcher (finish process)
                feed.set(0);
                leftThruster.set(0);
                rightThruster.set(0);
                stage = 0;
            }
        }

        // Amp Launch:
        if (stage == 21) { // Angle to amp score position
            aimMotor.goTo(pos.amp);
            if (aimMotor.almost()) {
                stage = 22;
                launchTimer.reset();
            }
        }
        if (stage == 22) { // Discard note at low speed
            leftThruster.set(0.2);
            rightThruster.set(0.2);
            feed.set(0.4);
            if (launchTimer.get() > 1000) { // Stop launcher (finish process)
                leftThruster.set(0);
                rightThruster.set(0);
                feed.set(0);
                stage = 0;
            }
        }

        // Keep note from slipping down into intake:
        if (stage == 0) {
            if (iseenote()) {
                feed.set(0.04);
            } else {
                feed.set(0);
            }
        }

    }

}
