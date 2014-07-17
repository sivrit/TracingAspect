package fr.sivrit.tracing.aj;

/**
 * This MBean allows us to enable/disable tracing at runtime
 */
public interface TracerConfigurationMBean {

   /**
    * Setting this property to <code>true</code> on the JVM will enable tracing
    * at startup
    */
   public final static String ACTIVATED_PROPERTY = "tacer.activated";

   /**
    * Setting this property to <code>true</code> on the JVM will make pretty
    * tracing outputs
    */
   public final static String PRETTY_PROPERTY = "tacer.pretty";

   /**
    * File to use for tracing output. Defaults to {@value #DEFAULT_FILE}. If
    * present, <code>${time}</code> will be replaced by the time in
    * milliseconds. If the file name ends with <code>".gz"</code>, output will
    * be compressed.
    */
   public final static String FILE_PROPERTY = "tacer.file";

   public final static String DEFAULT_FILE = "${time}.trace";

   boolean isActivated();

   void setActivated(boolean activated);

   boolean isPretty();

   void setPretty(boolean pretty);

   String getFileName();
}
