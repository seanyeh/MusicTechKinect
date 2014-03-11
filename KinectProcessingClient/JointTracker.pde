class JointTracker {
  int pos, joint;
  boolean isUpdated;
  String name;

  public JointTracker(int joint, String name){
    this.joint = joint;
    this.name = name;

    isUpdated = false;
    pos = 0;
  }

  public void update(SimpleOpenNI context){
    PVector jointPos = new PVector();
    context.getJointPositionSkeleton(0, this.joint, jointPos);

    // hacky +800 now for removing negative values. change later
    int newPos = int(jointPos.x) + 800;

    if (pos == newPos){
      this.isUpdated = false;
    } else{
      this.isUpdated = true;
      this.pos = newPos;
    }
  }

  public boolean isUpdated(){ return this.isUpdated; }

  public int getPos(){ return this.pos; }
  public String getName(){ return this.name; }
}

