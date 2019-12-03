import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import lejos.nxt.*;

public class Grid {

  private ArrayList<Node> nodes;
  private HashMap<Node, ArrayList<Node>> obstacles;

  private HashSet<ArrayList<Node>> temp_paths;

  public Grid() {
    nodes = new ArrayList<>();
    obstacles = new HashMap<>();
  }

  public void addObstacle(Node n1, Node n2) {
    if (!obstacles.containsKey(n1)) obstacles.put(n1, new ArrayList<>());
    obstacles.get(n1).add(n2);

    if (!obstacles.containsKey(n2)) obstacles.put(n2, new ArrayList<>());
    obstacles.get(n2).add(n1);
  }

  public boolean isObstacle(Node n1, Node n2) {
    if (!obstacles.containsKey(n1)) return false;
    if (!obstacles.get(n1).contains(n2)) return false;
    return true;
  }

  public void addNode(Node n) {
    nodes.add(n);
  }

  public ArrayList<Node> getShortestPath(Node n1, Node n2) {
    temp_paths = new HashSet<>();

    ArrayList<Node> isVisited = new ArrayList<>();
    ArrayList<Node> pathList = new ArrayList<>();
    pathList.add(n1);

    //Call recursive utility
    findPath(n1, n2, isVisited, pathList);

    ArrayList<Node> best_path = null;
    for (ArrayList<Node> path : temp_paths) {
      if (null==best_path || best_path.size()>path.size()) best_path = path;
    }
    return best_path;
  }

  private void findPath(Node n1, Node n2, ArrayList<Node> isVisited, ArrayList<Node> localPathList) {

    // Mark the current node
    isVisited.add(n1);

    if (n1.equals(n2)) temp_paths.add(new ArrayList<>(localPathList));

    // Recur for all the vertices adjacent to current vertex
    for (Node i : n1.getNeighbours(true)) {
      if (!isVisited.contains(i)) {
        // store current node in path[]
        localPathList.add(i);
        findPath(i, n2, isVisited, localPathList);

        // remove current node in path[]
        localPathList.remove(i);
      }
    }
    // Mark the current node
    isVisited.remove(n1);
  }

  public Node getNode(int x, int y) {
    for (Node n : nodes) {
      if (n.getX() == x && n.getY() == y) return n;
    }
    return new Node(x,y);
  }

  public void printGrid(int maxLines) {
    int counter;
    for(Node n : nodes) {
      counter = 0;
      LCD.clear();
      LCD.drawString("Edges of "+ n.toString() +" (p "+counter+")", 0, 0);
      Iterator it = n.getNeighbours().iterator();
      for(int i=1; i<maxLines+1 && it.hasNext(); i++) {
        LCD.drawString(it.next().toString(),0,i);
      }
      Button.ENTER.waitForPressAndRelease();
      counter++;
    }
    LCD.clear();
    LCD.drawString("No more edges.",0,0);
    Button.ENTER.waitForPressAndRelease();

    LCD.clear();
    int line = 1;
    LCD.drawString("Obstacles found:",0,0);
    for (Node from : obstacles.keySet()) {
      ArrayList<Node> to = obstacles.get(from);
      LCD.drawString(from.toString()+": "+to.toString(),0,line);
    }
    Button.ENTER.waitForPressAndRelease();
  }

}
