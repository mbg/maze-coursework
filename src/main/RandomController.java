import uk.ac.warwick.dcs.maze.logic.*;
import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

public class RandomController implements IRobotController {
    // the robot in the maze
    private IRobot robot;
    // a flag to indicate whether we are looking for a path
    private boolean active = false;
    // a value (in ms) indicating how long we should wait
    // between moves
    private int delay;

    // this method is called when the "start" button is clicked
    // in the user interface
    public void start() {
        this.active = true;

        // loop while we haven't found the exit and the agent
        // has not been interrupted
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = 0;
        int heading = this.robot.getHeading();

        while(!robot.getLocation().equals(robot.getTargetLocation()) && active) {
            rand = random.nextInt(8);

            if(rand == 7 || robot.look(IRobot.AHEAD) == IRobot.WALL) {
                ArrayList<Integer> directions = new ArrayList<Integer>();

                if(robot.look(IRobot.AHEAD) != IRobot.WALL) directions.add(IRobot.AHEAD);
                if(robot.look(IRobot.LEFT) != IRobot.WALL) directions.add(IRobot.LEFT);
                if(robot.look(IRobot.RIGHT) != IRobot.WALL) directions.add(IRobot.RIGHT);
                if(robot.look(IRobot.BEHIND) != IRobot.WALL) directions.add(IRobot.BEHIND);

                rand = random.nextInt(directions.size());

                int direction = directions.get(rand);
                robot.face(direction);
                robot.getLogger().log(direction);
            }
            else {
                robot.getLogger().log(IRobot.AHEAD);
            }
            // move one step into the direction the robot is facing
            robot.advance();

            // wait for a while if we are supposed to
            if (delay > 0)
                robot.sleep(delay);
       }
    }

    // this method returns a description of this controller
    public String getDescription() {
       return "A controller which randomly chooses where to go";
    }

    // sets the delay
    public void setDelay(int millis) {
       delay = millis;
    }

    // gets the current delay
    public int getDelay() {
       return delay;
    }

    // stops the controller
    public void reset() {
       active = false;
    }

    // sets the reference to the robot
    public void setRobot(IRobot robot) {
       this.robot = robot;
    }
}
