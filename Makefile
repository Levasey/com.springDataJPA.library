# Maven (Spring Boot JAR) — wrapper по умолчанию (не нужен системный mvn).
# Windows: make MVN=mvnw.cmd package
MVN ?= ./mvnw

.PHONY: default help clean compile test package verify install skip-tests \
	run-package

default: package

help:
	@echo "Targets:"
	@echo "  make package       — build JAR (target/com.springdatajpa.library.jar)"
	@echo "  make compile       — compile sources"
	@echo "  make test          — run unit tests"
	@echo "  make verify        — package + integration checks (if any)"
	@echo "  make clean         — remove target/"
	@echo "  make install       — install into local Maven repo"
	@echo "  make skip-tests    — package without running tests (-DskipTests)"
	@echo "  Variables: MVN=$(MVN)"

clean:
	$(MVN) clean

compile:
	$(MVN) -q compile

test:
	$(MVN) test

package:
	$(MVN) package

verify:
	$(MVN) verify

install:
	$(MVN) install

skip-tests:
	$(MVN) package -DskipTests

# Show where the artifact ends up after package
run-package: package
	@echo "JAR: $$(pwd)/target/com.springdatajpa.library.jar"
