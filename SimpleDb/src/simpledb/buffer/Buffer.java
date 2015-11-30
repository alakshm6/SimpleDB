package simpledb.buffer;

import java.util.ArrayList;

import simpledb.server.SimpleDB;
import simpledb.buffer.IStatistics.Stats;
import simpledb.file.*;

/**
 * An individual buffer. A buffer wraps a page and stores information about its status, such as the
 * disk block associated with the page, the number of times the block has been pinned, whether the
 * contents of the page have been modified, and if so, the id of the modifying transaction and the
 * LSN of the corresponding log record.
 * 
 * @author Edward Sciore
 */
public class Buffer {
  ArrayList<String> stats = new ArrayList<>();

  private Page contents = new Page();
  private Block blk = null;
  private int pins = 0;
  private int modifiedBy = -1; // negative means not modified
  private int logSequenceNumber = -1; // negative means no corresponding log record
  private BasicBufferMgr bufferMgr = null;
  private int BUFFER_ID = 0;
  private int writes = 0; // Maintains number of times a buffer has been flushed/written to disk

  private int totalFeches = 0;

  /**
   * Creates a new buffer, wrapping a new {@link simpledb.file.Page page}. This constructor is
   * called exclusively by the class {@link BasicBufferMgr}. It depends on the
   * {@link simpledb.log.LogMgr LogMgr} object that it gets from the class
   * {@link simpledb.server.SimpleDB}. That object is created during system initialization. Thus
   * this constructor cannot be called until
   * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or is called first.
   */
  public Buffer() {}

  public Buffer(BasicBufferMgr basicBufferMgr, int id) {
    this.bufferMgr = basicBufferMgr;
    this.BUFFER_ID = id;
  }

  /**
   * Returns the integer value at the specified offset of the buffer's page. If an integer was not
   * stored at that location, the behavior of the method is unpredictable.
   * 
   * @param offset the byte offset of the page
   * @return the integer value at that offset
   */
  public int getInt(int offset) {
    return contents.getInt(offset);
  }

  /**
   * Returns the string value at the specified offset of the buffer's page. If a string was not
   * stored at that location, the behavior of the method is unpredictable.
   * 
   * @param offset the byte offset of the page
   * @return the string value at that offset
   */
  public String getString(int offset) {
    return contents.getString(offset);
  }

  /**
   * Writes an integer to the specified offset of the buffer's page. This method assumes that the
   * transaction has already written an appropriate log record. The buffer saves the id of the
   * transaction and the LSN of the log record. A negative lsn value indicates that a log record was
   * not necessary.
   * 
   * @param offset the byte offset within the page
   * @param val the new integer value to be written
   * @param txnum the id of the transaction performing the modification
   * @param lsn the LSN of the corresponding log record
   */
  public void setInt(int offset, int val, int txnum, int lsn) {
    modifiedBy = txnum;
    if (lsn >= 0) {
      bufferMgr.getLsnMap().remove(logSequenceNumber);
      logSequenceNumber = lsn;
      bufferMgr.getLsnMap().put(logSequenceNumber, this);

    }
    contents.setInt(offset, val);
  }

  /**
   * Writes a string to the specified offset of the buffer's page. This method assumes that the
   * transaction has already written an appropriate log record. A negative lsn value indicates that
   * a log record was not necessary. The buffer saves the id of the transaction and the LSN of the
   * log record.
   * 
   * @param offset the byte offset within the page
   * @param val the new string value to be written
   * @param txnum the id of the transaction performing the modification
   * @param lsn the LSN of the corresponding log record
   */
  public void setString(int offset, String val, int txnum, int lsn) {
    modifiedBy = txnum;
    if (lsn >= 0) {
      bufferMgr.getLsnMap().remove(logSequenceNumber);
      logSequenceNumber = lsn;
      bufferMgr.getLsnMap().put(logSequenceNumber, this);
    }
    contents.setString(offset, val);
  }

  /**
   * Returns a reference to the disk block that the buffer is pinned to.
   * 
   * @return a reference to a disk block
   */
  public Block block() {
    return blk;
  }

  /**
   * Writes the page to its disk block if the page is dirty. The method ensures that the
   * corresponding log record has been written to disk prior to writing the page to disk.
   */
  void flush() {
    if (modifiedBy >= 0) {
      SimpleDB.logMgr().flush(logSequenceNumber);
      contents.write(blk);
      modifiedBy = -1;
      writes++;
      // stats.add("[" + BUFFER_ID + "]" + " Writing block " + blk.number() + " to disk");
      // stats.add("[" + BUFFER_ID + "]" + " Number of writes : " + writes);
      // TODO: Task3 can go here.
    }
  }

  /**
   * Increases the buffer's pin count.
   */
  void pin() {
    pins++;
    // stats.add("[" + BUFFER_ID + "]" + " Number of reads : " + pins);
    totalFeches++;
  }

  /**
   * Decreases the buffer's pin count.
   */
  void unpin() {
    pins--;
    if (!isPinned()) {
      bufferMgr.getLsnMap().put(logSequenceNumber, this);
    }
    // stats.add("[" + BUFFER_ID + "]" + " Number of reads : " + pins);
  }

  /**
   * Returns true if the buffer is currently pinned (that is, if it has a nonzero pin count).
   * 
   * @return true if the buffer is pinned
   */
  boolean isPinned() {
    return pins > 0;
  }

  /**
   * Returns true if the buffer is dirty due to a modification by the specified transaction.
   * 
   * @param txnum the id of the transaction
   * @return true if the transaction modified the buffer
   */
  boolean isModifiedBy(int txnum) {
    return txnum == modifiedBy;
  }

  /**
   * Reads the contents of the specified block into the buffer's page. If the buffer was dirty, then
   * the contents of the previous page are first written to disk.
   * 
   * @param b a reference to the data block
   */
  void assignToBlock(Block b) {
    flush();
    blk = b;
    contents.read(blk);
    pins = 0;
    // stats.add("[" + BUFFER_ID + "]" + "Read block " + blk.number() + " from disk");
  }

  /**
   * Initializes the buffer's page according to the specified formatter, and appends the page to the
   * specified file. If the buffer was dirty, then the contents of the previous page are first
   * written to disk.
   * 
   * @param filename the name of the file
   * @param fmtr a page formatter, used to initialize the page
   */
  void assignToNew(String filename, PageFormatter fmtr) {

    flush();
    fmtr.format(contents);
    blk = contents.append(filename);
    pins = 0;
    // stats.add("[" + BUFFER_ID + "]" + "Read block " + blk.number() + " from disk");
  }

  @Override
  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append(BUFFER_ID);
    return str.toString();
  }

  public Stats getStatistics() {
    System.out.println("======================");
    System.out.println("BUFFER_ID :"+BUFFER_ID);
    System.out.println("WRITES :"+writes);
    System.out.println("TOTAL-FETCHES :"+totalFeches);
    System.out.println("======================");
    return new Stats(writes, totalFeches, BUFFER_ID);
  }
}
