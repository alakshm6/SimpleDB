package simpledb.server;

import simpledb.remote.*;
import java.rmi.registry.*;

public class Startup {
  private static final boolean HACK = true;

  public static void main(String args[]) throws Exception {
    if (HACK) {
      args = new String[1];
      args[0] = "simpleDB";
    } // configure and initialize the database
    SimpleDB.init(args[0]);

    // create a registry specific for the server on the default port
    Registry reg = LocateRegistry.createRegistry(1099);

    // and post the server entry in it
    RemoteDriver d = new RemoteDriverImpl();
    reg.rebind("simpledb", d);

    System.out.println("database server ready");
  }
}
