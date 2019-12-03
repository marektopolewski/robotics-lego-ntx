import lejos.nxt.*;
import lejos.robotics.navigation.*;
import java.util.*;
import java.lang.Math;

class Robot {

  // need a new class for calulating how to move
  // given from=(x,y,heading) and to=(x,y)

  // results of move operation
  public static final int OBSTACLE = 1;
  public static final int SUCCESS  = 0;
  public static final int ERROR    = -1;
  public static final int NO_MOVE  = -2;
  public static final int NO_PATH  = -3;

  // values for grid recognision
  private static final int JUNCTION = 0;
  private static final int CORRIDOR = 1;
  private static final int DEADEND  = 2;

  // values for directions
  public static final int H_NORTH = 0;
  public static final int H_EAST  = 1;
  public static final int H_SOUTH = 2;
  public static final int H_WEST  = 3;

  // movement and rotation speed
  // private static final double SPEED_NORMAL = 2;
  // private static final double SPEED_SLOW = SPEED_NORMAL/2;
  // private static final double ROTATE_NORMAL = 222;
  // private static final double ROTATE_SLOW = ROTATE_NORMAL/20;
  private static final double SPEED_NORMAL = 30;
  private static final double SPEED_SLOW = SPEED_NORMAL/2;
  private static final double ROTATE_NORMAL = 150;
  private static final double ROTATE_SLOW = ROTATE_NORMAL/20;

  // values for the light sensors
  private static int LS_LIGHT_R;
  private static int LS_LIGHT_L;
  private static final int LS_MARGIN = 10;

  // config
  private static final double OBSTACLE_DISTANCE = 20;
  private static final double BASE_ANGLE        = 95;

  // sensor and pilot definitions
  private DifferentialPilot pilot;
  private LightSensor lightSensorL, lightSensorR;
  private UltrasonicSensor sonar;


  // constructor
  public Robot() {
    sonar = new UltrasonicSensor(SensorPort.S3);
    lightSensorL = new LightSensor(SensorPort.S1);
    lightSensorR = new LightSensor(SensorPort.S2);
    pilot = new DifferentialPilot(2.25f, 6.65f, Motor.A, Motor.B);

    LS_LIGHT_L = lightSensorL.getLightValue();
    LS_LIGHT_R = lightSensorR.getLightValue();
    pilot.setTravelSpeed(5);
  }

  public int moveTo(Node from, Node to, int heading) {

    // sanity check - move only to adjecent
    int manhattan = Math.abs((int) (from.getX()-to.getX())) + Math.abs((int) (from.getY()-to.getY()));
    if(manhattan > 1) {
      return ERROR;
    }

    // sanity check - same coord, so the robot does not move
    if(from.getX()==to.getX() && from.getY()==to.getY()) {
      return NO_MOVE;
    }

    // obstacle already detected
    if(MazeRunner.grid.isObstacle(from, to)) {
      return NO_PATH;
    }

    // calc angle and rotate in the direction of target node
    double angle = Robot.getRotation(from, to, heading);
    boolean pathExists = rotateRobot(0.5*angle, 0.7*angle);

    if (!pathExists) return NO_PATH;

    int result = SUCCESS;
    result = followLine(result);

    pilot.stop();
    return result;
  }

  public int followPath(ArrayList<Node> path, int heading) {
    Node node_at = path.get(0);
    path.remove(node_at);
    for(Node node_to : path) {
      int result = this.moveTo(node_at, node_to, heading);
      heading = this.getHeading(node_at, node_to);
    }
    return heading;
  }

  private int followLine(int result) {
    int inten_left = lightSensorL.getLightValue();
    int inten_right = lightSensorR.getLightValue();

    while(true) {
      pilot.forward();

      // if obstacle found: stop, rotate 180 degrees and walk back
      if(detectedObstacle()) {
        pilot.stop();
        result = OBSTACLE;
        pilot.rotate(BASE_ANGLE*2);
        pilot.forward();
      }

      inten_left = lightSensorL.getLightValue();
      inten_right = lightSensorR.getLightValue();

      // junction
      if (LS_LIGHT_L - inten_left > LS_MARGIN && LS_LIGHT_R - inten_right > LS_MARGIN) {
        pilot.stop();
        centre(3);
        return result;
      }
      // recalibrate line
      else if (LS_LIGHT_L - inten_left > LS_MARGIN && LS_LIGHT_R - inten_right <= LS_MARGIN) {
        pilot.stop();
        pilot.setRotateSpeed(ROTATE_SLOW);
        pilot.rotate(5,true);
        while(pilot.isMoving()) {
          if (LS_LIGHT_L - lightSensorL.getLightValue() > LS_MARGIN && LS_LIGHT_R - lightSensorR.getLightValue() > LS_MARGIN) {
            pilot.stop();
          }
        }
        pilot.setRotateSpeed(ROTATE_NORMAL);
      }
      else if (LS_LIGHT_L - inten_left <= LS_MARGIN && LS_LIGHT_R - inten_right > LS_MARGIN) {
        pilot.stop();
        pilot.setRotateSpeed(ROTATE_SLOW);
        pilot.rotate(-5, true);
        while(pilot.isMoving()) {
          if (LS_LIGHT_L - lightSensorL.getLightValue() > LS_MARGIN && LS_LIGHT_R - lightSensorR.getLightValue() > LS_MARGIN) {
            pilot.stop();
          }
        }
        pilot.setRotateSpeed(ROTATE_NORMAL);
      }

    }
  }

  public static int getHeading(Node from, Node to) {

    if (from.getX()>to.getX())
      return H_WEST;
    if (from.getX()<to.getX())
      return H_EAST;
    if (from.getY()>to.getY())
      return H_SOUTH;
    if (from.getY()<to.getY())
      return H_NORTH;

    return -1;
  }

  private static double getRotation(Node from, Node to, int from_dir) {
    // find relative position of the target node
    int to_dir = Robot.getHeading(from, to);

    // check if a 180 turn
    if (Math.abs(from_dir-to_dir)==2) return BASE_ANGLE * 2;

    return (from_dir-to_dir)%2 * -BASE_ANGLE;
  }

  public boolean rotateRobot(double init_angle, double scan_angle) {

    if (init_angle==0) {
      boolean pathFound1 = rotateRobot(0, 20);
      boolean pathFound2 = rotateRobot(-20, -20);
      MazeRunner.debug("ang=0, "+pathFound1+" "+pathFound2);
      return pathFound1 || pathFound2;
    }

    pilot.rotate(init_angle);
    pilot.setRotateSpeed(ROTATE_SLOW);

    LightSensor outerLS = (scan_angle<0) ? lightSensorR : lightSensorL;
    int default_lv = (scan_angle<0) ? LS_LIGHT_R : LS_LIGHT_L;

    pilot.rotate(scan_angle, true);
    boolean isPath = false;

    while(pilot.isMoving()) {
      if (default_lv - outerLS.getLightValue() > LS_MARGIN) {
        isPath = true;
        pilot.stop();
      }
    }
    pilot.setRotateSpeed(ROTATE_NORMAL);

    // if no path, then rotate back
    if(!isPath) pilot.rotate(-init_angle-scan_angle);

    return isPath;
  }

  public void centre(int dist) {
    pilot.travel(dist);
  }

  private boolean detectedObstacle() {
    return sonar.getDistance() < OBSTACLE_DISTANCE;
  }
}
