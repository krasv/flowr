/**
 * DB Systel GmbH / i.S.A. Dresden GmbH & Co. KG (c) 2010
 */
package org.flowr.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author SvenKrause
 *
 */
public abstract class AbstractStringTask extends Task {

   protected String propName;
   protected String text;

   /**
    *
    */
   public AbstractStringTask() {
      super();
   }

   /**
    * set the property name the string operation result will be stored in.
    * 
    * @param propName
    */
   public void setProperty(String propName) {
      this.propName = propName;
   }

   /**
    * sets the text to manipulate by the string operation.
    * 
    * @param text
    */
   public void setText(String text) {
      this.text = text;
   }

   /**
    * validates the default task property values.
    */
   protected void validate() {
      if (propName == null) {
         throw new BuildException("property name is required");
      }

      if (text == null) {
         throw new BuildException("text is requiered");
      }
   }

   protected void validate(boolean condition, String errorMsg) throws BuildException {
      if (!condition) throw new BuildException(errorMsg);
   }

}
