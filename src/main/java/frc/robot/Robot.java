package frc.robot;

import java.lang.Math;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.systems.Tim;
import frc.robot.systems.Controls;
import frc.robot.systems.Launch;
import frc.robot.systems.Lights;
import frc.robot.systems.Limelight;
import frc.robot.systems.Motor;
import frc.robot.systems.Navx;
import frc.robot.systems.POF;
import frc.robot.systems.Wesswerve;

public class Robot extends TimedRobot {

    /**
     * Mode for robot during teleop.
     * Can be "raw" or "smart".
     */
    String mode = "smart";
    boolean intaking = false;
    boolean actualMatch = false;
    double dir = 0;
    double newAngle;
    int autoStage = 0;
    boolean conscious = true;
    boolean wasDisabled = false;
    double sensitivity = 5;

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    String noteToGet, getMoreNotes;

    Wesswerve go = new Wesswerve(14, 15, 16, 17, 20, 21, 22, 23, 10, 11, 12, 13, 358, 225, 159, 250);
    Controls c1 = new Controls(0, 0.1);
    Controls c2 = new Controls(1, 0.1);
    Tim matchTimer, Alec;
    Navx navx = new Navx();
    Lights leds = new Lights(30);
    Motor in = new Motor(5, true, true, 1);
    Motor aimMotor = new Motor(24, false, false, 1);
    Motor rightThruster = new Motor(7, false, false, 1);
    Motor leftThruster = new Motor(8, false, true, 1);
    Motor feedMotor = new Motor(9, false, false, 1);
    DigitalInput iseenote = new DigitalInput(2);
    Launch launcher;
    Limelight speaker, speaker2, ampCam;
    Limelight posCam = new Limelight(1);
    POF pof = new POF(posCam);

    double keepInRange(double number, double floor, double ceiling) {
        if (number >= floor && number <= ceiling) {
            return number;
        } else if (number < floor) {
            return floor;
        } else {
            return ceiling;
        }
    }

    @Override
    public void robotInit() {
        matchTimer = new Tim();
        Alec = new Tim();
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
        SmartDashboard.putBoolean("Alliance", leds.blueAlliance);
        SmartDashboard.putString("Event", DriverStation.getEventName());
        SmartDashboard.putNumber("Match", DriverStation.getMatchNumber());
        noteDropdown.setDefaultOption("Center", "2");
        noteDropdown.addOption("Left", "1");
        noteDropdown.addOption("None-Left", "0");
        noteDropdown.addOption("Right", "3");
        SmartDashboard.putData("Which Note To Get?", noteDropdown);
        getMoreDropdown.setDefaultOption("No", "n");
        getMoreDropdown.addOption("Yes", "y");
        getMoreDropdown.addOption("Only Collect", "c");
        SmartDashboard.putData("Get More Notes?", getMoreDropdown);
        SmartDashboard.putNumber("General Aim Offset", launcher.offset);
        SmartDashboard.putNumber("A Offset", go.A_offset);
        SmartDashboard.putNumber("B Offset", go.B_offset);
        SmartDashboard.putNumber("C Offset", go.C_offset);
        SmartDashboard.putNumber("D Offset", go.D_offset);
        SmartDashboard.putBoolean("Alliance", leds.blueAlliance);
        SmartDashboard.putString("Event", DriverStation.getEventName());
        SmartDashboard.putNumber("Match", DriverStation.getMatchNumber());
        SmartDashboard.putNumber("Smart Aim Offset", Launch.pos.smartAim_offset);
        SmartDashboard.putNumber("General Aim Offset", launcher.offset);
        SmartDashboard.putBoolean("Consciousness", conscious);
        navx.reset();
    }

    @Override
    public void robotPeriodic() {
        SmartDashboard.putString("Teleop Mode", mode);
        SmartDashboard.putNumber("Aim Pos", launcher.aimPos());
        SmartDashboard.putNumber("Limelight Y", Limelight.Y_());
        SmartDashboard.putNumber("Timer", Math.floor(matchTimer.get()/1000));
        SmartDashboard.putNumber("Yaw Angle", navx.coterminalYaw());
        SmartDashboard.putNumber("Speed", navx.velocity());
        SmartDashboard.putBoolean("I See Note", !iseenote.get());
        SmartDashboard.putBoolean("Note in Launcher Detected", launcher.iseenote());
        SmartDashboard.putNumber("Launcher Stage", launcher.stage);
        go.A_offset = SmartDashboard.getNumber("A Offset", go.A_offset);
        go.B_offset = SmartDashboard.getNumber("B Offset", go.B_offset);
        go.C_offset = SmartDashboard.getNumber("C Offset", go.C_offset);
        go.D_offset = SmartDashboard.getNumber("D Offset", go.D_offset);
        Launch.pos.smartAim_offset = SmartDashboard.getNumber("Smart Aim Offset", Launch.pos.smartAim_offset);
        launcher.offset = SmartDashboard.getNumber("General Aim Offset", launcher.offset);
        conscious = SmartDashboard.getBoolean("Consciousness", conscious);
        SmartDashboard.putNumber("DriveTempA", go.frontLeftDrive.getDeviceTemp().getValue());
        SmartDashboard.putNumber("DriveTempB", go.frontRightDrive.getDeviceTemp().getValue());
        SmartDashboard.putNumber("DriveTempC", go.backLeftDrive.getDeviceTemp().getValue());
        SmartDashboard.putNumber("DriveTempD", go.backRightDrive.getDeviceTemp().getValue());
    }

    @Override
    public void autonomousInit() {
        actualMatch = true;
        noteToGet = noteDropdown.getSelected();
        getMoreNotes = getMoreDropdown.getSelected();
        matchTimer.reset();
        if (noteToGet == "1" || noteToGet == "0") {
            navx.yaw_Offset += 60;
        } else if (noteToGet == "3") {
            navx.yaw_Offset -= 60;
        }
        dir = navx.yaw();
        launcher.holdingNote = true;
        intaking = false;
        autoStage = 0;
        Alec.reset();
    }

    @Override
    public void autonomousPeriodic() {
        if (autoStage == 0) {
            speaker.activate();
            if (Alec.get() > 1000) {
                autoStage = 1;
                launcher.aim(Launch.pos.closeup);
                launcher.LAUNCHstart();
            }
        }
        if (autoStage == 1) {
            if (launcher.holdingNote == false) {
                if (noteToGet == "0") {
                    autoStage = 8;
                } else {
                    autoStage = 2;
                    Alec.reset();
                }
            }
        }
        if (autoStage == 2) {
            if (noteToGet == "2") {
                go.swerve(0.4, -speaker.X()/20, 0, navx.yaw() + 180);
            } else if (noteToGet == "1") {
                go.swerve(0.3*0.65, -0.3, 0, 0);
            } else if (noteToGet == "3") {
                go.swerve(0.3*0.65, 0.3, 0, 0);
            }
            if (!iseenote.get()) {
                autoStage = 3;
                launcher.intake();
                intaking = true;
                Alec.reset();
            } else if (Alec.get() > 1700) {
                autoStage = 8;
            }
        }
        if (autoStage == 3) {
            go.swerve(-0.12, 0, 0, 0);
            if (launcher.holdingNote) {
                launcher.aimAndLAUNCH();
                autoStage = 4;
            } else if (Alec.get() > 1700) {
                autoStage = 8;
            }
        }
        if (autoStage == 4) {
            if (noteToGet == "2" || speaker.valid()) {
                go.swerve(0.2, 0, speaker.X()/40, 0);
            } else if (noteToGet == "1") {
                go.swerve(-0.1, 0, 0.2, 0);
            } else if (noteToGet == "3") {
                go.swerve(-0.1, 0, -0.2, 0);
            }
            if (!launcher.holdingNote) {
                autoStage = 8;
            }
        }
        if (autoStage == 8) {
            go.swerve(0, 0, 0, 0);
            intaking = false;
            autoStage = 9;
        }
        if (autoStage == 9) {
            //Do nothing
        }
        launcher.update();
        go.update();
        launcher.update();
        if (intaking) {
            in.set(0.5);
        } else {
            in.set(0);
        }
        // LED Strip Color:
        if (launcher.holdingNote) { // Holding Note
            leds.yellow();
        } else if (intaking) { // Getting Note
            leds.orange();
        } else {
            leds.allianceColor();
        }
    }

    @Override
    public void teleopInit() {
        matchTimer.set(15000);
        c1.refreshController();
        c2.refreshController();
        if (getMoreNotes != "c") {
            launcher.holdingNote = false;
        }
        if (!actualMatch) {
            dir = navx.yaw();
        }
        speaker.activate();
    }

    @Override
    public void teleopPeriodic() {

        if (c1.start() || c2.start()) { // Mode Change
            mode = "smart";
            dir = navx.yaw();
        } else if (c1.back() || c2.back()) {
            mode = "raw";
            dir = navx.yaw();
        }

        if (c2.y()) { // Turn in direction to launch in amp
            if (leds.blueAlliance) {
                newAngle = 90;
            } else {
                newAngle = -90;
            }
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c2.right() && conscious) {
            if (speaker.valid()) {
                dir = navx.yaw() + speaker.X()*0.7;
            } else {
                newAngle = 0;
                while (newAngle > dir + 180) { newAngle -= 360; }
                while (newAngle < dir - 180) { newAngle += 360; }
                dir = newAngle;
            }
        } else if (c1.pov() != -1) { // Controller 1 POV
            newAngle = (double)(c1.pov());
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c2.pov() != -1) { // Controller 2 POV
            newAngle = (double)(c2.pov());
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.y()) {
            newAngle = 0;
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.x()) {
            newAngle = 270;
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.b()) {
            newAngle = 90;
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.a()) {
            newAngle = 180;
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.left()) {
            newAngle = -60;
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.right()) {
            newAngle = 60;
            while (newAngle > dir + 180) { newAngle -= 360; }
            while (newAngle < dir - 180) { newAngle += 360; }
            dir = newAngle;
        } else if (c1.active() || c2.active()) { // Manual Rotation
            dir += 1.7 * (c1.stick(4) + c2.stick(4));
        }
        if (c1.left_stick() || c2.left_stick()) { // Turbo mode
            go.speed = go.default_speed;
        } else {
            go.speed = 0.7*go.default_speed;
        }
        go.swerve(
            c1.magnitudeWithRamp(),
            c1.direction(),
            keepInRange(-0.02 * (navx.yaw()-dir), -5, 5),
            navx.yaw() + 180
        );

        if (c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3) != 0) {
            launcher.changeAim(3*Math.pow(c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3), sensitivity));
        } else if (c2.x()) {
            launcher.aim(Launch.pos.closeup);
        } else if (c2.stick(5) < -0.95) {
            launcher.prepClimb();
        } else if (c2.stick(5) > 0.95) {
            launcher.climb();
        }
        if (c2.onRelease(Controls.LEFT) || c2.onRelease(Controls.RIGHT)) { // LAUNCH (Release held down button)
            launcher.LAUNCH();
        }
        if (c2.b()) { // Cancel Any Launcher Activity
            intaking = false;
            launcher.stop();
        } else if (launcher.stage == 0) { // If Launcher Not Doing Anything
            intaking = false;
        }
        if (c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) { // Intake
            intaking = true;
            launcher.intake();
        } else if (launcher.holdingNote) {
            if (c2.onPress(Controls.LEFT)) { // Basic Launch Preparation
                launcher.LAUNCHprep_noCam();
            } else if (c2.onPress(Controls.RIGHT)) { // Advanced Launch Preparation (Hold right button down)
                if (mode == "raw") {
                    launcher.aim(Launch.pos.closeup);
                    launcher.LAUNCHprep_noCam();
                } else {
                    launcher.LAUNCHprep();
                }
            } else if (c2.onRelease(Controls.Y)) { // Launch Into Amp
                launcher.amp();
            }
        }

        if (c2.right_stick() && c2.start()) { // NavX Calibration
            navx.zeroYaw();
            dir = 0;
        }

        if (actualMatch && matchTimer.get() >= 130000) { // Final Countdown!
            leds.turquoise();
        } else if (launcher.holdingNote) { // Holding Note
            leds.yellow();
        } else if (intaking) { // Getting Note
            leds.orange();
        } else {
            leds.allianceColor();
        }

        // Update Systems:
        go.update();
        launcher.update();
        if (c2.right_stick()) {
            in.set(-1);
        } else if (intaking) {
            in.set(0.5);
        } else {
            in.set(0);
        }

    }

    @Override
    public void disabledInit() {
        matchTimer.reset();
        if (wasDisabled) {
            leds.green();
        }
    }
    
    @Override
    public void disabledPeriodic() {
        if (!wasDisabled) {
            if (matchTimer.get() % 2000 > 1750) { // Blink Alliance Color every 2 seconds for 1/4 second
                leds.allianceColor();
            } else {
                leds.green();
            }
        }
    }

    @Override
    public void disabledExit() {
        wasDisabled = true;
    }

    @Override
    public void testInit() {
        matchTimer.reset();
        leds.white();
    }

    @Override
    public void testPeriodic() {
        leftThruster.set(1);
        rightThruster.set(1);
        feedMotor.set(1);
    }

}