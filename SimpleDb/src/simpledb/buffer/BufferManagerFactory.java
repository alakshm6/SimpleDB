package simpledb.buffer;

public class BufferManagerFactory {
  public static enum BufferManagerType {
    BASIC,MRM
  }

  static BasicBufferMgr getInstnace(BufferManagerType type, int numbuffs) {
    switch(type){
      case BASIC:
        return new BasicBufferMgr(numbuffs);
      case MRM:
        return new MRMBufferMgr(numbuffs);
        
        default:
          //TODO: Throw an exception
    }
    return null;
  }
}
