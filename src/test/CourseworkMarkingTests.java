import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import uk.ac.warwick.dcs.maze.generators.PrimGenerator;
import uk.ac.warwick.dcs.maze.logic.*;
import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;
import java.util.*;

/*
    This class contains unit tests for the HomingController class.
*/
public class CourseworkMarkingTests {
    class TerminatorListener implements IEventClient {
        private IRobotController controller;
        private long moves;
        private long movesLimit = 1000;

        public TerminatorListener(IRobotController controller) {
            this.controller = controller;
            this.moves = 0;
        }

        public void notify(IEvent event) {
            if(event.getMessage() == IEvent.ROBOT_RELOCATE) {
                this.moves++;

                if(this.moves >= this.movesLimit) {
                    this.controller.reset();
                }
            }
        }
    }
    class MovementListener implements IEventClient {
        private RobotImpl impl;
        private Boolean wallAhead = false;
        private long moveCount = 0;
        long movesLeft = 0;
        long movesAhead = 0;
        long movesRight = 0;
        long movesBack = 0;
        long headingChangedWhenNotFacingWall = 0;
        private Point lastPoint = null;
        private int lastHeading;

        public long getMoveCount() {
            return this.moveCount;
        }

        public MovementListener(RobotImpl impl) {
            this.impl = impl;
            this.lastPoint = new Point(
                this.impl.getLocationX(), this.impl.getLocationY());
            this.lastHeading = this.impl.getHeading();

            if(this.impl.look(IRobot.AHEAD) == IRobot.WALL) {
                this.wallAhead = true;
            }
        }

        public int getDirection(int prevHeading, int currHeading) {
            int x = prevHeading - IRobot.NORTH;
            int y = currHeading - IRobot.NORTH;

            int diff = Math.floorMod(y-x, 4);

            return IRobot.AHEAD + diff;
        }

        public void notify(IEvent event) {
            if(event.getMessage() == IEvent.ROBOT_RELOCATE) {
                Point location = (Point)event.getData();

                int directionMoved = 0;

                if(location.x > this.lastPoint.x) { directionMoved = IRobot.EAST; }
                if(location.x < this.lastPoint.x) { directionMoved = IRobot.WEST; }
                if(location.y > this.lastPoint.y) { directionMoved = IRobot.SOUTH; }
                if(location.y < this.lastPoint.y) { directionMoved = IRobot.NORTH; }

                switch(this.getDirection(this.lastHeading, this.impl.getHeading())) {
                    case IRobot.AHEAD: this.movesAhead++; break;
                    case IRobot.LEFT: this.movesLeft++; break;
                    case IRobot.RIGHT: this.movesRight++; break;
                    case IRobot.BEHIND: this.movesBack++; break;
                }

                this.moveCount++;
                this.lastPoint = location;
                this.lastHeading = this.impl.getHeading();

                if(this.impl.look(IRobot.AHEAD) == IRobot.WALL) {
                    this.wallAhead = true;
                }
                else {
                    this.wallAhead = false;
                }
            }
            else if (event.getMessage() == IEvent.ROBOT_HEADING_CHANGED) {
                Integer dir = (Integer)event.getData();

                if(!this.wallAhead) {
                    this.headingChangedWhenNotFacingWall++;
                }
            }
        }
    }

    /*@Test
    public void sanity_test() {
        RobotImpl i = new RobotImpl();
        MovementListener l = new MovementListener(i);
        assertEquals(l.getDirection(IRobot.NORTH, IRobot.WEST), IRobot.LEFT);
        assertEquals(l.getDirection(IRobot.NORTH, IRobot.EAST), IRobot.RIGHT);
        assertEquals(l.getDirection(IRobot.NORTH, IRobot.NORTH), IRobot.AHEAD);
        assertEquals(l.getDirection(IRobot.NORTH, IRobot.SOUTH), IRobot.BEHIND);
        assertEquals(l.getDirection(IRobot.EAST, IRobot.EAST), IRobot.AHEAD);
        assertEquals(l.getDirection(IRobot.EAST, IRobot.WEST), IRobot.BEHIND);
        assertEquals(l.getDirection(IRobot.EAST, IRobot.NORTH), IRobot.LEFT);
        assertEquals(l.getDirection(IRobot.EAST, IRobot.SOUTH), IRobot.RIGHT);
        assertEquals(l.getDirection(IRobot.WEST, IRobot.EAST), IRobot.BEHIND);
        assertEquals(l.getDirection(IRobot.WEST, IRobot.WEST), IRobot.AHEAD);
        assertEquals(l.getDirection(IRobot.WEST, IRobot.NORTH), IRobot.RIGHT);
        assertEquals(l.getDirection(IRobot.WEST, IRobot.SOUTH), IRobot.LEFT);
        assertEquals(l.getDirection(IRobot.SOUTH, IRobot.WEST), IRobot.RIGHT);
        assertEquals(l.getDirection(IRobot.SOUTH, IRobot.EAST), IRobot.LEFT);
        assertEquals(l.getDirection(IRobot.SOUTH, IRobot.NORTH), IRobot.BEHIND);
        assertEquals(l.getDirection(IRobot.SOUTH, IRobot.SOUTH), IRobot.AHEAD);
    }*/

    @Test(timeout=240000)
    public void testTask1_1_1() {
        for(int i=0; i<5; i++) {
            // generate a random maze
            Maze maze = (new PrimGenerator()).generateMaze();

            // initialise the robot
            RobotImpl robot = new RobotImpl();
            robot.setMaze(maze);

            // initialise the random robot controller
            RandomController controller = new RandomController();
            controller.setRobot(robot);

            // add an event listener
            TerminatorListener listener = new TerminatorListener(controller);
            EventBus.addClient(listener);

            // run the controller
            controller.start();

            // test whether the robot walked into walls during this run
            assertTrue(
                "RandomController walks into walls!",
                robot.getCollisions() == 0);

            // remove an event listener
            EventBus.removeClient(listener);
        }
    }

    @Test(timeout=240000)
    public void testTask1_2_1() {

        for(int i=0; i<5; i++) {
            // generate a random maze
            Maze maze = (new PrimGenerator()).generateMaze();

            // initialise the robot
            RobotImpl robot = new RobotImpl();
            robot.setMaze(maze);

            // add an event listener
            MovementListener listener = new MovementListener(robot);
            EventBus.addClient(listener);

            // initialise the random robot controller
            RandomController controller = new RandomController();
            controller.setRobot(robot);
            TerminatorListener terminator = new TerminatorListener(controller);
            EventBus.addClient(terminator);

            // run the controller
            controller.start();

            // test whether the robot walked into walls during this run
            assertEquals(
                "RandomController does not log correct number of moves!",
                robot.getSteps(), listener.getMoveCount());
            assertEquals(
                "RandomController does not log correct number of moves ahead!",
                robot.getLogger().getMovesForward(), listener.movesAhead);
            assertEquals(
                "RandomController does not log correct number of moves behind!",
                robot.getLogger().getMovesBackwards(), listener.movesBack);
            assertEquals(
                "RandomController does not log correct number of moves left!",
                robot.getLogger().getMovesLeft(), listener.movesLeft);
            assertEquals(
                "RandomController does not log correct number of moves right!",
                robot.getLogger().getMovesRight(), listener.movesRight);

            // remove an event listener
            EventBus.removeClient(listener);
            EventBus.removeClient(terminator);
        }
    }

    /*@Test(timeout=60000)
    public void testTask1_3_1() {

        for(int i=0; i<5; i++) {
            // generate a random maze
            Maze maze = new Maze(50, 50);

            // fill the maze with passages
            for (int x=1; x<maze.getWidth()-2; x++) {
                for (int y=1; y<maze.getHeight()-2; y++) {
                    maze.setCellType(x, y, Maze.PASSAGE);
                }
            }

            // set the starting point somewhere near the middle
            maze.setStart(25,25);
            maze.setFinish(1,1);

            // initialise the robot
            RobotImpl robot = new RobotImpl();
            robot.setMaze(maze);

            // add an event listener
            MovementListener listener = new MovementListener(robot);
            EventBus.addClient(listener);

            // initialise the random robot controller
            RandomController controller = new RandomController();
            controller.setRobot(robot);

            // run the controller
            controller.start();

            // test whether the robot changed direction
            assertEquals(
                "RandomController does not randomly choose a direction every eight moves!",
                0.125f, (float)listener.headingChangedWhenNotFacingWall / (float)listener.getMoveCount(), 0.0f);

            // remove an event listener
            EventBus.removeClient(listener);
        }
    }*/

    /*
        Tests whether the homing controller's isTargetNorth
        method works as specified.
    */
    @Test(timeout=10000)
    public void testTask1_5_1() {
        int columns = 5;
        int rows = 5;

        // generate a maze with the test dimensions
        Maze maze = new Maze(columns, rows);

        // fill the maze with passages
        for (int i=0; i<maze.getWidth(); i++) {
            for (int j=0; j<maze.getHeight(); j++) {
                maze.setCellType(i, j, Maze.PASSAGE);
            }
        }

        // set the starting point somewhere near the middle
        maze.setStart(2,2);
        maze.setFinish(0,0);

        // initialise the robot
        RobotImpl robot = new RobotImpl();
        robot.setMaze(maze);

        // initialise the random robot controller
        HomingController controller = new HomingController();
        controller.setRobot(robot);

        // move the target to some cells north of the robot and
        // test whether isTargetNorth correctly identifies this
        for(int i=0; i<maze.getWidth(); i++) {
            robot.setTargetLocation(new Point(i,0));

            assertTrue(
                "HomingController doesn't think the target is north!",
                controller.isTargetNorth() == 1);
        }

        // move the target to some cells south of the robot and
        // test whether isTargetNorth correctly identifies this
        for(int i=0; i<maze.getWidth(); i++) {
            robot.setTargetLocation(new Point(i,4));

            assertTrue(
                "HomingController doesn't think the target is south!",
                controller.isTargetNorth() == -1);
        }

        // move the target to some cells on the same y-level as the
        // robot and test whether isTargetNorth correctly identifies this
        for(int i=0; i<maze.getWidth(); i++) {
            robot.setTargetLocation(new Point(i,2));

            assertTrue(
                "HomingController doesn't think the target is on the same level!",
                controller.isTargetNorth() == 0);
        }
    }

    /*
        Tests whether the homing controller's isTargetEast
        method works as specified.
    */
    @Test(timeout=10000)
    public void testTask1_5_2() {
        int columns = 5;
        int rows = 5;

        // generate a maze with the test dimensions
        Maze maze = new Maze(columns, rows);

        // fill the maze with passages
        for (int i=0; i<maze.getWidth(); i++) {
            for (int j=0; j<maze.getHeight(); j++) {
                maze.setCellType(i, j, Maze.PASSAGE);
            }
        }

        // set the starting point somewhere near the middle
        maze.setStart(2,2);
        maze.setFinish(0,0);

        // initialise the robot
        RobotImpl robot = new RobotImpl();
        robot.setMaze(maze);

        // initialise the random robot controller
        HomingController controller = new HomingController();
        controller.setRobot(robot);

        // move the target to some cells east of the robot and
        // test whether isTargetEast correctly identifies this
        for(int i=0; i<maze.getWidth(); i++) {
            robot.setTargetLocation(new Point(4,i));

            assertTrue(
                "HomingController doesn't think the target is east!",
                controller.isTargetEast() == 1);
        }

        // move the target to some cells west of the robot and
        // test whether isTargetEast correctly identifies this
        for(int i=0; i<maze.getWidth(); i++) {
            robot.setTargetLocation(new Point(0,i));

            assertTrue(
                "HomingController doesn't think the target is west!",
                controller.isTargetEast() == -1);
        }

        // move the target to some cells on the same x-level as the
        // robot and test whether isTargetEast correctly identifies this
        for(int i=0; i<maze.getWidth(); i++) {
            robot.setTargetLocation(new Point(2,i));

            assertTrue(
                "HomingController doesn't think the target is on the same level!",
                controller.isTargetEast() == 0);
        }
    }

    /*
        Tests whether the homing controller's lookHeading method
        works correctly.
    */
    @Test(timeout=10000)
    public void testTask1_5_3() {
        int columns = 5;
        int rows = 5;

        // generate a maze with the test dimensions
        Maze maze = new Maze(columns, rows);

        // fill the maze with passages
        for (int i=0; i<maze.getWidth(); i++) {
            for (int j=0; j<maze.getHeight(); j++) {
                maze.setCellType(i, j, Maze.PASSAGE);
            }
        }

        // set the starting point somewhere near the middle
        maze.setStart(2,2);
        maze.setFinish(0,0);

        // initialise the robot
        RobotImpl robot = new RobotImpl();
        robot.setMaze(maze);

        // initialise the random robot controller
        HomingController controller = new HomingController();
        controller.setRobot(robot);

        // add some walls to the maze
        maze.setCellType(2, 1, Maze.WALL);
        maze.setCellType(2, 3, Maze.WALL);

        // test lookHeading for when the robot is facing north
        robot.setHeading(IRobot.NORTH);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);

        // test lookHeading for when the robot is facing east
        robot.setHeading(IRobot.EAST);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);

        // test lookHeading for when the robot is facing south
        robot.setHeading(IRobot.SOUTH);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);

        // test lookHeading for when the robot is facing west
        robot.setHeading(IRobot.WEST);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);


        // add some walls to the maze
        maze.setCellType(2, 1, Maze.WALL);
        maze.setCellType(2, 3, Maze.PASSAGE);

        // test lookHeading for when the robot is facing north
        robot.setHeading(IRobot.NORTH);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);

        // test lookHeading for when the robot is facing east
        robot.setHeading(IRobot.EAST);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);

        // test lookHeading for when the robot is facing south
        robot.setHeading(IRobot.SOUTH);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);

        // test lookHeading for when the robot is facing west
        robot.setHeading(IRobot.WEST);
        assertTrue(
            "HomingController doesn't see a wall in the north!",
            controller.lookHeading(IRobot.NORTH) == IRobot.WALL);
        assertTrue(
            "HomingController doesn't see a passage in the east!",
            controller.lookHeading(IRobot.EAST) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a wall in the south!",
            controller.lookHeading(IRobot.SOUTH) == IRobot.PASSAGE);
        assertTrue(
            "HomingController doesn't see a passage in the west!",
            controller.lookHeading(IRobot.WEST) == IRobot.PASSAGE);
    }

    // this method returns 1 if the target is north of the
    // robot, -1 if the target is south of the robot, or
    // 0 if otherwise.
    private byte isTargetNorth(IRobot robot) {
        if (robot.getLocation().y < robot.getTargetLocation().y) return -1;
        if (robot.getLocation().y > robot.getTargetLocation().y) return 1;
        return 0;
    }

    // this method returns 1 if the target is east of the
    // robot, -1 if the target is west of the robot, or
    // 0 if otherwise.
    private byte isTargetEast(IRobot robot) {
        if (robot.getLocation().x < robot.getTargetLocation().x) return 1;
        if (robot.getLocation().x > robot.getTargetLocation().x) return -1;
        return 0;
    }

    private int lookHeading(IRobot robot, int heading) {
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

    private ArrayList<Integer> determineValidHeadings(IRobot robot) {
        ArrayList<Integer> options = new ArrayList<Integer>();

        if(this.isTargetNorth(robot) > 0 && this.lookHeading(robot, IRobot.NORTH) != IRobot.WALL) options.add(IRobot.NORTH);
        if(this.isTargetNorth(robot) < 0 && this.lookHeading(robot, IRobot.SOUTH) != IRobot.WALL) options.add(IRobot.SOUTH);
        if(this.isTargetEast(robot) > 0 && this.lookHeading(robot, IRobot.EAST) != IRobot.WALL) options.add(IRobot.EAST);
        if(this.isTargetEast(robot) < 0 && this.lookHeading(robot, IRobot.WEST) != IRobot.WALL) options.add(IRobot.WEST);

        if(options.size() == 0) {
            if(this.lookHeading(robot, IRobot.NORTH) != IRobot.WALL) options.add(IRobot.NORTH);
            if(this.lookHeading(robot, IRobot.SOUTH) != IRobot.WALL) options.add(IRobot.SOUTH);
            if(this.lookHeading(robot, IRobot.EAST) != IRobot.WALL) options.add(IRobot.EAST);
            if(this.lookHeading(robot, IRobot.WEST) != IRobot.WALL) options.add(IRobot.WEST);
        }

        return options;
    }


    @Test(timeout=60000)
    public void testTask1_5_4_easy() {
        for(int i=0; i<5; i++) {
            // generate a random maze
            Maze maze = (new PrimGenerator()).generateMaze();

            // initialise the robot
            RobotImpl robot = new RobotImpl();
            robot.setMaze(maze);

            // add an event listener
            MovementListener listener = new MovementListener(robot);
            EventBus.addClient(listener);

            // initialise the random robot controller
            HomingController controller = new HomingController();
            controller.setRobot(robot);

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for(int j=0; j<1000; j++) {
                // put the robot in a random place
                int x = random.nextInt(1, maze.getWidth() - 2);
                int y = random.nextInt(1, maze.getHeight() - 2);

                robot.setLocation(new Point(x,y));

                if(robot.look(IRobot.AHEAD) == IRobot.WALL &&
                   robot.look(IRobot.LEFT) == IRobot.WALL &&
                   robot.look(IRobot.RIGHT) == IRobot.WALL &&
                   robot.look(IRobot.BEHIND) == IRobot.WALL) {
                       continue;
                }

                ArrayList<Integer> options = this.determineValidHeadings(robot);
                int result = controller.determineHeading();

                assertTrue(
                    "determineHeading returns an invalid heading!",
                    options.contains(result));
            }

            // remove an event listener
            EventBus.removeClient(listener);
        }
    }

    @Test(timeout=60000)
    public void testTask1_5_4_hard() {
        for(int i=0; i<5; i++) {
            // generate a random maze
            Maze maze = (new PrimGenerator()).generateMaze();

            // initialise the robot
            RobotImpl robot = new RobotImpl();
            robot.setMaze(maze);

            // add an event listener
            MovementListener listener = new MovementListener(robot);
            EventBus.addClient(listener);

            // initialise the random robot controller
            HomingController controller = new HomingController();
            controller.setRobot(robot);

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for(int j=0; j<1000; j++) {
                // put the robot in a random place
                int x = random.nextInt(1, maze.getWidth() - 2);
                int y = random.nextInt(1, maze.getHeight() - 2);

                robot.setLocation(new Point(x,y));

                ArrayList<Integer> options = this.determineValidHeadings(robot);
                int result = controller.determineHeading();

                assertTrue(
                    "determineHeading returns an invalid heading!",
                    (options.isEmpty() && !(result >= IRobot.NORTH && result <= IRobot.WEST)) || options.contains(result));
            }

            // remove an event listener
            EventBus.removeClient(listener);
        }
    }
}
