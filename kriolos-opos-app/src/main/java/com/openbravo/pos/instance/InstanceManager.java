package com.openbravo.pos.instance;

import com.openbravo.pos.forms.AppConfig; // Certifique-se de importar o seu gestor de configurações
import com.openbravo.pos.forms.AppProperties;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author adrianromero
 */
public class InstanceManager {

    private static final Logger LOGGER = Logger.getLogger(InstanceManager.class.getName());

    // Valores Padrão (Fallback) caso não existam no ficheiro de propriedades
    private static final String DEFAULT_APPLICATION_ID = "com.openbravo.pos.instance.Kriolos-POS";
    private static final int DEFAULT_RMI_PORT = 3005;

    private Registry registry;
    private final AppMessage message;

    // Variáveis que vão armazenar a configuração final ativa
    private final String applicationId;
    private final int rmiPort;

    /**
     * Construtor atualizado para receber o AppConfig da aplicação
     */
    public InstanceManager(AppMessage message, AppProperties config) throws RemoteException, AlreadyBoundException {
        this.message = message;

        // Se o objeto config for nulo por alguma falha, assume os defaults imediatamente
        if (config == null) {
            this.applicationId = DEFAULT_APPLICATION_ID;
            this.rmiPort = DEFAULT_RMI_PORT;
        } else {
            // Lê do ficheiro com fallback usando o método getProperty(chave, valorOmissao) do Java
            this.applicationId = config.getProperty("machine.rmi.id", DEFAULT_APPLICATION_ID);

            String portStr = config.getProperty("machine.rmi.port");
            this.rmiPort = parsePort(portStr, DEFAULT_RMI_PORT);
        }
    }

    /**
     * Método auxiliar estático para a busca (query) usando os defaults
     */
    public static AppMessage queryInstance(AppProperties config) throws RemoteException, NotBoundException {
        String id = (config != null) ? config.getProperty("machine.rmi.id", DEFAULT_APPLICATION_ID) : DEFAULT_APPLICATION_ID;
        String portStr = (config != null) ? config.getProperty("machine.rmi.port") : null;
        int port = parsePort(portStr, DEFAULT_RMI_PORT);

        LOGGER.info("Query for instance identified by ID: " + id + " on PORT: " + port);
        return (AppMessage) LocateRegistry.getRegistry("127.0.0.1", port).lookup(id);
    }

    /**
     * Regista a instância atual usando as definições carregadas
     */
    public boolean registerInstance() throws RemoteException, AlreadyBoundException {
        LOGGER.info("Creating instance identified by ID: " + this.applicationId + " on PORT: " + this.rmiPort);

        AppMessage stub = (AppMessage) UnicastRemoteObject.exportObject(this.message, 0);
        this.registry = LocateRegistry.createRegistry(this.rmiPort);
        this.registry.bind(this.applicationId, stub);
        return true;
    }

    /**
     * Método utilitário para converter a String do porto em Integer de forma segura
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
