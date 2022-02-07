package io.datakitchen.ide.psi;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharSequenceReader;
import io.datakitchen.ide.psi.antlr.sql.SQLLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SqlLexerAdapter extends Lexer {

    private static final Map<Integer, SqlElementType> ELEMENT_TYPES = new HashMap<>();
    private SQLLexer lexer;
    private LexerPositionImpl position;
    private CharSequence buffer;
    private Token currentToken;
    private int startOffset;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer.subSequence(startOffset, endOffset);
        this.startOffset = startOffset;
        this.position = new LexerPositionImpl(initialState, startOffset, startOffset);
        try {
            lexer = new SQLLexer(CharStreams.fromReader(new CharSequenceReader(this.buffer)));
            lexer.setChannel(SQLLexer.DEFAULT_TOKEN_CHANNEL);
            lexer.reset();
        }catch(IOException ex){}
        advance();
    }

    @Override
    public int getState() {
        return position != null ? position.getState() : 0;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        if (currentToken != null && currentToken.getType() != SQLLexer.EOF) {
            SqlElementType elementType = ELEMENT_TYPES.get(currentToken.getType());
            if (elementType == null) {
                elementType = new SqlElementType(SQLLexer.VOCABULARY.getDisplayName(currentToken.getType()));
                ELEMENT_TYPES.put(currentToken.getType(), elementType);
            }
            return elementType;
        }
        return null;
    }

    @Override
    public int getTokenStart() {
        return startOffset + (currentToken != null ? currentToken.getStartIndex() : 0);
    }

    @Override
    public int getTokenEnd() {
        return startOffset + (currentToken != null ? currentToken.getStopIndex()+1 : 0);
    }

    @Override
    public void advance() {
        currentToken = lexer.nextToken();
        if (currentToken != null) {
            position = new LexerPositionImpl(
                lexer.getState(),
                startOffset + currentToken.getStartIndex(),
                startOffset + currentToken.getStopIndex() + 1
            );
        } else {
            position = null;
        }
    }

    @Override
    public @NotNull LexerPosition getCurrentPosition() {
        return position;
    }

    @Override
    public void restore(@NotNull LexerPosition position) {
        this.position = (LexerPositionImpl) position;
        lexer.reset(); // TODO
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return buffer.length()-1;
    }

    private class LexerPositionImpl implements LexerPosition {

        private int state;
        private int offset;
        private int end;

        public LexerPositionImpl(int state, int offset, int end){
            this.state = state;
            this.offset = offset;
            this.end = end;
        }
        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getState() {
            return state;
        }

        public int getEnd() {
            return end;
        }

    }
}
