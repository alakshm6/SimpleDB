package simpledb.buffer;

import java.util.ArrayList;



public interface IStatistics {
  public ArrayList<Stats> getStatistics();

  class Stats {
    public Stats(int writes, int totalFeches, int bufferId) {
      super();
      this.writes = writes;
      this.totalFeches = totalFeches;
      this.bufferId = bufferId;
    }

    int writes;
    int totalFeches;
    int bufferId;

    @Override
    public String toString() {
      return "Stats [writes=" + writes + ", totalFeches=" + totalFeches + ", bufferId=" + bufferId
          + "]";
    }

  }
}
