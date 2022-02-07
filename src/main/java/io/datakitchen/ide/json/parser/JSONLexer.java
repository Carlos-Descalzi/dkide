// Generated from JSON.g4 by ANTLR 4.9.1

package io.datakitchen.ide.json.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSONLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, NULL=9, 
		BOOL=10, STRING=11, ID=12, NUMBER=13, WS=14;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "NULL", 
			"BOOL", "STRING", "ID", "ESC", "UNICODE", "HEX", "SAFECODEPOINT", "NUMBER", 
			"INT", "EXP", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "','", "'}'", "':'", "'['", "']'", "'{{'", "'}}'", "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "NULL", "BOOL", 
			"STRING", "ID", "NUMBER", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public JSONLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JSON.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\20\u0093\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3"+
		"\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13L\n\13\3\f\3\f\3\f\7\fQ\n\f\f\f"+
		"\16\fT\13\f\3\f\3\f\3\r\3\r\7\rZ\n\r\f\r\16\r]\13\r\3\16\3\16\3\16\5\16"+
		"b\n\16\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\21\3\21\3\22\5\22o\n"+
		"\22\3\22\3\22\3\22\6\22t\n\22\r\22\16\22u\5\22x\n\22\3\22\5\22{\n\22\3"+
		"\23\3\23\3\23\7\23\u0080\n\23\f\23\16\23\u0083\13\23\5\23\u0085\n\23\3"+
		"\24\3\24\5\24\u0089\n\24\3\24\3\24\3\25\6\25\u008e\n\25\r\25\16\25\u008f"+
		"\3\25\3\25\2\2\26\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\2\35\2\37\2!\2#\17%\2\'\2)\20\3\2\f\4\2C\\c|\b\2))\60\60\62;C]"+
		"__c|\n\2$$\61\61^^ddhhppttvv\5\2\62;CHch\5\2\2!$$^^\3\2\62;\3\2\63;\4"+
		"\2GGgg\4\2--//\5\2\13\f\17\17\"\"\2\u0099\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2#\3\2\2\2\2)\3"+
		"\2\2\2\3+\3\2\2\2\5-\3\2\2\2\7/\3\2\2\2\t\61\3\2\2\2\13\63\3\2\2\2\r\65"+
		"\3\2\2\2\17\67\3\2\2\2\21:\3\2\2\2\23=\3\2\2\2\25K\3\2\2\2\27M\3\2\2\2"+
		"\31W\3\2\2\2\33^\3\2\2\2\35c\3\2\2\2\37i\3\2\2\2!k\3\2\2\2#n\3\2\2\2%"+
		"\u0084\3\2\2\2\'\u0086\3\2\2\2)\u008d\3\2\2\2+,\7}\2\2,\4\3\2\2\2-.\7"+
		".\2\2.\6\3\2\2\2/\60\7\177\2\2\60\b\3\2\2\2\61\62\7<\2\2\62\n\3\2\2\2"+
		"\63\64\7]\2\2\64\f\3\2\2\2\65\66\7_\2\2\66\16\3\2\2\2\678\7}\2\289\7}"+
		"\2\29\20\3\2\2\2:;\7\177\2\2;<\7\177\2\2<\22\3\2\2\2=>\7p\2\2>?\7w\2\2"+
		"?@\7n\2\2@A\7n\2\2A\24\3\2\2\2BC\7v\2\2CD\7t\2\2DE\7w\2\2EL\7g\2\2FG\7"+
		"h\2\2GH\7c\2\2HI\7n\2\2IJ\7u\2\2JL\7g\2\2KB\3\2\2\2KF\3\2\2\2L\26\3\2"+
		"\2\2MR\7$\2\2NQ\5\33\16\2OQ\5!\21\2PN\3\2\2\2PO\3\2\2\2QT\3\2\2\2RP\3"+
		"\2\2\2RS\3\2\2\2SU\3\2\2\2TR\3\2\2\2UV\7$\2\2V\30\3\2\2\2W[\t\2\2\2XZ"+
		"\t\3\2\2YX\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\\32\3\2\2\2][\3\2\2"+
		"\2^a\7^\2\2_b\t\4\2\2`b\5\35\17\2a_\3\2\2\2a`\3\2\2\2b\34\3\2\2\2cd\7"+
		"w\2\2de\5\37\20\2ef\5\37\20\2fg\5\37\20\2gh\5\37\20\2h\36\3\2\2\2ij\t"+
		"\5\2\2j \3\2\2\2kl\n\6\2\2l\"\3\2\2\2mo\7/\2\2nm\3\2\2\2no\3\2\2\2op\3"+
		"\2\2\2pw\5%\23\2qs\7\60\2\2rt\t\7\2\2sr\3\2\2\2tu\3\2\2\2us\3\2\2\2uv"+
		"\3\2\2\2vx\3\2\2\2wq\3\2\2\2wx\3\2\2\2xz\3\2\2\2y{\5\'\24\2zy\3\2\2\2"+
		"z{\3\2\2\2{$\3\2\2\2|\u0085\7\62\2\2}\u0081\t\b\2\2~\u0080\t\7\2\2\177"+
		"~\3\2\2\2\u0080\u0083\3\2\2\2\u0081\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082"+
		"\u0085\3\2\2\2\u0083\u0081\3\2\2\2\u0084|\3\2\2\2\u0084}\3\2\2\2\u0085"+
		"&\3\2\2\2\u0086\u0088\t\t\2\2\u0087\u0089\t\n\2\2\u0088\u0087\3\2\2\2"+
		"\u0088\u0089\3\2\2\2\u0089\u008a\3\2\2\2\u008a\u008b\5%\23\2\u008b(\3"+
		"\2\2\2\u008c\u008e\t\13\2\2\u008d\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f"+
		"\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u0092\b\25"+
		"\2\2\u0092*\3\2\2\2\21\2KPRY[anuwz\u0081\u0084\u0088\u008f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}