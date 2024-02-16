package frc.robot.systems;

import edu.wpi.first.wpilibj.PWM;

public class Intake {

    private Motor intakeMotor;
    private PWM opener;
    public double openPos = 1;
    public double closedPos = 0;
    public boolean isOpen = true;
    public boolean running = false;

	public Intake(Motor IntakeMotor, int openerPin) {
		intakeMotor = IntakeMotor;
        opener = new PWM(openerPin);
        opener.setSpeed(openPos);
	}

    public void open() {
        opener.setSpeed(openPos);
        isOpen = true;
    }

    public void close() {
        opener.setSpeed(closedPos);
        isOpen = false;
    }

	public void run() {
        open();
        intakeMotor.set(0.7);
        running = true;
    }

    public void stop() {
        intakeMotor.set(0);
        running = false;
    }

    public void update() {
        if (running) {
            intakeMotor.set(1);
        } else {
            intakeMotor.set(0);
        }
    }

}
