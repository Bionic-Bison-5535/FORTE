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
    String mode = "raw";

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    String noteToGet, getMoreNotes;
    
    Wesswerve go = new Wesswerve(14, 15, 16, 17, 20, 21, 22, 23, 10, 11, 12, 13, -316, 542, 564, 695);
    Controls c1 = new Controls(0, 0.1);
    Controls c2 = new Controls(1, 0.1);
    Tim matchTimer = new Tim();
    Navx navx = new Navx();
    Lights leds = new Lights(30);

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
        SmartDashboard.putNumber("↖ Offset", go.A_offset);
        SmartDashboard.putNumber("↗ Offset", go.B_offset);
        SmartDashboard.putNumber("↘ Offset", go.C_offset);
        SmartDashboard.putNumber("↙ Offset", go.D_offset);
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
    }

    @Override
    public void teleopPeriodic() {
        if (mode == "raw") {
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(5), 3), 0);
            if (c1.start() || c2.start()) {
                mode = "smart";
            }
        } else if (mode == "smart") {
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(5), 3), navx.yaw());
            if (c1.back() || c2.back()) {
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) {
                mode = "auto";
            }
        } else if (mode == "auto") {
            if (c1.back() || c2.back()) {
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) {
                mode = "smart";
            }
        }
        if (mode == "auto") {
            leds.orange();
        } else if (DriverStation.getMatchTime() < 20) {
            leds.turquoise();
        } else if (/*intaking note*/false) {
            leds.yellow();
        } else {
            leds.allianceColor();
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
        SmartDashboard.putNumber("A Offset", go.frontLeft.getPosition().getValue()*360);
        SmartDashboard.putNumber("B Offset", go.frontRight.getPosition().getValue()*360);
        SmartDashboard.putNumber("C Offset", go.backRight.getPosition().getValue()*360);
        SmartDashboard.putNumber("D Offset", go.backLeft.getPosition().getValue()*360);
    }

}