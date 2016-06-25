package cas.cs4tb3.mellowd.parser;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CompilationException extends RuntimeException {
    private int start;
    private int line;
    private int startPosInLine;
    private int stop;
    private String text;

    public CompilationException(Token problem, Throwable cause) {
        super(cause);
        init(problem);
    }

    private void init(Token problem) {
        this.start = problem.getStartIndex();
        this.line = problem.getLine();
        this.startPosInLine = problem.getCharPositionInLine();
        this.stop = problem.getStopIndex();
        this.text = problem.getText();
    }

    public CompilationException(ParserRuleContext problem, Throwable cause) {
        super(cause);
        init(problem);
    }

    private void init(ParserRuleContext problem) {
        this.start = problem.start.getStartIndex();
        this.line = problem.start.getLine();
        this.startPosInLine = problem.start.getCharPositionInLine();
        this.stop = problem.stop.getStopIndex();
        this.text = problem.getText();
    }

    public CompilationException(ParseTree problem, Throwable cause) {
        super(cause);
        if (problem instanceof ParserRuleContext) {
            init((ParserRuleContext) problem);
        } else if (problem instanceof TerminalNode) {
            init(((TerminalNode) problem).getSymbol());
        } else {
            init(new CommonToken(-1, problem.getText()));
        }
    }

    public int getStart() {
        return start;
    }

    public int getLine() {
        return line;
    }

    public int getStartPosInLine() {
        return startPosInLine;
    }

    public int getStop() {
        return stop;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getMessage() {
        return getCause() != null ? getCause().getMessage() : "null";
    }
}