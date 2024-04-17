package frc.robot.systems;

import com.ctre.phoenix.led.CANdle;
import com.ctre.phoenix.led.CANdleConfiguration;
import com.ctre.phoenix.led.CANdle.LEDStripType;
import edu.wpi.first.wpilibj.DriverStation;

public class Lights {

    public CANdle leds;
    public boolean blueAlliance;

    public Lights(int canID) {
        leds = new CANdle(canID);
        CANdleConfiguration ledConfig = new CANdleConfiguration();
        ledConfig.stripType = LEDStripType.RGB;
        ledConfig.brightnessScalar = 1;
        leds.configAllSettings(ledConfig);
        checkAlliance();
    }

    public void setBrightness(double brightnessLevel) {
        if (brightnessLevel >= 1) {
            leds.configBrightnessScalar(1);
        } else if (brightnessLevel <= 0) {
            leds.configBrightnessScalar(0);
        } else {
            leds.configBrightnessScalar(brightnessLevel);
        }
    }

    public void blue() {
        leds.setLEDs(0, 0, 255);
    }

    public void red() {
        leds.setLEDs(255, 0, 0);
    }

    public void green() {
        leds.setLEDs(0, 255, 0);
    }

    public void orange() {
        leds.setLEDs(255, 70, 0);
    }

    public void turquoise() {
        leds.setLEDs(0, 255, 170);
    }

    public void cyan() {
        leds.setLEDs(0, 255, 255);
    }

    public void magenta() {
        leds.setLEDs(255, 0, 255);
    }

    public void yellow() {
        leds.setLEDs(255, 255, 0);
    }

    public void white() {
        leds.setLEDs(255, 255, 255);
    }

    public void off() {
        leds.setLEDs(0, 0, 0);
    }

    public void allianceColor() {
        if (DriverStation.isEStopped()) {
            magenta();
        } else {
            if (blueAlliance) {
                blue();
            } else {
                red();
            }
        }
    }

    public void checkAlliance() {
        blueAlliance = (DriverStation.getAlliance().get() == DriverStation.Alliance.Blue);
    }

}