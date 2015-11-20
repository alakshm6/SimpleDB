package simpledb.buffer;

import java.util.HashSet;
import java.util.Map;

import simpledb.file.Block;

public class MRMBufferMgr extends BasicBufferMgr {

  MRMBufferMgr(int numbuffs) {
    super(numbuffs);
  }

  @Override
  synchronized Buffer pin(Block blk) {
    Buffer buff = findExistingBuffer(blk);
    if (buff == null) {
      buff = chooseUnpinnedBuffer();
      if (buff == null) {
        // TODO: not very sure of the impl here.
        for (Map.Entry<Integer, Buffer> entry : getLsnMap().entrySet()) {
          System.out.println("pin (), entry key :" + entry.getKey() + " entryVal:"
              + entry.getValue().toString());
          
          return entry.getValue();
        }
      }
      buff.assignToBlock(blk);
    }
    if (!buff.isPinned())
      numAvailable--;
    buff.pin();
    return buff;
  }

  @Override
  protected Buffer findExistingBuffer(Block blk) {
    return getBufferPoolMap().get(blk);
  }

  @Override
  protected Buffer chooseUnpinnedBuffer() {
    HashSet<Buffer> set = getUnpinnedBuffers();
    for (Buffer buff : set) {
      return buff;
    }
    return null;
  }

}
