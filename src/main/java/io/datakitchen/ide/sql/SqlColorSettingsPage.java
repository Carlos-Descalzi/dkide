package io.datakitchen.ide.sql;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.RainbowColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import io.datakitchen.ide.psi.SqlLanguage;
import io.datakitchen.ide.psi.SqlSyntaxHighlighter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class SqlColorSettingsPage implements RainbowColorSettingsPage {
    @Override
    public @Nullable Icon getIcon() {
        return null;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new SqlSyntaxHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        return "SELECT * FROM PRODUCTS WHERE TYPE = 'Appliances';";
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return new AttributesDescriptor[]{
                new AttributesDescriptor("Keywords", SqlSyntaxHighlighter.KEYWORD_KEY),
                new AttributesDescriptor("String literals",SqlSyntaxHighlighter.STRING_LITERAL),
                new AttributesDescriptor("Operators",SqlSyntaxHighlighter.OPERATOR),
                new AttributesDescriptor("Identifiers", SqlSyntaxHighlighter.IDENTIFIER)
        };
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return new ColorDescriptor[0];
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "SQL";
    }

    @Override
    public boolean isRainbowType(TextAttributesKey type) {
        return true;
    }

    @Override
    public @Nullable Language getLanguage() {
        return SqlLanguage.INSTANCE;
    }
}
