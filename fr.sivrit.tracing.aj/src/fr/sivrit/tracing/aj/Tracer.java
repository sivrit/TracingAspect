package fr.sivrit.tracing.aj;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.zip.GZIPOutputStream;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.aspectj.lang.JoinPoint;

/**
 * This class does the actual tracing
 */
public class Tracer {
   /** This class is a singleton */
   public final static Tracer INSTANCE;
   static {
      try {
         INSTANCE = new Tracer();
      } catch (final Exception e) {
         e.printStackTrace();
         throw new Error(e);
      }
   }

   private final PrintWriter output;

   /** MBean holding our configuration */
   private final TracerConfigurationMBean mbean;

   /** Current indentation level per thread. Used when making pretty outputs. */
   private final ThreadLocal<Integer> indentation = new ThreadLocal<Integer>() {
      @Override
      protected Integer initialValue() {
         return 0;
      }
   };

   /**
    * Used to remember if we have already written the thread's name when using
    * dense outputs.
    */
   private final ThreadLocal<String> threadName = new ThreadLocal<String>();

   /** We keep a StringBuilder per thread to avoid spewing too much garbage */
   private final ThreadLocal<StringBuilder> builder = new ThreadLocal<StringBuilder>() {
      @Override
      protected StringBuilder initialValue() {
         return new StringBuilder();
      }
   };

   private Tracer() throws InstanceAlreadyExistsException,
         MBeanRegistrationException, NotCompliantMBeanException,
         MalformedObjectNameException, IOException {
      super();

      // register the MBean that acts as out public interface
      mbean = new TracerConfigurationMBeanImpl();
      final ObjectName mbeanName = new ObjectName(
            "tracing:type=TracerConfigurationMBean");
      ManagementFactory.getPlatformMBeanServer()
            .registerMBean(
                  new StandardMBean(mbean, TracerConfigurationMBean.class),
                  mbeanName);

      // Create out output
      final String fileName = mbean.getFileName();
      if (fileName.endsWith(".gz")) {
         output = new PrintWriter(new GZIPOutputStream(new FileOutputStream(
               fileName), 8 * 1024));
      } else {
         output = new PrintWriter(fileName);
      }

      // Make sure we flush when the JVM exits
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            output.flush();
            output.close();
         }
      });
   }

   /**
    * This method defines how a method call is written in the logs
    * 
    * @param joinPoint
    * @return
    */
   private String joinPointToString(final JoinPoint.StaticPart joinPoint) {
      return joinPoint.getSignature().toString();
   }

   /**
    * Trace when we go into a method call
    * 
    * @param joinPoint
    *           the call join point we are going in
    */
   public final void traceIn(final JoinPoint.StaticPart joinPoint) {
      if (mbean.isActivated()) {
         final StringBuilder buffer = builder.get();
         buffer.setLength(0);

         final String joinPointStr = joinPointToString(joinPoint);
         if (mbean.isPretty()) {
            // Pretty is
            // [tread name][indentation] -> [method signature]

            final String threadName = Thread.currentThread().getName();
            final Integer indent = indentation.get();

            buffer.append(threadName);
            for (int i = indent; i >= 0; i--) {
               buffer.append(' ');
            }
            buffer.append(" -> ");
            buffer.append(joinPointStr);

            indentation.set(indent + 1);
         } else {
            final long id = Thread.currentThread().getId();

            // First time we see a thread we write
            // t[thread id]=[thread name]
            if (threadName.get() == null) {
               final String name = Thread.currentThread().getName();
               threadName.set(name);

               buffer.append('t');
               buffer.append(id);
               buffer.append('=');
               buffer.append(name.contains("\\n") ? name.replaceAll("\\n", "")
                     : name);
               buffer.append('\n');
            }

            // dense output is
            // [thread id]>[method signature]
            buffer.append(id);
            buffer.append(">");
            buffer.append(joinPointStr);
         }

         output.println(buffer);
      }
   }

   /**
    * Trace when we go out of a method call
    */
   public final void traceOut() {
      if (mbean.isActivated()) {
         final StringBuilder buffer = builder.get();
         buffer.setLength(0);

         if (mbean.isPretty()) {
            // Pretty is
            // [tread name][indentation] <-

            final String threadName = Thread.currentThread().getName();
            final int indent = Math.max(0, indentation.get() - 1);

            buffer.append(threadName);
            for (int i = indent; i >= 0; i--) {
               buffer.append(' ');
            }
            buffer.append(" <-");

            indentation.set(indent);
         } else {
            // dense output is
            // [thread id]<

            final long id = Thread.currentThread().getId();
            buffer.append(id);
            buffer.append("<");
         }

         output.println(buffer);
      }
   }
}
