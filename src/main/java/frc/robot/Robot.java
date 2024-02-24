package frc.robot;

import java.lang.Math;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.systems.*;

public class Robot extends TimedRobot {

    /**
     * Mode for robot during teleop.
     * Can be "raw", "smart", or "auto".
     */
    String mode = "smart";

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    String noteToGet, getMoreNotes;

    Wesswerve go = new Wesswerve(14, 15, 16, 17, 20, 21, 22, 23, 10, 11, 12, 13, 358, 225, 159, 250);
    Controls c1 = new Controls(0, 0.1);
    Controls c2 = new Controls(1, 0.1);
    Tim matchTimer = new Tim();
    Navx navx = new Navx();
    Lights leds = new Lights(30);
    Motor in = new Motor(5, true, true, 1);
    Motor aimMotor = new Motor(24, false, false, 1);
    Motor rightThruster = new Motor(7, false, false, 1);
    Motor leftThruster = new Motor(8, false, true, 1);
    Motor feedMotor = new Motor(9, false, false, 1);
    Launch launcher = new Launch(leftThruster, rightThruster, feedMotor, aimMotor, 25, 1);
    DigitalInput iseenote = new DigitalInput(2);
    Limelight speaker, speaker2, ampCam;
    Limelight posCam = new Limelight(1);

    /** Whether or not the intake is running */
    boolean intaking = false;
    /** The desired yaw angle of the robot */
    double dir = 0;
    /** To be used for finding the nearest coterminal angle when a POV button is pressed */
    double newAngle;

    @Override
    public void robotInit() {
        Limelight.enableLimelightUSB();
        navx.reset();
        navx.yaw_Offset += 180;
        noteDropdown.setDefaultOption("None", "0");
        noteDropdown.addOption("Left", "1");
        noteDropdown.addOption("Center", "2");
        noteDropdown.addOption("Right", "3");
        SmartDashboard.putData("Which Note To Get?", noteDropdown);
        getMoreDropdown.setDefaultOption("Yes", "y");
        getMoreDropdown.addOption("No", "n");
        getMoreDropdown.addOption("Only Collect", "c");
        SmartDashboard.putData("Get More Notes?", getMoreDropdown);
        SmartDashboard.putNumber("A Offset", go.A_offset);
        SmartDashboard.putNumber("B Offset", go.B_offset);
        SmartDashboard.putNumber("C Offset", go.C_offset);
        SmartDashboard.putNumber("D Offset", go.D_offset);
        if (leds.blueAlliance) {
            speaker = new Limelight(3);
            speaker2 = new Limelight(5);
            ampCam = new Limelight(7);
        } else {
            speaker = new Limelight(4);
            speaker2 = new Limelight(6);
            ampCam = new Limelight(8);
        }
    }

    @Override
    public void robotPeriodic() {
        SmartDashboard.putString("Teleop Mode", mode);
        SmartDashboard.putNumber("Aim Pos", launcher.aimPos());
        SmartDashboard.putNumber("Timer", Math.floor(matchTimer.get()/1000));
        SmartDashboard.putNumber("Internal Robot Celsius Temeprature", Math.round(navx.celsius()));
        SmartDashboard.putNumber("Yaw Angle", navx.coterminalYaw());
        SmartDashboard.putNumber("Speed", navx.velocity());
        SmartDashboard.putBoolean("I See Note", !iseenote.get());
        SmartDashboard.putBoolean("Note in Launcher Detected", launcher.iseenote());
        SmartDashboard.putNumber("Launcher Stage", launcher.stage);
        go.A_offset = SmartDashboard.getNumber("A Offset", go.A_offset);
        go.B_offset = SmartDashboard.getNumber("B Offset", go.B_offset);
        go.C_offset = SmartDashboard.getNumber("C Offset", go.C_offset);
        go.D_offset = SmartDashboard.getNumber("D Offset", go.D_offset);
    }

    @Override
    public void autonomousInit() {
        noteToGet = noteDropdown.getSelected();
        getMoreNotes = getMoreDropdown.getSelected();
        matchTimer.reset();
        leds.orange();
        dir = navx.yaw();
    }

    @Override
    public void autonomousPeriodic() {}

    @Override
    public void teleopInit() {
        matchTimer.set(15000);
        c1.refreshController();
        c2.refreshController();
    }

    @Override
    public void teleopPeriodic() {

        // Main Code:
        if (mode == "raw") {
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(4), 3), 0);
            if (c1.start() || c2.start()) {
                mode = "smart";
                dir = navx.yaw();
            }
            launcher.changeAim(3*Math.pow(c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3), 3));
            if (c1.b() || c2.b()) {
                intaking = false;
                launcher.stopIntake();
            }
            if (launcher.stage == 0) {
                go.unlock();
                intaking = false;
                if (c1.onPress(Controls.A) || c2.onPress(Controls.A)) {
                    intaking = true;
                    launcher.intake();
                }
                if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) {
                    go.lock();
                    go.update();
                    launcher.LAUNCHstart();
                }
                if (c1.right() || c2.right()) {
                    launcher.LAUNCHprep();
                }
                if (c1.onRelease(Controls.RIGHT) || c2.onRelease(Controls.RIGHT)) {
                    launcher.LAUNCH();
                }
                if (c1.onPress(Controls.Y) || c2.onPress(Controls.Y)) {
                    launcher.amp();
                }
                if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) {
                    launcher.aim(Launch.pos.closeup);
                }
            }
        } else if (mode == "smart") {
            if (c1.left_stick() || c2.left_stick()) {
                go.lock();
            } else {
                go.unlock();
                if (c1.pov() != -1) {
                    newAngle = (double)(c1.pov() + 180);
                    while (newAngle > dir+180) { newAngle -= 360; }
                    while (newAngle < dir-180) { newAngle += 360; }
                    dir = newAngle;
                } else if (c2.pov() != -1) {
                    newAngle = (double)(c2.pov() + 180);
                    while (newAngle > dir+180) { newAngle -= 360; }
                    while (newAngle < dir-180) { newAngle += 360; }
                    dir = newAngle;
                }
                dir += 4 * (Math.pow(c1.stick(4), 3) + Math.pow(c2.stick(4), 3));
                go.swerve(
                    Math.pow(c1.stick(1), 3) + Math.pow(c2.stick(1), 3),
                    Math.pow(c1.stick(0), 3) + Math.pow(c2.stick(0), 3),
                    -0.02*(navx.yaw()-dir)*(2*Math.abs(c1.magnitude()+c2.magnitude())+1),
                    navx.yaw()
                );
            }
            if (c1.back() || c2.back()) {
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) {
                mode = "auto";
            }
            if ((c1.right_stick() && c1.start()) || (c2.right_stick() && c2.start())) {
                navx.zeroYaw();
                dir = 0;
            }
            if (c1.b() || c2.b()) {
                intaking = false;
                launcher.stopIntake();
            }
            if (launcher.stage == 0) {
                intaking = false;
                if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) {
                    intaking = true;
                    launcher.intake();
                }
                if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) {
                    launcher.LAUNCHstart();
                }
                if (c1.right() || c2.right()) {
                    launcher.LAUNCHprep();
                }
                if (c1.onRelease(Controls.RIGHT) || c2.onRelease(Controls.RIGHT)) {
                    launcher.LAUNCH();
                }
                if (c1.onPress(Controls.Y) || c2.onPress(Controls.Y)) {
                    launcher.amp();
                }
            }
        } else if (mode == "auto") {
            if (c1.back() || c2.back()) {
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X) || c1.active() || c2.active()) {
                mode = "smart";
            }
            autonomousPeriodic();
        }

        // LED Strip Color:
        if (mode == "auto") {
            leds.orange();
        } else if (DriverStation.getMatchTime() < 20) { // Final Countdown
            leds.turquoise();
        } else if (launcher.stage > 0 && launcher.stage < 10) { // Intaking Note
            leds.yellow();
        } else {
            leds.allianceColor();
        }

        // Update Systems:
        go.update();
        launcher.update();
        if (intaking) {
            in.set(0.4);
        } else {
            in.set(0);
        }

    }

    @Override
    public void disabledInit() {
        leds.green();
    }

    @Override
    public void disabledPeriodic() {}

    @Override
    public void testInit() {
        matchTimer.reset();
        leds.white();
    }

    @Override
    public void testPeriodic() {
        aimMotor.goTo(SmartDashboard.getNumber("Aim Pos", aimMotor.goToPos));
        SmartDashboard.putNumber("Area_April", posCam.area());
        SmartDashboard.putNumber("Yaw_April", posCam.yaw());
    }

}