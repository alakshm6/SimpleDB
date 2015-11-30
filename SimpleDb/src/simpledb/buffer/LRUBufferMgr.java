package simpledb.buffer;

import java.util.Map;
import java.util.TreeMap;

import simpledb.file.Block;

public class LRUBufferMgr extends BasicBufferMgr implements IStatistics{

  LRUBufferMgr(int numbuffs) {
    super(numbuffs);
  }

  @Override
  synchronized Buffer pin(Block blk) {
    getStatistics();
    System.out.println("------------------------------------");
    Buffer buff = findExistingBuffer(blk);
    if (buff == null) {
      buff = chooseUnpinnedBuffer();
      if (buff == null) {
        return null;
      }
      if (buff.block() != null) {
        getBufferPoolMap().remove(buff.block());
      }
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
    if (map.size() == 0) {
      return null;
    }
    Buffer retBuf = null;
    int retLsn = Integer.MIN_VALUE;
    for (Map.Entry<Integer, Buffer> entry : map.entrySet()) {
      retBuf = entry.getValue();
      retLsn = entry.getKey();
      break;
    }
    map.remove(retLsn);
    return retBuf;
  }
}
