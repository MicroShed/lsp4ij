# Tools for IntelliJ IDEA

This project provides classes necessary for the JetBrains IntelliJ interactive development environment to communicate with components that adhere to the language server protocol. This protocol is described in a [Specification](https://microsoft.github.io/language-server-protocol/)

## Requirements
**Java 11** or later.

## Build
Run `gradle jar` to generate the jar file. To use as a jar dependency run `mvnInstallFile` to copy it into your local Maven repository. To use as a plugin use `makeZip` to create an installasble zip plugin. During development the Liberty Tools IntelliJ `runIde` task will add the jar as a plugin.

## Contributing

See the [CONTRIBUTING](CONTRIBUTING.md) document for more details.

## Issues

Please report bugs, issues and feature requests as described in the [CONTRIBUTING](CONTRIBUTING.md) document.
