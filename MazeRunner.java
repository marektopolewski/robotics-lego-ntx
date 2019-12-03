import java.util.ArrayList;
import java.util.HashMap;
import lejos.nxt.*;

public class MazeRunner {

  public static Grid grid;

  private static final HashMap<Node, Node> obstacles = new HashMap<>();

  private static Node current_node;
  private static int heading;
  private static Robot robot;

  public static void main(String[] args) {

    debug("Click to start");
    LCD.clear();

    // Initialise search
    robot = new Robot();
    grid = new Grid();

    Node startNode = new Node(0,0);
    grid.addNode(startNode);
    startNode.makeNeighbours();

    // Recursively explore the grid world
    current_node = startNode;
    heading = Robot.H_NORTH;
    explore(startNode);

    // End
    LCD.clear();
    LCD.drawString("Press button to finish",0,0);
    Button.ENTER.waitForPressAndRelease();
  }

  // Explore all the possible neighbours of the given node
  private static void explore(Node n) {

    grid.addNode(n);
    n.setExplored(true);

    while(!n.getNeighbours(false).isEmpty()) {

      // backtrack
      if (n != current_node) {
        ArrayList<Node> path = grid.getShortestPath(current_node, n);
        heading = robot.followPath(path, heading);
        current_node = n;
      }

      Node selectedNode = n.getRandNeigbour();
      selectedNode.makeNeighbours();
      int moveResult = robot.moveTo(n, selectedNode, heading);

      // if obstacle found, then add it to the list and explore other neighbours
      if (moveResult==Robot.OBSTACLE) {
        heading = Robot.getHeading(selectedNode, n);
        grid.addObstacle(n, selectedNode);
        n.removeNeighbour(selectedNode);
      }

      // if no path, then explore other neighbours of this node
      else if (moveResult==Robot.NO_PATH) {
        n.removeNeighbour(selectedNode);
      }

      // if new neighbour found, then explore it
      else {
        heading = Robot.getHeading(n, selectedNode);
        current_node = selectedNode;
        explore(selectedNode);
      }
    }
  }

  public static void debug(String msg) {
    LCD.clear();
    LCD.drawString(msg,0,0);
    Button.ENTER.waitForPressAndRelease();
  }

}
