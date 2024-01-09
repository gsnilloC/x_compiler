clean:
	@echo "Deleting all class files..."
	rm -rf target
	find . -name "*.class" -type f -delete

compiler: clean
	@echo "Building compiler/Compiler.java..."
	javac -d target compiler/Compiler.java

compiler-run: compiler
	@echo "Running compiler.Compiler $(source)"
	java -cp target compiler.Compiler $(source)

compiler-simple: compiler
	java -cp target compiler.Compiler sample_files/simple.x

build-test: clean
	@echo "Building project with tests..."
	find . -name "*.java" > sources.txt
	-javac -d target -cp target:lib/junit-platform-console-standalone-1.9.0.jar:. @sources.txt
	rm sources.txt

test: build-test
	@echo "Running tests... (Note that tests that fail to compile will not be included!)"
	java -jar lib/junit-platform-console-standalone-1.9.0.jar --class-path ./target --scan-classpath
