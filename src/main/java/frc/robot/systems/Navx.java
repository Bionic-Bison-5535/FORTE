package frc.robot.systems;

import java.lang.Math;
import edu.wpi.first.wpilibj.I2C;
import com.kauailabs.navx.frc.AHRS;

public class Navx {

    public AHRS NavX = new AHRS(I2C.Port.kMXP);
    public double yaw_Offset, roll_Offset, pitch_Offset;

    public Navx() {
        roll_Offset = NavX.getRoll();
        pitch_Offset = NavX.getPitch();
    }

    public void reset() {
        NavX.reset();
        roll_Offset = NavX.getRoll();
        pitch_Offset = NavX.getPitch();
        yaw_Offset = 0;
    }

    public void zeroYaw() {
        yaw_Offset = NavX.getAngle();
    }

    public double roll() {
        return NavX.getRoll() - roll_Offset;
    }

    public double pitch() {
        return NavX.getPitch() - pitch_Offset;
    }

    public double yaw() {
        return NavX.getAngle() - yaw_Offset;
    }

    public double coterminalYaw() {
        return yaw() % 360;
    }

    public double velocity() {
        return Math.sqrt(Math.pow(NavX.getVelocityX(),2)+Math.pow(NavX.getVelocityX(),2));
    }

    public boolean moving() {
        return velocity() > 0.05;
    }

    public double celsius() {
        return NavX.getTempC();
    }

    public double compass() {
        return NavX.getCompassHeading();
    }

}
