# Script to run the JSON Scala CLI JAR
# Handle piped input by passing it to the Java process
if ($MyInvocation.ExpectingInput) {
    # If input is being piped, read from stdin and pass to Java
    $input | java -jar cli/target/scala-3.7.2/cli-app-assembly-0.1.0-SNAPSHOT.jar @args
} else {
    # If no piped input, just run Java with arguments
    java -jar cli/target/scala-3.7.2/cli-app-assembly-0.1.0-SNAPSHOT.jar @args
}
