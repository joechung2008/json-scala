name := "shared-lib"

coverageEnabled := sys.props.getOrElse("env", "dev") != "prod"
