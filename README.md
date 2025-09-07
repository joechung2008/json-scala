# json-scala

Port of the .NET 8.0 F# JSON parser to Scala 3.7.2.

## License

MIT

## Reference

[json.org](https://www.json.org/json-en.html)

## Prerequisites

- [Scala](https://www.scala-lang.org/) (version 3.7.2 or later)
- [sbt](https://www.scala-sbt.org/) (version 1.x or later)
- sbt-assembly plugin (included in project/plugins.sbt)

## Build

Compile all projects in the monorepo:

```bash
sbt compile
```

## Format

Format the code using Scalafmt (if configured):

```bash
sbt scalafmt
```

## Test

Run tests for all projects:

```bash
sbt test
```

## Code Coverage

This project uses [Scoverage](https://github.com/scoverage/sbt-scoverage) for measuring code coverage.

### Run Tests with Coverage

To run tests and measure code coverage:

```bash
sbt "project lib" coverage test
```

### Generate Coverage Report

To run tests with coverage and generate an HTML coverage report:

```bash
sbt "project lib" coverage test coverageReport
```

### View Coverage Report

After generating the report, open the HTML coverage report in your browser:

- **File location**: `./lib/target/scala-3.7.2/scoverage-report/index.html`
- **Coverage metrics**: Shows statement and branch coverage percentages
- **Interactive view**: Click through packages and files to see line-by-line coverage

### Additional Coverage Commands

```bash
# Generate coverage report without re-running tests
sbt "project lib" coverageReport

# Clean coverage data
sbt "project lib" coverage clean

# Run coverage for all projects (if enabled)
sbt coverage test coverageReport
```

### Coverage Output

The coverage report includes:
- **Statement coverage**: Percentage of executable statements covered
- **Branch coverage**: Percentage of branches (if/else, match cases) covered
- **Line-by-line highlighting**: Green for covered, red for uncovered lines
- **XML reports**: Available for CI/CD integration at `lib/target/scala-3.7.2/scoverage-report/scoverage.xml`

## Run the CLI

The CLI application reads JSON from stdin, parses it, and outputs a pretty-printed version or an error message.

First, build the assembly JAR (requires sbt-assembly plugin):

```bash
sbt cli/assembly
```

This creates a fat JAR at `cli/target/scala-3.7.2/cli-app-assembly-0.1.0-SNAPSHOT.jar`.

Run the CLI with the JAR:

```bash
java -jar cli/target/scala-3.7.2/cli-app-assembly-0.1.0-SNAPSHOT.jar
```

Use the Bash script to run the JAR:

```bash
./bin/cli
```

Pipe JSON input to the script:

```bash
echo '{"name": "John", "age": 30}' | ./bin/cli
```

### Windows

On Windows, use the provided batch script or PowerShell script:

**Batch (cmd.exe):**

```cmd
bin\cli.cmd
```

Pipe JSON input:

```cmd
echo {"name": "John", "age": 30} | bin\cli.cmd
```

**PowerShell:**

```powershell
.\bin\cli.ps1
```

Pipe JSON input:

```powershell
echo '{"name": "John", "age": 30}' | .\bin\cli.ps1
```

### Examples

Valid JSON:

```bash
echo '[1, 2, {"key": "value"}]' | ./bin/cli
```

Output:

```
[
  1,
  2,
  {
    "key": "value"
  }
]
```

Invalid JSON:

```bash
echo '{"name": "John", "age":}' | ./bin/cli
```

Output:

```
Unexpected delimiter
```

## Package

Create JAR files for the projects:

```bash
sbt package
```

For the CLI specifically:

```bash
sbt cli/package
```

To create a fat JAR for the CLI (includes all dependencies):

```bash
sbt cli/assembly
```
