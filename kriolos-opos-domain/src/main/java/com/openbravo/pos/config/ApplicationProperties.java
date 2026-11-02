//    KriolOS POS
//    Copyright (c) 2019-2026 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.config;

import com.openbravo.pos.spi.config.AppProperties;
import com.openbravo.pos.spi.config.BootstrapConfig;
/**
 * Production implementation of {@link AppProperties} for the running application lifecycle.
 * <p>
 * Delegates property reads to the database-backed {@link ApplicationConfigurationService} with
 * low-latency hierarchical resolution (Station UUID -&gt; Global 0000-0000-0000-0000 -&gt; BootstrapConfig).
 *
 * @author KriolOS POS
 */
public class ApplicationProperties implements AppProperties {

    private final ApplicationConfigurationService configService;
    private final BootstrapConfig bootstrapConfig;


    public ApplicationProperties(ApplicationConfigurationService configService, BootstrapConfig bootstrapConfig) {
        this.configService = configService;
        this.bootstrapConfig = bootstrapConfig;
    }

    public ApplicationConfigurationService getConfigurationService() {
        return configService;
    }

    public BootstrapConfig getBootstrapConfig() {
        return bootstrapConfig;
    }

    @Override
    public String getHost() {
        return (bootstrapConfig != null) ? bootstrapConfig.getHost() : "localhost";
    }

    @Override
    public String getProperty(String sKey) {
        return getProperty(sKey, null);
    }

    @Override
    public String getProperty(String sKey, String defaultValue) {
        if (configService != null) {
            return configService.getProperty(sKey, defaultValue);
        }
        if (bootstrapConfig != null) {
            return bootstrapConfig.getProperty(sKey, defaultValue);
        }
        return defaultValue;
    }
}
