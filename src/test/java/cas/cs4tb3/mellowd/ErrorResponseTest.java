//Error Response Test
//===================

package cas.cs4tb3.mellowd;

import cas.cs4tb3.mellowd.parser.*;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import static org.junit.Assert.fail;

//This class runs various tests with various erroneous inputs and verifying that
//the compiler catches the issue and responds appropriately.
@RunWith(JUnit4.class)
public class ErrorResponseTest {

    //The tests will require the instantiation of quite a few parsers so we will define
    //some helper methods for creating parsers from a file input or a string.
    private static MellowDParser parserFor(File input) throws IOException {
        ANTLRFileStream inStream = new ANTLRFileStream(input.getAbsolutePath());
        MellowDLexer lexer = new MellowDLexer(inStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MellowDParser parser = new MellowDParser(tokenStream);
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private static MellowDParser parserFor(String input) {
        ANTLRInputStream inStream = new ANTLRInputStream(input);
        MellowDLexer lexer = new MellowDLexer(inStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MellowDParser parser = new MellowDParser(tokenStream);
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private static void printExpectedError(Exception e) {
        System.out.printf("Expected exception was thrown: %s\n", e.getMessage());
    }

    private static MellowD parseAndCompileSong(MellowDParser parser) {
        MellowD mellowD = new MellowD(new TimingEnvironment(4, 4, 120));
        MellowDParseTreeWalker walker = new MellowDParseTreeWalker(mellowD);
        walker.visitSong(parser.song());
        return mellowD;
    }

    //Make sure the parser throws an error if a crescendo token
    //is not specified which would result in some sounds being skipped and
    //remaining in the buffer.
    @Test
    public void noTargetCrescendo() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "block{" +
                "    pp << [a, b, c]*<q> " +
                "}"
        );
        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Compiler did not throw an exception despite a crescendo with no target being specified.");
    }

    //Make sure the compiler notices the block's crescendo target is specified
    //even though it appears in the next fragment.
    @Test
    public void crescendoTargetNextFragment() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "block{" +
                "    pp << [a, b, c]*<q> " +
                "}block{" +
                "    ff" +
                "}");
        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            fail("Compiler threw an exception even though the crescendo was closed in the following block.");
        }
    }

    @Test
    public void crescendoToLowerDynamic() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "block{" +
                "    f << [a]*<q> pp" +
                "}");

        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Expected an error due to a crescendo being specified by the volume decreasing but nothing" +
                "was thrown.");
    }

    //Due to the GM specifications we are restricted in the number of channels available. If
    //an input contains too many different blocks such that they can not be properly distributed
    //amongst these channels an exception should be thrown explaining the problem.
    @Test
    public void tooManyBlocks() throws Exception {
        MellowDParser parser = parserFor(new File(Thread.currentThread().getContextClassLoader()
                .getResource("errortest/largeInput.mlod").toURI().getPath()));

        try {
            parseAndCompileSong(parser).record();
            fail("Expected largeInput.mlod to throw a NoSuchElementException about too many channels but" +
                    "it didn't.");
        } catch (NoSuchElementException e) {
            printExpectedError(e);
        }

    }

    //Test that various incorrect variable declarations and references are caught
    //and handled appropriately.

    //Make sure that attempts to index a melody are caught
    @Test
    public void indexAMelody() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "myChord -> [a, b, c]" +
                "block{" +
                "    [myChord:0]*<q>" +
                "}");

        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Variable myChord was a melody when it should have been a chord to index but the compiler" +
                "silently handled it.");
    }

    //Make sure that attempts to make a phrase from a rhythm identifier starred with
    //a rhythm are caught.
    @Test
    public void rhythmCrossRhythm() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "myChord -> <q, q, q>" +
                "block{" +
                "    myChord*<q>" +
                "}");

        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Variable myChord was a rhythm and the compiler silently starred it with a rhythm.");
    }

    //Ensure attempts to use a melody identifier as a chord param are caught
    @Test
    public void chordMelodyConcatenation() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "myChord -> [a, b, c]" +
                "block{" +
                "    (myChord, a)*<q>" +
                "}");

        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Variable myChord was a melody and the compiler silently accepted it as a chord parameter.");
    }

    //Ensure the correct erroneous identifier is marked
    @Test
    public void secondIdentIncorrect() throws Exception {
        MellowDParser parser = parserFor("" +
                "def block\n" +
                "myMel -> [a, b, c]" +
                "myChord -> (c, e, g)" +
                "block{" +
                "    (myChord, myMel, a)*<q>" +
                "}");

        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Variable myMel was not marked as the problematic token.");
    }

    //Ensure that percussion mappings can be overwritten
    @Test
    public void baseOverridePercussion() throws Exception {
        MellowDParser parser = parserFor("" +
                "def percussion block\n" +
                "hHat -> <q>" +
                "sample ->* [hHat, tri, lBongo]*<q>" +
                "block { sample }");

        try {
            parseAndCompileSong(parser);
        } catch (CompilationException e) {
            printExpectedError(e);
            return;
        }

        fail("Compiler ignored redefined percussion sound hHat.");

    }
}
