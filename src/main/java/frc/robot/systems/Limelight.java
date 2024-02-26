package frc.robot.systems;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.net.PortForwarder;

public class Limelight {

    private static NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
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

	/**
	 * Activates correct pipeline
	 * @return True if camera is immediately ready (on the correct pipeline)
	 */
	public boolean activate() {
		table.getEntry("pipeline").setNumber(pipeline);
		return pipelineActivated();
	}

	/** @return True if camera is set to correct pipeline */
	public boolean pipelineActivated() {
		return table.getEntry("pipeline").getNumber(-1).intValue() == pipeline;
	}

	/** @return X position of centre of object of focus */
	public double X() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("tx").getDouble(0);
	}

	public static double X_() {
		return table.getEntry("tx").getDouble(0);
	}

	/** @return Y position of centre of object of focus */
    public double Y() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("ty").getDouble(0);
	}

	public static double Y_() {
		return table.getEntry("ty").getDouble(0);
	}

	/** @return Area of image that the object of focus takes up */
    public double area() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("ta").getDouble(0);
	}

	public double area_() {
		return table.getEntry("ta").getDouble(0);
	}

	/** @return Width, in pixels, of object of focus on image plane */
	public double width() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("thor").getDouble(0);
	}

	public static double width_() {
		return table.getEntry("thor").getDouble(0);
	}

	/** @return Height, in pixels, of object of focus on image plane */
	public double height() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("tvert").getDouble(0);
	}

	public static double height_() {
		return table.getEntry("tvert").getDouble(0);
	}

	/** @return True if object of focus is in view and not so far away it is a speck */
	public boolean valid() {
		table.getEntry("pipeline").setNumber(pipeline);
		if (table.getEntry("ta").getDouble(0) > invalidArea) {
			return true;
		} else {
			return false;
		}
	}

	/** @return Horizontal metres from camera to centre of aprilTag */
	public double x_pos() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[0];
	}

	/** @return Forward distance in metres from camera to centre of aprilTag */
	public double y_pos() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[1];
	}

	/** @return Vertical distance (up) in metres from height of camera to height of centre aprilTag */
	public double z_pos() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[2];
	}

	/** @return Roll angle of AprilTag relative to camera, in degrees */
	public double roll() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[3];
	}

	/** @return Pitch angle of AprilTag relative to camera, in degrees */
	public double pitch() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[4];
	}

	/** @return Yaw angle of AprilTag relative to camera, in degrees */
	public double yaw() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("targetpose_cameraspace").getDoubleArray(getData)[5];
	}

	/** @return X Position of robot on the field, in metres */
	public double robot_x() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[0];
	}

	/** @return Y Position of robot on the field, in metres */
	public double robot_y() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[1];
	}

	/** @return How high the robot is flying, in metres */
	public double robot_z() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[2];
	}

	/** @return Roll angle of robot relative to field plane, in degrees */
	public double robot_roll() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[3];
	}

	/** @return Pitch angle of robot relative to field plane, in degrees */
	public double robot_pitch() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[4];
	}

	/** @return Yaw angle of robot on field plane, in degrees */
	public double robot_yaw() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData)[5];
	}

	public double[] allPosData() {
		table.getEntry("pipeline").setNumber(pipeline);
		return table.getEntry("botpose").getDoubleArray(getData);
	}

	public static boolean inRange(double value, double setting, double errorRange) {
		return ((value >= setting - errorRange) && (value <= setting + errorRange));
	}

}
