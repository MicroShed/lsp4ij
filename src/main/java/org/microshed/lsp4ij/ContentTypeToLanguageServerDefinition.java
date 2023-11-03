package org.microshed.lsp4ij;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.concurrent.CompletableFuture;

public class ContentTypeToLanguageServerDefinition extends AbstractMap.SimpleEntry<Language, LanguageServersRegistry.LanguageServerDefinition> {

    private final DocumentMatcher documentMatcher;

    public ContentTypeToLanguageServerDefinition(@NotNull Language language,
                                                 @NotNull LanguageServersRegistry.LanguageServerDefinition provider,
                                                 @NotNull DocumentMatcher documentMatcher) {
        super(language, provider);
        this.documentMatcher = documentMatcher;
    }

    public boolean match(VirtualFile file, Project project) {
        return documentMatcher.match(file, project);
    }

    public boolean shouldBeMatchedAsynchronously(Project project) {
        return documentMatcher.shouldBeMatchedAsynchronously(project);
    }

    public boolean isEnabled() {
        return getValue().isEnabled();
    }

    public @NotNull <R> CompletableFuture<Boolean> matchAsync(VirtualFile file, Project project) {
        return documentMatcher.matchAsync(file, project);
    }
}
