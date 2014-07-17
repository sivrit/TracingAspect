package fr.sivrit.tracing.aj;

public class TracerConfigurationMBeanImpl implements TracerConfigurationMBean {

   /** we actively log when <code>true</code> */
   private volatile boolean activated;

   /** if <code>true</code>, logs will be pretty */
   private volatile boolean pretty;

   /** Name of the output file */
   private final String fileName;

   public TracerConfigurationMBeanImpl() {
      super();

      activated = "true".equalsIgnoreCase(System
            .getProperty(ACTIVATED_PROPERTY));

      pretty = "true".equalsIgnoreCase(System.getProperty(PRETTY_PROPERTY));

      fileName = System.getProperty(FILE_PROPERTY, DEFAULT_FILE).replaceAll(
            "\\$\\{time\\}", String.valueOf(System.currentTimeMillis()));
   }

   public boolean isActivated() {
      return activated;
   }

   public void setActivated(final boolean activated) {
      this.activated = activated;
   }

   public boolean isPretty() {
      return pretty;
   }

   public void setPretty(final boolean pretty) {
      this.pretty = pretty;
   }

   public String getFileName() {
      return fileName;
   }
}
