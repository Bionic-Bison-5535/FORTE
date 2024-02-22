package frc.robot;

import java.lang.Math;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
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

    boolean intaking = false;

    @Override
    public void robotInit() {
        navx.reset();
        noteDropdown.setDefaultOption("None", "0");
        noteDropdown.addOption("Left Note", "1");
        noteDropdown.addOption("Center Note", "2");
        noteDropdown.addOption("Right Note", "3");
        SmartDashboard.putData("Which Note To Get?", noteDropdown);
        getMoreDropdown.setDefaultOption("Yes", "y");
        getMoreDropdown.addOption("No", "n");
        getMoreDropdown.addOption("Only Collect", "c");
        SmartDashboard.putData("Get More Notes?", getMoreDropdown);
        SmartDashboard.putNumber("A Offset", go.A_offset);
        SmartDashboard.putNumber("B Offset", go.B_offset);
        SmartDashboard.putNumber("C Offset", go.C_offset);
        SmartDashboard.putNumber("D Offset", go.D_offset);
    }

    @Override
    public void robotPeriodic() {
        SmartDashboard.putString("Teleop Mode", mode);
    }

    @Override
    public void autonomousInit() {
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

    @Override
    public void teleopPeriodic() {

        // Main Code:
        if (mode == "raw") {
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(4), 3), 0);
            if (c1.start() || c2.start()) {
                mode = "smart";
            }
            aimMotor.goToPos += 3*Math.pow(c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3), 3);
            if (c1.b() || c2.b()) {
                intaking = false;
                launcher.stopIntake();
            }
            if (launcher.stage == 0) {
                go.unlock();
                if (c1.onPress(Controls.A) || c2.onPress(Controls.A)) {
                    intaking = true;
                    launcher.intake();
                }
                if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) {
                    go.lock();
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
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(4), 3), navx.yaw());
            if (c1.back() || c2.back()) {
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) {
                mode = "auto";
            }
            if ((c1.right_stick() && c1.start()) || (c2.right_stick() && c2.start())) {
                navx.zeroYaw();
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
            in.set(0.7);
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

        aimMotor.posMode = true;
    }

    @Override
    public void testPeriodic() {
        aimMotor.goToPos -= c1.stick(5);
        aimMotor.update();
        SmartDashboard.putNumber("Aim Value", launcher.aimPos());
    }

}