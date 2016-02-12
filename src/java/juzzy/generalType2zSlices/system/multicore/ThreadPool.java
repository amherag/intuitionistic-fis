

package generalType2zSlices.system.multicore;

/**
 *
 * @author Paul Hyde (modified by Christian Wagner
 */
class ThreadPool extends Object {
  private ObjectFIFO idleWorkers;

  private ThreadPoolWorker[] workerList;

  public ThreadPool(int numberOfThreads) {
    // make sure that it's at least one
    numberOfThreads = Math.max(1, numberOfThreads);

    idleWorkers = new ObjectFIFO(numberOfThreads);
    workerList = new ThreadPoolWorker[numberOfThreads];

    for (int i = 0; i < workerList.length; i++) {
      workerList[i] = new ThreadPoolWorker(idleWorkers);
    }
  }

  public void execute(Runnable target) throws InterruptedException {
    // block (forever) until a worker is available
    ThreadPoolWorker worker = (ThreadPoolWorker) idleWorkers.remove();
    worker.process(target);
  }

  public void stopRequestIdleWorkers() {
    try {
      Object[] idle = idleWorkers.removeAll();
      for (int i = 0; i < idle.length; i++) {
        ((ThreadPoolWorker) idle[i]).stopRequest();
      }
    } catch (InterruptedException x) {
      Thread.currentThread().interrupt(); // re-assert
    }
  }

  public void stopRequestAllWorkers() {
    stopRequestIdleWorkers();

    try {
      Thread.sleep(250);
    } catch (InterruptedException x) {
    }

    for (int i = 0; i < workerList.length; i++) {
      if (workerList[i].isAlive()) {
        workerList[i].stopRequest();
      }
    }
  }
  
  public int getNumberOfThreads()
  {
      return idleWorkers.getCapacity();
  }
  
  public int getNumberOfBusyThreads()
  {
      return workerList.length - idleWorkers.getSize();
  }
}