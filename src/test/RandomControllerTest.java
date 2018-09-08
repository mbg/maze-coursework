import org.junit.Test;
import static org.junit.Assert.*;
import uk.ac.warwick.dcs.maze.logic.Maze;
import uk.ac.warwick.dcs.maze.generators.PrimGenerator;
import uk.ac.warwick.dcs.maze.logic.RobotImpl;

/*
    This class contains unit tests for the RandomController class.
*/
public class RandomControllerTest {
    /*
        Tests whether the random controller causes the robot
        to walk into walls.
    */
    @Test(timeout=10000)
    public void doesNotRunIntoWallsTest() {
        // generate a random maze
        Maze maze = (new PrimGenerator()).generateMaze();

        // initialise the robot
        RobotImpl robot = new RobotImpl();
        robot.setMaze(maze);

        // initialise the random robot controller
        RandomController controller = new RandomController();
        controller.setRobot(robot);

        // run the controller
        controller.start();

        // test whether the robot walked into walls during this run
        assertTrue(
            "RandomController walks into walls!",
            robot.getCollisions() == 0);
    }
}
