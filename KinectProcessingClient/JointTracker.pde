import java.util.Arrays;

class JointTracker {
  int[] pos;
  int joint;
  boolean isUpdated;
  String name;

  int MAX = 1600;

  public JointTracker(int joint, String name){
    this.joint = joint;
    this.name = name;

    isUpdated = false;
    pos = new int[3];
  }

  // Convert value to range from 0 to MAX
  public int normalizeValue(double d){
    int i = (int)d + MAX/2;
    if (i < 0){
      return 0;
    } else if (i > MAX){
      return MAX;
    } else{
      return i;
    }
  }

  public void update(SimpleOpenNI context){
    PVector jointPos = new PVector();
    context.getJointPositionSkeleton(0, this.joint, jointPos);

    int[] newPos = new int[]{ 
      normalizeValue(jointPos.x), 
      normalizeValue(jointPos.y),
      normalizeValue(jointPos.z)
    };

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

