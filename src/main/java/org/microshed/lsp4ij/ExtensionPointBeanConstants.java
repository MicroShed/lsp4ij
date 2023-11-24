package org.microshed.lsp4ij;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class ExtensionPointBeanConstants {
    private static final Logger LOGGER = Logger.getLogger(ExtensionPointBeanConstants.class.getName());
    private static String propertiesName = "pluginNamespace";
    private static String defaultNamespace = "org.microshed";
    private static String pluginNamespaceProperty = "pluginNamespace";
    private static ResourceBundle resourceBundle = null;
    private static String SERVER_EXT_NAME = ".server";
    private static String SERVER_ICON_EXT_NAME = ".serverIconProvider";
    private static String LANGUAGE_MAPPING_EXT_NAME = ".languageMapping";

    private static String getNamespaceName() {
        if (resourceBundle == null) {
            LOGGER.warning("resourceBundle == null");
            LOGGER.warning("Meta="+new File("META-INF").exists());
            LOGGER.warning("Meta Path="+new File("META-INF").getAbsolutePath());
            resourceBundle = ResourceBundle.getBundle(propertiesName, new Locale("en"));
            LOGGER.warning("pluginNamespaceProperty1="+resourceBundle.getString(pluginNamespaceProperty));
        }
        LOGGER.warning("pluginNamespaceProperty2="+resourceBundle.getString(pluginNamespaceProperty));
        if (resourceBundle.containsKey(pluginNamespaceProperty)) {
            return resourceBundle.getString(pluginNamespaceProperty);
        } else {
            return defaultNamespace;
        }
    }
    public static String getServerExtensionName() {
        return getNamespaceName() + SERVER_EXT_NAME;
    }

    public static String getServerIconExtensionName() {
        return getNamespaceName() + SERVER_ICON_EXT_NAME;
    }

    public static String getLanguageMappingExtensionName() {
        return getNamespaceName() + LANGUAGE_MAPPING_EXT_NAME;
    }
}
