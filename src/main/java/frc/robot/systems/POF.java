package frc.robot.systems;

public class POF {

    public Limelight cam;
    public double[] posData;
    public double x, y, yaw;

    public POF(Limelight AprilTagCam) {
        cam = AprilTagCam;
        cam.activate();
    }

    public void update() {
        if (cam.pipelineActivated()) {
            posData = cam.allPosData();
            x = posData[0];
            y = posData[1];
            yaw = posData[5];
        }
    }

}