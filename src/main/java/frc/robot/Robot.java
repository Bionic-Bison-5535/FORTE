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
    String mode = "raw";
    boolean intaking = false;
    boolean actualMatch = false;
    double dir = 0;
    double newAngle;
    double launchOver = 12;
    int autoStage = 0;
    boolean conscious = true;
    boolean wasDisabled = false;
    double sensitivity = 5;

    private final SendableChooser<String> noteDropdown = new SendableChooser<>();
    private final SendableChooser<String> getMoreDropdown = new SendableChooser<>();
    String noteToGet, getMoreNotes;

    Wesswerve go = new Wesswerve(14, 15, 16, 17, 20, 21, 22, 23, 10, 11, 12, 13, 358, 225, 159, 250);
    Controls c1 = new Controls(0, 0.02);
    Controls c2 = new Controls(1, 0.02);
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
        SmartDashboard.putBoolean("Alliance", leds.blueAlliance);
        SmartDashboard.putString("Event", DriverStation.getEventName());
        SmartDashboard.putNumber("Match", DriverStation.getMatchNumber());
        launcher = new Launch(leftThruster, rightThruster, feedMotor, aimMotor, 25, 1, speaker);
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
        SmartDashboard.putNumber("Launch Over", launchOver);
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
                    autoStage = 9;
                } else {
                    autoStage = 2;
                    Alec.reset();
                }
            }
        }
        // Temprary Auto Stages:
        if (autoStage == 2) {
            if (noteToGet == "2") {
                go.swerve(0.2, 0, 0, 0);
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
            } else if (Alec.get() > 3500) {
                autoStage = 8;
            }
        }
        if (autoStage == 3) {
            go.swerve(0.12, 0, speaker.X()/20, 0);
            if (launcher.holdingNote) {
                launcher.aimAndLAUNCH();
                autoStage = 4;
            } else if (Alec.get() > 3500) {
                autoStage = 8;
            }
        }
        if (autoStage == 4) {
            if (noteToGet == "2" || speaker.valid()) {
                go.swerve(-0.1, 0, speaker.X()/40, 0);
            } else if (noteToGet == "1") {
                go.swerve(-0.1, 0, 0.2, 0);
            } else if (noteToGet == "3") {
                go.swerve(-0.1, 0, -0.2, 0);
            }
            if (!launcher.holdingNote) {
                autoStage = 8;
            }
        }
        /* Auto Stages to use later:
        if (autoStage == 2) {
            if (speaker.valid()) {
                go.swerve(0, 0, speaker.X()/40, 0);
                if (speaker.X() > -5 && speaker.X() > 5); {
                    autoStage = 3;
                    go.swerve(0, 0, 0, 0);
                }
            } else {
                go.swerve(0, 0, 0.05, 0);
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
            go.swerve(-0.15, 0, 0, navx.yaw()+180);
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
                //autoStage = 6;
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
        */
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

        // RAW MODE PERIODIC:
        if (mode == "raw") {
            if (c1.pov() != -1) { // Controller 1 POV
                newAngle = (double)(c1.pov());
                while (newAngle > dir + 180) { newAngle -= 360; }
                while (newAngle < dir - 180) { newAngle += 360; }
                dir = newAngle;
            } else if (c2.pov() != -1) { // Controller 2 POV
                newAngle = (double)(c2.pov());
                while (newAngle > dir + 180) { newAngle -= 360; }
                while (newAngle < dir - 180) { newAngle += 360; }
                dir = newAngle;
            } else if (c1.active() || c2.active()) { // Manual Rotation
                dir += 1.7 * Math.pow(c1.stick(4), sensitivity);
            }
            if (c1.left_stick() || c2.left_stick()) { // Turbo mode
                go.speed = go.default_speed;
            } else {
                go.speed = 0.7*go.default_speed;
            }
            go.swerve( // Drive with Headless Mode
                Math.pow(c1.stick(1) + c2.stick(1), sensitivity),
                Math.pow(c1.stick(0) + c2.stick(0), sensitivity),
                keepInRange(-0.04*(navx.yaw()-dir)*(2*Math.abs(c1.magnitude()+c2.magnitude())+1), -1.5, 1.5),
                navx.yaw() + 180
            );
            launcher.changeAim(3*Math.pow(c1.stick(2) - c1.stick(3) + c2.stick(2) - c2.stick(3), sensitivity));
            if (c1.x() || c2.x()) {
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
            }
            if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) { // Intake
                intaking = true;
                launcher.intake();
            } else if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) { // Launch Sequence
                go.update();
                launcher.LAUNCHstart();
            } else if (c1.onPress(Controls.RIGHT) || c2.onPress(Controls.RIGHT)) { // Launch Preparation (Hold right button down)
                launcher.aim(Launch.pos.closeup);
                launcher.LAUNCHprep_noCam();
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
            if ((c1.right_stick() && c1.start()) || (c2.right_stick() && c2.start())) { // NavX Calibration
                navx.zeroYaw();
                dir = 0;
            }

        // SMART MODE PERIODIC:
        } else if (mode == "smart") {
            if (conscious && launcher.holdingNote && !c1.y() && !c2.y()) {
                if (speaker.valid()) {
                    dir = navx.yaw() + speaker.X()*0.4;
                } else if (navx.coterminalYaw() < -45 || navx.coterminalYaw() > 45) {
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
            } else if (c1.active() || c2.active()) { // Manual Rotation
                dir += 3 * Math.pow(c1.stick(4) + c2.stick(4), sensitivity);
            }
            if (c1.left_stick() || c2.left_stick()) { // Turbo mode
                go.speed = go.default_speed;
            } else {
                go.speed = 0.35*go.default_speed;
            }
            go.swerve( // Drive with Headless Mode
                Math.pow(c1.stick(1) + c2.stick(1), sensitivity),
                Math.pow(c1.stick(0) + c2.stick(0), sensitivity),
                keepInRange(-0.04*(navx.yaw()-dir)*(2*Math.abs(c1.magnitude()+c2.magnitude())+1), -1.5, 1.5),
                navx.yaw() + 180
            );
            if (c1.onPress(Controls.X) || c2.onPress(Controls.X)) { // Toggle Consciousness
                conscious = !conscious;
                SmartDashboard.putBoolean("Consciousness", conscious);
            }
            if ((c1.right_stick() && c1.start()) || (c2.right_stick() && c2.start())) { // NavX Calibration
                navx.zeroYaw();
                dir = 0;
            } else if (c1.onRelease(Controls.LEFT) || c2.onRelease(Controls.LEFT)) {
                if (launcher.holdingNote) {
                    launcher.aim(Launch.pos.closeup);
                }
            } else if (c1.onRelease(Controls.RIGHT) || c2.onRelease(Controls.RIGHT)) { // LAUNCH
                launcher.LAUNCH();
            } else if (conscious && !c1.y() && !c2.y() && launcher.prepping && speaker.pipelineActivated() && speaker.valid()) { // Automatic LAUNCH
                if (speaker.Y() >= launchOver) {
                    launcher.LAUNCH();
                }
            } else if (c1.stick(5) < -0.95 || c2.stick(5) < -0.95) { // Climbing System
                launcher.prepClimb();
            } else if (c1.stick(5) > 0.95 || c2.stick(5) > 0.95) {
                launcher.climb();
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
            if (c1.onPress(Controls.B) || c2.onPress(Controls.B)) { // Cancel Any Launcher Activity
                intaking = false;
                launcher.stop();
                conscious = false;
                SmartDashboard.putBoolean("Consciousness", conscious);
            } else if (launcher.stage == 0) { // If Launcher Not Doing Anything
                intaking = false;
                if (conscious && launcher.holdingNote && speaker.valid() && c1.stick(1) == 0 && c2.stick(1) == 0) { // Automatic Launch Prep
                    launcher.LAUNCHprep();
                } else if (c1.onPress(Controls.A) || c2.onPress(Controls.A) || (!iseenote.get() && !launcher.holdingNote)) { // Intake
                    intaking = true;
                    launcher.intake();
                } else if (c1.onPress(Controls.LEFT) || c2.onPress(Controls.LEFT)) { // Automatic Launch Sequence
                    go.update();
                    launcher.LAUNCHstart();
                } else if (c1.onPress(Controls.RIGHT) || c2.onPress(Controls.RIGHT)) { // Prepare to Launch (Hold Button Down)
                    launcher.LAUNCHprep();
                }
            }
        }

        // LED Strip Color:
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
        if (mode == "smart" && c1.stick(2) + c2.stick(2) >= 0.1) {
            in.set(-c1.stick(2) - c2.stick(2));
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