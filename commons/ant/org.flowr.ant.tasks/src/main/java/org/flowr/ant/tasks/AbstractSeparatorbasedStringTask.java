/**
 * DB Systel GmbH / i.S.A. Dresden GmbH & Co. KG (c) 2010
 */
package org.flowr.ant.tasks;

import org.apache.tools.ant.BuildException;

/**
 * basic string manipulation ant tasks
 * 
 * @author SvenKrause
 * 
 */
public abstract class AbstractSeparatorbasedStringTask extends AbstractStringTask {

   protected String separator;

   /**
    * set up the default values
    */
   @Override
   public void init() throws BuildException {
      super.init();
      separator = ".";
   }

   /**
    * sets the string operation text separator.
    * 
    * @param separator
    */
   public void setSeparator(String separator) {
      this.separator = separator;
   }

}
