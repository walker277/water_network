#!/bin/bash
#!/bin/bash
set -e

# vytvoří složku bin jen pokud ještě neexistuje
mkdir -p ./bin

# poskládá všechny JARy v lib/
JARS=$(printf ":%s" lib/*.jar); JARS="${JARS#:}"

# vlastní kompilace
javac -encoding UTF-8 -cp "./lib/Jama-1.0.3.jar:./src:$JARS" -d ./bin src/*.java

