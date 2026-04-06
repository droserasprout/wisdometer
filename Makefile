JAVA_HOME    ?= /usr/lib/jvm/java-17-openjdk
ANDROID_HOME := $(HOME)/Android/Sdk

export JAVA_HOME
export ANDROID_HOME

GRADLEW      := ./gradlew
GRADLE_OPTS  := --no-daemon

.PHONY: $(MAKECMDGOALS)

# ─── Build ────────────────────────────────────────────────────────────────────

build:
	$(GRADLEW) $(GRADLE_OPTS) assembleDebug

build-release:
	$(GRADLEW) $(GRADLE_OPTS) assembleRelease

bundle:
	$(GRADLEW) $(GRADLE_OPTS) bundleRelease

# ─── Tests ───────────────────────────────────────────────────────────────────

test:
	$(GRADLEW) $(GRADLE_OPTS) test

test-watch:
	$(GRADLEW) test --continuous

# Run a single test class, e.g.:  make test-one CLASS=com.wisdometer.domain.ScoringEngineTest
test-one:
	$(GRADLEW) $(GRADLE_OPTS) test --tests "$(CLASS)"

# ─── Code quality ────────────────────────────────────────────────────────────

compile:
	$(GRADLEW) $(GRADLE_OPTS) compileDebugKotlin

lint:
	$(GRADLEW) $(GRADLE_OPTS) lintDebug

# ─── Install / run ───────────────────────────────────────────────────────────

install: build
	$(GRADLEW) $(GRADLE_OPTS) installDebug

uninstall:
	adb uninstall com.wisdometer || true

# ─── Clean ───────────────────────────────────────────────────────────────────

clean:
	$(GRADLEW) $(GRADLE_OPTS) clean

clean-build: clean build

# ─── Utilities ───────────────────────────────────────────────────────────────

deps:
	$(GRADLEW) $(GRADLE_OPTS) dependencies --configuration debugRuntimeClasspath

tasks:
	$(GRADLEW) $(GRADLE_OPTS) tasks

env:
	@echo "JAVA_HOME   = $(JAVA_HOME)"
	@echo "ANDROID_HOME= $(ANDROID_HOME)"
	@java -version 2>&1 | head -1
	@$(GRADLEW) --version | head -3
