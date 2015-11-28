package simpledb.buffer;

public class BufferManagerFactory {
  public static enum BufferManagerType {
    BASIC,MRM
  }

  static BasicBufferMgr getInstance(BufferManagerType type, int numbuffs) {
    switch(type){
      case BASIC:
        return new BasicBufferMgr(numbuffs);
      case MRM:
        return new LRUBufferMgr(numbuffs);
        
        default:
          //TODO: Throw an exception
    }
    return null;
  }
}
