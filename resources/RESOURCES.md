This directory contains some resources or sample resources that will be required when using this component in an IntelliJ plugin.

XML

The classes in this jar require certain extension points to be defined in the host plugin. The file lsp4ij.xml represents a sample of the definitions you will need to use this jar. It is possible that you can copy this file into your src/main/resources/META-INF directory in your plugin and then add an include element to plugin.xml to bring these definitions into your plugin.

Alternatively you could use the plugin.xml file in this directory to make this jar file into an IntelliJ plugin. Then your plugin could declare a dependency on this plugin to make use of its definitions.

Messages

This directory contains LanguageServerBundle.properties which is required to be copied to src/main/resources/messages in your plugin. This file is referenced in the XML file.