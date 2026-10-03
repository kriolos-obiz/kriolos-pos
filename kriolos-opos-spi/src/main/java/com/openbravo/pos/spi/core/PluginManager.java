package com.openbravo.pos.spi.core;

import com.openbravo.pos.spi.annotation.PluginMetadata;
import com.openbravo.pos.spi.annotation.PropertyDefinition;
import com.openbravo.pos.spi.annotation.PluginProperties;
import com.openbravo.pos.spi.provider.ConfigurableProvider;
import com.openbravo.pos.spi.provider.ConfigProperty;
import com.openbravo.pos.spi.provider.PropertyType;
import com.openbravo.pos.spi.localization.LocalizationProvider;
import com.openbravo.pos.spi.hardware.PeripheralProvider;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Centralized master registry manager responsible for discovering pluggable POS
 * components and handling lifecycles. Fully decoupled from compilation-time
 * types by resolving schema structures via pure string lookup routing.
 *
 * @author KriolOS POS
 * @since 1.0.0
 */
public final class PluginManager {

    private static final Logger LOGGER = Logger.getLogger(PluginManager.class.getName());

    private static PluginManager instance;
    private ConfigurationStore configurationStore;
    private final List<Class<?>> registeredContracts;

    private PluginManager() {
        this.configurationStore = pluginId -> Collections.emptyMap();
        List<Class<?>> contracts = new ArrayList<>();
        contracts.add(LocalizationProvider.class);
        contracts.add(PeripheralProvider.class);
        this.registeredContracts = List.copyOf(contracts);
    }

    /**
     * Thread-safe access pattern to acquire the system global plugin catalog
     * broker.
     *
     * @return The active PluginManager instance.
     */
    public static synchronized PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    /**
     * Injects the centralized concrete storage execution bridge into the plugin
     * management pipeline layer.
     *
     * @param store The custom external implementation of the configuration
     * persistence strategy.
     */
    public void setConfigurationStore(ConfigurationStore store) {
        if (store != null) {
            this.configurationStore = store;
        }
    }

    /**
     * Scans all monitored core SPI contracts to harvest an inventory of every
     * single pluggable component available.
     *
     * @return An unmodifiable List containing {@link PluginInfo} descriptors
     * for all discovered extensions.
     */
    public List<PluginInfo> getAllPlugins() {
        List<PluginInfo> masterCatalog = new ArrayList<>();

        for (Class<?> contract : registeredContracts) {
            // Instantiates a local targeted stream avoiding cross-thread caching overrides
            ServiceLoader<?> loader = ServiceLoader.load(contract);

            for (ServiceLoader.Provider<?> provider : loader.stream().toList()) {
                Class<?> pluginClass = provider.type();

                if (pluginClass.isAnnotationPresent(PluginMetadata.class)) {
                    PluginMetadata annotation = pluginClass.getAnnotation(PluginMetadata.class);
                    masterCatalog.add(new PluginInfo(
                            annotation.id(),
                            annotation.schema(),
                            annotation.service()
                    ));
                }
            }
        }
        return List.copyOf(masterCatalog);
    }

    /**
     * Filters the ecosystem inventory to isolate plugins bound strictly to one
     * specific operational category.
     *
     * @param targetCategory The internal routing token filter to enforce
     * compliance (e.g., "L10N").
     * @return An unmodifiable List containing {@link PluginInfo} records
     * matching the requested category, or empty.
     */
    public List<PluginInfo> getPluginBySelector(String targetCategory) {
        if (targetCategory == null) {
            return List.of();
        }

        List<PluginInfo> masterCatalog = new ArrayList<>();

        for (Class<?> contract : registeredContracts) {
            ServiceLoader<?> loader = ServiceLoader.load(contract);

            for (ServiceLoader.Provider<?> provider : loader.stream().toList()) {
                Class<?> pluginClass = provider.type();

                if (pluginClass.isAnnotationPresent(PluginMetadata.class)) {
                    PluginMetadata annotation = pluginClass.getAnnotation(PluginMetadata.class);

                    for (String selector : annotation.selectors()) {
                        if (targetCategory.equalsIgnoreCase(selector.trim())) {
                            masterCatalog.add(new PluginInfo(
                                    annotation.id(),
                                    annotation.schema(),
                                    annotation.service()
                            ));
                            break;
                        }
                    }
                }
            }
        }
        return List.copyOf(masterCatalog);
    }

    /**
     * Searches the master catalog to locate a single registered plugin
     * blueprint matching the unique ID string.
     *
     * @param uniquePluginId The raw string identity signature of the plugin
     * (e.g., "org.kriolos.pos.l10n.cv.escudo").
     * @return An {@link Optional} enclosing the matching {@link PluginInfo}
     * metadata descriptor if found, or empty.
     */
    public Optional<PluginInfo> getPluginById(String uniquePluginId) {
        if (uniquePluginId == null) {
            return Optional.empty();
        }

        return getAllPlugins().stream()
                .filter(info -> uniquePluginId.equalsIgnoreCase(info.id()))
                .findFirst();
    }

    /**
     * Filters the ecosystem inventory to isolate plugins bound strictly to one
     * specific contract interface.
     *
     * @param serviceType The targeted contract interface token to scan (e.g.,
     * LocalizationProvider.class).
     * @return An unmodifiable List of {@link PluginInfo} records matching the
     * requested type, or empty.
     */
    public List<PluginInfo> getPluginsByService(Class<?> serviceType) {
        if (serviceType == null) {
            return List.of();
        }

        ServiceLoader<?> loader = ServiceLoader.load(serviceType);

        return loader.stream()
                .map(ServiceLoader.Provider::type)
                .filter(clazz -> clazz.isAnnotationPresent(PluginMetadata.class))
                .map(clazz -> clazz.getAnnotation(PluginMetadata.class))
                .map(annotation -> new PluginInfo(
                annotation.id(),
                annotation.schema(),
                annotation.service()
        ))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Harvests configuration property schemas dynamically using ONLY the plugin
     * ID string.
     *
     * @param uniquePluginId The raw string identity signature of the plugin
     * (e.g., "org.kriolos.pos.l10n.cv.escudo").
     * @return An unmodifiable List of strongly-typed ConfigProperty metadata
     * entities available for the given ID.
     */
    public List<ConfigProperty> getPluginSchemaMetadata(String uniquePluginId) {
        if (uniquePluginId == null) {
            return List.of();
        }

        for (Class<?> contract : registeredContracts) {
            ServiceLoader<?> loader = ServiceLoader.load(contract);

            for (ServiceLoader.Provider<?> provider : loader.stream().toList()) {
                Class<?> pluginClass = provider.type();

                if (pluginClass.isAnnotationPresent(PluginMetadata.class)) {
                    PluginMetadata annotation = pluginClass.getAnnotation(PluginMetadata.class);

                    if (uniquePluginId.equalsIgnoreCase(annotation.id())) {
                        Class<?> schemaClass = annotation.schema();
                        if (schemaClass == null || schemaClass.equals(Void.class)) {
                            return List.of();
                        }
                        return parseSchemaProperties(schemaClass);
                    }
                }
            }
        }
        return List.of();
    }

    /**
     * Resolves, instantiates, and automatically hydrates a target plugin using
     * the assigned decoupled configuration store.
     *
     * @param <T> The target service contract interface type.
     * @param serviceType The contractual interface class being targeted.
     * @param uniquePluginId The unique fully-qualified identification signature
     * of the pluggable module.
     * @return An active, fully configured implementation instance provided by
     * the vendor JAR, or null if unmapped.
     */
    public <T> T getInstance(Class<T> serviceType, String uniquePluginId) {
        if (serviceType == null || uniquePluginId == null) {
            return null;
        }
        
        return getInstanceById(serviceType, uniquePluginId);
    }

    private <T> T getInstance(Class<T> serviceType, PluginFilter filter) {

        ServiceLoader<T> loader = ServiceLoader.load(serviceType);

        for (ServiceLoader.Provider<T> provider : loader.stream().toList()) {
            Class<? extends T> clazz = provider.type();

            if (filter != null & filter.matches(clazz)) {
                try {
                    T instance = provider.get();

                    String pluginId = null;
                    if (clazz.isAnnotationPresent(PluginMetadata.class)) {
                        PluginMetadata annotation = clazz.getAnnotation(PluginMetadata.class);
                        pluginId = annotation.id();
                    }

                    if (pluginId != null && instance instanceof ConfigurableProvider configurable) {
                        Map<String, String> persistedSettings = configurationStore.loadSettings(pluginId);
                        configurable.configure(persistedSettings);
                    }
                    return instance;
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    public <T> T getInstanceBySelector(Class<T> serviceType, String selector) {
        // Implementation for getting instance by selection
        return getInstance(serviceType, new PluginFilter() {
            @Override
            public boolean matches(Class<?> clazz) {
                if (clazz.isAnnotationPresent(PluginMetadata.class)) {
                    PluginMetadata annotation = clazz.getAnnotation(PluginMetadata.class);
                    return java.util.Arrays.asList(annotation.selectors()).contains(selector);
                }
                return false;
            }
        });
    }

    public <T> T getInstanceById(Class<T> serviceType, String pluginId) {
        // Implementation for getting instance by ID
        return getInstance(serviceType, new PluginFilter() {
            @Override
            public boolean matches(Class<?> clazz) {
                if (clazz.isAnnotationPresent(PluginMetadata.class)) {
                    PluginMetadata annotation = clazz.getAnnotation(PluginMetadata.class);
                    return annotation.id().equals(pluginId);
                }
                return false;
            }
        });
    }

    private List<ConfigProperty> parseSchemaProperties(Class<?> schemaClass) {
        List<ConfigProperty> configProperties = new ArrayList<>();
        if (schemaClass.isAnnotationPresent(PluginProperties.class)) {
            PropertyDefinition[] definitions = schemaClass.getAnnotation(PluginProperties.class).value();
            for (PropertyDefinition def : definitions) {
                configProperties.add(new ConfigProperty(def.key(), def.label(), def.i18nLabelKey(), def.description(), PropertyType.fromCode(def.type()), def.required(), def.defaultValue()));
            }
        } else if (schemaClass.isAnnotationPresent(PropertyDefinition.class)) {
            PropertyDefinition def = schemaClass.getAnnotation(PropertyDefinition.class);
            configProperties.add(new ConfigProperty(def.key(), def.label(), def.i18nLabelKey(), def.description(), PropertyType.fromCode(def.type()), def.required(), def.defaultValue()));
        }
        return List.copyOf(configProperties);
    }

    public interface PluginFilter {

        boolean matches(Class<?> clazz);
    }
}
