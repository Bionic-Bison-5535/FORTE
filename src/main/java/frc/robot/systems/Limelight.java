package frc.robot.systems;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.net.PortForwarder;

public class Limelight {
    
    private NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
	private int pipeline = 0;
	private double invalidArea = 0.04;
	
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

	public boolean inRange(double value, double setting, double errorRange) {
		return ((value >= setting - errorRange) && (value <= setting + errorRange));
	}

}
