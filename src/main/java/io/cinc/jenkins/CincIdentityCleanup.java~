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
package io.chef.jenkins;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildWrapper;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Modeled after the Workspace Cleanup plugin, but just for the .chef folder
 *
 * @author tfitch
 */
public class ChefIdentityCleanup extends Notifier implements MatrixAggregatable {

    private static final Logger LOGGER = Logger.getLogger(ChefIdentityCleanup.class.getName());

    private ExecuteOn executeOn;

    @DataBoundConstructor
    public ChefIdentityCleanup(ExecuteOn executeOn) {
        this.executeOn = executeOn;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        Job job = build.getProject();

        boolean axis = isMatrixAxe(job);
        if ((axis && executeOn.axes())) {    // matrix axis, and set on execute on axis' nodes
            return _perform(build, launcher, listener);
        }

        return axis || _perform(build, launcher, listener);
    }

    public boolean _perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return cleanup(build.getWorkspace(), listener);
    }

    private boolean cleanup(FilePath workspace, TaskListener listener) throws AbortException {
        listener.getLogger().append("\nChef Identity cleanup happening... \n");
        try {
            if (workspace == null || !workspace.exists()) {
                return true;
            }
            new FilePath(workspace, ".chef").deleteRecursive();

            listener.getLogger().append(".chef folder removed\n");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            listener.getLogger().append("Cannot delete .chef folder: " + ex.getMessage() + "\n");
            throw new AbortException("Cannot delete .chef folder: " + ex.getMessage());
        }

        return true;
    }

    @Override
    public MatrixAggregator createAggregator(final MatrixBuild matrixBuild, Launcher launcher, final BuildListener buildListener) {
        return new MatrixAggregator(matrixBuild, launcher, buildListener) {
            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                return !executeOn.matrix() || _perform(matrixBuild, launcher, buildListener);
            }
        };
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    private static boolean isMatrixAxe(Job job) {
        return job instanceof MatrixConfiguration;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(ChefIdentityCleanup.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.ChefIdentityCleanup_delete_chef_folder();
        }

        @Override
        public boolean isApplicable(Class clazz) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return super.newInstance(req, formData);
        }
    }

    public enum ExecuteOn {
        MATRIX,
        AXES,
        BOTH;

        public boolean matrix() {
            return this == MATRIX || this == BOTH;
        }

        public boolean axes() {
            return this == AXES || this == BOTH;
        }
    }

    public static final class WrapperDisposerImpl extends SimpleBuildWrapper.Disposer {
        public void tearDown(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
            ChefIdentityCleanup cleanup = new ChefIdentityCleanup(null);
            cleanup.cleanup(workspace,listener);
        }
    }
}
