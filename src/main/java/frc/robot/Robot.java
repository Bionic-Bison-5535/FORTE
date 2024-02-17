package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.systems.*;

public class Robot extends TimedRobot {

    /**
     * Mode for robot during teleop.
     * Can be "raw", "normal", or "auto".
     */
    String mode = "raw";

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    String noteToGet, getMoreNotes;
    
    Wesswerve go = new Wesswerve(14, 15, 16, 17, 20, 21, 22, 23, 10, 11, 12, 13, 0, 0, 0, 0);
    Controls c1 = new Controls(0, 0.1);
    Controls c2 = new Controls(1, 0.1);
    Tim matchTimer = new Tim();
    

    @Override
    public void robotInit() {
        noteDropdown.setDefaultOption("None", "0");
        noteDropdown.addOption("Left Note", "1");
        noteDropdown.addOption("Center Note", "2");
        noteDropdown.addOption("Right Note", "3");
        SmartDashboard.putData("Which Note To Get?", noteDropdown);
        getMoreDropdown.setDefaultOption("Yes", "y");
        getMoreDropdown.addOption("No", "n");
        getMoreDropdown.addOption("Only Collect", "c");
        SmartDashboard.putData("Get More Notes?", getMoreDropdown);
    }

    @Override
    public void robotPeriodic() {}

    @Override
    public void autonomousInit() {
        noteToGet = noteDropdown.getSelected();
        getMoreNotes = getMoreDropdown.getSelected();
        matchTimer.reset();
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
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(), 3), 0);
        }
    }

    @Override
    public void disabledInit() {}

    @Override
    public void disabledPeriodic() {}

    @Override
    public void testInit() {
        matchTimer.reset();
    }

    @Override
    public void testPeriodic() {}

}
