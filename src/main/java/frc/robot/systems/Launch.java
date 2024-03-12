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
    public boolean prepping = false;
    public boolean holdingNote = false;
    private double targetWidth;
    private double nowX, previousX;
    public double offset = 0;
    public static final double pullback = 12;
    public static final double loopsToLaunch = 25;

    /** Encoder-based positions for launcher to go to */
    public class pos {
        public static double min = -41;
        public static double max = 132;
        /** Intake position */
        public static double intake = 0;
        /** Position for scoring in amp */
        public static double amp = 68;
        /** Position for scoring in speaker while pressed up against subwoofer */
        public static double closeup = -8;
        /** Position to go to before climbing */
        public static double climbPrep = 75;
        /** Position to go to to climb (when chain is under launcher) */
        public static double climb = 7;

        private static double smartPosVal;
        private static double previousLimelightY;
        public static double smartAim_offset = -5;
        /** Function to calculate necessary aim encoder position based on Limelight camera input */
        public static double smartAim(double limelightY, boolean moving) {
            if (moving) { // Use previous position to predict future
                smartPosVal = 10 - Math.pow(limelightY + loopsToLaunch*(limelightY - previousLimelightY), 2)/34 - smartAim_offset;
            } else {
                smartPosVal = 10 - Math.pow(limelightY, 2)/34 - smartAim_offset;
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
        //aimMotor.setEnc((aimCoder.getAbsolutePosition().getValue())*182);
        aimMotor.setEnc(0);
        aim(pos.intake);
        feed.pwr = 1.87;
        cam = Cam;
	}

    public double aimPos() {
        return aimMotor.getEnc() - offset;
    }

    public void aim(double encValue) {
        if (encValue < pos.min) {
            aimMotor.goTo(pos.min - offset);
        } else if (encValue > pos.max) {
            aimMotor.goTo(pos.max - offset);
        } else {
            aimMotor.goTo(encValue - offset);
        }
    }

    public void changeAim(double changeInEncValue) {
        aim(aimMotor.goToPos + changeInEncValue + offset);
    }

    public void prepClimb() {
        aim(pos.climbPrep);
    }

    public void climb() {
        aimMotor.maxSpeed = 0.5;
        aim(pos.climb);
    }

    public boolean iseenote() {
        return !note.get();
    }

    public void intake() {
        stage = 1;
        aimMotor.maxSpeed = 1;
        aim(pos.intake);
    }

    public void stop() {
        stage = 0;
        feed.set(0);
        leftThruster.set(0);
        rightThruster.set(0);
        aim(pos.intake);
        prepping = false;
    }

    /** Fires up thrusters and aims */
    public void LAUNCHprep() {
        cam.activate();
        stage = 31;
        prepping = true;
    }

    /** Fires up thrusters */
    public void LAUNCHprep_noCam() {
        stage = 11;
        prepping = true;
    }

    /** LAUNCH (officially) after "LAUNCHprep" or "LAUNCHprep_noCam" function called */
    public void LAUNCH() {
        prepping = false;
    }

    /** Starts automatic launch sequence without aim */
    public void LAUNCHstart() {
        launchTimer.reset();
        stage = 11;
        prepping = false;
    }

    /** Starts automatic launch sequence with aim */
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
            if (iseenote()) {
                stage = 12;
                launchTimer.reset();
            }
        }
        if (stage == 12) { // Pull note in further
            feed.set(-0.05);
            if (launchTimer.get() > pullback) {
                stage = 13;
                feed.goTo(feed.getEnc());
            }
        }
        if (stage == 13) { // Fire up thrusters
            leftThruster.set(1);
            rightThruster.set(1);
            if (launchTimer.get() > 1100 && !prepping) {
                stage = 14;
                launchTimer.reset();
            }
        }
        if (stage == 14) { // Push note into thrusters
            feed.set(2);
            leftThruster.set(1);
            rightThruster.set(1);
            if (launchTimer.get() > 300) { // Stop launcher (finish process)
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
            aim(pos.amp);
            if (aimMotor.almost()) {
                stage = 11;
                launchTimer.reset();
            }
        }

        // Aim and Launch:
        if (stage == 31) { // Pull note in
            feed.set(-0.08);
            if (iseenote()) {
                stage = 32;
                launchTimer.reset();
            }
        }
        if (stage == 32) { // Pull note in further
            feed.set(-0.05);
            if (launchTimer.get() > pullback) {
                stage = 33;
                feed.goTo(feed.getEnc());
            }
        }
        if (stage == 33) { // Fire up thrusters and wait for camera to start
            leftThruster.set(1);
            rightThruster.set(1);
            if (cam.pipelineActivated()) {
                stage = 34;
                previousX = cam.X();
            }
        }
        if (stage == 34) { // Wait until shot is possible (Tag in view, close enough, and horizontally aligned)
            leftThruster.set(1);
            rightThruster.set(1);
            if (cam.area() > 0.16) {
                targetWidth = 3*cam.width();
                nowX = cam.X();
                if (-targetWidth < (nowX + loopsToLaunch*(nowX - previousX)) && loopsToLaunch*(nowX + (nowX - previousX)) < targetWidth) {
                    stage = 35;
                    aim(pos.smartAim(cam.Y(), false));
                    launchTimer.reset();
                }
                previousX = nowX;
            }
        }
        if (stage == 35) { // Aim
            leftThruster.set(1);
            rightThruster.set(1);
            if (cam.valid()) {
                aim(pos.smartAim(cam.Y(), true));
                if (!prepping && launchTimer.get() > 1000) {
                    stage = 14;
                    launchTimer.reset();
                }
            }
        }

    }

}