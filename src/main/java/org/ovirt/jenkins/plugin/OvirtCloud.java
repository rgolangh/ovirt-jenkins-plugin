package org.ovirt.jenkins.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.*;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.security.ACL;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.ConnectionBuilder;

import javax.annotation.CheckForNull;

import static org.ovirt.engine.sdk4.ConnectionBuilder.connection;


public class OvirtCloud extends Cloud {

    private static final Logger LOGGER = Logger.getLogger(OvirtCloud.class.getName());

    Label label;
    @CheckForNull
    private String engineURL;
    @CheckForNull
    private String credentialsId;

    @DataBoundConstructor
    public OvirtCloud(String name) {
        super(name);
    }

    public boolean canProvision(Label label) { return checkIfLabelExists(label); }

    public Collection<NodeProvisioner.PlannedNode> provision(@CheckForNull final Label label, final int excessWorkload) {

        LOGGER.log(Level.INFO, "Provisioning {0} VMs", excessWorkload);

        List<NodeProvisioner.PlannedNode> provisionedList = new ArrayList<>();

        for (int i = 1; i <= excessWorkload; ++i) {
            try {
                oVirtProvision(label);
                LOGGER.log(Level.INFO, "Successfully provisioned {" + i + "}/{" + excessWorkload + "} VMs");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not provision oVirt VM: {0}", e.getMessage());
            }
        }

        return provisionedList;
    }


    @CheckForNull
    public String getEngineURL() {
        return engineURL;
    }

    @DataBoundSetter
    public void setEngineURL(@CheckForNull String engineURL) {
        this.engineURL = engineURL;
    }

    @CheckForNull
    public String getCredentialsId() {
        return credentialsId;
    }

    @DataBoundSetter
    public void setCredentialsId(@CheckForNull String credentialsId) {
        this.credentialsId = credentialsId;
    }

    @DataBoundSetter
    public void setLabel(Label label) {
        this.label = label;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Cloud> {
        @Override
        public String getDisplayName() {
            return "oVirt";
        }

        @RequirePOST
        public FormValidation doTestConnection(@QueryParameter String engineURL,
                                               @QueryParameter String credentialsId) {

            if (StringUtils.isBlank(engineURL)) {
                return FormValidation.error("Engine URL is required");
            }

            if (StringUtils.isBlank(credentialsId)) {
                return FormValidation.error("Credentials ID is required");
            }

            try (Connection connection = oVirtConnect(engineURL, credentialsId)) {
                return FormValidation.ok("Successfully connected to oVirt!");
            } catch (Exception e) {
                return FormValidation.error("Could not connect to oVirt! {0}", e);
            }
        }

        @RequirePOST
        public ListBoxModel doFillCredentialsIdItems() {

            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

            return CredentialsProvider.listCredentials(
                    StandardUsernamePasswordCredentials.class,
                    Jenkins.getInstance(),
                    ACL.SYSTEM,
                    null,
                    null
            );
        }
    }

    /**
     * Provision an oVirt VM to connect back to Jenkins
     *
     * @param label
     * @return true if successfully provisioned oVirt VM
     * @throws Exception if something went wrong
     */
    private boolean oVirtProvision(Label label) throws Exception {
        String virtualMachineLabel = resolveVirtualMachineLabel(label);

        try {
            // TODO: Provision a VM using oVirt SDK
        } catch (Exception e) {
            throw e;
        }

        return true;
    }

    /**
     * Given Jenkins agent label as requested by user, return the matching oVirt VM label
     *
     * @param label
     * @return oVirt VM label matching the given Jenkins agent label
     */
    private String resolveVirtualMachineLabel(Label label) {
        // TODO: resolve matching oVirt VM label for the given Jenkins agent label
        // TODO: Do we want to require users to configure VM labels with the same label or include some logic here?

        return "something";
    }

    /**
     * Check if we have the specified VM label in oVirt
     *
     * @param label
     * @return true if such label exists in oVirt and false otherwise
     */
    private boolean checkIfLabelExists(Label label) {

        // TODO: Implement this method

        return true;
    }

    /**
     * Try to establish connection to oVirt engine
     *
     * @param engineURL
     * @param credentialsId
     * @return {@link Connection}
     */
    static private Connection oVirtConnect(final String engineURL, final String credentialsId) {
        UsernamePasswordCredentials credentials = getCredentials(credentialsId);
        try {
            return connection()
                    .url(engineURL)
                    .user(credentials.getUsername())
                    .password(Secret.toString(credentials.getPassword()))
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Return the credentials matching the given ID.
     *
     * @param credentialsId
     * @return UsernamePasswordCredentials or null if could not find matching credentials
     */
    static private UsernamePasswordCredentials getCredentials(final String credentialsId) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        UsernamePasswordCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.emptyList()),
                CredentialsMatchers.withId(credentialsId));
    }

}