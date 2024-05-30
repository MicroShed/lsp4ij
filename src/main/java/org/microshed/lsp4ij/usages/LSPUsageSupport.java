/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.microshed.lsp4ij.usages;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.microshed.lsp4ij.internal.CompletableFutures;
import org.microshed.lsp4ij.LSPIJUtils;
import org.microshed.lsp4ij.LanguageServerItem;
import org.microshed.lsp4ij.LanguageServiceAccessor;
import org.microshed.lsp4ij.internal.CancellationSupport;
import org.microshed.lsp4ij.features.AbstractLSPFeatureSupport;
import org.microshed.lsp4ij.features.LSPRequestConstants;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * LSP usage support which collect:
 * <ul>
 *      <li>textDocument/declaration</li>
 *      <li>textDocument/definition</li>
 *      <li>textDocument/typeDefinition</li>
 *      <li>textDocument/references</li>
 *      <li>textDocument/implementation</li>
 *  </ul>
 */
public class LSPUsageSupport extends AbstractLSPFeatureSupport<LSPUsageSupport.LSPUsageSupportParams, List<LSPUsagePsiElement>> {

    public static record LSPUsageSupportParams(@NotNull Position position) {
    }

    public LSPUsageSupport(@NotNull PsiFile file) {
        super(file, false);
    }

    @Override
    protected CompletableFuture<List<LSPUsagePsiElement>> doLoad(LSPUsageSupportParams params, CancellationSupport cancellationSupport) {
        PsiFile file = super.getFile();
        return collectUsages(file.getVirtualFile(), file.getProject(), params, cancellationSupport);
    }

    private static @NotNull CompletableFuture<List<LSPUsagePsiElement>> collectUsages(@NotNull VirtualFile file,
                                                                                      @NotNull Project project,
                                                                                      @NotNull LSPUsageSupportParams params,
                                                                                      @NotNull CancellationSupport cancellationSupport) {
        var textDocumentIdentifier = LSPIJUtils.toTextDocumentIdentifier(file);
        return LanguageServiceAccessor.getInstance(project)
                .getLanguageServers(file, LSPUsageSupport::isUsageSupported)
                .thenComposeAsync(languageServers -> {
                    // Here languageServers is the list of language servers which matches the given file
                    // and which have usage (references, implementation, etc) capability
                    if (languageServers.isEmpty()) {
                        return CompletableFuture.completedFuture(null);
                    }

                    DeclarationParams declarationParams = new DeclarationParams(textDocumentIdentifier, params.position());
                    DefinitionParams definitionParams = new DefinitionParams(textDocumentIdentifier, params.position());
                    TypeDefinitionParams typeDefinitionParams = new TypeDefinitionParams(textDocumentIdentifier, params.position());
                    ReferenceParams referenceParams = createReferenceParams(textDocumentIdentifier, params.position(), project);
                    ImplementationParams implementationParams = new ImplementationParams(textDocumentIdentifier, params.position());

                    List<CompletableFuture<List<LSPUsagePsiElement>>> allFutures = new ArrayList<>();
                    for (var ls : languageServers) {

                        // Collect declarations
                        if (ls.isDeclarationSupported()) {
                            allFutures.add(
                                    cancellationSupport.execute(ls
                                                    .getTextDocumentService()
                                                    .declaration(declarationParams), ls, LSPRequestConstants.TEXT_DOCUMENT_DECLARATION)
                                            .handle(reportUsages(project, LSPUsagePsiElement.UsageKind.declarations))
                            );
                        }

                        // Collect definitions
                        if (ls.isDefinitionSupported()) {
                            allFutures.add(
                                    cancellationSupport.execute(ls
                                                    .getTextDocumentService()
                                                    .definition(definitionParams), ls, LSPRequestConstants.TEXT_DOCUMENT_DEFINITION)
                                            .handle(reportUsages(project, LSPUsagePsiElement.UsageKind.definitions))
                            );
                        }

                        // Collect type definitions
                        if (ls.isTypeDefinitionSupported()) {
                            allFutures.add(
                                    cancellationSupport.execute(ls
                                                    .getTextDocumentService()
                                                    .typeDefinition(typeDefinitionParams), ls, LSPRequestConstants.TEXT_DOCUMENT_TYPE_DEFINITION)
                                            .handle(reportUsages(project, LSPUsagePsiElement.UsageKind.typeDefinitions))
                            );
                        }

                        // Collect references
                        if (ls.isReferencesSupported()) {
                            allFutures.add(
                                    cancellationSupport.execute(ls
                                                    .getTextDocumentService()
                                                    .references(referenceParams), ls, LSPRequestConstants.TEXT_DOCUMENT_REFERENCES)
                                            .handle(reportUsages2(project, LSPUsagePsiElement.UsageKind.references))
                            );
                        }

                        // Collect implementation
                        if (ls.isImplementationSupported()) {
                            allFutures.add(
                                    cancellationSupport.execute(ls
                                                    .getTextDocumentService()
                                                    .implementation(implementationParams), ls, LSPRequestConstants.TEXT_DOCUMENT_IMPLEMENTATION)
                                            .handle(reportUsages(project, LSPUsagePsiElement.UsageKind.implementations))
                            );
                        }

                    }

                    // Merge list of textDocument/references future in one future which return the list of location information
                    return CompletableFutures.mergeInOneFuture(allFutures, cancellationSupport);
                });
    }

    private static BiFunction<? super List<? extends Location>, Throwable, ? extends List<LSPUsagePsiElement>> reportUsages2(
            Project project,
            LSPUsagePsiElement.UsageKind usageKind) {
        return (locations, error) -> {
            if (error != null) {
                return Collections.emptyList();
            }
            return createUsages(locations, usageKind, project);
        };
    }

    @NotNull
    private static BiFunction<Either<List<? extends Location>, List<? extends LocationLink>>, Throwable, List<LSPUsagePsiElement>> reportUsages(
            @NotNull Project project,
            @NotNull LSPUsagePsiElement.UsageKind usageKind) {
        return (locations, error) -> {
            if (error != null) {
                // How to report error ?
                // - in the log it is a bad idea since it is an error in language server and some ls like go throw an error when there are no result
                // - in the Find usages tree, it should be a good idea, bit how to manage that?
                return Collections.emptyList();
            }
            return createUsages(locations, usageKind, project);
        };
    }

    private static List<LSPUsagePsiElement> createUsages(@Nullable List<? extends Location> locations,
                                                         @NotNull LSPUsagePsiElement.UsageKind usageKind,
                                                         @NotNull Project project) {
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }
        return locations
                .stream()
                .map(location -> LSPUsagesManager.toPsiElement(location, usageKind, project))
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<LSPUsagePsiElement> createUsages(@Nullable Either<List<? extends Location>, List<? extends LocationLink>> locations,
                                                         @Nullable LSPUsagePsiElement.UsageKind usageKind,
                                                         @Nullable Project project) {
        if (locations == null) {
            return Collections.emptyList();
        }
        if (locations.isLeft()) {
            return createUsages(locations.getLeft(), usageKind, project);
        }
        return createUsagesFromLocationLinks(locations.getRight(), usageKind, project);
    }

    private static List<LSPUsagePsiElement> createUsagesFromLocationLinks(@Nullable List<? extends LocationLink> locations,
                                                                          @NotNull LSPUsagePsiElement.UsageKind usageKind,
                                                                          @NotNull Project project) {
        if (locations == null || locations.isEmpty()) {
            return Collections.emptyList();
        }
        return locations
                .stream()
                .map(location -> LSPUsagesManager.toPsiElement(location, usageKind, project))
                .filter(Objects::nonNull)
                .toList();
    }

    private static ReferenceParams createReferenceParams(@NotNull TextDocumentIdentifier textDocument, @NotNull Position position, @NotNull Project project) {
        ReferenceContext context = new ReferenceContext();
        // TODO: manage "IncludeDeclaration" with a settings
        context.setIncludeDeclaration(true);
        return new ReferenceParams(textDocument, position, context);
    }

    private static boolean isUsageSupported(ServerCapabilities serverCapabilities) {
        return LanguageServerItem.isDeclarationSupported(serverCapabilities) ||
                LanguageServerItem.isTypeDefinitionSupported(serverCapabilities) ||
                LanguageServerItem.isDefinitionSupported(serverCapabilities) ||
                LanguageServerItem.isReferencesSupported(serverCapabilities) ||
                LanguageServerItem.isImplementationSupported(serverCapabilities);
    }

}
