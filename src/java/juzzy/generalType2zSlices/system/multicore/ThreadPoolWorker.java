package generalType2zSlices.system.multicore;

/**
 *
 * @author Paul Hyde
 */
class ThreadPoolWorker extends Object {
  private static int nextWorkerID = 0;

  private ObjectFIFO idleWorkers;

  private int workerID;

  private ObjectFIFO handoffBox;

  private Thread internalThread;

  private volatile boolean noStopRequested;

  public ThreadPoolWorker(ObjectFIFO idleWorkers) {
    this.idleWorkers = idleWorkers;

    workerID = getNextWorkerID();
    handoffBox = new ObjectFIFO(1); // only one slot

    // just before returning, the thread should be created and started.
    noStopRequested = true;

    Runnable r = new Runnable() {
      public void run() {
        try {
          runWork();
        } catch (Exception x) {
          // in case ANY exception slips through
          x.printStackTrace();
        }
      }
    };

    internalThread = new Thread(r);
    internalThread.start();
  }

  public static synchronized int getNextWorkerID() {
    // notice: synchronized at the class level to ensure uniqueness
    int id = nextWorkerID;
    nextWorkerID++;
    return id;
  }

  public void process(Runnable target) throws InterruptedException {
    handoffBox.add(target);
  }

  private void runWork() {
    while (noStopRequested) {
      try {
        //System.out.println("workerID=" + workerID + ", ready for work");

        idleWorkers.add(this);

        Runnable r = (Runnable) handoffBox.remove();

        //System.out.println("workerID=" + workerID + ", starting execution of new Runnable: " + r);
        runIt(r);
      } catch (InterruptedException x) {
        Thread.currentThread().interrupt(); // re-assert
      }
      
    }
  }

  private void runIt(Runnable r) {
    try {
      r.run();
    } catch (Exception runex) {
      System.err.println("Uncaught exception fell through from run()");
      runex.printStackTrace();
    } finally {
      Thread.interrupted();
    }
  }

  public void stopRequest() {
    System.out
        .println("workerID=" + workerID + ", stopRequest() received.");
    noStopRequested = false;
    internalThread.interrupt();
  }

  public boolean isAlive() {
    return internalThread.isAlive();
  }
}
