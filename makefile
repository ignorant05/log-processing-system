.PHONY: all install build run lint check fmt clean test 

APP_NAME = klog 
JAR_FILE = $(APP_NAME).jar
JAVA = java
MAVEN = mvn


all: install | build | test | lint | fmt 
	@echo "All tasks completed"

install: 
	@echo "Resolving dependencies..."
	$(MAVEN) clean install
	@echo "dependencies resolved"

build:
	@echo "Building ..."
	$(MAVEN) clean package
	@echo "Built Successfully"

run: build
	@echo "Running ..."
	$(JAVA) -jar target/$(JAR_FILE)

lint:
	@echo "Linting..."
	$(MAVEN) checkstyle:check
	@echo "Linting completed"
check:
	@echo "Checking for formatting issues..."
	$(MAVEN) spotless:check
	@echo "Checking completed"
	
fmt:
	@echo "Running go fmt..."
	$(MAVEN) spotless:apply
	@echo "Formatting completed"

clean:
	@echo "Cleaning Up..."
	$(MAVEN) clean
	@echo "Cleaned up"

test: 
	@echo "Testing code..."
	$(MAVEN) test 
	@echo "Test Completed"

help:
	@echo "Makefile commands:"
	@echo "  make install- Resolve all dependencies (pom.xml file)"
	@echo "  make build  - Build the application"
	@echo "  make run    - Run the application"
	@echo "  make lint   - Linting the code"
	@echo "  make check  - Check for formatting issues"
	@echo "  make fmt    - Formatting code"
	@echo "  make clean  - Clean the project"
	@echo "  make help   - Show this help message"

