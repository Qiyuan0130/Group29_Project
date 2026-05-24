# JavaDoc — TA Recruitment System

This folder holds generated API documentation for the Java backend, as required by the EBU6304 final submission (code documentation such as JavaDocs).

## Generate HTML documentation

From the `software-code` directory:

```bash
mvn javadoc:javadoc
```

Output is written to:

```
software-code/target/site/apidocs/index.html
```

Open that file in a browser to browse packages, classes, and public methods.

## Generate Javadoc JAR (optional)

```bash
mvn javadoc:jar
```

Produces `target/java-web-json-javadoc.jar` for inclusion in the submission ZIP.

## What is documented

- **Package overviews** — `package-info.java` in each package (`web`, `servlet`, `repo`, `model`, `dto`, `service`, `util`, `filter`)
- **Classes** — purpose and main responsibilities
- **Public methods** — repositories, utilities, and key API helpers
- **DTO fields** — request/response JSON shapes for REST endpoints

## Submission checklist

When preparing `Software_groupXXX.zip`, include either:

1. The generated `target/site/apidocs/` folder, or  
2. The `target/java-web-json-javadoc.jar` file, or  
3. Both

Ensure JavaDoc is regenerated after significant code changes before the final hand-in.
