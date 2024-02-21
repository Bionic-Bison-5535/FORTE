package frc.robot.systems;

import java.lang.Math;
import edu.wpi.first.wpilibj.Timer;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;

public class Motor {

    private CANSparkMax maxMotor;
    private TalonSRX talonMotor;
    private RelativeEncoder canEncoder;
    private boolean invertThisMotor = false;
    public double goToPos = 0;
    public boolean usingTalon;
    public double db = 0.01;
    public boolean posMode = false;
    public double maxSpeed = 1;

    public Motor(int canID, boolean isTalon, boolean invert, double maximum_speed) {
        usingTalon = isTalon;
        invertThisMotor = invert;
        Timer.delay(0.2);
        if (usingTalon) {
            talonMotor = new TalonSRX(canID);
            talonMotor.configOpenloopRamp(0);
            talonMotor.setInverted(invertThisMotor);
        } else {
            maxMotor = new CANSparkMax(canID, MotorType.kBrushless);
            maxMotor.setInverted(!invertThisMotor);
            canEncoder = maxMotor.getEncoder();
        }
        maxSpeed = maximum_speed;
    }

    public void stop() {
        if (usingTalon) {
            talonMotor.set(ControlMode.PercentOutput, 0);
        } else {
            maxMotor.set(0);
        }
        if (posMode) {
            setEnc(getEnc());
        }
    }

    public void set(double power) {
        if (usingTalon) {
            talonMotor.setInverted(invertThisMotor);
        } else {
            maxMotor.setInverted(!invertThisMotor);
        }
        if (usingTalon) {
            talonMotor.set(ControlMode.PercentOutput, power);
        } else {
            maxMotor.set(power);
        }
        posMode = false;
    }

    public void goTo(double newEncValueToGoTo) {
        goToPos = newEncValueToGoTo;
        posMode = true;
    }

    public double getEnc() {
        if (usingTalon) {
            return talonMotor.getSelectedSensorPosition();
        } else {
            return canEncoder.getPosition();
        }
    }

    public double getRotations() {
        return (getEnc() / rotationValue());
    }

    public void setRotations(double rotations) {
        setEnc(rotations * rotationValue());
    }

    public void setEnc(double newEncValue) {
        if (usingTalon) {
            talonMotor.setSelectedSensorPosition(newEncValue);
        } else {
            canEncoder.setPosition(newEncValue);
        }
        goToPos = newEncValue;
    }

    public double rotationValue() {
        if (usingTalon) {
            return 2048;
        } else {
            return 4;
        }
    }

    public void ramp(double secondsToFullSpeed) {
        if (usingTalon) {
            talonMotor.configOpenloopRamp(secondsToFullSpeed);
        }
    }

    public boolean there() {
        if (posMode && Math.abs(getEnc()-goToPos) > db * rotationValue()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean almost() {
        if (Math.abs(0.3*((goToPos/rotationValue())-getRotations())) >= 1) {
            return false;
        } else {
            return true;
        }
    }

    private double keepInRange(double number, double floor, double ceiling) {
        if (number >= floor && number <= ceiling) {
            return number;
        } else if (number < floor) {
            return floor;
        } else {
            return ceiling;
        }
    }

    public void update() {
        if (posMode) {
            if (!there()) {
                set(keepInRange(0.5*((goToPos/rotationValue())-getRotations()), -maxSpeed, maxSpeed));
            } else {
                set(0);
            }
            posMode = true;
        }
    }

}
