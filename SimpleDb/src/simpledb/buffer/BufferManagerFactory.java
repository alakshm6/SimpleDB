package simpledb.buffer;

public class BufferManagerFactory {
  public static enum BufferManagerType {
    BASIC,LRU
  }

  static BasicBufferMgr getInstance(BufferManagerType type, int numbuffs) {
    switch(type){
      case BASIC:
        return new BasicBufferMgr(numbuffs);
      case LRU:
        return new LRUBufferMgr(numbuffs);
        
        default:
          //TODO: Throw an exception
    }
    return null;
  }
}
