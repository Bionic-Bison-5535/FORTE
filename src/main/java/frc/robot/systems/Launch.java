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
    private boolean prepping = false;
    public boolean holdingNote = false;
    private double targetWidth;

    /** Encoder-based positions for launcher to go to */
    public class pos {
        public static double min = -21;
        public static double max = 120;
        /** Intake position */
        public static double intake = 20;
        /** Position for scoring in amp */
        public static double amp = 88.14;
        /** Position for scoring in speaker while pressed up against subwoofer */
        public static double closeup = 15;
        /** Position to go to before climbing */
        public static double climbPrep = 95;
        /** Position to go to to climb (when chain is under launcher) */
        public static double climb = 55;

        private static double smartPosVal;
        private static double previousLimelightY;
        public static double smartAim_b = 40.35;
        public static double smartAim_m = -0.97;
        /** Function to calculate encoder position based on Limelight camera input */
        public static double smartAim(double limelightY, boolean moving) {
            if (moving) { // Use previous position to predict future
                smartPosVal = smartAim_b + smartAim_m * (2*limelightY - previousLimelightY);
            } else {
                smartPosVal = smartAim_b + smartAim_m * limelightY;
            }
            previousLimelightY = limelightY;
            return smartPosVal;
        }
    }

	public Launch(Motor LaunchLeftMotor, Motor LaunchRightMotor, Motor FeedMotor, Motor AimMotor, int aimCoderID, int sensorPin, Limelight Cam) {
        note = new DigitalInput(sensorPin);
		leftThruster = LaunchLeftMotor;
        rightThruster = LaunchRightMotor;
        feed = FeedMotor;
        aimMotor = AimMotor;
        aimCoder = new CANcoder(aimCoderID);
        aimMotor.setEnc(aimCoder.getPosition().getValue()*182);
        aimMotor.goTo(pos.intake);
        feed.pwr = 3;
        cam = Cam;
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

    public void prepClimb() {
        aim(pos.climbPrep);
    }

    public void climb() {
        aim(pos.climb);
    }

    public boolean iseenote() {
        return !note.get();
    }

    public void intake() {
        if (stage == 0) {
            if (frc.robot.Robot.sensorError) {
                feed.set(0.15);
                leftThruster.set(-0.05);
                rightThruster.set(-0.05);
            } else {
			    stage = 1;
                aimMotor.goTo(pos.intake);
            }
		}
    }

    public void stop() {
        stage = 0;
        feed.set(0);
        leftThruster.set(0);
        rightThruster.set(0);
        aim(pos.intake);
    }

    /** Fires up thrusters and aims */
    public void LAUNCHprep() {
        cam.activate();
        stage = 31;
        prepping = true;
    }

    /** LAUNCH (officially) after "LAUNCHprep" function called */
    public void LAUNCH() {
        prepping = false;
    }

    /** Starts automatic launch sequence */
    public void LAUNCHstart() {
        launchTimer.reset();
        stage = 11;
        prepping = false;
    }

    /** Aims and then begins automatic launch sequence */
    public void aimAndLAUNCH() {
        stage = 31;
        cam.activate();
        prepping = false;
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
            leftThruster.set(-0.05);
            rightThruster.set(-0.05);
            if (iseenote()) {
                feed.set(0);
                leftThruster.set(0);
                rightThruster.set(0);
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
            feed.set(-0.08);
            if (iseenote() || frc.robot.Robot.sensorError) {
                stage = 12;
                launchTimer.reset();
            }
        }
        if (stage == 12) { // Pull note in further
            feed.set(-0.08);
            if (launchTimer.get() > 50) {
                stage = 13;
                feed.goTo(feed.getEnc());
            }
        }
        if (stage == 13) { // Fire up thrusters
            leftThruster.set(0.8);
            rightThruster.set(2);
            if (launchTimer.get() > 1100) {
                stage = 14;
            }
        }
        if (stage == 14) { // Push note into thrusters
            feed.set(2);
            leftThruster.set(0.8);
            rightThruster.set(2);
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
        if (stage == 31) { // Pull note in
            feed.set(-0.08);
            if (iseenote() || frc.robot.Robot.sensorError) {
                stage = 32;
                launchTimer.reset();
            }
        }
        if (stage == 32) { // Pull note in further
            feed.set(-0.08);
            if (launchTimer.get() > 50) {
                stage = 33;
                feed.goTo(feed.getEnc());
            }
        }
        if (stage == 33) { // Fire up thrusters and wait for camera to start
            leftThruster.set(0.8);
            rightThruster.set(2);
            if (launchTimer.get() > 1100 && cam.pipelineActivated()) {
                stage = 34;
            }
        }
        if (stage == 34) { // Wait until shot is possible (Tag in view, close enough, and horizontally aligned)
            leftThruster.set(0.8);
            rightThruster.set(2);
            if (cam.area() > 0.16) {
                targetWidth = 3*cam.width();
                if (-targetWidth < cam.X() && cam.X() < targetWidth) {
                    stage = 35;
                    aim(pos.smartAim(cam.Y(), false));
                }
            }
        }
        if (stage == 35) { // Aim
            leftThruster.set(0.8);
            rightThruster.set(2);
            aim(pos.smartAim(cam.Y(), true));
            if (aimMotor.almost() && !prepping) {
                stage = 14;
                launchTimer.set(1100);
            }
        }

    }

}
