# Tools for IntelliJ IDEA

This project provides classes necessary for the JetBrains IntelliJ interactive development environment to communicate with components that adhere to the language server protocol. This protocol is described in a [Specification](https://microsoft.github.io/language-server-protocol/)

## Requirements
**Java 11** or later.

## Build
Run `gradle jar` to generate the jar file. To use as a jar dependency run `mvnInstallFile` to copy it into your local Maven repository. 
Your plugin must define the extension points in plugin.xml or include lsp4ij.xml. It must also define the plugin namespace in pluginNamespace.properties as "pluginNamespace=xxx" for use at runtime.

## Contributing

See the [CONTRIBUTING](CONTRIBUTING.md) document for more details.

## Issues

Please report bugs, issues and feature requests as described in the [CONTRIBUTING](CONTRIBUTING.md) document.
