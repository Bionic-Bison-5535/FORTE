package frc.robot.systems;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.net.PortForwarder;

public class Limelight {

    private NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
	private int pipeline = 0;
	private double invalidArea = 0.04;
	private double[] getData = new double[6];

	public Limelight(int Pipline) {
		if (Pipline >= 0 && Pipline < 10) {
			pipeline = Pipline;
		}
	}

	public static void enableLimelightUSB() {
		PortForwarder.add(5800, "limelight.local", 5800);
		PortForwarder.add(5801, "limelight.local", 5801);
		PortForwarder.add(5802, "limelight.local", 5802);
		PortForwarder.add(5803, "limelight.local", 5803);
		PortForwarder.add(5804, "limelight.local", 5804);
		PortForwarder.add(5805, "limelight.local", 5805);
		PortForwarder.add(5806, "limelight.local", 5806);
		PortForwarder.add(5807, "limelight.local", 5807);
	}

	public double X() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("tx").getDouble(0);
	}

    public double Y() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("ty").getDouble(0);
	}

    public double area() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("ta").getDouble(0);
	}

	public double width() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("thor").getDouble(0);
	}

	public double height() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("tvert").getDouble(0);
	}

	public boolean valid() {
		table.getEntry("pipeline").setNumber(pipeline);
		if (table.getEntry("ta").getDouble(0) > invalidArea) {
			return true;
		} else {
			return false;
		}
	}

	public double x_pos() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[0];
	}

	public double y_pos() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[1];
	}

	public double z_pos() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[2];
	}

	public double roll() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[3];
	}

	public double pitch() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[4];
	}

	public double yaw() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[5];
	}

	public double robot_x() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[0];
	}

	public double robot_y() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[1];
	}

	public double robot_z() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[2];
	}

	public double robot_roll() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[3];
	}

	public double robot_pitch() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[4];
	}

	public double robot_yaw() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[5];
	}

	public static boolean inRange(double value, double setting, double errorRange) {
		return ((value >= setting - errorRange) && (value <= setting + errorRange));
	}

}
