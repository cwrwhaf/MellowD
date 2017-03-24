package cas.cs4tb3.mellowd.intermediate.executable;

import cas.cs4tb3.mellowd.parser.CompilationException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class SourceLink {
    public final int start;
    public final int line;
    public final int startPosInLine;
    public final int stop;
    public final String text;

    public SourceLink(ParserRuleContext info) {
        this.start = info.getStart().getStartIndex();
        this.line = info.getStart().getLine();
        this.startPosInLine = info.getStart().getCharPositionInLine();
        this.stop = info.getStop().getStopIndex();
        this.text = info.getText();
    }

    public SourceLink(TerminalNode node) {
        Token info = node.getSymbol();
        this.start = info.getStartIndex();
        this.line = info.getLine();
        this.startPosInLine = info.getCharPositionInLine();
        this.stop = info.getStopIndex();
        this.text = info.getText();
    }

    public CompilationException toCompilationException(Throwable cause) {
        return new CompilationException(start, line, startPosInLine, stop, text, cause);
    }
}
