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
    private static String LANGUAGE_MAPPING_EXT_NAME = ".languageMapping";
    private static String FILENAMEPATTERN_MAPPING_EXT_NAME = ".filenamepatternMapping";
    private static String FILETYPE_MAPPING_EXT_NAME = ".filetypeMapping";


    private static String getNamespaceName() {
        if (resourceBundle == null) {
            resourceBundle = ResourceBundle.getBundle(propertiesName, new Locale("en"));
        }
        if (resourceBundle.containsKey(pluginNamespaceProperty)) {
            return resourceBundle.getString(pluginNamespaceProperty);
        } else {
            return defaultNamespace;
        }
    }
    public static String getServerExtensionName() {
        return getNamespaceName() + SERVER_EXT_NAME;
    }

    public static String getLanguageMappingExtensionName() {
        return getNamespaceName() + LANGUAGE_MAPPING_EXT_NAME;
    }

    public static String getFileNamePatternMappingExtensionName() {
        return getNamespaceName() + FILENAMEPATTERN_MAPPING_EXT_NAME;
    }

    public static String getFileTypeMappingExtensionName() {
        return getNamespaceName() + FILETYPE_MAPPING_EXT_NAME;
    }
}
