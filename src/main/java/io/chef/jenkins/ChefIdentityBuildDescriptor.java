package io.chef.jenkins;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.List;

/**
 * Chef cookbook install to remote host
 */
public class ChefIdentityBuildDescriptor extends BuildStepDescriptor<Builder> {

	public ChefIdentityBuildDescriptor() {
		super(ChefIdentityBuild.class);
		load();
	}

	@Override
	public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
		save();
		return true;
	}

	@Override
	public String getDisplayName() {
		return Messages.ChefIdentityBuild_install_chef_cookbook();
	}

	@Override
	public boolean isApplicable(Class<? extends AbstractProject> jobType) {
		return true;
	}

	@Override
	public ChefIdentityBuild newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
		return req.bindJSON(ChefIdentityBuild.class, formData);
	}
}
