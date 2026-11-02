package com.openbravo.pos.instance;

import com.openbravo.pos.spi.config.AppProperties;
import com.openbravo.pos.config.AppConfig;
import com.openbravo.pos.spi.config.BootstrapConfig;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages single-instance application enforcement via Java RMI.
 *
 * @author adrianromero, KriolOS POS
 */
public class InstanceManager {

    private static final Logger LOGGER = Logger.getLogger(InstanceManager.class.getName());

    // Fallback default values if not defined in configuration
    private static final String DEFAULT_APPLICATION_ID = "com.openbravo.pos.instance.Kriolos-POS";
    private static final int DEFAULT_RMI_PORT = 3005;

    private Registry registry;
    private final AppMessage message;

    // Active instance identifier and port
    private final String applicationId;
    private final int rmiPort;


    /**
     * Constructs an InstanceManager using the AppProperties interface.
     *
     * @param message remote message handler for inter-process notifications
     * @param config application properties instance
     * @throws RemoteException if RMI initialization fails
     * @throws AlreadyBoundException if an instance is already bound
     */
    public InstanceManager(AppMessage message, AppConfig config) throws RemoteException, AlreadyBoundException {
        this.message = message;

        if (config == null) {
            this.applicationId = DEFAULT_APPLICATION_ID;
            this.rmiPort = DEFAULT_RMI_PORT;
        }else {
            //this.applicationId = config.getProperty("machine.rmi.id", DEFAULT_APPLICATION_ID);
            //String portStr = config.getProperty("machine.rmi.port");
            //this.rmiPort = parsePort(portStr, DEFAULT_RMI_PORT);
            
            
            this.applicationId = config.getRmiApplicationId();
            this.rmiPort = config.getRmiPort();
        }
    }

    /**
     * Queries for an existing running application instance using BootstrapConfig.
     *
     * @param config bootstrap configuration
     * @return remote handle to the existing instance
     * @throws RemoteException on RMI communication failure
     * @throws NotBoundException if no instance is currently registered
     */
    public static AppMessage queryInstance(AppConfig config) throws RemoteException, NotBoundException {
        String id = (config != null) ? config.getRmiApplicationId() : DEFAULT_APPLICATION_ID;
        int port = (config != null) ? config.getRmiPort() : DEFAULT_RMI_PORT;

        LOGGER.info("Querying for instance identified by ID: " + id + " on PORT: " + port);
        return (AppMessage) LocateRegistry.getRegistry("127.0.0.1", port).lookup(id);
    }

    /**
     * Registers the current application instance in the local RMI registry.
     *
     * @return true if successfully registered
     * @throws RemoteException on RMI error
     * @throws AlreadyBoundException if another instance bound the name first
     */
    public boolean registerInstance() throws RemoteException, AlreadyBoundException {
        LOGGER.info("Creating instance identified by ID: " + this.applicationId + " on PORT: " + this.rmiPort);

        AppMessage stub = (AppMessage) UnicastRemoteObject.exportObject(this.message, 0);
        this.registry = LocateRegistry.createRegistry(this.rmiPort);
        this.registry.bind(this.applicationId, stub);
        return true;
    }

    /**
     * Utility method to safely parse the RMI port string.
     */
    private static int parsePort(String portStr, int defaultPort) {
        if (portStr == null || portStr.trim().isEmpty()) {
            return defaultPort;
        }
        try {
            return Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid RMI port configuration value: '" + portStr + "'. Using default: " + defaultPort, e);
            return defaultPort;
        }
    }
}
