package banana.core.util;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
/**
 * 可计数的线程池
 */
public class CountableThreadPool implements Closeable{
	
	    private int threadNum;

	    private AtomicInteger threadAlive = new AtomicInteger();

	    private ReentrantLock reentrantLock = new ReentrantLock();

	    private Condition condition = reentrantLock.newCondition();
	    
	    private ThreadPoolExecutor executorService;

	    public CountableThreadPool(int threadNum) {
	        executorService = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	        setThread(threadNum);
	    }

	    public int getThreadAlive() {
	        return executorService.getActiveCount();
	    }

	    public int getThreadNum() {
	        return threadNum;
	    }
	    
	    public void setThread(int thread){
	    	threadNum = thread;
	    	executorService.setMaximumPoolSize(threadNum * 3);
	    }

	    public void execute(final Runnable runnable) {
	        if (threadAlive.get() >= threadNum) {
	            try {
	                reentrantLock.lock();
	                while (threadAlive.get() >= threadNum) {
	                    try {
	                        condition.await();
	                    } catch (InterruptedException e) {
	                    }
	                }
	            } finally {
	                reentrantLock.unlock();
	            }
	        }
	        threadAlive.incrementAndGet();
	        executorService.execute(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    runnable.run();
	                } finally {
	                    try {
	                        reentrantLock.lock();
	                        threadAlive.decrementAndGet();
	                        condition.signal();
	                    } finally {
	                        reentrantLock.unlock();
	                    }
	                }
	            }
	        });
	    }

	    public boolean isShutdown() {
	        return executorService.isShutdown();
	    }

	    public void shutdown() {
	        executorService.shutdown();
	    }
	    
	    public int getIdleThreadCount(){
	    	return threadNum - getThreadAlive();
	    }

		@Override
		public void close()  {
			executorService.shutdown();
			try {
				while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
					//The thread pool has no closing
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
}
