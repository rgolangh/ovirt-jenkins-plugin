package org.ovirt.jenkins.plugin.resources;

import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.services.VmsService;

public enum OvirtApi {

    INSTANCE;

    private Connection connection;

    public void store(Connection connection) {
        if (this.connection != null) {
            try {
                this.connection.close(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public VmsService getVmService() {
        return this.connection.systemService().vmsService();
    }
}
