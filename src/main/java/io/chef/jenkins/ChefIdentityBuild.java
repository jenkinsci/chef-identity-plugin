package io.chef.jenkins;

import hudson.*;
import hudson.model.*;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Chef cookbook install to remote host
 */
public final class ChefIdentityBuild extends Builder {

	/**
	 * Minimal constructor.
	 */
	@DataBoundConstructor
	public ChefIdentityBuild() {
		super();
	}

	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
		listener.getLogger().println("Chef cookbook installation starting ");

		EnvVars env = build.getEnvironment(listener);
		env.putAll(build.getBuildVariables());

		ArgumentListBuilder args = new ArgumentListBuilder();
		args.addTokenized("sh " + ChefIdentity.SHELL_SCRIPT_FILE);

		try {
			int r = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getWorkspace()).join();

			return (r == 0);
		} catch (final IOException e) {
			Util.displayIOException(e, listener);
			e.printStackTrace(listener.fatalError("Chef cookbook installation error: " + e));
			return false;
		}
	}


	@Extension
	public static final ChefIdentityBuildDescriptor DESCRIPTOR = new ChefIdentityBuildDescriptor();

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public ChefIdentityBuildDescriptor getDescriptor() {
		return DESCRIPTOR;
	}
}
