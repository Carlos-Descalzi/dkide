package io.datakitchen.ide.json;

import com.intellij.openapi.vfs.VirtualFile;
import io.datakitchen.ide.json.parser.JSONLexer;
import io.datakitchen.ide.json.parser.JSONParser;
import io.datakitchen.ide.json.parser.JSONVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * JSON Parsing with basic Jinja support.
 * Jinja support is restricted to values in objects and arrays only.
 */
public class CustomJsonParser {

    public static <T> T parse(VirtualFile virtualFile) throws IOException, ParseException {
        try (InputStream input = virtualFile.getInputStream()) {
            return parse(input);
        }
    }

    public static <T> T parse(InputStream input) throws IOException, ParseException {
        return doParse(CharStreams.fromStream(input));
    }

    public static <T> T parse(String jsonString) throws ParseException {
        return doParse(CharStreams.fromString(jsonString));
    }

    @SuppressWarnings({"raw","unchecked"})
    private static <T> T doParse(CharStream charStream) throws ParseException{
        try {
            JSONLexer lexer = new JSONLexer(charStream);
            ErrorListener errorListener = new ErrorListener();
            lexer.addErrorListener(errorListener);
            CommonTokenStream stream = new CommonTokenStream(lexer);

            JSONParser parser = new JSONParser(stream);
            JSONParser.JsonContext ctx = parser.json();
            Visitor visitor = new Visitor();
            parser.addErrorListener(errorListener);
            return (T)visitor.visitJson(ctx);
        }catch (RuntimeParseException ex){
            throw new ParseException(ex.message,ex.line,ex.charPosition);
        }
    }

    private static class Pair {
        private final String key;
        private final Object value;

        public Pair(String key, Object value){
            this.key = key;
            this.value = value;
        }
    }

    private static class Visitor implements JSONVisitor<Object> {

        @Override
        public Object visitJson(JSONParser.JsonContext ctx) {
            return visitValue(ctx.value());
        }

        @Override
        public Object visitObj(JSONParser.ObjContext ctx) {
            Map<String, Object> obj = new LinkedHashMap<>();
            for (JSONParser.PairContext pc : ctx.pair()){
                Pair pair = (Pair)this.visitPair(pc);
                obj.put(pair.key, pair.value);
            }
            return obj;
        }

        @Override
        public Object visitPair(JSONParser.PairContext ctx) {
            return new Pair((String)visitTerminal(ctx.STRING()),this.visitValue(ctx.value()));
        }

        @Override
        public Object visitArr(JSONParser.ArrContext ctx) {
            List<Object> arr = new ArrayList<>();
            for (JSONParser.ValueContext vc : ctx.value()){
                arr.add(this.visitValue(vc));
            }
            return arr;
        }

        @Override
        public Object visitVarRef(JSONParser.VarRefContext ctx) {
            return new VariableReference(String.valueOf(this.visitTerminal(ctx.ID())));
        }

        @Override
        public Object visitValue(JSONParser.ValueContext ctx) {
            if (ctx.NUMBER() != null){
                return visitTerminal(ctx.NUMBER());
            } else if (ctx.STRING() != null){
                return visitTerminal(ctx.STRING());
            } else if (ctx.obj() != null){
                return visitObj(ctx.obj());
            } else if (ctx.arr() != null){
                return visitArr(ctx.arr());
            } else if (ctx.BOOL() != null){
                return visitTerminal(ctx.BOOL());
            } else if (ctx.varRef() != null){
                return visitVarRef(ctx.varRef());
            }
            return null;
        }

        @Override
        public Object visit(ParseTree tree) {
            return null;
        }

        @Override
        public Object visitChildren(RuleNode node) {
            return null;
        }

        @Override
        public Object visitErrorNode(ErrorNode node) {
            return null;
        }

        @Override
        public Object visitTerminal(TerminalNode node) {
            Token token = node.getSymbol();
            String text = node.getText();
            switch(token.getType()){
                case JSONLexer.STRING:
                    return text.substring(1,text.length()-1);
                case JSONLexer.NUMBER:
                    return Integer.parseInt(text);
                case JSONLexer.BOOL:
                    return Boolean.valueOf(text);
                case JSONLexer.ID:
                    return text;
            }
            return null;
        }

    }

    private static class ErrorListener implements ANTLRErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new RuntimeParseException(msg, line, charPositionInLine);
        }

        @Override
        public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {
        }

        @Override
        public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {
        }
    }

    private static class RuntimeParseException extends RuntimeException {
        private final String message;
        private final int line;
        private final int charPosition;
        public RuntimeParseException(String message, int line, int charPosition){
            this.message = message;
            this.line = line;
            this.charPosition = charPosition;
        }
    }
}
