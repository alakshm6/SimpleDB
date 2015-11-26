package simpledb.buffer;

import java.util.Map;
import java.util.TreeMap;

import simpledb.file.Block;

public class LRUBufferMgr extends BasicBufferMgr {

  LRUBufferMgr(int numbuffs) {
    super(numbuffs);
  }

  @Override
  synchronized Buffer pin(Block blk) {
    Buffer buff = findExistingBuffer(blk);
    if (buff == null) {
      buff = chooseUnpinnedBuffer();
      if (buff == null) {
        return null;
      }
      getBufferPoolMap().remove(buff.block());
      buff.assignToBlock(blk);
      getBufferPoolMap().put(blk, buff);
    }
    if (!buff.isPinned()) {
      numAvailable--;
    }
    buff.pin();
    return buff;
  }

  @Override
  synchronized void unpin(Buffer buff) {
    super.unpin(buff);
  }

  @Override
  protected Buffer findExistingBuffer(Block blk) {
    return getBufferPoolMap().get(blk);
  }

  @Override
  protected Buffer chooseUnpinnedBuffer() {
    TreeMap<Integer, Buffer> map = getLsnMap();
    int retLSN = Integer.MIN_VALUE;

    for (Map.Entry<Integer, Buffer> entry : map.entrySet()) {
      retLSN = entry.getKey();
      break;
    }

    Buffer retBuf = getLsnMap().get(retLSN);
    getLsnMap().remove(retLSN);
    return retBuf;
  }
}
