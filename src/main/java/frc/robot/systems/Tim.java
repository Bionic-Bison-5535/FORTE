package frc.robot.systems;

public class Tim {

    public long startTim;
    
    public Tim() {
        startTim = System.currentTimeMillis();
    }

    /**
     * Sets the timer's time to 0 and continues counting.
     */
    public void reset() {
        startTim = System.currentTimeMillis();
    }

    /**
     * Sets the timer's value to a certain number and continues counting up
     * @param t Time in milliseconds to set the timer to
     */
    public void set(long t) {
        startTim = System.currentTimeMillis() - t;
    }

    /**
     * @return the timer's time in milliseconds.
     */
    public long get() {
        return System.currentTimeMillis() - startTim;
    }

}
