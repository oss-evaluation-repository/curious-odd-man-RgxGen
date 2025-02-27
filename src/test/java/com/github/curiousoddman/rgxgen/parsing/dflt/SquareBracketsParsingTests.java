package com.github.curiousoddman.rgxgen.parsing.dflt;

import com.github.curiousoddman.rgxgen.model.MatchType;
import com.github.curiousoddman.rgxgen.model.RgxGenCharsDefinition;
import com.github.curiousoddman.rgxgen.nodes.Node;
import com.github.curiousoddman.rgxgen.nodes.SymbolSet;
import com.github.curiousoddman.rgxgen.util.chars.CharList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static com.github.curiousoddman.rgxgen.model.SymbolRange.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


public class SquareBracketsParsingTests {

    private static SymbolSet mkSS(char... chars) {
        return SymbolSet.ofAsciiCharacters(Arrays.toString(chars), chars, MatchType.POSITIVE);
    }

    private static SymbolSet mkRange(char start, char end) {
        return SymbolSet.ofAsciiRanges(start + ":" + end, Collections.singletonList(range(start, end)), MatchType.POSITIVE);
    }

    private static SymbolSet mkRangeAndChars(char start, char end, char... chars) {
        return SymbolSet.ofAscii(start + ":" + end, Collections.singletonList(range(start, end)), CharList.charList(chars), MatchType.POSITIVE);
    }

    private static SymbolSet mkWhitespaceAnd(char... chars) {
        RgxGenCharsDefinition negativeMatchDefinitions = RgxGenCharsDefinition
                .of(chars)
                .withCharacters('\t', '\n', '\u000B', '\f', '\r', ' ');
        return SymbolSet.ofAscii("",
                                 RgxGenCharsDefinition.of(chars).withCharacters('\t', ' '),
                                 negativeMatchDefinitions,
                                 MatchType.POSITIVE);
    }


    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("[a-c]", mkRange('a', 'c')),
                Arguments.of("[a-c-]", mkRangeAndChars('a', 'c', '-')),
                Arguments.of("[a-c-x]", mkRangeAndChars('a', 'c', 'x', '-')),
                Arguments.of("[-a-c]", mkRangeAndChars('a', 'c', '-')),
                Arguments.of("[\\x30-\\x{0032}]", mkRange('0', '2')),
                Arguments.of("[\\s-]", mkWhitespaceAnd('-')),
                Arguments.of("[-]", mkSS('-')),
                Arguments.of("[\\s-a-\\s]", new RgxGenParseException("Cannot make range with a shorthand escape sequences before '\n" +
                                                                             "'s-a-\\s]'\n" +
                                                                             "      ^'")),
                Arguments.of("[\\s-a]", mkWhitespaceAnd('a', '-')),
                Arguments.of("[\\s]", mkWhitespaceAnd()),
                Arguments.of("[a-]", mkSS('a', '-')));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void parsingTest(String pattern, Object expected) {
        try {
            DefaultTreeBuilder builder = new DefaultTreeBuilder(pattern, null);
            Node node = builder.get();
            assertEquals(expected.toString(), node.toString());
        } catch (RgxGenParseException e) {
            if (expected instanceof Throwable) {
                assertEquals(e.getMessage(), ((Throwable) expected).getMessage(), e.getMessage());
            } else {
                e.printStackTrace();
                fail("Got exception when expected SymbolSet. " + e.getMessage());
            }
        }
    }
}
