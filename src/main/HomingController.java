import uk.ac.warwick.dcs.maze.logic.*;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HomingController implements IRobotController {
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

        while(!robot.getLocation().equals(robot.getTargetLocation()) && active) {
            // look in the direction in which we should go
            robot.setHeading(this.determineHeading());

            // move one step into the direction the robot is facing
            robot.advance();

            // wait for a while if we are supposed to
            if (delay > 0)
                robot.sleep(delay);
        }
    }

    // this method returns 1 if the target is north of the
    // robot, -1 if the target is south of the robot, or
    // 0 if otherwise.
    public byte isTargetNorth() {
        if (robot.getLocation().y < robot.getTargetLocation().y) return -1;
        if (robot.getLocation().y > robot.getTargetLocation().y) return 1;
        return 0;
    }

    // this method returns 1 if the target is east of the
    // robot, -1 if the target is west of the robot, or
    // 0 if otherwise.
    public byte isTargetEast() {
        if (robot.getLocation().x < robot.getTargetLocation().x) return 1;
        if (robot.getLocation().x > robot.getTargetLocation().x) return -1;
        return 0;
    }

    // this method causes the robot to look to the absolute
    // direction that is specified as argument and returns
    // what sort of square there is
    public int lookHeading(int heading) {
        switch(robot.getHeading()) {
            case IRobot.NORTH:
                if (heading == IRobot.NORTH) return robot.look(IRobot.AHEAD);
                if (heading == IRobot.EAST) return robot.look(IRobot.RIGHT);
                if (heading == IRobot.SOUTH) return robot.look(IRobot.BEHIND);
                if (heading == IRobot.WEST) return robot.look(IRobot.LEFT);
            case IRobot.WEST:
                if (heading == IRobot.NORTH) return robot.look(IRobot.RIGHT);
                if (heading == IRobot.EAST) return robot.look(IRobot.BEHIND);
                if (heading == IRobot.SOUTH) return robot.look(IRobot.LEFT);
                if (heading == IRobot.WEST) return robot.look(IRobot.AHEAD);
            case IRobot.SOUTH:
                if (heading == IRobot.NORTH) return robot.look(IRobot.BEHIND);
                if (heading == IRobot.EAST) return robot.look(IRobot.LEFT);
                if (heading == IRobot.SOUTH) return robot.look(IRobot.AHEAD);
                if (heading == IRobot.WEST) return robot.look(IRobot.RIGHT);
            case IRobot.EAST:
                if (heading == IRobot.NORTH) return robot.look(IRobot.LEFT);
                if (heading == IRobot.EAST) return robot.look(IRobot.AHEAD);
                if (heading == IRobot.SOUTH) return robot.look(IRobot.RIGHT);
                if (heading == IRobot.WEST) return robot.look(IRobot.BEHIND);
        }
        return -1;
    }

    // this method determines the heading in which the robot
    // should head next to move closer to the target
    public int determineHeading() {
        ArrayList<Integer> options = new ArrayList<Integer>();

        if(this.isTargetNorth() > 0 && this.lookHeading(IRobot.NORTH) != IRobot.WALL) options.add(IRobot.NORTH);
        if(this.isTargetNorth() < 0 && this.lookHeading(IRobot.SOUTH) != IRobot.WALL) options.add(IRobot.SOUTH);
        if(this.isTargetEast() > 0 && this.lookHeading(IRobot.EAST) != IRobot.WALL) options.add(IRobot.EAST);
        if(this.isTargetEast() < 0 && this.lookHeading(IRobot.WEST) != IRobot.WALL) options.add(IRobot.WEST);

        if(options.size() == 0) {
            if(this.lookHeading(IRobot.NORTH) != IRobot.WALL) options.add(IRobot.NORTH);
            if(this.lookHeading(IRobot.SOUTH) != IRobot.WALL) options.add(IRobot.SOUTH);
            if(this.lookHeading(IRobot.EAST) != IRobot.WALL) options.add(IRobot.EAST);
            if(this.lookHeading(IRobot.WEST) != IRobot.WALL) options.add(IRobot.WEST);
        }

        if(options.size() != 0) {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            Integer result = options.get(random.nextInt(options.size()));
            return result;
        }

        return IRobot.CENTRE;
    }

    // this method returns a description of this controller
    public String getDescription() {
       return "A controller which homes in on the target";
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
