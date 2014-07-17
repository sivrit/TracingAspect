package fr.sivrit.tracing.aj;

public aspect TracingAspect {
   // Trace all method calls, but make sure we do not intercept ourselves
   pointcut allCalls() : call(* *.*(..)) && !within(fr.sivrit.tracing.aj.*) ;

   before() : allCalls() {
      Tracer.INSTANCE.traceIn(thisJoinPointStaticPart);
   }

   after() : allCalls() {
      Tracer.INSTANCE.traceOut();
   }
}
