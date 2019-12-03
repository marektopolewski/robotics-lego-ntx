import java.util.ArrayList;
import java.util.Random;

public class Node {

    private int x, y;
    private boolean explored;
    private ArrayList<Node> neighbours;

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.explored = false;
        this.neighbours = new ArrayList<>();
    }

    public void makeNeighbours() {
      addIfValidNode(x+1,y);
      addIfValidNode(x,y+1);
      addIfValidNode(x-1,y);
      addIfValidNode(x,y-1);
    }

    private void addIfValidNode(int x, int y) {
      if (x<0 || y<0) return;

      Node temp_neigh = MazeRunner.grid.getNode(x,y);
      if (MazeRunner.grid.isObstacle(this, temp_neigh)) return;

      this.neighbours.add(MazeRunner.grid.getNode(x,y));
    }

    public ArrayList<Node> getNeighbours(boolean exp) {
      ArrayList<Node> result = new ArrayList<>();
      for(Node neighbour : this.neighbours) {
        if(neighbour.getExplored() == exp) result.add(neighbour);
      }
      return result;
    }

    public Node getRandNeigbour() {
      int size = this.getNeighbours(false).size();
      Random rand = new Random();
      return this.getNeighbours(false).get(rand.nextInt(size));
    }

    public void removeNeighbour(Node n) {
      this.neighbours.remove(n);
    }

    public ArrayList<Node> getNeighbours() {
      return this.neighbours;
    }

    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public void setExplored(boolean explored) {
      this.explored = explored;
    }

    public boolean getExplored() {
      return this.explored;
    }

    public boolean equals(Node node) {
      if (node == null) {
        return false;
      }
      if (this.x==node.getX() && this.y==node.getY()) {
        return true;
      }
      return false;
    }

    public String toString() {
      return "(" + this.x + "," + this.y + ")";
    }
}
