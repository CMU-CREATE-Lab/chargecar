package edu.cmu.ri.createlab;

import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class BoundedIntegerSequenceNumber
   {

   private final Lock lock = new ReentrantLock();
   private final int min;
   private final int max;
   private int sequenceNumber;

   public BoundedIntegerSequenceNumber(final int min, final int max)
      {
      if (min >= max)
         {
         throw new IllegalArgumentException("The min value [" + min + "] must be less than the max value [" + max + "]");
         }

      this.min = min;
      this.max = max;

      sequenceNumber = min;
      }

   public int getMin()
      {
      return min;
      }

   public int getMax()
      {
      return max;
      }

   public int next()
      {
      lock.lock();  // block until condition holds
      try
         {
         final int val = sequenceNumber++;
         if (sequenceNumber > max)
            {
            sequenceNumber = min;
            }
         return val;
         }
      finally
         {
         lock.unlock();
         }
      }

   public static void main(final String[] args) throws InterruptedException
      {
      final BoundedIntegerSequenceNumber sequenceNumber = new BoundedIntegerSequenceNumber(0, 20);

      final SequenceNumberGetter t1 = new SequenceNumberGetter(1, sequenceNumber);
      final SequenceNumberGetter t2 = new SequenceNumberGetter(2, sequenceNumber);
      final SequenceNumberGetter t3 = new SequenceNumberGetter(3, sequenceNumber);
      final SequenceNumberGetter t4 = new SequenceNumberGetter(4, sequenceNumber);
      final SequenceNumberGetter t5 = new SequenceNumberGetter(5, sequenceNumber);

      t1.start();
      t2.start();
      t3.start();
      t4.start();
      t5.start();

      Thread.sleep(200);
      }

   private static final class SequenceNumberGetter
      {
      private final int threadNumber;
      private final BoundedIntegerSequenceNumber sequenceNumber;
      private final ScheduledExecutorService scheduledExecutorService;

      private SequenceNumberGetter(final int threadNumber, final BoundedIntegerSequenceNumber sequenceNumber)
         {
         this.threadNumber = threadNumber;
         this.sequenceNumber = sequenceNumber;
         this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("SequenceNumberGetter " + threadNumber));
         }

      public void start()
         {
         scheduledExecutorService.scheduleAtFixedRate(
               new Runnable()
               {
               public void run()
                  {
                  System.out.printf("Thread [%2d] got [%3d]\n", threadNumber, sequenceNumber.next());
                  }
               },
               0, 50, TimeUnit.MILLISECONDS);
         }
      }
   }


