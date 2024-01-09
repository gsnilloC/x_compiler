package tests.requirements.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ast.*;
import lexer.ILexer;
import parser.Parser;
import tests.helpers.Helpers;
import tests.helpers.TestVisitor;
import visitor.ASTVisitor;

public class TypeTests {

    @ParameterizedTest
    @MethodSource("provideTypePrograms")
    void testTypes(ILexer lexer, List<AST> expectedAst) throws Exception {
        final Parser parser = new Parser(lexer);
        AST ast = parser.execute();

        // Helpful for debugging (please remember to comment before submission!):
        // PrintVisitor printer = new PrintVisitor();
        // ast.accept(printer);

        ASTVisitor visitor = new TestVisitor(expectedAst);
        Object result = ast.accept(visitor);

        assertEquals(null, result);
    }

    private static Stream<Arguments> provideTypePrograms() throws Exception {
        return Stream.of(
                Arguments.of(lexerForType("int"), expectedAstForType("int")),
                Arguments.of(lexerForType("boolean"), expectedAstForType("boolean")),
                Arguments.of(lexerForType("string"), expectedAstForType("string")),
                Arguments.of(lexerForType("hex"), expectedAstForType("hex")));
    }

    private static ILexer lexerForType(String type) throws Exception {
        return Helpers.lexerFromPseudoProgram(
                String.format(String.join(
                        System.lineSeparator(),
                        "program { %s <id>",
                        "<id> = <%s>",
                        "}"), type, type));
    }

    private static List<AST> expectedAstForType(String type) {
        return Arrays.asList(
                new ProgramTree(),
                new BlockTree(),
                new DeclTree(),
                Helpers.getTestAst(type),
                new IdTree(Helpers.getTestToken("<id>")),
                new AssignTree(),
                new IdTree(Helpers.getTestToken("<id>")),
                Helpers.getTestAst(String.format("<%s>", type)));
    }
}
