import java.util.Arrays;

class JointTracker {
  int[] pos;
  int[] joints;
  /* int joint; */
  JointTrackerType trackerType;

  boolean isUpdated;
  String name;

  int MAX = 1600;


  public JointTracker(String name, JointTrackerType trackerType, int ... joints){
    this.name = name;
    this.trackerType = trackerType;
    this.joints = joints;

    this.isUpdated = false;
    this.pos = new int[3];
  }

  public JointTracker(String name, int joint){
    this(name, JointTrackerType.POS, joint);
  }

  // Convert value to range from 0 to MAX
  public int normalizeValue(double d){
    int i = (int)d + MAX/2;
    /* if (i < 0){ */
    /*   return 0; */
    /* } else if (i > MAX){ */
    /*   return MAX; */
    /* } else{ */
    /*   return i; */
    /* } */
    return (int)d;
  }



  private int[] getJointValue(SimpleOpenNI context, int joint){
    PVector j = new PVector();
    context.getJointPositionSkeleton(0, joint, j);
    return new int[]{(int)j.x, (int)j.y, (int)j.z};
  }

  public int[] getValue(SimpleOpenNI context){
    if (this.trackerType == JointTrackerType.POS){
      return getJointValue(context, this.joints[0]);
    } 
    else if (this.trackerType == JointTrackerType.DIFF){
      assert this.joints.length == 2;

      int[] j1 = getJointValue(context, this.joints[0]);
      int[] j2 = getJointValue(context, this.joints[1]);
      return new int[]{j1[0] - j2[0], j1[1] - j2[1], j1[2] - j2[2]};
    }
    else{
      return null;
    }
  }


  public void update(SimpleOpenNI context){
    int[] newPos = getValue(context);

    if (Arrays.equals(pos, newPos)){
      this.isUpdated = false;
    } else{
      this.isUpdated = true;
      this.pos = newPos;
    }
  }

  public boolean isUpdated(){ return this.isUpdated; }

  public int[] getPos(){ 
    return this.pos;
  }
  public String getName(){ return this.name; }
}

