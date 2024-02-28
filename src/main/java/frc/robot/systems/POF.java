package frc.robot.systems;

public class POF {

    public Limelight cam;
    public double[] posData;
    public double setX, setY;
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

    public boolean there() {
        return false;
    }

    public void goTo(double[] pos) {
        if (pos.length == 2) {
            setX = pos[0];
            setY = pos[1];
        }
    }

}