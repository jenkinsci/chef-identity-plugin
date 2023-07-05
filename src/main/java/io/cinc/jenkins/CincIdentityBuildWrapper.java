/* The MIT License (MIT)
 *
 * Copyright (c) 2014 Tyler Fitch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.cinc.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.List;

/**
 * Sample {@link SimpleBuildWrapper}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link CincIdentityBuildWrapper} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #jobIdentity})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #setUp(Context, Run, FilePath, Launcher, TaskListener, EnvVars)}
 * method will be invoked. 
 *
 * @author tfitch
 */
public class CincIdentityBuildWrapper extends SimpleBuildWrapper {

	private final String jobIdentity;

	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public CincIdentityBuildWrapper(String jobIdentity) {
		this.jobIdentity = jobIdentity;
	}

	/**
	 * We'll use this from the <tt>config.jelly</tt>.
	 */
	public String getJobIdentity() {
		return jobIdentity;
	}

	@Override
	public void setUp(Context context, Run<?, ?> build, FilePath ws, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) throws IOException, InterruptedException {
		// This is where you 'build' the project.
		listener.getLogger().println("Running build with Cinc Identity of " + this.jobIdentity);
		context.setDisposer(new CincIdentityCleanup.WrapperDisposerImpl());
		CincIdentity cincIdentity;

		// now grab that Identity to write the files 
		// and stop using the jobIdentity reference because an Identity could be removed from system Config
		// so we want the lookup of it and fail accordingly
		// using InterruptedException here because it Aborts the build vs IOException which fails it
		try {
			cincIdentity = getDescriptor().getCincIdentity(jobIdentity);
		} catch (InterruptedException e) {
			listener.getLogger().println(e.getMessage());
			throw new InterruptedException(e.getMessage());
		}

		// Workflow - How to track which identity's files are in the workspace
		// We'll create a file .cinc/jobCincIdentity with the cincIdentity.idName()
		// Read file and verify when a build happens
		// Fails to read (doesn't exist then write the files).
		// The cincIdentity.getIdName() name doesn't match then overwrite the files.
		// Finally if cincIdentity.getIdName() matches then we're golden and do nothing.
		if (ws != null) {
			try {
                if (!(new FilePath(ws, ".cinc/.jenkinsCincIdentity")).readToString().equals(cincIdentity.getIdName())) {
					listener.getLogger().println("Job's existing Cinc Identity did not match.  Changing to " + cincIdentity.getIdName());
					// throw the IOException to get the catch to write the updated files
					throw new IOException("Jenkins Cinc Identity did not match");
				}
			} catch (IOException e) {
				new FilePath(ws, ".cinc").mkdirs();
				new FilePath(ws, ".cinc/.jenkinsCincIdentity").write(cincIdentity.getIdName(), "UTF-8");
				new FilePath(ws, ".cinc/user.pem").write(cincIdentity.getPemKey(), "UTF-8");
				new FilePath(ws, ".cinc/knife.rb").write(cincIdentity.getKnifeRb(), "UTF-8");
			}
		}
	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
     * Descriptor for {@link CincIdentityBuildWrapper}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/CincIdentityBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		/**
		 * To persist global configuration information,
		 * simply store it in a field and call save().
		 *
		 * <p>
		 * If you don't want fields to be persisted, use <tt>transient</tt>.
		 */
		private List<CincIdentity> cincIdentities;

		/**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
		public DescriptorImpl() {
			super(CincIdentityBuildWrapper.class);
			load();
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			// Indicates that this builder can be used with all kinds of project types
			return true;
		}

		/**
         * This human readable name is used in the configuration screen.
         */
		public String getDisplayName() {
			return "Cinc Identity Plugin";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
			// To persist global configuration information,
			// set that to properties and call save().
			Object identities = formData.get("cincIdentity");
			if (!JSONNull.getInstance().equals(identities)) {
				cincIdentities = req.bindJSONToList(CincIdentity.class, identities);
			} else {
				cincIdentities = null;
			}

			save();
			return super.configure(req,formData);
		}

		public List<CincIdentity> getCincIdentities() {
			return cincIdentities;
		}

		public CincIdentity getCincIdentity(String idName) throws InterruptedException {
			for(CincIdentity ident : cincIdentities ) {
				if(ident != null && ident.getIdName().equals(idName)) {
					return ident;
				}
			}
			// Aborts the build, 
			// this can happen in a weird race condition of someone editing identies while jobs are running
			// or a job is using a deleted identity
			throw new InterruptedException ("Cinc Identity Plugin::Lookup of identity '" + idName + "' failed. Aborting build.");
		}

		// Required by external plugins (according to Articfactory plugin)
		@SuppressWarnings({"UnusedDeclartaions"})
		public void setCincIdentities(List<CincIdentity> cincIdentities) {
			this.cincIdentities = cincIdentities;
		}
	}
}

