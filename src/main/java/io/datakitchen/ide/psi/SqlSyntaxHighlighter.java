package io.datakitchen.ide.psi;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SqlSyntaxHighlighter implements SyntaxHighlighter {

    private final static Set<String> TOKEN_SKIP = Set.of("SPACES","EOF","UNEXPECTED_CHAR");
    private final static Set<String> TOKEN_KEYWORDS = Set.of("USE","OR","REPLACE","IF","NOT","EXISTS","DATABASE","SELECT","VIEW","FROM","INSERT","UPDATE","ALTER","TABLE","DROP","CREATE","WHERE","ORDER","BY","VALUES");
    private final static Set<String> TOKEN_OPERATORS = Set.of("'!='","'='","'>'","'<'","'<>'","'>='","'<='","IN","';'","'*'","'('","')'","'.'","','");
    private final static String TOKEN_STRING_LITERAL = "STRINGLITERAL";
    private static final String TOKEN_NUMERIC_LITERAL = "NUMERICLITERAL";
    private static final String TOKEN_IDENTIFIER = "IDENTIFIER";

    public static final TextAttributesKey KEYWORD_KEY = TextAttributesKey.createTextAttributesKey("sql.keyword", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey STRING_LITERAL = TextAttributesKey.createTextAttributesKey("sql.string.literal", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey("sql.identifier", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey("sql.operator", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey NUMERIC_LITERAL = TextAttributesKey.createTextAttributesKey("sql.numeric.literal", DefaultLanguageHighlighterColors.NUMBER);

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new SqlLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (!TOKEN_SKIP.contains(tokenType.getDebugName())){

            String token = tokenType.getDebugName().replace("_","").trim();

            if (TOKEN_KEYWORDS.contains(token)){
                return new TextAttributesKey[]{KEYWORD_KEY};
            } else if (TOKEN_OPERATORS.contains(token)){
                return new TextAttributesKey[]{OPERATOR};
            } else if (TOKEN_STRING_LITERAL.equals(token)){
                return new TextAttributesKey[]{STRING_LITERAL};
            } else if (TOKEN_NUMERIC_LITERAL.equals(token)) {
                return new TextAttributesKey[]{NUMERIC_LITERAL};
            } else if (TOKEN_IDENTIFIER.equals(token)){
                return new TextAttributesKey[]{IDENTIFIER};
            } else {
                System.out.println("Unknown token "+token);
            }
        }
        return new TextAttributesKey[0];
    }
}
