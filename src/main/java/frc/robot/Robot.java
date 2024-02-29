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
     * Can be "raw" or "smart".
     */
    String mode = "smart";
    boolean intaking = false;
    boolean actualMatch = false;
    double dir = 0;
    double newAngle;
    double launchOver = 9;
    int autoStage = 0;
    boolean verySmart = true;
    boolean conscious = true;

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    String noteToGet, getMoreNotes;

    Wesswerve go = new Wesswerve(14, 15, 16, 17, 20, 21, 22, 23, 10, 11, 12, 13, 358, 225, 159, 250);
    Controls c1 = new Controls(0, 0.1);
    Controls c2 = new Controls(1, 0.1);
    Tim matchTimer = new Tim();
    Tim Alec = new Tim();
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
        Limelight.enableLimelightUSB();
        navx.reset();
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
        noteDropdown.setDefaultOption("None", "0");
        noteDropdown.addOption("Left", "1");
        noteDropdown.addOption("Center", "2");
        noteDropdown.addOption("Right", "3");
        SmartDashboard.putData("Which Note To Get?", noteDropdown);
        getMoreDropdown.setDefaultOption("Yes", "y");
        getMoreDropdown.addOption("No", "n");
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
        SmartDashboard.putNumber("Launch Over", launchOver);
        SmartDashboard.putNumber("Smart Aim Offset", Launch.pos.smartAim_offset);
        SmartDashboard.putNumber("General Aim Offset", launcher.offset);
        SmartDashboard.putBoolean("Consciousness", conscious);
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
        launchOver = SmartDashboard.getNumber("Launch Over", launchOver);
        Launch.pos.smartAim_offset = SmartDashboard.getNumber("Smart Aim Offset", Launch.pos.smartAim_offset);
        launcher.offset = SmartDashboard.getNumber("General Aim Offset", launcher.offset);
        conscious = SmartDashboard.getBoolean("Consciousness", conscious);
    }

    @Override
    public void autonomousInit() {
        actualMatch = true;
        noteToGet = noteDropdown.getSelected();
        getMoreNotes = getMoreDropdown.getSelected();
        matchTimer.reset();
        leds.orange();
        dir = navx.yaw();
        launcher.holdingNote = true;
        intaking = false;
    }

    @Override
    public void autonomousPeriodic() {
        if (autoStage == 0) {
            autoStage = 1;
        }       
        if (autoStage == 1) {
            launcher.LAUNCHprep();
            autoStage = 2;
        }
        if (autoStage == 2) {
            if (speaker.valid()) {
                go.swerve(0, 0, speaker.X()/40, 0);
                if (speaker.X() > -5 &&  speaker.X() > 5); {
                    autoStage = 3;
                    go.swerve(0, 0, 0, 0);
                }
            } else {
                go.swerve(0, 0, 0.5535, 0);
            }
        }
        if (autoStage == 3) {
            launcher.LAUNCH();
            if (launcher.holdingNote == false) {
                autoStage = 4;
                Alec.reset();
            }
        }
        if (autoStage == 4) {
            go.swerve(-0.75, 0, 0, navx.yaw()+180);
            if (Alec.get() > 500) {
                autoStage = 5;
            }
        }
        if (autoStage == 5) {
            if (noteToGet != "0") {
                if (noteToGet == "1") {
                    pof.goTo(pof.note1);
                } else if (noteToGet == "2") {
                    pof.goTo(pof.note2);
                } else if (noteToGet == "3") {
                    pof.goTo(pof.note3);
                }
            }
            if (pof.there()) {
                autoStage = 6;
            }
        }
        if (autoStage == 6) {
            if (iseenote.get()) {
                go.swerve(-0.25, 0, 0, navx.yaw() + 180);
            } else {
                go.swerve(0, 0, 0, 0);
                intaking = true;
                launcher.intake();
                autoStage = 7;
            }
        }
        if (autoStage == 7) {
            if (launcher.stage == 0) {
                intaking = false;
                go.swerve(0.1, 0, 0, 0);
                autoStage = 8;
                launcher.aimAndLAUNCH();
            }
        }
        if (autoStage == 8) {
            go.swerve(0, 0, 0, 0);
            autoStage = 9;
        }
        if (autoStage == 9) {
            //Do nothing
        }
        launcher.update();
        go.update();
        launcher.update();
        if (intaking) {
            in.set(0.45);
        } else {
            in.set(0);
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
    }

    @Override
    public void teleopPeriodic() {

        // RAW MODE PERIODIC:
        if (mode == "raw") {
            go.swerve(Math.pow(c1.stick(1), 3), Math.pow(c1.stick(0), 3), Math.pow(c1.stick(4), 3), 0); // Drive
            if (c1.start() || c2.start()) { // Mode Change
                mode = "smart";
                dir = navx.yaw();
            } else if (c1.stick(2) + c1.stick(3) != 0) { // Adjust Launcher Aim
                launcher.changeAim(3*Math.pow(c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3), 3));
            } else if (c1.x() || c2.x()) {
                launcher.aim(Launch.pos.closeup);
            } else if (c1.stick(5) < -0.95 || c2.stick(5) < -0.95) {
                launcher.prepClimb();
            } else if (c1.stick(5) > 0.95 || c2.stick(5) > 0.95) {
                launcher.climb();
            }
            if (c1.onRelease(Controls.RIGHT) || c2.onRelease(Controls.RIGHT)) { // LAUNCH (Release held down right button)
                launcher.LAUNCH();
            }
            if (c1.b() || c2.b()) { // Cancel Any Launcher Activity
                intaking = false;
                launcher.stop();
            } else if (launcher.stage == 0) { // If Launcher Not Doing Anything
                intaking = false;
                if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) { // Intake
                    intaking = true;
                    launcher.intake();
                } else if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) { // Launch Sequence
                    go.lock();
                    go.update();
                    launcher.LAUNCHstart();
                } else if (c1.right() || c2.right()) { // Launch Preparation (Hold right button down)
                    launcher.LAUNCHprep();
                } else if (c1.onPress(Controls.Y) || c2.onPress(Controls.Y)) { // Launch into Amp
                    launcher.amp();
                }
            }
        
        // SMART MODE PERIODIC:
        } else if (mode == "smart") {
            if (conscious && verySmart && launcher.holdingNote) {
                if (speaker.valid()) {
                    dir = navx.yaw() + speaker.X()*0.5;
                } else if (navx.coterminalYaw() < -45 || navx.coterminalYaw() > 45) {
                    newAngle = 0;
                    while (newAngle > dir + 180) { newAngle -= 360; }
                    while (newAngle < dir - 180) { newAngle += 360; }
                    dir = newAngle;
                }
            } else if (c1.pov() != -1) { // Controller 1 POV
                newAngle = (double)(c1.pov() + 180);
                while (newAngle > dir + 180) { newAngle -= 360; }
                while (newAngle < dir - 180) { newAngle += 360; }
                dir = newAngle;
            } else if (c2.pov() != -1) { // Controller 2 POV
                newAngle = (double)(c2.pov() + 180);
                while (newAngle > dir + 180) { newAngle -= 360; }
                while (newAngle < dir - 180) { newAngle += 360; }
                dir = newAngle;
            } else if (c1.active() || c2.active()) { // Manual Rotation
                dir += 2.5 * (Math.pow(c1.stick(4), 3) + Math.pow(c2.stick(4), 3));
            }
            go.swerve( // Drive with Headless Mode
                Math.pow(c1.stick(1), 3) + Math.pow(c2.stick(1), 3),
                Math.pow(c1.stick(0), 3) + Math.pow(c2.stick(0), 3),
                keepInRange(-0.02*(navx.yaw()-dir)*(2*Math.abs(c1.magnitude()+c2.magnitude())+1), -0.7, 0.7),
                navx.yaw() + 180
            );
            if (c1.back() || c2.back()) { // Change Mode
                mode = "raw";
            }
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) { // Toggle Consciousness
                conscious = !conscious;
            }
            if ((c1.right_stick() && c1.start()) || (c2.right_stick() && c2.start())) { // NavX Calibration
                navx.zeroYaw();
                dir = 0;
            } else if (c1.onRelease(Controls.RIGHT) || c2.onRelease(Controls.RIGHT)) { // LAUNCH
                launcher.LAUNCH();
            } else if (conscious && verySmart && launcher.prepping && speaker.pipelineActivated() && speaker.valid()) {
                if (speaker.Y() >= launchOver || c1.left() || c2.left()) {
                    launcher.LAUNCH();
                }
            }
            if (c1.stick(5) < -0.95 || c2.stick(5) < -0.95) { // Climbing System
                launcher.prepClimb();
            } else if (c1.stick(5) > 0.95 || c2.stick(5) > 0.95) {
                launcher.climb();
            }
            if (c1.b() || c2.b()) { // Cancel Any Launcher Activity
                intaking = false;
                launcher.stop();
                verySmart = false;
                SmartDashboard.putBoolean("Consciousness", false);
            } else if (launcher.stage == 0) { // If Launcher Not Doing Anything
                intaking = false;
                if (conscious && verySmart && launcher.holdingNote) {
                    launcher.LAUNCHprep();
                } else if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) { // Intake
                    intaking = true;
                    launcher.intake();
                    verySmart = true;
                    SmartDashboard.putBoolean("Consciousness", true);
                } else if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) { // Automatic Launch Sequence
                    launcher.aimAndLAUNCH();
                    while (!speaker.pipelineActivated()) {
                        launcher.update();
                        if (c1.b() || c2.b()) { break; }
                    }
                    dir = navx.yaw() + speaker.X()*0.8;
                } else if (c1.onPress(Controls.RIGHT) || c2.onPress(Controls.RIGHT)) { // Prepare to Launch (Hold Button Down)
                    launcher.LAUNCHprep();
                } else if (c1.onPress(Controls.Y) || c2.onPress(Controls.Y)) { // Turn in direction to launch in amp
                    if (leds.blueAlliance) {
                        newAngle = 90;
                    } else {
                        newAngle = -90;
                    }
                    while (newAngle > dir + 180) { newAngle -= 360; }
                    while (newAngle < dir - 180) { newAngle += 360; }
                    dir = newAngle;
                } else if (c1.onRelease(Controls.Y) || c2.onRelease(Controls.Y)) { // Launch Into Amp
                    launcher.amp();
                }
            }
        }

        // LED Strip Color:
        if (actualMatch && matchTimer.get() >= 115000) { // Final Countdown!
            leds.turquoise();
        } else if (launcher.holdingNote) { // Holding Note
            leds.yellow();
        } else if (!iseenote.get()) { // Getting Note
            leds.orange();
        } else {
            leds.allianceColor();
        }

        // Update Systems:
        go.update();
        launcher.update();
        if (intaking) {
            in.set(0.5);
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
        leftThruster.set(1);
        rightThruster.set(1);
        feedMotor.set(1);
    }

}