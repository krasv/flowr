/*******************************************************************************
 * Copyright (c) 2008 flowr.org - all rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License (EPL) v1.0. The EPL is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: flowr.org - initial API and implementation
 ******************************************************************************/
package org.flowr.utils.logging;

import java.text.MessageFormat;

import org.flowr.utils.collections.Composite;

/**
 * Execution time measurement. Usage
 * 
 * <pre>
 * class Foo {
 * 
 *    public static void main(String... args) {
 *          TimeMeasurement h = TimeMeasurement.start(&quot;Foo.main&quot;)
 *          try {
 *              new Foo().bar();
 *          } finally {
 *              long duration = h.done();
 *              System.out.printf(&quot;Total execution time %d&quot;, duration);
 *          }
 *      }
 * 
 *    Foo() {
 *       TimeMeasurement h = TimeMeasurement.start(&quot;Foo()&quot;);
 *       try {
 *          ...
 *       }
 *       finally {
 *          h.done();
 *       }
 *    }
 * 
 *    void bar() {
 *       TimeMeasurement h = TimeMeasurement.start(&quot;Foo.bar()&quot;);
 *       try {
 *          ...
 *       }
 *       finally {
 *          h.done();
 *       }
 *    }
 * }
 * 
 * </pre>
 * 
 * @author <a href="mailto:skrause@flowr.org">sven.krause</a>
 */
public final class TimeMeasurement extends Composite<String> {

   private static ThreadLocal<TimeMeasurement> threadLocal = new ThreadLocal<TimeMeasurement>();
   private static boolean keepThreadName = true;
   private static String dumpFormat = "{0} [{1}] {2}: {3,number,integer} ms [{4,number,integer} - {5,number,integer}]";
   private static String dumpFormatQ = "{0} [{1}] {2}: {3,number,integer} ms [{4,number,integer} - {5,number,integer}] {6}";
   private static String lead = "  ";

   /**
    * @param keepThreadName the keepThreadName to set
    */
   public static void setKeepThreadName(boolean keepThreadName) {
      TimeMeasurement.keepThreadName = keepThreadName;
   }

   /**
    * @return the keepThreadName
    */
   public static boolean isKeepThreadName() {
      return keepThreadName;
   }

   /**
    * @param dumpFormat the dumpFormat to set
    */
   public static void setDumpFormat(String dumpFormat) {
      TimeMeasurement.dumpFormat = dumpFormat;
   }

   /**
    * @return the dumpFormat
    */
   public static String getDumpFormat() {
      return dumpFormat;
   }

   /**
    * @param lead the lead to set
    */
   public static void setLead(String lead) {
      TimeMeasurement.lead = lead;
   }

   /**
    * @return the lead
    */
   public static String getLead() {
      return lead;
   }

   /**
    * begins an new TimeMeasurement block.
    *
    * @param id a block label.
    * @return the block handle
    */
   public static TimeMeasurement start(String id) {
      return start(id, null, (Object[]) null);
   }

   public static TimeMeasurement start() {
      StackTraceElement[] stackTrace = new Exception().getStackTrace();
      StackTraceElement stackTraceElement = stackTrace[1];
      return start(stackTraceElement.toString(), null, (Object[]) null);
   }

   public static TimeMeasurement start(String methodPattern, Object... args) {
      StackTraceElement[] stackTrace = new Exception().getStackTrace();
      StackTraceElement stackTraceElement = stackTrace[1];
      return start(stackTraceElement.toString(), methodPattern, args);
   }

   private static TimeMeasurement start(String id, String methodPattern, Object... args) {
      TimeMeasurement element = new TimeMeasurement(id, threadLocal.get(), methodPattern, args);
      threadLocal.set(element);
      return threadLocal.get();
   }

   private long start;
   private long end;
   private String threadName = "";
   private final String methodPattern;
   private final Object[] args;

   private TimeMeasurement(String id, TimeMeasurement parent, String methodPattern, Object... args) {
      super(parent, id);
      this.methodPattern = methodPattern;
      this.args = args;
      if (keepThreadName) {
         this.threadName = Thread.currentThread().getName();
      }
      this.start = System.currentTimeMillis();
      this.end = 0;
   }

   /**
    * @return the methodPattern
    */
   public String getMethodPattern() {
      return methodPattern;
   }

   /**
    * @return the args
    */
   public Object[] getArgs() {
      return args;
   }

   /**
    * gets the thead' name this measurement block covers
    *
    * @return the threadName
    */
   public String getThreadName() {
      return threadName;
   }

   /**
    * determines, if the block end is already reached.
    *
    * @return
    */
   public boolean isDone() {
      TimeMeasurement parent = getParentNode();
      return end != 0 || (parent != null && parent.isDone());
   }

   private TimeMeasurement getParentNode() {
      return (TimeMeasurement) getParent();
   }

   /**
    * dedicates the end of the measurement block
    *
    * @return the consumed block execution time
    */
   public long done() {
      if (!isDone()) {
         this.end = System.currentTimeMillis();
         TimeMeasurement parent = getParentNode();
         if (parent != null) {
            threadLocal.set(parent);
         } else {
            threadLocal.remove();
         }
      }
      return time();
   }

   /**
    * gets the start time stamp
    * 
    * @return the start
    */
   public long getStart() {
      return start;
   }

   /**
    * gets the end time stamp. The end time is only available. if this block or one of the enclosing blocks has been
    * closed.
    * 
    * @return the blocks end time.
    * @see #isDone()
    */
   public long getEnd() {
      TimeMeasurement parent = getParentNode();
      if (end == 0 && parent != null) {
         return parent.getEnd();
      } else {
         return end;
      }
   }

   /**
    * determines the total block execution time, if the blocks end has been reached.
    * 
    * @return the execution time, if the block end has been reached - otherwise <code>-1</code>
    */
   public long time() {
      if (!isDone()) {
         return -1;
      }
      return getEnd() - start;
   }

   private static String lead(int count) {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < count; i++) {
         b.append(lead);
      }
      return b.toString();
   }

   /**
    * gets the execution time tree
    * 
    * @return
    */
   public String asTree() {
      StringBuilder b = new StringBuilder();
      for (CompositeIterator<Composite< ? >> it = elements(0); it.hasNext();) {
         TimeMeasurement c = (TimeMeasurement) it.next();
         String dump =
            MessageFormat.format(dumpFormat, lead(it.level()), c.getThreadName(), c.getObject(), c.time(), c.getEnd(), c.getStart());
         b.append(dump);
         b.append("\n");
      }
      return b.toString();
   }

   /**
    * gets the execution time tree with detailed debug information
    * 
    * @return
    */
   public String asQualifiedTree() {
      StringBuilder b = new StringBuilder();
      for (CompositeIterator<Composite< ? >> it = elements(0); it.hasNext();) {
         TimeMeasurement c = (TimeMeasurement) it.next();
         if (c.getMethodPattern() != null && c.getArgs() != null && c.getArgs().length > 0) {
            String fmt = MessageFormat.format(c.getMethodPattern(), c.getArgs());
            String dump =
               MessageFormat.format(dumpFormatQ, lead(it.level()), c.getThreadName(), fmt, c.time(), c.getEnd(), c.getStart(), c
                  .getObject());
            b.append(dump);
         } else {
            String dump =
               MessageFormat.format(dumpFormat, lead(it.level()), c.getThreadName(), c.getObject(), c.time(), c.getEnd(), c.getStart());
            b.append(dump);
         }
         b.append("\n");
      }
      return b.toString();
   }

   @Override
   public String toString() {
      StringBuilder b = new StringBuilder();
      if (isKeepThreadName()) {
         b.append("[").append(getThreadName()).append("] ");
      }
      b.append(getObject()).append(": ");
      b.append(time()).append(" [").append(getEnd()).append(" - ").append(start).append("]");
      return b.toString();
   }

}
