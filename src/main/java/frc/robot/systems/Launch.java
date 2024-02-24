package frc.robot.systems;

import edu.wpi.first.wpilibj.DigitalInput;
import com.ctre.phoenix6.hardware.*;

public class Launch {

    private Motor leftThruster, rightThruster, feed, aimMotor;
    public CANcoder aimCoder;
    private DigitalInput note;
    private Tim launchTimer = new Tim();
    private Limelight cam;
    public int stage = 0;
    public boolean holdingNote = false;

    /** Encoder-based positions for launcher to go to */
    public class pos {
        public static double min = -21;
        public static double max = 120;
        /** Intake position */
        public static double intake = 19.5;
        /** Position for scoring in amp */
        public static double amp = 88.14;
        /** Position for scoring in speaker while pressed up against subwoofer */
        public static double closeup = 15;
        /** Function to calculate encoder position based on Limelight camera input */
        public static double smartAim(double limelightArea, double limelightY) {
            return limelightArea + limelightY; // DOES NOT WORK! (PLACEHOLDER FOR ACTUAL FORMULA UNTIL FORMULA DISCOVERED)
        }
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
        feed.pwr = 3;
	}

    public double aimPos() {
        return aimMotor.getEnc();
    }
    
    public void aim(double encValue) {
        if (encValue < pos.min) {
            aimMotor.goTo(pos.min);
        } else if (encValue > pos.max) {
            aimMotor.goTo(pos.max);
        } else {
            aimMotor.goTo(encValue);
        }
    }

    public void changeAim(double changeInEncValue) {
        aim(aimMotor.goToPos + changeInEncValue);
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

    /** LAUNCH (officially) */
    public void LAUNCH() {
        launchTimer.reset();
        stage = 12;
    }

    /** Starts automatic launch sequence */
    public void LAUNCHstart() {
        launchTimer.reset();
        stage = 11;
    }

    /** Aims and then begins automatic launch sequence */
    public void aimAndLAUNCH(Limelight Cam) {
        stage = 31;
        cam = Cam;
        cam.activate();
    }

    /** Launch at downward angle perfect for scoring in amp */
    public void amp() {
        stage = 21;
    }

    /** Call periodically to run this system */
    public void update() {

        aimMotor.update();
        feed.update();

        // Intake System:
        if (stage == 1) { // Intake note until detected
            feed.set(0.15);
            if (iseenote()) {
                feed.set(0);
                stage = 2;
            }
        } else if (stage == 2) { // Bring note to exact launch position
            feed.set(0.07);
            if (!iseenote()) { // Stop intake (finish process)
                feed.set(0);
                stage = 0;
                holdingNote = true;
            }
        }

        // Launch System:
        if (stage == 11) { // Pull note in
            feed.goTo(feed.getEnc() - 0.5535);
            if (launchTimer.get() > 100) {
                stage = 12;
            }
        }
        if (stage == 12) { // Fire up thrusters
            leftThruster.set(2);
            rightThruster.set(0.8);
            if (launchTimer.get() > 1100) {
                stage = 13;
            }
        }
        if (stage == 13) { // Push note into thrusters
            feed.set(2);
            leftThruster.set(2);
            rightThruster.set(0.8);
            if (launchTimer.get() > 1400) { // Stop launcher (finish process)
                feed.set(0);
                leftThruster.set(0);
                rightThruster.set(0);
                stage = 0;
                holdingNote = false;
                aim(pos.intake);
            }
        }

        // Amp Launch:
        if (stage == 21) { // Angle to amp score position
            aimMotor.goTo(pos.amp);
            if (aimMotor.almost()) {
                stage = 11;
                launchTimer.reset();
            }
        }

        // Aim and Launch:
        if (stage == 31) { // Pull back note
            feed.goTo(feed.getEnc() - 0.5535);
            if (launchTimer.get() > 100) {
                stage = 32;
            }
        }
        if (stage == 32) { // Fire up thrusters and wait for camera to start
            leftThruster.set(2);
            rightThruster.set(0.8);
            if (launchTimer.get() > 1100 && cam.activate()) {
                stage = 33;
            }
        }
        if (stage == 33) { // Wait until shot is possible (Tag in view and close enough)
            leftThruster.set(2);
            rightThruster.set(0.8);
            if (cam.area() > 1) {
                stage = 13;
            }
        }

    }

}
