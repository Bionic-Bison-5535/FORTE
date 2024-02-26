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
    boolean intaking = false;
    boolean actualMatch = false;

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
    DigitalInput iseenote = new DigitalInput(2);
    Limelight speaker, speaker2, ampCam;
    Launch launcher;
    Limelight posCam = new Limelight(1);

    @Override
    public void robotInit() {
        Limelight.enableLimelightUSB();
        navx.reset();
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
        SmartDashboard.putBoolean("Alliance", leds.blueAlliance);
        SmartDashboard.putString("Event", DriverStation.getEventName());
        SmartDashboard.putNumber("Match", DriverStation.getMatchNumber());
        if (leds.blueAlliance) {
            speaker = new Limelight(3);
            speaker2 = new Limelight(5);
            ampCam = new Limelight(7);
        } else {
            speaker = new Limelight(4);
            speaker2 = new Limelight(6);
            ampCam = new Limelight(8);
        }
        launcher = new Launch(leftThruster, rightThruster, feedMotor, aimMotor, 25, 1, speaker);
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
        actualMatch = true;
        noteToGet = noteDropdown.getSelected();
        getMoreNotes = getMoreDropdown.getSelected();
        matchTimer.reset();
        leds.orange();
    }

    @Override
    public void autonomousPeriodic() {}

    @Override
    public void teleopInit() {
        matchTimer.set(15000);
        c1.refreshController();
        c2.refreshController();
    }

    double temporary = 0;
    @Override
    public void teleopPeriodic() {

        // Main Code:
        if (mode == "raw") {
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(4), 3), 0);
            if (c1.start() || c2.start()) {
                mode = "smart";
            }
            if (c1.stick(2) + c1.stick(3) != 0) {
                launcher.changeAim(3*Math.pow(c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3), 3));
                temporary = launcher.aimPos();
            } else if (c1.x() || c2.x()) {
                launcher.aim(Launch.pos.closeup);
            }
            SmartDashboard.putNumber("Tag Y", speaker.Y());
            SmartDashboard.putNumber("Aim Set", temporary);
            if (c1.b() || c2.b()) {
                intaking = false;
                launcher.stop();
            }
            if (launcher.stage == 0) {
                go.unlock();
                intaking = false;
                if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) {
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
                go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(4), 3), navx.yaw()+180);
            }
            if (c1.back() || c2.back()) {
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) {
                mode = "auto";
            }
            if ((c1.right_stick() && c1.start()) || (c2.right_stick() && c2.start())) {
                navx.zeroYaw();
            }
            if (c1.b() || c2.b()) {
                intaking = false;
                launcher.stop();
            }
            if (c1.stick(5) > 0.95 || c2.stick(5) > 0.95) {
                launcher.prepClimb();
            } else if (c1.stick(5) < -0.95 || c2.stick(5) < -0.95) {
                launcher.climb();
            }
            if (launcher.stage == 0) {
                intaking = false;
                if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) {
                    intaking = true;
                    launcher.intake();
                }
                if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) {
                    launcher.aimAndLAUNCH();
                }
                if (c1.onPress(Controls.RIGHT) || c2.onPress(Controls.RIGHT)) {
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
        } else if (actualMatch && matchTimer.get() >= 150000) { // Final Countdown
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
            if (actualMatch) {
                in.set(0.4 + Math.pow(matchTimer.get()/1000-1500,3)/3000000);
            } else {
                in.set(0.45);
            }
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
    public void testPeriodic() {}

}