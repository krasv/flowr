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
 * &lt;leadString property="<i>result.prop.name</i>" separator="<i>anySeparatorText</i>" last="<i>true|false, default==false</i>" text="<i>any text or ${property.to.scan}</i>" /&gt;
 * </pre>
 * <p>Sample</p>
 *<pre>
 * &lt;property name="build.version.full" value="1.0.0.201101012359" /&gt;<br>
 * &lt;leadString property="build.version" separator="." last="true" text="${build.version.full}" /&gt;<br>
 * &lt;echo message="build.version = ${build.version}" /&gt;<br>
 *
 * results "build.version = 1.0.0"
 *</pre>
 * @author SvenKrause
 *
 */
public class LeadStringTask extends AbstractSeparatorbasedStringTask {

	private boolean last;

	/**
	 * @param last
	 *        the last to set
	 */
	public void setLast(boolean last) {
		this.last = last;
	}

	@Override
	public void init() throws BuildException {
		super.init();
		last = false;
	}

	@Override
	public void execute() throws BuildException {
		validate();

		PropertyHelper ph = PropertyHelper.getPropertyHelper(this.getProject());
		int indexOf =  last ? text.lastIndexOf(separator) : text.indexOf(separator);
		if (indexOf != -1) {
			String lead = text.substring(0, indexOf);
			ph.setProperty("", propName, lead, true);
		} else {
			ph.setProperty("", propName, text, true);
		}
	}

}
