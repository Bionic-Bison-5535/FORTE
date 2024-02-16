package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.systems.*;

public class Robot extends TimedRobot {

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    
    String noteToGet, getMoreNotes;

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
    }

    @Override
    public void autonomousPeriodic() {}

    @Override
    public void teleopInit() {}

    @Override
    public void teleopPeriodic() {}

    @Override
    public void disabledInit() {}

    @Override
    public void disabledPeriodic() {}

    @Override
    public void testInit() {}

    @Override
    public void testPeriodic() {}

}
