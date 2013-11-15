/**
 * DB Systel GmbH / i.S.A. Dresden GmbH & Co. KG
 * (c) 2010
 */
package org.flowr.ant.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.FeatureExportOperation;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

/**
 * @author SvenKrause
 *
 */
public class JNLPFeatureExportTask extends Task {

	private IFeatureModel[] models = new IFeatureModel[0];
	private String fDestination;
	private String fZipFilename;
	private boolean fToDirectory;
	private boolean fUseJarFormat;
	private boolean fExportSource;
	private String fQualifier;
	private String jnlpCodeBase;
	private String jnlpJSE;
	private boolean exclusive;

	public void setFeatures(String features) {
		FeatureModelManager featureModelManager = PDECore.getDefault().getFeatureModelManager();
		List<IFeatureModel> list = new ArrayList<IFeatureModel>();
		for (StringTokenizer tokenizer = new StringTokenizer(features, ","); tokenizer.hasMoreTokens();) {
			String f = tokenizer.nextToken().trim();
			IFeatureModel feature = featureModelManager.findFeatureModel(f);
			list.add(feature);
		}
		models = list.toArray(new IFeatureModel[list.size()]);
	}

	public void setExportType(String type) {
		fToDirectory = !"zip".equals(type); //$NON-NLS-1$
	}

	public void setUseJARFormat(String useJarFormat) {
		fUseJarFormat = "true".equals(useJarFormat); //$NON-NLS-1$
	}

	public void setExportSource(String doExportSource) {
		fExportSource = "true".equals(doExportSource); //$NON-NLS-1$
	}

	public void setDestination(String destination) {
		fDestination = destination;
	}

	public void setFilename(String filename) {
		fZipFilename = filename;
	}

	public void setQualifier(String qualifier) {
		fQualifier = qualifier;
	}

	public void setJnlpCodeBase(String jnlpCodeBase) {
		this.jnlpCodeBase = jnlpCodeBase;
	}

	public void setJnlpJSE(String jnlpJSE) {
		this.jnlpJSE = jnlpJSE;
	}

	public void setExclusive(String exclusive) {
		this.exclusive = "true".equals(exclusive);
	}

	public boolean isAntRunner() {
		String args[] = Platform.getCommandLineArgs();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-application")) //$NON-NLS-1$
				return args[i + 1].equals("org.eclipse.ant.core.antRunner"); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public void execute() throws BuildException {
		if (fDestination == null) throw new BuildException("No destination is specified"); //$NON-NLS-1$

		if (!fToDirectory && fZipFilename == null) throw new BuildException("No zip file is specified"); //$NON-NLS-1$

		Job job = getExportJob("Export");

		// if running in ant runner, block until job is done. Prevents Exiting before completed
		// blocking will cause errors if done when running in regular runtime.
		if (isAntRunner() ) {
			try {
				job.schedule();
				job.join();
			}
			catch (InterruptedException e) {}
		} else {
			job.schedule(2000);
			while(exclusive && job.getState() != Job.NONE) {
				this.getProject().log("waiting for job completion");
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}

	}

	protected Job getExportJob(String jobName) {
		FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = fToDirectory;
		info.useJarFormat = fUseJarFormat;
		info.exportSource = fExportSource;
		info.zipFileName = fZipFilename;
		info.items = models;
		info.qualifier = fQualifier;
		if (jnlpCodeBase != null && jnlpJSE != null) {
			info.jnlpInfo = new String[] { jnlpCodeBase, jnlpJSE };
		}
		// if destination is relative, then make it absolute
		if (!new File(fDestination).isAbsolute()) {
			File home = new File(getLocation().getFileName()).getParentFile();
			info.destinationDirectory = new File(home, fDestination).toString();
		} else
			info.destinationDirectory = fDestination;
		return new FeatureExportOperation(info, jobName);
	}

}
