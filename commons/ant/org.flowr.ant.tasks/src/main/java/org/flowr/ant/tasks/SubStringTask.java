/**
 * DB Systel GmbH / i.S.A. Dresden GmbH & Co. KG (c) 2010
 */
package org.flowr.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PropertyHelper;

/**
 * Ant task evaluating the text part after a specified separator.
 *<p>usage:</p>
 * <pre>
 * &lt;substring property="<i>result.prop.name</i>" begin="<i>fromIndex</i>" end="<i>endIndex</i>" text="<i>any text or ${property.to.scan}</i>" /&gt;
 * </pre>
 * <p>Sample</p>
 *<pre>
 * &lt;property name="build.version.full" value="1.0.0.201101012359" /&gt;<br>
 * &lt;subString property="temp.y" begin="0" end="4" text="${build.version.full}" /&gt;
 * &lt;subString property="temp.m" begin="4" end="6" text="${build.version.full}" /&gt;
 * &lt;subString property="temp.d" begin="6" end="8" text="${build.version.full}" /&gt;
 * &lt;subString property="temp.t" begin="8" end="12" text="${build.version.full}" /&gt;
 * &lt;property name="build.version" value="${temp.y}-${temp.m}-${temp.d}:${temp.t}" /&gt;
 * &lt;echo message="build.version = ${build.version}" /&gt;<br>
 * results "build.version = 2011-01-01:2358"
 *</pre>
 * @author SvenKrause
 *
 */
public class SubStringTask extends AbstractStringTask {

   private int begin;
   private int end;

   @Override
   public void init() throws BuildException {
      super.init();
      begin = 0;
      end = -1;
   }

   /**
    * @param begin the begin to set
    */
   public void setBegin(int begin) {
      this.begin = begin;
   }

   /**
    * @param end the end to set
    */
   public void setEnd(int end) {
      this.end = end;
   }

   @Override
   protected void validate() {
      if (end < 0 && text != null) {
         end = text.length();
      }
      super.validate();
      validate(begin >= 0, "begin can not be less then zero");
      validate(begin <= end, "begin can not be greater than end");
      validate(begin <= text.length(), "begin can not be greater that text length");
      validate(end <= text.length(), "end can not be greater that text length");
   }

   @Override
   public void execute() throws BuildException {
      validate();

      PropertyHelper ph = PropertyHelper.getPropertyHelper(this.getProject());
      ph.setProperty("", propName, text.substring(begin, end), true);
   }

}
