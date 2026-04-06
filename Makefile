JAVA_HOME  ?= /usr/lib/jvm/java-17-openjdk
ANDROID_HOME ?= $(HOME)/Android

export JAVA_HOME
export ANDROID_HOME

GRADLEW := ./gradlew
GRADLE_OPTS := --no-daemon

# ─── Build ────────────────────────────────────────────────────────────────────

.PHONY: build
build:
	$(GRADLEW) $(GRADLE_OPTS) assembleDebug

.PHONY: build-release
build-release:
	$(GRADLEW) $(GRADLE_OPTS) assembleRelease

.PHONY: bundle
bundle:
	$(GRADLEW) $(GRADLE_OPTS) bundleRelease

# ─── Tests ───────────────────────────────────────────────────────────────────

.PHONY: test
test:
	$(GRADLEW) $(GRADLE_OPTS) test

.PHONY: test-watch
test-watch:
	$(GRADLEW) test --continuous

# Run a single test class, e.g.:  make test-one CLASS=com.wisdometer.domain.ScoringEngineTest
.PHONY: test-one
test-one:
	$(GRADLEW) $(GRADLE_OPTS) test --tests "$(CLASS)"

# ─── Code quality ────────────────────────────────────────────────────────────

.PHONY: compile
compile:
	$(GRADLEW) $(GRADLE_OPTS) compileDebugKotlin

.PHONY: lint
lint:
	$(GRADLEW) $(GRADLE_OPTS) lintDebug

# ─── Install / run ───────────────────────────────────────────────────────────

.PHONY: install
install: build
	$(GRADLEW) $(GRADLE_OPTS) installDebug

.PHONY: uninstall
uninstall:
	adb uninstall com.wisdometer || true

# ─── Clean ───────────────────────────────────────────────────────────────────

.PHONY: clean
clean:
	$(GRADLEW) $(GRADLE_OPTS) clean

.PHONY: clean-build
clean-build: clean build

# ─── Utilities ───────────────────────────────────────────────────────────────

.PHONY: deps
deps:
	$(GRADLEW) $(GRADLE_OPTS) dependencies --configuration debugRuntimeClasspath

.PHONY: tasks
tasks:
	$(GRADLEW) $(GRADLE_OPTS) tasks

.PHONY: env
env:
	@echo "JAVA_HOME   = $(JAVA_HOME)"
	@echo "ANDROID_HOME= $(ANDROID_HOME)"
	@java -version 2>&1 | head -1
	@$(GRADLEW) --version | head -3
