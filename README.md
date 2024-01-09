## Functionality
The compiler comprises several classes that work together seamlessly to process and analyze the source code. The "Lexer" class is responsible for breaking down the source file into individual tokens by analyzing characters based on specific patterns. It efficiently handles whitespace and comments while reporting any illegal characters encountered during the lexing process. The result is a list of tokens, each with its position and type, facilitating further processing by the parser and other compiler components.

The "Parser" class takes the tokenized input from the lexer and performs recursive-descent parsing, adhering to a predefined grammar. It constructs the Abstract Syntax Tree (AST) representation of the source program, ensuring the correct syntax is followed based on the recognized tokens.

Next, the "Constrainer" class plays a crucial role in ensuring that expressions, variables, and function calls in the program adhere to the language's typing rules. It performs type checking to guarantee that the program's types are correctly matched and compatible, which is essential for generating bytecode during code generation.

These classes use a visitor design pattern to perform different operations on the AST nodes without altering the node classes directly. Specifically, the "DrawOffsetVisitor" is responsible for drawing the AST with offsets, enhancing the visual representation of the AST. Meanwhile, the "OffsetVisitor" calculates the offsets of the AST nodes, facilitating efficient memory management during compilation. Both visitors implement the ASTVisitor interface, which defines methods for visiting different types of AST nodes.

Overall, this cohesive system of classes effectively processes, analyzes, and ensures the correctness of the source code, leading to the successful compilation of the program.

## Data Strucutres Used
In the OffsetVisitor class, an efficient data structure called "HashMap<AST, Integer>" is utilized to store the offset values for each AST node. This "HashMap" allows easy association of AST nodes with their corresponding offset values, providing a quick lookup mechanism for the compiler during compilation.

To manage sets of specific tokens effectively, the Parser class employs "EnumSet" instances named "relationalOps", "addingOps", and "multiplyingOps". These "EnumSets" act as compact and optimized containers for token sets, allowing for efficient membership checks and set operations.

The DrawOffsetVisitor class leverages arrays to organize and track various aspects of the AST drawing process. The "int[] nCount" array stores the count of nodes at each depth level, aiding in the accurate layout of the AST. The "int[] currOffset" array keeps track of the current offset value for each depth level, assisting the OffsetVisitor in calculating offsets for the AST nodes. Additionally, the "int[] progress" array helps the drawing process by tracking the traversal progress at each depth level.

For graphical rendering, the DrawOffsetVisitor class utilizes the "BufferedImage" data structure. This allows the compiler to create a graphical canvas on which the AST nodes are drawn, resulting in a clear visual representation of the AST.

Finally, in the Constrainer class, a "Stack" named "symTabStack" serves as a powerful tool for managing the symbol table during the semantic analysis phase. This stack efficiently handles scoping rules and symbol table nesting, ensuring the correct matching of types and proper adherence to language rules, crucial for generating bytecode during code generation.
