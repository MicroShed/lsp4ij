# Tools for IntelliJ IDEA

This project is a fork of an early version of [LSP4IJ](https://github.com/redhat-developer/lsp4ij) which provides classes necessary for the JetBrains IntelliJ interactive development environment to communicate with components that adhere to the language server protocol. This protocol is described in a [Specification](https://microsoft.github.io/language-server-protocol/). [Liberty Tools for IntelliJ](https://github.com/OpenLiberty/liberty-tools-intellij) was the primary consumer of this project but has since [adopted](https://github.com/OpenLiberty/liberty-tools-intellij/releases/tag/24.0.9) the official LSP4IJ plugin. **If your project requires LSP support it is recommended to use the [LSP4IJ plugin](https://plugins.jetbrains.com/plugin/23257-lsp4ij) available in the JetBrains Marketplace.**

## Requirements
**Java 11** or later.

## Build
Run `./gradlew jar` to generate the jar file. To use as a jar dependency run this command to copy it into your local Maven repository: `./gradlew publishToMavenLocal`

Your plugin must define the extension points in plugin.xml or include lsp4ij.xml. It must also define the plugin namespace in pluginNamespace.properties as "pluginNamespace=xxx" for use at runtime.

## Contributing

See the [CONTRIBUTING](CONTRIBUTING.md) document for more details.

## Issues

Please report bugs, issues and feature requests as described in the [CONTRIBUTING](CONTRIBUTING.md) document.
